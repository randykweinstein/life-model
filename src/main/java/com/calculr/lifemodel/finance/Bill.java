package com.calculr.lifemodel.finance;

import java.time.LocalDate;

import com.calculr.lifemodel.books.AssetAccount;
import com.calculr.lifemodel.books.Transaction;
import com.calculr.lifemodel.engine.Actor;
import com.calculr.lifemodel.engine.Simulation;

/**
 * A request for payment from a {@link Payee} to a {@link Payer}. 
 */
public class Bill extends Actor<Bill> {
  private final Simulation sim;
  private final Payee payee;
  private final Payer payer;
  private final LocalDate issueDate;
  private final LocalDate dueDate;
  private Money payed = Money.zero();
  
  private Bill(Simulation sim, Payee payee, Payer payer, int gracePeriod) {
    super(sim);
    this.sim = sim;
    this.payee = payee;
    this.payer = payer;
    this.issueDate = sim.getCurrentDate();
    this.dueDate = issueDate.plusDays(gracePeriod);
  }
  
  /**
   * Constructs a new {@link Bill}.
   * 
   * @param sim the current {@link Simulation}
   * @param payee the originator of the {@link Bill}
   * @param payer the provider of the payment to this {@link Bill}
   * @param gracePeriod the number of days of grace period before additional finance charges or 
   *        other penalties are levied
   */
  public static Bill issue(Simulation sim, Payee payee, Payer payer, int gracePeriod) {
    return new Bill(sim, payee, payer, gracePeriod);
  }

  
  /**
   * Returns the date when this {@link Bill} is due. 
   */
  public LocalDate getDueDate() {
    return dueDate;
  }

  /**
   * @see com.calculr.lifemodel.finance.Payee#getMinimumPayment()
   */
  public Money getMinimumPayment() {
    return payee.getMinimumPayment();
  }

  /**
   * @see com.calculr.lifemodel.finance.Payee#getPeriodPayment()
   */
  public Money getPeriodPayment() {
    return payee.getPeriodPayment();
  }

  /**
   * @see com.calculr.lifemodel.finance.Payee#getTotalLiability()
   */
  public Money getTotalLiability() {
    return payee.getTotalLiability();
  }
  
  /**
   * Pays a bill by withdrawing an amount from the specified {@link AssetAccount}.
   */
  public void pay(AssetAccount account, Money amount) {
    LocalDate date = sim.getCurrentDate();
    if (date.isAfter(dueDate)) {
      throw new FinanceException("No payment allowed after due date");
    }
    Transaction transaction = Transaction.create(date, payee.getName() + " Payment", amount);
    payee.remit(account, transaction);
    payed = payed.add(amount);
  }

  @Override
  public void onRegister(LocalDate date) {
    schedule(date.plusDays(1), context -> payer.onBill(payee, this));
    scheduleEndOfDay(getDueDate(), context -> {
      if (!payed.isAtLeast(getMinimumPayment())) {
        throw new PaymentDefault("Minimum payment to '%s' of %s not received by %s",
            payee.getName(), getMinimumPayment(), getDueDate());
      }
    });    
  }

  /**
   * A runtime exception issued when a minimum payment has not been met.
   */
  static class PaymentDefault extends FinanceException {
    private static final long serialVersionUID = 1L;

    PaymentDefault(String msg, Object... args) {
      super(msg, args);
    }
  }
}
