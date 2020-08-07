package com.calculr.lifemodel.engine;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;

/**
 * An interface defining the {@code LocalDate} when an event should be invoked. 
 */
public interface Starting {
  /**
   * Returns the starting date for a recurring event.
   */
  LocalDate start(LocalDate date);

  static Starting on(DayOfWeek dayOfWeek) {
    return d -> d.getDayOfWeek() == dayOfWeek ? d : 
      d.plusDays((dayOfWeek.getValue() - d.getDayOfWeek().getValue()) % 7);
  }

  static Starting on(LocalDate startDate) {
    return d -> startDate;
  }

  static Starting onMonthDay(Month month, int dayOfMonth) {
    return d -> {
      if (d.getMonth().getValue() < month.getValue() ||
          (d.getMonth().equals(month) && d.getDayOfMonth() <= dayOfMonth)) {
        return LocalDate.of(d.getYear(), month, dayOfMonth);
      } else {
        return LocalDate.of(d.getYear() + 1, month, dayOfMonth);
      }
    };
  }

  static Starting inNDays(int days) {
    return d -> d.plusDays(days);
  }
  
  static Starting immediately() {
    return d -> d;
  }
  
  static Starting firstOfMonth() {
    return dayOfMonth(1);
  }
  
  class DayOfMonth implements Starting {
    private final int day;
    DayOfMonth(int day) {
      this.day = day;
    }

    @Override
    public LocalDate start(LocalDate date) {
      int curDay = date.getDayOfMonth();
      if (curDay == day) {
        return date;
      }      
      LocalDate dateOnDay = LocalDate.of(date.getYear(), date.getMonth(), day);
      if (curDay < day) {
        return dateOnDay;
      } else {
        return dateOnDay.plusMonths(1);
      }
    }
  }
  
  static Starting dayOfMonth(int day) {
    return new DayOfMonth(day);
  }
}