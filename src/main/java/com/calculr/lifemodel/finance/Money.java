package com.calculr.lifemodel.finance;

import java.util.Arrays;

public class Money {
  private static final long EXTRA_PRECISION = 10000;
  
  private final long fractionalCents; 

  static Money fromPrimitive(long fractionalCents) {
    return new Money(fractionalCents);
  }

  long toPrimitive() {
    return fractionalCents;
  }

  public static Money dollars(long dollars) {
    return new Money(dollars * 100 * EXTRA_PRECISION);
  }

  public static Money dollarsCents(int dollars, int cents) {
    if (dollars < 0) {
      return new Money(-(-dollars * 100 + cents) * EXTRA_PRECISION);
    } else {
      return new Money((dollars * 100 + cents) * EXTRA_PRECISION);
    }
  }
  
  public static Money dollars(double dollars) {
    return new Money(Math.round(dollars * (100 * EXTRA_PRECISION)));
  }
  
  public static Money zero() {
    return new Money(0);
  }
  
  private Money(long cents) {
    this.fractionalCents = cents;
  }
  
  public int fullDollars() {
    return (int) Math.floor(1.0 * fractionalCents / (100 * EXTRA_PRECISION));
  }
  
  public int remainingCents() {
    if (fractionalCents >= 0) {
      return (int) (fractionalCents % (100 * EXTRA_PRECISION)/EXTRA_PRECISION);
    } else {
      return (int) ((-fractionalCents) % (100 * EXTRA_PRECISION)/EXTRA_PRECISION);
    }
  }

  public int sign() {
    if (fractionalCents > 0) {
      return 1;
    } else if (fractionalCents < 0) {
      return -1;
    } else {
      return 0;
    }
  }
  
  public Money scale(int factor) {
    return new Money(fractionalCents * factor);
  }

  public Money scale(double factor) {
    return new Money(Math.round(fractionalCents * factor));
  }

  public Money add(Money amount) {
    return new Money(fractionalCents + amount.fractionalCents);
  }
  
  public Money plusPercent(float percent) {
    return new Money(Math.round(fractionalCents * ((100 + percent) / 100)));
  }
  
  public Money minusPercent(float percent) {
    return new Money(Math.round(fractionalCents * ((100 - percent) / 100)));
  }  
  
  public Money sub(Money amount) {
    return add(amount.negate());
  }
  
  public Money negate() {
    return new Money(-fractionalCents);
  }
  
  public boolean isAtLeast(Money amount) {
    return sub(amount).sign() >= 0;
  }
  
  public static Money sum(Money... values) {
    return sum(Arrays.asList(values));
  }
  
  public static Money sum(Iterable<Money> values) {
    int cents = 0;
    for (Money value : values) {
      cents += value.fractionalCents;
    }
    return new Money(cents);
  }
  
  public static Money max(Money first, Money... rest) {
    Money maximum = first;
    for (Money amount : rest) {
      if (amount.isAtLeast(maximum)) {
        maximum = amount;
      }
    }
    return maximum;
  }
  
  public static Money min(Money first, Money... rest) {
    Money minimum = first;
    for (Money amount : rest) {
      if (minimum.isAtLeast(amount)) {
        minimum = amount;
      }
    }
    return minimum;
  }
  
  public static double ratio(Money numerator, Money denominator) {
    return (double) numerator.fractionalCents / (double) denominator.fractionalCents;
  }
  
  public String toString() {
    return String.format("$%d.%02d", fullDollars(), remainingCents());
  }
}
