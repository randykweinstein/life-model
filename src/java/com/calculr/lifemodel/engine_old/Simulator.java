package com.calculr.lifemodel.engine_old;

import java.time.LocalDate;

public class Simulator<T extends State<T>> {

  private final Scheduler<T> scheduler;
  
  public static <T extends State<T>> Simulator<T> create(T state) {
    return new Simulator<T>(Scheduler.create(state));
  }
  
  Simulator(Scheduler<T> scheduler) {
    this.scheduler = scheduler;
  }
    
  public void runFrom(LocalDate date) {
    scheduler.init(date);
    start();
  }

  public void runBetween(LocalDate startDate, LocalDate endDate) {
    scheduler.init(startDate);
    scheduler.scheduleOn(new CompletionEvent(), endDate);
    start();
  }
  
  void start() {
    scheduler.start();
  }
  
  void end() {
    
  }

  private final class CompletionEvent implements Event<T> {
    @Override
    public void evaluate(Scheduler<T> scheduler, T state) {
      scheduler.end();
    }
  }
}
