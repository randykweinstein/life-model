package com.calculr.lifemodel.books;

import com.calculr.lifemodel.engine.Simulation;
import com.calculr.lifemodel.finance.Money;

/**
 * An {@link Account} that tracks a liability, such as a credit card account or a loan.
 */
public class LiabilityAccount extends Account {
  protected LiabilityAccount(Simulation sim, String accountName) {
    super(sim, accountName);
  }

  /**
   * Deposits a {@link Transaction} into the {@link Account} to pay back the account.
   */
  public void payment(Transaction transaction) {
    getJournal().deposit(transaction);
  }

  /**
   * Withdraws a {@link Transaction} from the {@link Account} as a purchase or a
   * deduction from the account.
   */
  public void purchase(Transaction transaction) {
    getJournal().withdraw(transaction);
  }
  
  /**
   * Returns the latest computed balance from the {@link Account}. May not
   * include transactions that have not cleared. This balance is the last
   * balance from the {@link DailyLedger}.
   */
  public Money getBalance() {
    return getLedger().getBalance();
  }
}
