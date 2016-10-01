package com.calculr.lifemodel.books;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.function.Consumer;

import com.calculr.lifemodel.finance.Money;

/**
 * A Journal is a time order entry of {@link Transaction Transactions}.
 */
public class Journal {

  private final Collection<LineItem> items;
  private final Collection<Consumer<LineItem>> consumers;
  
  Journal() {    
    items = new ArrayList<>();
    consumers = new HashSet<>();
  }
  
  /**
   * Constructs a new {@link Journal} that can collect transactions.
   */
  public static Journal create() {
    return new Journal();
  }
  
  /**
   * Deposits a {@link Transaction} in this {@link Journal}.
   */
  public Journal deposit(Transaction transaction) {
    add(new DepositLineItem(transaction));
    return this;
  }

  /**
   * Withdraws a {@link Transaction} from this {@link Journal}.
   */
  public Journal withdraw(Transaction transaction) {
    add(new WithdrawalLineItem(transaction));
    return this;
  }

  /**
   * Adds a new consumer of {@line LineItem} objects that will accept each new added transaction.
   */
  public void registerLineItemConsumer(Consumer<LineItem> consumer) {
    consumers.add(consumer);
  }
  
  /**
   * Removes a specified consumer of {@link LineItem} objects.
   */
  public void removeLineItemConsumer(Consumer<LineItem> consumer) {
    consumers.remove(consumer);
  }
  
  private void add(LineItem item) {
    items.add(item);
    for (Consumer<LineItem> consumer : consumers) {
      consumer.accept(item);
    }
  }

  private static class DepositLineItem extends LineItem {
    private DepositLineItem(Transaction transaction) {
      super(transaction);
    }

    @Override
    public Money getAmount() {
      return getTransaction().getAmount();
    }
    
    @Override
    public String toString() {
      return String.format("%6d|%12s|%30s|%12s|\n", getTransaction().getId(),
          getTransaction().getDate(), getTransaction().getDescription(), getAmount());
    }
  }

  private static class WithdrawalLineItem extends LineItem {
    private WithdrawalLineItem(Transaction transaction) {
      super(transaction);
    }

    @Override
    public Money getAmount() {
      return getTransaction().getAmount().negate();
    }
    
    @Override
    public String toString() {
      return String.format("%6d|%12s|%30s|%12s|%12s\n", getTransaction().getId(),
          getTransaction().getDate(), getTransaction().getDescription(), "", getAmount());
    }
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append(String.format("%6s|%12s|%30s|%12s|%12s\n", "id", "date", "description",
        "deposits", "withdrawals"));
    builder
        .append("------+------------+------------------------------+------------+------------\n");
    for (LineItem item : items) {
      builder.append(item);
    }
    return builder.toString();
  }
}
