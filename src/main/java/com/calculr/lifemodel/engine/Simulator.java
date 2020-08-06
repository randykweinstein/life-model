package com.calculr.lifemodel.engine;

import java.time.LocalDate;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A Simulator controls the operation of {@link Actor} objects producing tasks.
 */
public final class Simulator implements Simulation {
  private final Scheduler scheduler;
  private SimulatorActor actor;
  private Map<String, Metric<?>> metrics;
  
  Simulator(Scheduler scheduler) {
    this.scheduler = scheduler;
    metrics = new LinkedHashMap<>();
  }
  
  /**
   * Constructs a new {@link Simulator} that begins on the specified start date.
   */
  public static Simulator create(LocalDate startDate) {
    Scheduler scheduler = new Scheduler(startDate);
    Simulator sim = new Simulator(scheduler);
    scheduler.setSimulation(sim);
    SimulatorActor actor = new SimulatorActor(sim);
    sim.actor = actor;
    return sim;
  }

  /**
   * Begins the simulation at the current date, simulating until reaching the final date.
   */
  public Collection<Metric<?>> runUntil(LocalDate date) {
    actor.stopOn(date);
    scheduler.run();
    return metrics.values();
  }
  
  /**
   * Registers the specified {@link Actor} with this {@link Simulator}. This
   * will assign the {@link Scheduler} into the {@link Actor}.
   */
  @Deprecated
  public Simulator register(Actor<?> actor) {
    //actor.assignScheduler(scheduler);
    return this;
  }
  
  /**
   * An internal {@link Actor} that is used by the {@link Simulator}.
   */
  private static class SimulatorActor extends Actor<SimulatorActor> {

    SimulatorActor(Simulation simulation) {
      super(simulation);
    }
    
    /**
     * Stops the simulation on the specified date. 
     */
    void stopOn(LocalDate date) {
      scheduleEndOfDay(date, context -> {
        complete();
      });
    }

    @Override
    public void onRegister(LocalDate date) {
      // Ignore...
    }
  }

  @Override
  public Scheduler getScheduler() {
    return scheduler;
  }

  @Override
  public <T> void update(Metric<T> metric, T value) {
    String name = metric.getName();
    if (metrics.containsKey(name)) {
      if (!metrics.get(name).equals(metric)) {
        throw new RuntimeException(String.format("Metric '%s' has been reassigned", name));
      }
    } else {
      metrics.put(metric.getName(), metric);
    }
    metric.add(value);
  }

  @Override
  public Collection<Metric<?>> getMetrics() {
    return metrics.values();
  }
}
