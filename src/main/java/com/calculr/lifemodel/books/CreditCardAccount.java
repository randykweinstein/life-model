package com.calculr.lifemodel.books;

import static java.time.temporal.ChronoUnit.DAYS;

import java.time.LocalDate;

import com.calculr.lifemodel.engine.Simulation;
import com.calculr.lifemodel.finance.Bill;
import com.calculr.lifemodel.finance.FinanceException;
import com.calculr.lifemodel.finance.Money;
import com.calculr.lifemodel.finance.Payee;
import com.calculr.lifemodel.finance.Payer;

/**
 * A {@link LiabilityAccount} simulating a credit card.
 */
public class CreditCardAccount extends LiabilityAccount implements Payee {
  private final Payer payer;
  private final Money creditLimit;
  private final double interestRate;
  private final Money startingBalance;
  private final Money minimumPayment;
  private final double minimumPaymentRate;
  
  private Money periodBalance = Money.zero();

  CreditCardAccount(CreditCardAccountBuilder builder) {
    super(builder.sim, builder.accountName);
    this.payer = builder.payer;
    this.creditLimit = builder.creditLimit;
    this.interestRate = builder.interestRate;
    this.startingBalance = builder.startingBalance;
    this.minimumPayment = builder.minimumPayment;
    this.minimumPaymentRate = builder.minimumPaymentRate;
  }

  static CreditCardAccountBuilder newBuilder(Simulation sim, String accountName, Payer payer,
      Money creditLimit, double interestRate) {
    return new CreditCardAccountBuilder(sim, accountName, payer, creditLimit, interestRate);
  }
    
  public static class CreditCardAccountBuilder {
    private final Simulation sim;
    private final String accountName;
    private final Payer payer;
    private final Money creditLimit;
    private final double interestRate;
    private Money startingBalance = Money.zero();
    private double minimumPaymentRate = 0.02;
    private Money minimumPayment = Money.dollars(35);
    
    private CreditCardAccountBuilder(Simulation sim, String accountName, Payer payer,
        Money creditLimit, double interestRate) {
      this.sim = sim;
      this.accountName = accountName;
      this.payer = payer;
      this.creditLimit = creditLimit;
      this.interestRate = interestRate;
    }
   
    /**
     * Sets the maximum of the minimum payment and minimum payment rate based on the period balance.
     */
    public CreditCardAccountBuilder setMinimumPayment(Money minimumPayment,
        double minimumPaymentRate) {
      this.minimumPayment = minimumPayment;
      this.minimumPaymentRate = minimumPaymentRate;
      return this;
    }
    
    public CreditCardAccountBuilder setStartingBalance(Money balance) {
      this.startingBalance = balance;
      return this;
    }
    
    public CreditCardAccount build() {
      return new CreditCardAccount(this);
    }
  }

  @Override 
  public void payment(Transaction transaction) {
    super.payment(transaction);
    periodBalance = Money.min(Money.zero(), periodBalance.add(transaction.getAmount()));    
  }
  
  @Override
  public void purchase(Transaction transaction) {
    if (creditLimit.add(getBalance().add(transaction.getAmount().negate())).sign() >= 0) {
      super.purchase(transaction);
    } else {
      throw new CreditLimitExceeded("On credit card '%s', credit limit of %s exceeded with purchase of %s", getAccountName(), creditLimit, transaction);
    }
  }
  
  @Override
  public void onRegister(LocalDate date) {
    super.onRegister(date);
    if (startingBalance.sign() != 0) {
      Transaction transaction = Transaction.create(date, "Initial balance",
          startingBalance.negate());
      scheduleImmediately(context -> purchase(transaction));
    }
    
    LocalDate closingDate = date.plusMonths(1).minusDays(1);
    onSchedule().startingIn((int) DAYS.between(date, closingDate))
        .runMonthly()
        .schedule(context -> {
           periodBalance = getPostedBalance();
           if (periodBalance.sign() < 0) {
             // If we owe money on the credit card
             LocalDate d = context.getDate();
             int gracePeriod = (int) DAYS.between(d, d.plusMonths(1).minusDays(3));
             Bill.issue(sim, this, payer, gracePeriod);
             schedule(d.plusDays(gracePeriod + 1), innerContext -> {
               if (periodBalance.sign() < 0) {
                 // If there is still a balance, we can add interest to the remaining balance.
                Transaction transaction = new Transaction(innerContext.getDate(),
                    "Interest charged", periodBalance.scale(interestRate).negate());
                purchase(transaction);
               }
             });
           }         
        });
  }

  @Override
  public String getName() {
    return getAccountName();
  }

  @Override
  public Money getMinimumPayment() {
    if (getPeriodPayment().sign() > 0) {
      return Money.min(getPeriodPayment(),
          Money.max(minimumPayment, getPeriodPayment().scale(minimumPaymentRate)));
    }
    return Money.zero();
  }

  @Override
  public Money getPeriodPayment() {
    return periodBalance.negate();
  }

  @Override
  public Money getTotalLiability() {
    return getPostedBalance();
  }

  @Override
  public void remit(AssetAccount account, Transaction transaction) {
    account.makePayment(transaction, this);
  }
  
  static class CreditLimitExceeded extends FinanceException {
    private static final long serialVersionUID = 1L;

    CreditLimitExceeded(String msg, Object... args) {
      super(msg, args);
    }
  }
}
