package com.calculr.lifemodel.engine;

import java.time.LocalDate;

public class TestMain {

  static class LoggingActor extends Actor<LoggingActor> {
    public LoggingActor(Simulation simulator) {
      super(simulator);
    }

    @Override
    public void onRegister(LocalDate date) {
      onSchedule().starting(15).runDaily()
          .schedule(context -> System.out.format("%s\n", context.getDate()));
      onSchedule().starting(15).runWeekly().atStartOfDay()
          .schedule(context -> System.out.format("Weekly %s\n", context.getDate()));
      onSchedule().starting(15).runMonthly().stopAfter(20).atEndOfDay()
          .schedule(context -> System.out.format("Monthly %s\n", context.getDate()));
    };
  }
  
  public static void main(String[] args) {
    Simulator simulator = Simulator.create(LocalDate.now());
    new LoggingActor(simulator);
    simulator.runUntil(LocalDate.now().plusYears(1));    
  }
}
