package com.calculr.lifemodel.books;

import java.time.LocalDate;

import com.calculr.lifemodel.engine.Actor;
import com.calculr.lifemodel.engine.Simulation;
import com.calculr.lifemodel.finance.Money;

/**
 * An account is an asset or liability containing one or more
 * {@link Transaction} items in a {@link DailyLedger}.
 */
public abstract class Account extends Actor<Account> {
  protected final Simulation sim;
  private final String accountName;
  private final Journal journal;
  private final DailyLedger ledger;
  
  protected Account(Simulation sim, String accountName) {
    super(sim);
    this.sim = sim;
    this.accountName = accountName;
    this.journal = Journal.create();
    this.ledger = DailyLedger.create(sim).linkTo(journal);
  }

  /**
   * Returns the account name associated with this {@link Account}.
   */
  public String getAccountName() {
    return accountName;
  }
  
  /**
   * Returns the {@link DailyLedger} containing daily account totals for this {@link Account}.
   */
  protected DailyLedger getLedger() {
    return ledger;
  }

  /**
   * Returns the {@link Journal} of all transactions in this {@link Account}.
   */
  protected Journal getJournal() {
    return journal;
  }

  /**
   * Returns the current posted balance (the daily balance).
   */
  public Money getPostedBalance() {
    return getLedger().getBalance();
  }
  
  /**
   * Returns the latest balance (including unposted transactions).
   */
  public Money getBalance() {
    return getLedger().getUnpostedBalance();
  }
  
  /**
   * Assigns an {@link ActorFactory} to act on this {@link Account}.
   */
  public <T extends Actor<T>> void actOn(ActorFactory<Account, T> factory) {
    factory.build(sim, this);
  }
  

  @Override
  public void onRegister(LocalDate date) {
    // Nothing needs to be registered when the simulation begins.
  }
  
  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("Account: ");
    builder.append(getAccountName());
    builder.append('\n');
    builder.append(getJournal());
    builder.append('\n');
    builder.append(getLedger());
    return builder.toString();
  }
}
