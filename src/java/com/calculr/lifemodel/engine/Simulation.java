package com.calculr.lifemodel.engine;

import java.time.LocalDate;
import java.util.Collection;

/**
 * A {@link Simulation} provides the context for scheduling events. 
 */
public interface Simulation {

  /**
   * Begins the simulation at the current date, simulating until reaching the final date.
   * 
   * @param date the final date to run until, stopping at the end of day
   * @return a collection of all updated metrics
   */
  Collection<Metric<?>> runUntil(LocalDate date);
  
  /**
   * Returns a scheduler object from the {@link Simulator}.
   */
  Scheduler getScheduler();
  
  /**
   * Returns the current date of the running simulation.
   */
  public default LocalDate getCurrentDate() {
    return getScheduler().getCurrentDate();
  }
  
  /**
   * Updates the specified metric with the new value.
   */
  <T> void update(Metric<T> metric, T value);
  
  /**
   * Returns all metrics that have been updated as part of the simulation.
   */
  Collection<Metric<?>> getMetrics();
}
