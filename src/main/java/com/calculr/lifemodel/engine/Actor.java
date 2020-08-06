package com.calculr.lifemodel.engine;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Optional;
import java.util.function.Supplier;

import com.calculr.lifemodel.engine.Scheduler.TimeOfDay;
import com.calculr.lifemodel.finance.SchedulerException;

/**
 * An actor is an entity that can schedule tasks.
 * 
 * @param T the type of Actor
 */
public abstract class Actor<T extends Actor<T>> {

  /** State of the {@link Actor}. */
  enum State {
    /** The state has been initialized but not registered to a simulator. */  
    UNREGISTERED,
    
    /** The state has been registered to a simulator but is not in an enabled state. */
    REGISTERED,
    
    /**
     * The state is registered and enabled, and all tasks invoked by this state
     * will be executed.
     */
    ENABLED;
  }
  private State state = State.REGISTERED;
  
  private final Simulation simulation;
  
  @SuppressWarnings("unchecked")
  protected Actor(Simulation simulation) {
    this.simulation = simulation;
    simulation.getScheduler().scheduleImmediately((T) this,
        context -> ((Actor<T>) context.getActor()).onRegister(context.getDate()));
  }

  /**
   * Schedules a {@link Task} to execute on the specified date.
   */
  public void schedule(LocalDate date, Task<T> task) {
    scheduleInternal(date, task, TimeOfDay.WITHIN_DAY);
  }

  /**
   * Schedules a {@link Task} to execute at the start of the specified date.
   */
  public void scheduleStartOfDay(LocalDate date, Task<T> task) {
    scheduleInternal(date, context -> task.run(context),
        TimeOfDay.START_OF_DAY);
  }

  /**
   * Schedules a {@link Task} to execute at the end of the specified date.
   */
  public void scheduleEndOfDay(LocalDate date, Task<T> task) {
    scheduleInternal(date, context -> task.run(context), TimeOfDay.END_OF_DAY);
  }

  /**
   * Schedules a {@link Task} to execute immediately.
   */
  protected void scheduleImmediately(Task<T> task) {
    scheduleInternal(simulation.getCurrentDate(), task, TimeOfDay.IMMEDIATELY);
  }
  
  void scheduleInternal(LocalDate date, Task<T> task, TimeOfDay timeOfDay) {
    if (state == State.UNREGISTERED) {
      throw new SchedulerException("Actor '%s' can not be scheduled if it's not registered.",
          this);      
    }
    state = State.ENABLED;
    @SuppressWarnings("unchecked")
    T actor = (T) this;
    simulation.getScheduler().scheduleFromActor(date, timeOfDay, actor, task);    
  }
  
  private static class ScheduleSpec {
    private final LocalDate startingDate;
    private Starting starting = null;
    private Optional<Repeating> repeating = Optional.empty();
    private Completing completing = Completing.indefinitely();
    private Scheduler.TimeOfDay timeOfDay = Scheduler.TimeOfDay.WITHIN_DAY;
    
    ScheduleSpec(LocalDate date) {
      startingDate = date;
    }
  }
  
  public ScheduleStart onSchedule() {
    ScheduleSpec spec = new ScheduleSpec(simulation.getScheduler().getCurrentDate());
    return new ScheduleStart(spec);
  }
  
  public class ScheduleStart {
    private final ScheduleSpec spec;
    
    private ScheduleStart(ScheduleSpec spec) {
      this.spec = spec;
    }

    public ScheduleRepeating startingToday() {
      spec.starting = Starting.immediately();
      return new ScheduleRepeating(spec);
    }
    
    public ScheduleRepeating starting(DayOfWeek dayOfWeek) {
      spec.starting = Starting.on(dayOfWeek);
      return new ScheduleRepeating(spec);
    }

    public ScheduleRepeating starting(int dayOfMonth) {
      spec.starting = Starting.dayOfMonth(dayOfMonth);
      return new ScheduleRepeating(spec);
    }

    public ScheduleRepeating startingIn(int numDays) {
      spec.starting = Starting.inNDays(numDays);
      return new ScheduleRepeating(spec);
    }

