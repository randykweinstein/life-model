package com.calculr.lifemodel.books;

import com.calculr.lifemodel.engine.Simulation;

/**
 * An {@link Account} that tracks an asset, such as a cash account, a savings
 * account, or an investment.
 */
public class AssetAccount extends Account {
  protected AssetAccount(Simulation sim, String accountName) {
    super(sim, accountName);
  }

  /**
   * Deposits a {@link Transaction} into the {@link Account}.
   */
  public void deposit(Transaction transaction) {
    getJournal().deposit(transaction);
  }

  /**
   * Withdraws a {@link Transaction} from the {@link Account}.
   */
  public void withdraw(Transaction transaction) {
    getJournal().withdraw(transaction);
  }
  
  /**
   * Withdraws the specified transaction from this {@link AssetAccount} and
   * applies it as a payment to the {@link LiabilityAccount}.
   */
  public void makePayment(Transaction transaction, LiabilityAccount liability) {
    withdraw(transaction);
    liability.payment(transaction);
  }
}
