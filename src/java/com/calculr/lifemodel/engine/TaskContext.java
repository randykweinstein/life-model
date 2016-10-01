package com.calculr.lifemodel.engine;

import java.time.LocalDate;

/**
 * Returns a context of the simulation that is provided to scheduled tasks.
 */
public final class TaskContext<T extends Actor<T>> {
  private final T actor;
  private final Simulation sim;
  
  TaskContext(T actor, Simulation sim) {
    this.actor = actor;
    this.sim = sim;
  }
  
  /**
   * Returns the current date of the simulation.
   */
  public LocalDate getDate() {
    return sim.getScheduler().getCurrentDate();    
  }
  
  /**
   * Returns the actor that the task is operating over.
   */
  public T getActor() {
    return actor;
  }
  
  /**
   * Updates the value of the specified {@link Metric}.
   */
  public <S> void updateMetric(Metric<S> metric, S value) {
    sim.update(metric, value);
  }
}
