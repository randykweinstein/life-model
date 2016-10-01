package com.calculr.lifemodel.books;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Consumer;

import com.calculr.lifemodel.engine.Actor;
import com.calculr.lifemodel.engine.Simulation;
import com.calculr.lifemodel.engine.TaskContext;
import com.calculr.lifemodel.finance.Money;

public class DailyLedger extends Actor<DailyLedger> implements Consumer<LineItem> {

  public static DailyLedger create(Simulation simulation) {
    return new DailyLedger(simulation);
  }
  
  private Money balance = Money.zero();
  private final Collection<LineItem> items;
  private final Collection<LedgerEntry> entries;  
  
  private DailyLedger(Simulation simulation) {
    super(simulation);
    items = new ArrayList<>();
    entries = new ArrayList<>();
  }
  
  @Override
  public void onRegister(LocalDate date) {
    onSchedule()
        .startingToday()
        .runDaily()
        .atEndOfDay()
        .schedule(DailyLedger::accrual);
  }
  
  /**
   * Links this {@link DailyLedger} with a {@link Journal} such that all
   * transactions added to the journal are also processed by this ledger.
   */
  public DailyLedger linkTo(Journal journal) {
    journal.registerLineItemConsumer(this);
    return this;
  }
  
  /**
   * Returns the latest computed daily balance.
   */
  public Money getBalance() {
    return balance;
  }
  
  /**
   * Returns the balance after including all unposted transactions (i.e. those from the same day).
   */
  public Money getUnpostedBalance() {
    Money unposted = balance;
    for (LineItem item : items) {
      unposted = unposted.add(item.getAmount());
    }
    return unposted;
  }
  
  static class LedgerEntry {
    private final LocalDate date;
    private final Money change;
    private final Money balance;
    
    LedgerEntry(LocalDate date, Money change, Money balance) {
      this.date = date;
      this.change = change;
      this.balance = balance;
    }
    
    @Override
    public String toString() {
      return String.format("%12s|%12s|%12s\n", date, change, balance); 
    }
  }
  
  private static void accrual(TaskContext<DailyLedger> context) {
    LocalDate date = context.getDate();
    DailyLedger ledger = context.getActor();
    Money runningTotal = Money.zero();
    for (LineItem item : ledger.items) {
      runningTotal = runningTotal.add(item.getAmount());
    }
    ledger.balance = ledger.balance.add(runningTotal);
    ledger.entries.add(new LedgerEntry(date, runningTotal, ledger.balance));
    ledger.items.clear();    
  }

  @Override
  public void accept(LineItem t) {
    items.add(t); 
  }
  
  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append(String.format("%12s|%12s|%12s\n", "date", "change", "balance"));
    builder.append("------------+------------+------------\n");
    for (LedgerEntry entry : entries) {
      builder.append(entry);
    }
    return builder.toString();
  }
}
