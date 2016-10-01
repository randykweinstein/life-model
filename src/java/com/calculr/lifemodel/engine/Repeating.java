package com.calculr.lifemodel.engine;

import java.time.LocalDate;

/**
 * A generator for producing sequences of dates.
 */
public interface Repeating {

  Repeating DAILY = d -> d.plusDays(1);
  Repeating WEEKLY = everyNWeeks(1);
  Repeating BIWEEKLY = everyNWeeks(2);
  Repeating MONTHLY = everyNMonths(1);
  Repeating QUARTERLY = everyNMonths(3);

  /**
   * Returns a new local date in the sequence given the current date. 
   */
  LocalDate next(LocalDate date);

  /**
   * Returns a {@link Repeating} that starts on the given day and continues
   * every month on that day.
   * 
   * @param day the day ranges from 1 to 31
   * @return
   */
  @Deprecated
  static Repeating dayOfMonth(final int day) {
    return new Repeating() {
      @Override
      public LocalDate next(LocalDate date) {
        if (date.getDayOfMonth() < day) {
          return date.plusDays(day - date.getDayOfMonth());
        } else if (date.getDayOfMonth() == day) {
          return date.plusMonths(1);
        } else {
          return date.plusDays(day - date.getDayOfMonth()).plusMonths(1);
        }
      }
    };
  }

  static Repeating everyNDays(final int numDays) {
    return d -> d.plusDays(numDays);
  }

  static Repeating everyNWeeks(final int numWeeks) {
    return d -> d.plusWeeks(numWeeks);
  }

  static Repeating everyNMonths(final int numMonths) {
    return d -> d.plusMonths(numMonths);
  }
}