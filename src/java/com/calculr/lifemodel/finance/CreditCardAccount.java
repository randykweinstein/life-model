package com.calculr.lifemodel.finance;

import java.util.Optional;

import com.calculr.lifemodel.books.AssetAccount;
import com.calculr.lifemodel.books.Transaction;
import com.calculr.lifemodel.engine_old.Scheduler;
import com.calculr.lifemodel.engine_old.State;

public class CreditCardAccount extends OldAccount implements Payee {
  private final Money limit;
  private final int dayOfMonthClosing;
  private Money periodBalance = Money.zero();

  CreditCardAccount(CreditCardAccountBuilder builder) {
    super(builder.name, builder.balance);
    this.limit = builder.limit;
    this.dayOfMonthClosing = builder.dayOfMonthClosing;
  }

  @Override
  protected Optional<Money> getMinimumBalance() {
    return Optional.of(limit);
  }
  
  static CreditCardAccountBuilder newBuilder(BalanceSheet sheet, String name) {
    return new CreditCardAccountBuilder(sheet, name);
  }
  
  public static class CreditCardAccountBuilder {
    private final BalanceSheet sheet;
    private final String name;
    private Money balance = Money.zero();
    private double interestRate = 0.17;
    private Money limit = Money.dollars(-10000);
    private int dayOfMonthClosing = 10;
    private int dayOfMonthDue = 7;
    
    // Minimum payment is the greater of the following:
    private double minimumPaymentRate = 0.03;
    private Money minimumPayment = Money.dollars(35);    
    
    public CreditCardAccountBuilder(BalanceSheet sheet, String name) {
      this.sheet = sheet;
      this.name = name;
    }
    
    public CreditCardAccountBuilder setBalance(Money balance) {
      this.balance = balance;
      return this;
    }
    
    public CreditCardAccountBuilder setInterestRate(double interestRate) {
      this.interestRate = interestRate;
      return this;
    }
    
    public CreditCardAccountBuilder setCreditLimit(Money limit) {
      this.limit = limit;
      return this;
    }
    
    public CreditCardAccountBuilder setAccountDates(int dayOfMonthClosing, int dayOfMonthDue) {
      this.dayOfMonthClosing = dayOfMonthClosing;
      this.dayOfMonthDue = dayOfMonthDue;
      return this;
    }
    
    public CreditCardAccountBuilder setMinimumPayment(Money payment, double rate) {
      this.minimumPayment = payment;
      this.minimumPaymentRate = rate;
      return this;
    }
    
    public CreditCardAccount build() {
      CreditCardAccount account = new CreditCardAccount(this);
      sheet.addAccount(account);
      return account;
    }
  }

  @Override
  public void onStart(Scheduler<BalanceSheet> scheduler, State<BalanceSheet> state) {
    
  }

  @Override
  public void onFinish(Scheduler<BalanceSheet> scheduler, State<BalanceSheet> state) {
    // TODO Auto-generated method stub
  }

  @Override
  public Money getMinimumPayment() {
    return getMinimumPayment();
  }

  @Override
  public Money getPeriodPayment() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Money getTotalLiability() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void remit(AssetAccount account, Transaction transaction) {
    // TODO Auto-generated method stub
    
  }
}
