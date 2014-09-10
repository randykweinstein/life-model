package com.calculr.lifemodel.engine;

import java.time.LocalDate;

/**
 * The state of the simulation.
 */
public class State {
  private EventQueue queue;
  private LocalDate currentDate;
  
  State(EventQueue queue, LocalDate startDate) {
    this.queue = queue;
    this.currentDate = startDate;
  }
  
  /**
   * Increment to the next local date.
   */
  void incrementDate() {
    currentDate = currentDate.plusDays(1);
  }
  
  /**
   * Returns the current local date. 
   */
  public LocalDate getCurrentDate() {
    return currentDate;
  }
  
  /**
   * Schedule a future event to execute.
   * 
   * @param event the {@link Event} to schedule
   * @param date the date for it to run
   */
  public void schedule(Event event, LocalDate date) {
    queue.add(event, date);
  }
  
  /**
   * Schedule a future event to run in a set number of days.
   * 
   * @param event the {@link Event} to schedule
   * @param days the number of days from the current date
   */
  public void scheduleIn(Event event, int days) {
    queue.add(event, currentDate.plusDays(days));
  }
}
