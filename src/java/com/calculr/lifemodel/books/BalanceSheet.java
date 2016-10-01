package com.calculr.lifemodel.books;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.calculr.lifemodel.books.CreditCardAccount.CreditCardAccountBuilder;
import com.calculr.lifemodel.engine.Actor;
import com.calculr.lifemodel.engine.Simulation;
import com.calculr.lifemodel.finance.Money;
import com.calculr.lifemodel.finance.Payer;

/**
 * A factory for producing various types of accounts.
 */
public final class BalanceSheet extends Actor<BalanceSheet> {

  public static final String ALL_ASSETS = "All Assets"; 
  public static final String ALL_LIABILITIES = "All Liabilities"; 
  
  private final Simulation sim;
  private final Map<String, Account> accounts = new TreeMap<>();

  /**
   * Constructs a new {@link BalanceSheet} that produces and accounts for all
   * {@link Account} objects.
   * 
   * <p>
   * Additional built-in accounts are generated including {@link #ALL_ASSETS}
   * and {@link #ALL_LIABILITIES}.
   */
  public static BalanceSheet create(Simulation sim) {
    BalanceSheet sheet = new BalanceSheet(sim);
    sheet.initialize();
    return sheet;
  }

  private BalanceSheet(Simulation sim) {
    super(sim);
    this.sim = sim;
  }
    
  /**
   * Initializes this {@link BalanceSheet} by removing all defined accounts and
   * setting up each {@link VirtualAccount}.
   */
  public void initialize() {
    accounts.clear();
    add(new VirtualAccount(sim, ALL_ASSETS));
    add(new VirtualAccount(sim, ALL_LIABILITIES));
  }
  
  /**
   * Adds a new {@link Account} to the {@link BalanceSheet}.
   */
  protected void add(Account account) {
    accounts.put(account.getAccountName(), account);
  }
  
  /**
   * Returns the specified account by the account name.
   */
  public Account get(String accountName) {
    return accounts.get(accountName);
  }
  
  /**
   * Adds a new {@link AssetAccount} to the {@link BalanceSheet}.
   */
  protected void add(AssetAccount assetAccount) {
    add((Account) assetAccount);
    ((VirtualAccount) accounts.get(ALL_ASSETS)).linkTo(assetAccount);
  }
  
  /**
   * Adds a new {@link LiabilityAccount} to the {@link BalanceSheet}.
   */
  protected void add(LiabilityAccount liabilityAccount) {
    add((Account) liabilityAccount);
    ((VirtualAccount) accounts.get(ALL_LIABILITIES)).linkTo(liabilityAccount);
  }
  
  /**
   * Adds a new {@link VirtualAccount} to the {@link BalanceSheet}.
   */
  protected void add(VirtualAccount virtualAccount) {
    add((Account) virtualAccount);
  }
  
  /**
   * Constructs a new {@link AssetAccount}.
   */
  public AssetAccount createAssetAccount(String name) {
    AssetAccount account = new AssetAccount(sim, name);
    add(account);
    return account;    
  }
  
  /**
   * Constructs a new {@link AssetAccount} with name "cash".
   */
  public AssetAccount createCashAccount() {
    return createAssetAccount("Cash");
  }
  
  /**
   * Constructs a new {@link Account} that provides a month interest payment.
   */
  public AssetAccount createInterestAccount(String accountName, double interestRate) {
    AssetAccount account = createAssetAccount(accountName);
    account.actOn(new Interest(interestRate));
    return account;
  }
  
  /**
   * Constructs a new {@link LiabilityAccount} that indicates a loan, credit, or
   * other instrument that requires eventual repayment.
   */
  public LiabilityAccount createLiabilityAccount(String accountName) {
    LiabilityAccount account = new LiabilityAccount(sim, accountName);
    add(account);
    return account;        
  }

  /**
   * Constructs a new {@link CreditCard} via the builder. This
   * {@link CreditCard} is a {@link LiabilityAccount} that can accept purchases
   * and payments.
   */
  public CreditCardAccountBuilder createCreditCard(Payer payer, String accountName,
      Money creditLimit, double interestRate) {
    return CreditCardAccount.newBuilder(sim, accountName, payer, creditLimit, interestRate);
  }

  /**
   * Constructs a new {@link Mortgage} via the builder that takes out a loan for
   * an asset according to the parameters of the loan specified by the builder.
   */
  public Mortgage.MortgageBuilder createMortgage(Payer payer, String mortgageProperty,
      Money salePrice) {
    return Mortgage.newBuilder(sim, this, payer, mortgageProperty, salePrice);
  }
  
  /**
   * Constructs a new {@link VirtualAccount}, linking each {@link Account}
   * specified to this new {@link VirtualAccount}.
   */
  public VirtualAccount createVirtualAccount(String accountName, Account... accounts) {
    VirtualAccount newAccount = new VirtualAccount(sim, accountName);
    add(newAccount);
    for (Account account : accounts) {
      newAccount.linkTo(account);
    }
    return newAccount;
  }
  
  @SuppressWarnings("serial")
  private static final Set<String> IGNORED_ACCOUNTS = new HashSet<String>() {{
    add(ALL_ASSETS);
    add(ALL_LIABILITIES);
  }};
  
  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("All assets: " + get(ALL_ASSETS).getBalance() + "\n");
    builder.append("All liabilities: " + get(ALL_LIABILITIES).getBalance() + "\n");
    builder.append("-----------------------------------\n");
    builder.append("All accounts\n");
    for (Account account : accounts.values()) {
      if (!(IGNORED_ACCOUNTS.contains(account.getAccountName()))) {
        builder.append("  " + account.getAccountName() + ": " + account.getBalance() + "\n");
      }
    }
    return builder.toString();
  }

  @Override
  public void onRegister(LocalDate date) {
    // Do nothing here.
  }
}
