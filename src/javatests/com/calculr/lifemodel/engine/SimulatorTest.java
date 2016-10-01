package com.calculr.lifemodel.engine;

import java.time.LocalDate;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.calculr.lifemodel.engine_old.Actor;
import com.calculr.lifemodel.engine_old.Event;
import com.calculr.lifemodel.engine_old.Scheduler;
import com.calculr.lifemodel.engine_old.Simulator;
import com.calculr.lifemodel.engine_old.State;

@RunWith(JUnit4.class)
public class SimulatorTest {

  private static class LoggingEvent implements Event<TestingState> {
    @Override
    public void evaluate(Scheduler<TestingState> scheduler, TestingState state) {
      System.out.format("LoggingEvent @ %s\n", scheduler.getCurrentDate());
    }
  }
  
  private static class TestingState implements State<TestingState> {
    @Override
    public void init(Scheduler<TestingState> scheduler) {
      scheduler.scheduleIn(new LoggingEvent(), 4);
      scheduler.scheduleIn(new LoggingEvent(), 6);
      scheduler.scheduleIn(new LoggingEvent(), 8);
    }

    @Override
    public void reset(Scheduler<TestingState> scheduler) {
    }

    @Override
    public void addActor(Actor<TestingState> actor) {
      // TODO Auto-generated method stub
      
    }
  }
  
  @Test
  public void simulatorShouldStartAndFinish() {
    Simulator<TestingState> simulator = Simulator.create(new TestingState());
    simulator.runBetween(LocalDate.of(2016, 4, 15), LocalDate.of(2016, 8, 15));
  }

  @Test
  public void simulatorShouldFinishLeavingEvents() {
    Simulator<TestingState> simulator = Simulator.create(new TestingState());
    simulator.runBetween(LocalDate.of(2016, 4, 15), LocalDate.of(2016, 4, 22));
  }
}
