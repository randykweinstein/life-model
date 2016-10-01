package com.calculr.lifemodel.finance;

import java.time.LocalDate;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

import com.calculr.lifemodel.engine_old.Actor;

public abstract class OldAccount implements Actor<BalanceSheet> {
  private final String name;
  private Money balance;
  private Ledger ledger;
  
  OldAccount(String name, Money startingBalance) {
    this.name = name;
    this.balance = startingBalance;
    this.ledger = new Ledger();
  }

  public Money getBalance() {
    return balance;
  }
  
  public void deposit(LocalDate date, String desc, Money amount) {
    System.out.printf("  %s (%s): deposit %s, balance %s ('%s')\n", date, getName(), amount,
        getBalance(), desc);
    ledger.add(new Item(date, desc, amount, this));
    this.balance = this.balance.add(amount);
  }
  
  public void withdraw(LocalDate date, String desc, Money amount) {
    System.out.printf("  %s (%s): withdraw %s, balance %s ('%s')\n", date, getName(), amount,
        getBalance(), desc);
    Optional<Money> minimum = getMinimumBalance();
    if (!minimum.isPresent() || getBalance().sub(amount).isAtLeast(minimum.get())) {
      ledger.add(new Item(date, desc, amount.negate(), this));
      this.balance = this.balance.add(amount.negate());
    } else {
      onOverdraft(date, desc, amount);
    }
  }
  
  /**
   * Returns the minimum funds in the account to avoid an overdraft.  Override to change this value. 
   */
  protected Optional<Money> getMinimumBalance() {
    return Optional.empty();
  }
  
  protected void onOverdraft(LocalDate date, String desc, Money amount) {
    throw new FinanceException("Overdraft on account '%s' on %s for %s ('%s'). Balance is %s of %s",
        getName(), date, amount, desc, getBalance(), getMinimumBalance().get());
  }
  
  public void transfer(LocalDate date, String desc, Money amount, OldAccount toAccount) {
    withdraw(date, desc, amount);
    toAccount.deposit(date, desc, amount);
  }
  
  public String getName() {
    return name;
  }
  
  public String toString() {
    return getName() + ": " + getBalance();
  }
  
  /**
   * An accounting of each transaction for a particular account.
   */
  static class Ledger {
    private final Map<LocalDate, List<Item>> items;
    
    private Ledger() {
      items = new TreeMap<>();
    }
    
    void add(Item item) {
      if (!items.containsKey(item.date)) {
        items.put(item.date, new LinkedList<>());
      }
      items.get(item.date).add(item);
    }
  }
  
  static class Item {
    private final LocalDate date;
    private final String desc;
    private final Money amount;
    private final OldAccount account;
    
    private Item(LocalDate date, String desc, Money amount, OldAccount account) {
      this.date = date;
      this.desc = desc;
      this.amount = amount;
      this.account = account;
    }
  }

}
