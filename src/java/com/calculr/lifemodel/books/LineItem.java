package com.calculr.lifemodel.books;

import com.calculr.lifemodel.finance.Money;

/**
 * A transactional item added into a {@link Journal}. 
 */
public abstract class LineItem {
  private final Transaction transaction;
  
  protected LineItem(Transaction transaction) {
    this.transaction = transaction;
  }
  
  protected Transaction getTransaction() {
    return transaction;
  }
  
  /**
   * Returns the amount of {@link Money} in the transaction.
   */
  abstract public Money getAmount();     
}