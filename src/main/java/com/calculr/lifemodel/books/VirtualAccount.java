package com.calculr.lifemodel.books;

import com.calculr.lifemodel.engine.Simulation;

/**
 * A Virtual account is an {@link Account} that can not be directly deposited or
 * withdrawn from. Instead, it gets linked to other accounts and shows a view of
 * their transactions as if they were in the same account.
 */
public class VirtualAccount extends Account {

  protected VirtualAccount(Simulation sim, String accountName) {
    super(sim, accountName);
  }
  
  /**
   * Links this {@link VirtualAccount} to another {@link Account} such that all
   * transactions will be added to this virtual account.
   */
  public void linkTo(Account other) {
    other.getJournal().registerLineItemConsumer(getLedger());
  }
}
