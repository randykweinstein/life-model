package com.calculr.lifemodel.finance;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import com.calculr.lifemodel.engine_old.Actor;
import com.calculr.lifemodel.engine_old.Scheduler;
import com.calculr.lifemodel.engine_old.State;
import com.calculr.lifemodel.finance.CheckingAccount.CheckingAccountBuilder;
import com.calculr.lifemodel.finance.CreditCardAccount.CreditCardAccountBuilder;

public abstract class BalanceSheet implements State<BalanceSheet> {

  private final Map<String, OldAccount> accounts;

  private Scheduler<BalanceSheet> scheduler;
  
  // Specialized accounts for tracking the five parts of a balance sheet.
  private AccountImpl income;
  private AccountImpl expenses;
  private AccountImpl assets;
  private AccountImpl liabilities;
  private AccountImpl equity;
  
  protected BalanceSheet() {
    accounts = new HashMap<>();
    income = new AccountImpl("Income", Money.zero());
    expenses = new AccountImpl("Expenses", Money.zero());
    assets = new AccountImpl("Assets", Money.zero());
    liabilities = new AccountImpl("Liabilities", Money.zero());    
    equity = new AccountImpl("Equity", Money.zero());
  }
  
  @Override
  public void addActor(Actor<BalanceSheet> actor) {
    if (actor instanceof OldAccount) {
      OldAccount account = (OldAccount) actor;
      accounts.put(account.getName(), account);
    }
  }
  
  public void addAccount(OldAccount account) {
    accounts.put(account.getName(), account);
    scheduler.register(account);
  }
  
  @Override
  public void reset(Scheduler<BalanceSheet> scheduler) {
    init(scheduler); 
  }
  
  @Override
  public void init(Scheduler<BalanceSheet> scheduler) {
    clearAccounts();
    this.scheduler = scheduler;
    initSheet(scheduler);
  }
  
  protected abstract void initSheet(Scheduler<BalanceSheet> scheduler);
  
  protected void initBalanceSheets(Money income, Money expenses, Money assets, Money liabilities) {
    this.income = new AccountImpl("Income", income);
    this.expenses = new AccountImpl("Expenses", expenses);
    this.assets = new AccountImpl("Assets", assets);
    this.liabilities = new AccountImpl("Liabilities", liabilities);
    this.equity = new AccountImpl("Equity", assets.sub(liabilities));
  }
  
  protected void clearAccounts() {
    accounts.clear();
  }
  
  /**
   * Creates a new {@link OldAccount} associated with this balance sheet.
   */
  protected CheckingAccountBuilder newCheckingAccount(String name) {
    return CheckingAccount.newBuilder(this, name);
  }

  /**
   * Creates a new {@link OldAccount} associated with this balance sheet.
   */
  protected CreditCardAccountBuilder newCreditAccount(String name) {
    return CreditCardAccount.newBuilder(this, name);
  }

  /**
   * Deposits money into an account and classify it as income.
   */
  public void income(LocalDate date, OldAccount account, String desc, Money amount) {
    account.deposit(date, desc, amount);
    income.deposit(date, desc, amount);
    assets.deposit(date, desc, amount);
    equity.deposit(date, desc, amount);
  }
  
  /**
   * Withdraws money from an account and classifies it as an expense.
   */
  public void expense(LocalDate date, OldAccount account, String desc, Money amount) {
    account.withdraw(date, desc, amount);
    expenses.deposit(date, desc, amount);
    assets.withdraw(date, desc, amount);
    equity.withdraw(date, desc, amount);
  }

  /**
   * Borrows money from one account and tracks it as a liability. We use cash
   * basis to track expenses.
   */
  public void borrow(LocalDate date, OldAccount account, String desc, Money amount) {
    account.withdraw(date, desc, amount);
    liabilities.deposit(date, desc, amount);
    equity.withdraw(date, desc, amount);
  }

  /**
   * Pays back a liability by transferring from one account back into a new account.
   */
  public void payment(LocalDate date, OldAccount fromAccount, OldAccount toAccount, String desc,
      Money amount) {
    System.out.printf("    %s: Payment of %s from <%s> to <%s> ('%s')\n", 
        date, amount, fromAccount, toAccount, desc);
    transfer(date, fromAccount, toAccount, desc, amount);
    expenses.deposit(date, desc, amount);
    liabilities.withdraw(date, desc, amount);
    assets.withdraw(date, desc, amount);
  }

  /**
   * Moves money from one account to another.  This has no impact on any of the financial statements.
   */
  public void transfer(LocalDate date, OldAccount fromAccount, OldAccount toAccount, String desc,
      Money amount) {
    fromAccount.transfer(date, desc, amount, toAccount);
  }
    
  public OldAccount lookup(String name) {
    if (!accounts.containsKey(name)) {
      throw new FinanceException("Account '%s' does not exist", name);
      //accounts.put(name, new Account(name, Money.zero()));
    }
    return accounts.get(name);
  }
  
  public String toString() {
    return String.format("(%s, %s) Cash Flow: %s, (%s, %s), %s", income, expenses, income.getBalance().sub(expenses.getBalance()), assets, liabilities, equity);
  }
}
