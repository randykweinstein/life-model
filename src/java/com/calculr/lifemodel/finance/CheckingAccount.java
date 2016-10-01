package com.calculr.lifemodel.finance;

import java.util.Optional;

import com.calculr.lifemodel.engine.Repeating;
import com.calculr.lifemodel.engine_old.Event;
import com.calculr.lifemodel.engine_old.Scheduler;
import com.calculr.lifemodel.engine_old.State;

public class CheckingAccount extends OldAccount {
  private final double interestRate;
  private final boolean overdraftAllowed;
  private final Money overdraftFee;

  CheckingAccount(CheckingAccountBuilder builder) {
    super(builder.name, builder.balance);
    this.interestRate = builder.interestRate;
    this.overdraftAllowed = builder.overdraftAllowed;
    this.overdraftFee = builder.overdraftFee;
  }

  static CheckingAccountBuilder newBuilder(BalanceSheet sheet, String name) {
    return new CheckingAccountBuilder(sheet, name);
  }
  
  public static class CheckingAccountBuilder {
    private final BalanceSheet sheet;
    private final String name;
    private Money balance = Money.zero();
    private double interestRate = 0.0;
    private boolean overdraftAllowed = false;
    private Money overdraftFee = Money.zero();
    
    CheckingAccountBuilder(BalanceSheet sheet, String name) {
      this.sheet = sheet;
      this.name = name;
    }
    
    public CheckingAccountBuilder setBalance(Money balance) {
      this.balance = balance;
      return this;
    }
    
    /**
     * Sets the annual interest rate earned on the account.
     */
    public CheckingAccountBuilder setInterestRate(double interestRate) {
      this.interestRate = interestRate;
      return this;
    }
    
    public CheckingAccountBuilder enableOverdraft(Money fee) {
      this.overdraftAllowed = true;
      this.overdraftFee = fee;
      return this;
    }
    
    public CheckingAccount build() {
      CheckingAccount account = new CheckingAccount(this);
      sheet.addAccount(account);
      return account;
    }
  }

  /**
   * A daily event for tracking overdrafts.
   */
  static class OverdraftTracker implements Event<BalanceSheet> {
    private final OldAccount account;
    private final Money fee;
    private boolean overdraft = false;
    
    OverdraftTracker(OldAccount account, Money fee) {
      this.account = account;
      this.fee = fee;
    }
    
    @Override
    public void evaluate(Scheduler<BalanceSheet> scheduler, BalanceSheet state) {
      if (account.getBalance().sign() < 0) {
        if (!overdraft) {
          System.out.format("Checking account overdraft, %s\n", account);
          state.expense(scheduler.getCurrentDate(), account, "Overdraft", fee);
          overdraft = true;
        }
      } else {
        overdraft = false;
      }
    }
  }
  
  @Override
  public void onStart(Scheduler<BalanceSheet> scheduler, State<BalanceSheet> state) {
    CheckingAccount account = this;
    if (interestRate > 0.0) {
      scheduler.scheduleRepeating(new Event<BalanceSheet>() {
        @Override
        public void evaluate(Scheduler<BalanceSheet> scheduler, BalanceSheet state) {
          // A gross simplification - just use the last balance instead of the
          // average monthly balance to compute the interest.
          if (getBalance().sign() > 0) {
            state.income(scheduler.getCurrentDate(), account, "Interest",
                getBalance().scale(interestRate / 12));
          }
        }
      }, Repeating.MONTHLY);
    }
    if (overdraftAllowed) {
      scheduler.scheduleRepeating(new OverdraftTracker(this, overdraftFee), Repeating.DAILY);
    }
  }
  
  @Override
  protected Optional<Money> getMinimumBalance() {
    if (overdraftAllowed) {
      return Optional.empty();
    }
    return Optional.of(Money.zero());
  }

  @Override
  public void onFinish(Scheduler<BalanceSheet> scheduler, State<BalanceSheet> state) {
    // TODO Auto-generated method stub
  }
}
