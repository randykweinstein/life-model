package com.calculr.lifemodel.model;

import com.calculr.lifemodel.finance.BalanceSheet;
import com.calculr.lifemodel.engine_old.Event;
import com.calculr.lifemodel.engine_old.Scheduler;
import com.calculr.lifemodel.finance.OldAccount;
import com.calculr.lifemodel.finance.Money;

public class RecurringEvents {

  public static Event<BalanceSheet> income(OldAccount account, String desc, Money amount) {
    return new Event<BalanceSheet>() {
      @Override
      public void evaluate(Scheduler<BalanceSheet> scheduler, BalanceSheet state) {
        state.income(scheduler.getCurrentDate(), account, desc, amount);
      }
    };
  }

  public static Event<BalanceSheet> dailyExpenses(OldAccount account, String desc, Money amount) {
    return new Event<BalanceSheet>() {
      @Override
      public void evaluate(Scheduler<BalanceSheet> scheduler, BalanceSheet state) {
        state.borrow(scheduler.getCurrentDate(), account, desc, amount);        
      }
    };
  }

  private static final Money MINIMUM_BALANCE = Money.dollars(500);
  
  public static Event<BalanceSheet> creditCardPayment(OldAccount fromAccount, OldAccount toAccount) {
    return new Event<BalanceSheet>() {
      @Override
      public void evaluate(Scheduler<BalanceSheet> scheduler, BalanceSheet state) {
        Money cashBalance = fromAccount.getBalance();
        Money creditBalance = toAccount.getBalance();
        if (creditBalance.sign() >= 0) {
          // No payment due
          return;
        }
        Money paymentAmount;
        if (cashBalance.sub(MINIMUM_BALANCE).isAtLeast(creditBalance.negate())) {
          paymentAmount = creditBalance.negate();
        } else {
          paymentAmount = cashBalance.sub(MINIMUM_BALANCE);
        }
        state.payment(scheduler.getCurrentDate(), fromAccount, toAccount,
            "Payment:" + toAccount.getName(), paymentAmount);
      }
    };
  }
}