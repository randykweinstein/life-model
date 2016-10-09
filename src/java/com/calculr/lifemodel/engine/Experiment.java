package com.calculr.lifemodel.engine;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

public class Experiment {
  private Collection<Trial> trials;
  
  Experiment(Collection<Trial> trials) {
    this.trials = trials;
  }
  
  public static Experiment create(Trial... trials) {
    return new Experiment(Arrays.asList(trials));
  }
  
  /**
   * Executes each of the specified trials, returning a summarized result at the
   * end.
   * 
   * @param start
   *          the start date for the simulation. Each {@link Trial} is free to
   *          stop according to its needs.
   */
  public void run(LocalDate start) {
    Map<String, Collection<Metric<?>>> trialMetrics = new LinkedHashMap<>();
    Set<String> metricNames = new HashSet<>();
    for (Trial trial : trials) {
      Simulation sim = Simulator.create(start);
      System.out.println("Running Trial <" + trial.getTrialName() + ">");
      trial.run(start, sim);
      Collection<Metric<?>> metrics = sim.getMetrics();
      for (Metric<?> metric : metrics) {
        metricNames.add(metric.getName());
      }
      trialMetrics.put(trial.getTrialName(), metrics);
    }
    
    for (Entry<String, Collection<Metric<?>>> entry : trialMetrics.entrySet()) {
      System.out.println("\nTrial <" + entry.getKey() + ">");
      for (Metric<?> metric : entry.getValue()) {
        System.out.println("  " + metric);
      }
    }
  }
}
