package com.calculr.lifemodel.books;

import java.time.LocalDate;

import com.calculr.lifemodel.finance.Money;

/**
 * A transaction is one financial payment or exchange of an asset for funds.
 */
public class Transaction extends Record {
  private final LocalDate date;
  private final String description;
  private final Money amount;
  
  Transaction(LocalDate date, String description, Money amount) {
    super();
    this.date = date;
    this.description = description;
    this.amount = amount;
  }

  /**
   * Constructs a new {@link Transaction}.
   */
  public static Transaction create(LocalDate date, String description, Money amount) {
    return new Transaction(date, description, amount);
  }
  
  /**
   * Returns the date.
   */
  public LocalDate getDate() {
    return date;
  }

  /**
   * Returns the description.
   */
  public String getDescription() {
    return description;
  }

  /**
   * Returns the amount.
   */
  public Money getAmount() {
    return amount;
  }
  
  @Override
  public String toString() {
    return getDate() + ": " + getAmount() + " " + getDescription();
  }
}
