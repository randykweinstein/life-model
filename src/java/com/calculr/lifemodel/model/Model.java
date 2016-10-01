package com.calculr.lifemodel.model;

import java.time.DayOfWeek;
import java.time.LocalDate;

import com.calculr.lifemodel.engine.Repeating;
import com.calculr.lifemodel.engine.Starting;
import com.calculr.lifemodel.engine_old.Event;
import com.calculr.lifemodel.engine_old.Scheduler;
import com.calculr.lifemodel.engine_old.Simulator;
import com.calculr.lifemodel.finance.BalanceSheet;
import com.calculr.lifemodel.finance.OldAccount;
import com.calculr.lifemodel.finance.CheckingAccount;
import com.calculr.lifemodel.finance.CreditCardAccount;
import com.calculr.lifemodel.finance.Money;

public class Model {
  static class LoggingEvent implements Event<BalanceSheet> {
    @Override
    public void evaluate(Scheduler<BalanceSheet> scheduler, BalanceSheet state) {
      System.out.format("%s: %s\n", scheduler.getCurrentDate(), state);
    }
  }
  
  /**
   * A {@link BalanceSheet} that is used for building accounts and tracking income.
   */
  static class PersonalBalanceSheet extends BalanceSheet {
    @Override
    public void initSheet(Scheduler<BalanceSheet> scheduler) {
      initBalanceSheets(Money.zero(), Money.zero(), Money.zero(), Money.zero());
      
      // Add a monthly logger
      scheduler.scheduleRepeating(new LoggingEvent(), Starting.firstOfMonth(), Repeating.MONTHLY);
      
      // Add a checking account.
      CheckingAccount checking = newCheckingAccount("Checking")
          .setBalance(Money.dollars(5000))
          .setInterestRate(0.001)
          .enableOverdraft(Money.dollars(30))
          .build();
      
      // Add a credit card account.
      CreditCardAccount credit = newCreditAccount("Credit")
          .setBalance(Money.dollars(-2000))
          .setCreditLimit(Money.dollars(-18000))
          .setInterestRate(0.1)
          .build();
          
      
      // Add a salary
      scheduler.scheduleRepeating(RecurringEvents.income(checking, "Income", Money.dollars(3500)), 
          Starting.on(DayOfWeek.FRIDAY), Repeating.everyNWeeks(2));
            
      // Add some expenses
      scheduler.scheduleRepeating(
          RecurringEvents.dailyExpenses(credit, "Purchases", Money.dollars(300)),
          Repeating.DAILY);
//      
      // Every 5th of the month, schedule a credit card payment.
      scheduler.scheduleRepeating(RecurringEvents.creditCardPayment(checking, credit),
          Starting.inNDays(5), Repeating.MONTHLY);
    }
    
    public OldAccount checking() {
      return lookup("Checking");
    }
  }
  
  public static void main(String[] args) {
    BalanceSheet sheet = new PersonalBalanceSheet();
    Simulator<BalanceSheet> sim = Simulator.create(sheet);
    sim.runBetween(LocalDate.of(2016, 1, 1), LocalDate.of(2017, 1, 1));
  }
}
