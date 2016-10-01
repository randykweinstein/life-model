package com.calculr.lifemodel.engine_old;

import java.time.LocalDate;

public class ScheduledEvent<T extends State<T>> implements Event<T>, Comparable<ScheduledEvent<T>> {

  private final Event<T> event;
  private final LocalDate date;
  
  ScheduledEvent(Event<T> event, LocalDate date) {
    this.event = event;
    this.date = date;
  }
  
  public LocalDate getDate() {
    return date;
  }
  
  @Override
  public void evaluate(Scheduler<T> scheduler, T state) {
    event.evaluate(scheduler, state);
  }

  @Override
  public int compareTo(ScheduledEvent<T> o) {
    return date.compareTo(o.getDate());
  }
}
