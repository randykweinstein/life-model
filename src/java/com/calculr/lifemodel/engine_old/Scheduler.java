package com.calculr.lifemodel.engine_old;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Collection;
import java.util.LinkedList;

import com.calculr.lifemodel.engine.Completing;
import com.calculr.lifemodel.engine.Repeating;
import com.calculr.lifemodel.engine.Starting;

/**
 * The state of the simulation.
 */
public class Scheduler<T extends State<T>> {
  private LocalDate currentDate;
  private final EventPriorityQueue<T> queue;
  private final T state;
  private final Collection<Actor<T>> actors;
  private boolean continueRunning = true;
  
  /**
   * Constructs a new {@link Scheduler} starting at the given day with a defined duration.
   */
  public static <T extends State<T>> Scheduler<T> create(T state) {
	  return new Scheduler<T>(new EventPriorityQueue<T>(), state);
  }
  
  Scheduler(EventPriorityQueue<T> queue, T state) {
    this.queue = queue;
    this.state = state;
    this.actors = new LinkedList<>();
  }
  
  public void register(Actor<T> actor) {
    actors.add(actor);
    state.addActor(actor);
    actor.onStart(this, state);
  }
  
  /**
   * Returns the current local date. 
   */
  public LocalDate getCurrentDate() {
    return currentDate;
  }
  
  void init(LocalDate startDate) {    
    if (currentDate == null) {
      currentDate = startDate;
      state.init(this);
    } else if (currentDate.isAfter(startDate)) {
      currentDate = startDate;
      state.reset(this);
      for (Actor<T> actor : actors) {
        actor.onFinish(this, state);
      }
      for (Actor<T> actor : actors) {
        actor.onStart(this, state);
      }
    }
    currentDate = startDate;
  }
  
  void start() {
    continueRunning = true;
    while (!queue.isEmpty() && continueRunning) {
      ScheduledEvent<T> event = queue.getNext();
      currentDate = event.getDate();
      event.evaluate(this, state);
    }
  }
  
  void end() {
    continueRunning = false;
    for (Actor<T> actor : actors) {
      actor.onFinish(this, state);
    }
  }

  /**
   * An {@link Event} that repeats according to the specified {@link Repeating}
   * criteria. This event will also stop repeating if the {@link Completing}
   * criteria is met.
   * 
   * @param <T> the type of {@link State} that is being simulated
   */
  private static class RepeatingEventWrapper<T extends State<T>> implements Event<T> {

    private final Event<T> event;
    private final Repeating recurrence;
    private final Completing completing;

    RepeatingEventWrapper(Event<T> event, Repeating recurrence, Completing completing) {
      this.event = event;
      this.recurrence = recurrence;
      this.completing = completing;
    }

    @Override
    public void evaluate(Scheduler<T> scheduler, T state) {
      LocalDate date = scheduler.getCurrentDate();
      event.evaluate(scheduler, state);
      if (!completing.isComplete(date)) {
        scheduler.scheduleOn(this, recurrence.next(date));
      }
    }
  }
  
  public void scheduleStarting(Event<T> event, Starting starting) {
    scheduleOn(event, starting.start(currentDate));
  }
  
  public void scheduleToday(Event<T> event) {
    scheduleOn(event, currentDate);
  }
  
  public void scheduleRepeating(Event<T> event, Repeating repeating) {
    scheduleRepeating(event, Starting.immediately(), repeating, Completing.indefinitely());
  }
  
  public void scheduleRepeating(Event<T> event, Starting starting, Repeating repeating) {
    scheduleRepeating(event, starting, repeating, Completing.indefinitely());
  }

  public void scheduleRepeating(Event<T> event, Starting starting, Repeating repeating,
      Completing completing) {
    scheduleStarting(new RepeatingEventWrapper<>(event, repeating, completing), starting);
  }

  /**
   * Schedules a future event to execute.
   * 
   * @param event the {@link Event} to schedule
   * @param date the date for it to run
   */
  public void scheduleOn(Event<T> event, LocalDate date) {
    queue.add(event, date);
  }
  
  /**
   * Schedules a future event to run in a set number of days.
   * 
   * @param event the {@link Event} to schedule
   * @param days the number of days from the current date
   */
  public void scheduleIn(Event<T> event, int days) {
    scheduleStarting(event, Starting.inNDays(days));
  }
  
  public void scheduleUpcoming(Event<T> event, DayOfWeek dayOfWeek) {
    scheduleIn(event, (dayOfWeek.getValue() - currentDate.getDayOfWeek().getValue()) % 7);
  }
}
