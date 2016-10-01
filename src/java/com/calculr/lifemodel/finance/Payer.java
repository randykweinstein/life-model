package com.calculr.lifemodel.finance;

import com.calculr.lifemodel.books.AssetAccount;
import com.calculr.lifemodel.engine.Actor;
import com.calculr.lifemodel.engine.Simulation;

/**
 * A Payer coordinates the payment of each {@link Bill} issued from a {@link Payer}.
 */
public abstract class Payer extends Actor<Payer> {

  protected Payer(Simulation simulation) {
    super(simulation);
  }

  /**
   * Submits a {@link Bill} to the {@link Payer} for payment. 
   */
  public abstract void onBill(Payee payee, Bill bill);

  /**
   * Pays the bill according to the period payment amount. If that amount is not
   * available in the {@link AssetAccount}, the full amount left in the
   * {@link AssetAccount} is paid. If there is less than the minimum payment, no
   * payment to the {@link Bill} is made.
   */
  protected void payPeriodPayment(AssetAccount account, Bill bill) {
    if (account.getPostedBalance().isAtLeast(bill.getPeriodPayment())) {
      // Pay the amount asked if it's in the account
      bill.pay(account, bill.getPeriodPayment());
    } else if (account.getPostedBalance().isAtLeast(bill.getMinimumPayment())) {
      // Otherwise, pay all you can assuming the minimum is in the account
      bill.pay(account, account.getPostedBalance());
    }
    // else do nothing because we don't have the money
  }
  
  protected void payPeriodAndExtraPayment(AssetAccount account, Bill bill, Money extraPayment) {
    Money fullPayment = bill.getPeriodPayment().add(extraPayment);
    if (fullPayment.isAtLeast(bill.getTotalLiability())) {
      // Pay the total owed if the full payment is above the total liability.
      fullPayment = bill.getTotalLiability();
    }
    if (account.getPostedBalance().isAtLeast(fullPayment)) {
      // Pay the amount plus the extra amount.
      bill.pay(account, fullPayment);      
    } else if (account.getPostedBalance().isAtLeast(bill.getMinimumPayment())) {
      // Otherwise, pay all you can assuming the minimum is in the account
      bill.pay(account, account.getPostedBalance());
    }
    // else do nothing because we don't have the money
    
  }
}