    public ScheduleRepeating starting(LocalDate startDate) {
      spec.starting = Starting.on(startDate);
      return new ScheduleRepeating(spec);
    }
  }
  
  public class ScheduleRepeating extends ScheduleTimeOfDay {
    private ScheduleRepeating(ScheduleSpec spec) {
      super(spec);
    }

    public ScheduleCompletion runDaily() {
      spec.repeating = Optional.of(Repeating.DAILY);
      return new ScheduleCompletion(spec);
    }
    public ScheduleCompletion runWeekly() {
      spec.repeating = Optional.of(Repeating.WEEKLY);
      return new ScheduleCompletion(spec);
    }
    public ScheduleCompletion runMonthly() {
      spec.repeating = Optional.of(Repeating.MONTHLY);
      return new ScheduleCompletion(spec);
    }
    public ScheduleCompletion runEveryNDays(int numDays) {
      spec.repeating = Optional.of(Repeating.everyNDays(numDays));
      return new ScheduleCompletion(spec);
    }
    public ScheduleCompletion runEveryNWeeks(int numWeeks) {
      spec.repeating = Optional.of(Repeating.everyNWeeks(numWeeks));
      return new ScheduleCompletion(spec);
    }
    public ScheduleCompletion runEveryNMonths(int numMonths) {
      spec.repeating = Optional.of(Repeating.everyNMonths(numMonths));
      return new ScheduleCompletion(spec);
    }
  }
  
  public class ScheduleCompletion extends ScheduleTimeOfDay {
    private ScheduleCompletion(ScheduleSpec spec) {
      super(spec);
    }

    public ScheduleTimeOfDay stopAfter(int numTimes) {
      spec.completing = Completing.after(numTimes);
      return new ScheduleTimeOfDay(spec);
    }
    public ScheduleTimeOfDay stopAfter(LocalDate lastDate) {
      spec.completing = Completing.after(lastDate);
      return new ScheduleTimeOfDay(spec);
    }
    public ScheduleTimeOfDay stopWhen(Supplier<Boolean> predicate) {
      spec.completing = Completing.when(predicate);
      return new ScheduleTimeOfDay(spec);
    }
  }
  
  public class ScheduleTimeOfDay extends ScheduleRun {    
    private ScheduleTimeOfDay(ScheduleSpec spec) {
      super(spec);
    }

    public ScheduleRun atStartOfDay() {
      spec.timeOfDay = Scheduler.TimeOfDay.START_OF_DAY;
      return new ScheduleRun(spec);
    }

    public ScheduleRun atEndOfDay() {
      spec.timeOfDay = Scheduler.TimeOfDay.END_OF_DAY;
      return new ScheduleRun(spec);
    }
  }
  
  public class ScheduleRun {
    protected final ScheduleSpec spec;
    
    private ScheduleRun(ScheduleSpec spec) {
      this.spec = spec;
    }

    public void schedule(Task<T> task) {   
      scheduleInternal(spec.starting.start(spec.startingDate), new RepeatingTask<T>(task, spec),
          spec.timeOfDay);
    }
    
//    public void schedule(SimpleTask task) {
//      scheduleInternal(spec.starting.start(spec.startingDate),
//          new RepeatingTask<T>((date, actor) -> task.run(date), spec), spec.timeOfDay);
//    }
  }
  
  static class RepeatingTask<T extends Actor<T>> implements Task<T> {
    private final Task<T> task;
    private final ScheduleSpec spec;
    
    RepeatingTask(Task<T> task, ScheduleSpec spec) {
      this.task = task;
      this.spec = spec;
    }
    
    @Override
    public void run(TaskContext<T> context) {      
      // Run this iteration.
      task.run(context);
      if (!spec.repeating.isPresent()) {
        return;
      }
      LocalDate date = context.getDate();
      if (!spec.completing.isComplete(date)) {
        context.getActor().scheduleInternal(spec.repeating.get().next(date), this, spec.timeOfDay);
      }
    }
  }
  
  /**
   * Ends the execution of this simulation.
   */
  public void complete() {
    simulation.getScheduler().complete();
  }
  
  /**
   * Invokes any code that should run at the moment the {@link Actor} is registered.
   */
  public abstract void onRegister(LocalDate date);
}
