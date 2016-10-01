package com.calculr.lifemodel.engine;

import java.time.LocalDate;
import java.util.Collection;

/**
 * A trial is a single invocation of the simulator.
 */
public interface Trial {

  /**
   * Returns the name of the trial.
   */
  String getTrialName();

  /**
   * Executes the trial returning the simulation metrics for evaluation.
   * 
   * @param startDate the date that the simulation will begin
   * @param simulation the interface into the {@link Simulation}
   */
  void run(LocalDate startDate, Simulation simulation);
}
