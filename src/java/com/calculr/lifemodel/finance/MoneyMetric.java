package com.calculr.lifemodel.finance;

import com.calculr.lifemodel.engine.Metric;

/**
 * A {@link Metric} that returns an aggregated {@link Money} value.
 */
public abstract class MoneyMetric extends Metric<Money> {

  private MoneyMetric(String name) {
    super(name);
  }
  
  public static MoneyMetric first(String name) {
    return new MoneyMetric(name) {
      private Money aggregate = null;
      
      @Override
      protected void add(Money value) {
        if (aggregate == null) {
          aggregate = value;
        }
      }

      @Override
      public Money getValue() {
        return aggregate;
      }      
    };
  }

  public static MoneyMetric last(String name) {
    return new MoneyMetric(name) {
      private Money aggregate;
      
      @Override
      protected void add(Money value) {
        aggregate = value;
      }

      @Override
      public Money getValue() {
        return aggregate;
      }      
    };
  }

  public static MoneyMetric sum(String name) {
    return new MoneyMetric(name) {
      private Money aggregate = Money.zero();
      
      @Override
      protected void add(Money value) {
        aggregate = aggregate.add(value);
      }

      @Override
      public Money getValue() {
        return aggregate;
      }      
    };
  }

  public static MoneyMetric average(String name) {
    return new MoneyMetric(name) {
      private Money aggregate = Money.zero();
      private int count;
      
      @Override
      protected void add(Money value) {
        aggregate = aggregate.add(value);
        count++;
      }

      @Override
      public Money getValue() {
        return aggregate.scale(1.0/count);
      }      
    };
  }

  public static MoneyMetric max(String name) {
    return new MoneyMetric(name) {
      private Money aggregate = Money.zero();
      
      @Override
      protected void add(Money value) {
        if (value.isAtLeast(aggregate)) {
          aggregate = value;
        }
      }

      @Override
      public Money getValue() {
        return aggregate;
      }      
    };
  }

  public static MoneyMetric min(String name) {
    return new MoneyMetric(name) {
      private Money aggregate = null;
      
      @Override
      protected void add(Money value) {
        if (aggregate == null || aggregate.isAtLeast(value)) {
          aggregate = value;
        }
      }

      @Override
      public Money getValue() {
        return aggregate;
      }      
    };
  }
}
