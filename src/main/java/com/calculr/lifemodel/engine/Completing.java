package com.calculr.lifemodel.engine;

import java.time.LocalDate;
import java.util.function.Supplier;

/**
 * An interface that indicates when a sequence of events should be completed. 
 */
public interface Completing {
  /**
   * Returns {@code true} if the {@link RecurringEvent} is exhausted.   
   */
  boolean isComplete(LocalDate date);

  static Completing after(int numInvoke) {
    return new Completing() {
      private int count = 0;
      
      @Override
      public boolean isComplete(LocalDate date) {
        count++;
        return count >= numInvoke;
      }
    };
  }

  static Completing after(LocalDate lastDate) {
    return d -> d.isAfter(lastDate);
  }

  static Completing indefinitely() {
    return d -> false;
  }

  static Completing when(Supplier<Boolean> predicate) {
    return d -> predicate.get();
  }
}