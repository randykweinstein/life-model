package com.calculr.lifemodel.finance;

import com.calculr.lifemodel.books.AssetAccount;
import com.calculr.lifemodel.books.Transaction;

/**
 * A Payee is an account or other financial instrument that requires payments for debts.
 * 
 * <p>A Payee will issue a {@link Bill} to a {@link Payer}.
 */
public interface Payee {

  /**
   * Returns the name of the {@link Payee}.
   */
  String getName();
  
  /**
   * Returns the minimum payment required to satisfy this bill.
   */
  Money getMinimumPayment();
  
  /**
   * Returns the amount owed on this bill. Full payment of this amount by the
   * end of the due date would incur no additional charges.
   */
  Money getPeriodPayment();
  
  /**
   * Returns the total owed on this account, while only the
   * {@link #getPeriodPayment()} is expected for this pay period.
   */
  Money getTotalLiability();
  
  /**
   * Accepts a new transaction as a payment into this account.
   */
  void remit(AssetAccount account, Transaction transaction);
}
