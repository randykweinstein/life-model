package com.calculr.lifemodel.engine;

import java.time.LocalDate;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.calculr.lifemodel.finance.SchedulerException;

/**
 * A Scheduler schedules tasks within {@link Actor Actors}. 
 */
public class Scheduler {
  private LocalDate currentDate;
  private Simulation sim;
  private final Map<LocalDate, DailyTasks> insertionMap;
  private final PriorityQueue<DailyTasks> queue;
  
  Scheduler(LocalDate startDate) {
    currentDate = startDate;
    insertionMap = new HashMap<>();
    queue = new PriorityQueue<>();
  }

  /**
   * The time of the day that the {@link Task} is scheduled to run at.
   */
  enum TimeOfDay {
    START_OF_DAY,
    WITHIN_DAY,
    END_OF_DAY,
    IMMEDIATELY
  }

  /**
   * Schedules a {@link Task} by an {@link Actor} on a scheduled {@code date}
   * and {@link TimeOfDay}.
   * 
   * @param date the date to execute the {@link Task}
   * @param timeOfDay the time of day (start of day, end of day, or within the day)
   * @param actor the {@link Actor} that has produced the {@link Task}
   * @param task the {@link Task} that will be executed
   * @throws SchedulerException if the task is attempted to be scheduled after the date/time has 
   *     passed.
   */
  <T extends Actor<T>> void scheduleFromActor(LocalDate date, TimeOfDay timeOfDay, T actor,
      Task<T> task) {
    if (currentDate.isAfter(date)) {
      throw new SchedulerException("Can not schedule a task on %s which after the current date %s",
          date, currentDate);
    }
    TaskEntry<T> entry = new TaskEntry<>(sim, actor, task);
    getDailyTasks(date).add(entry, timeOfDay);
  }  
  
  private DailyTasks getDailyTasks(LocalDate date) {
    DailyTasks tasks;
    if (!insertionMap.containsKey(date)) {
      tasks = new DailyTasks(date);
      
      // The date has not been previously scheduled, so we create the DailyTasks
      // object and add it to the hash map used for modifications and the priority
      // queue used for simulation.
      insertionMap.put(date, tasks);
      queue.add(tasks);
    } else {    
      tasks = insertionMap.get(date);
    }
    return tasks;
  }
  
  <T extends Actor<T>> void scheduleImmediately(T actor, Task<T> task) {
    TaskEntry<T> entry = new TaskEntry<>(sim, actor, task);
    DailyTasks tasks = getDailyTasks(currentDate);
    tasks.add(entry, TimeOfDay.IMMEDIATELY);
  }
  
  void setSimulation(Simulation sim) {
    this.sim = sim;
  }
  
  /**
   * The internal state of the {@link Scheduler}.
   */
  private enum State {
    /** The scheduler has not started. */
    INIT,
    
    /** The scheduler is running. */
    RUNNING,
    
    /**
     * The scheduler has completed. Note that tasks can be still be scheduled in
     * this state. The scheduler will complete the final day and then exit.
     */
    COMPLETED
  }
  private State state = State.INIT;
  
  /**
   * Runs the scheduler. Additional tasks can be scheduled as the scheduler
   * executed. Execution will complete after the call to {@link #complete()}.
   */
  void run() {
    switch (state) {
    case INIT:
      // good state
      break;
    case RUNNING:
      throw new SchedulerException("Unexpected reinvocation of run method.");
    case COMPLETED:
      throw new SchedulerException("The simulation has already completed.");
    }
    state = State.RUNNING;
    while (state == State.RUNNING && !queue.isEmpty()) {
      DailyTasks tasks = queue.remove();
      currentDate = tasks.date;
      tasks.run();
      
      // Remove last reference to the tasks.
      insertionMap.remove(currentDate);
    }
  }

  /**
   * Completes the execution of the schedule. Once this state change is made,
   * the rest of the daily tasks will complete, but no more tasks will run after
   * the end of day.
   */
  void complete() {
    state = State.COMPLETED;
  }
  
  /**
   * Returns the current date of the {@link Scheduler}.
   */
  public LocalDate getCurrentDate() {
    return currentDate;
  }
  
  /**
   * The set of tasks that are scheduled to run on a specified date. 
   */
  static class DailyTasks implements Comparable<DailyTasks> {    
    private final LocalDate date;
    
    DailyTasks(LocalDate date) {
      this.date = date;
    }
    
    /** The state of the daily task from the perspective of the simulator. */
    private enum State {
      /**
       * States begin in this initialized state. All {@link Scheduler.TimeOfDay} tasks can
       * be scheduled now.
       */
      INIT,
      
      /**
       * When the DailyTasks starts running, it is the START_OF_DAY tasks. Only
       * {@link State#WITHIN_DAY} and {@link State#END_OF_DAY} tasks can
       * be scheduled in this state.
       */
      START_OF_DAY,
      
      /**
       * The tasks in the middle of the day are running. Only tasks that are at
       * the {@link State#END_OF_DAY} can be scheduled.
       */
      WITHIN_DAY,
      
      /**
       * The final tasks for the day are running.  No more tasks can be scheduled.
       */
      END_OF_DAY,
      
      /**
       * All tasks have been completed. This is now inactive and all references
       * to this can be removed.
       */
      DAY_COMPLETED
    }
    private State state = State.INIT;
    
    private Queue<TaskEntry<?>> immediateTasks;
    private final Queue<TaskEntry<?>> startOfDayTasks = new LinkedList<>();
    private final Queue<TaskEntry<?>> dayTasks = new LinkedList<>();
    private final Deque<TaskEntry<?>> endOfDayTasks = new LinkedList<>();
    
    /**
     * Add a new {@link TaskEntry} to this set of daily executable tasks, at the
     * specified time of day.
     */
    void add(TaskEntry<?> entry, Scheduler.TimeOfDay timeOfDay) {
      switch (timeOfDay) {
      case START_OF_DAY:
        //noinspection SwitchStatementWithTooFewBranches
        switch (state) {
          case INIT -> startOfDayTasks.add(entry);
          default -> throw new SchedulerException(
              "Can not add start of day tasks to an already running or completed day (state = %s).",
              state.name());
        }
        break;
      case WITHIN_DAY:
        switch (state) {
          case INIT, START_OF_DAY -> dayTasks.add(entry);
          default -> throw new SchedulerException(
              "Can not add within day tasks to an already running or completed day (state = %s).",
              state.name());
        }
        break;
      case END_OF_DAY:
        switch (state) {
          case INIT, START_OF_DAY, WITHIN_DAY -> endOfDayTasks.addFirst(entry);
          default -> throw new SchedulerException(
              "Can not add end of day tasks to an already running or completed day (state = %s).",
              state.name());
        }
        break;
      case IMMEDIATELY:
        switch (state) {
          case INIT, START_OF_DAY, WITHIN_DAY, END_OF_DAY -> {
            if (immediateTasks == null) {
              immediateTasks = new ConcurrentLinkedQueue<>();
            }
            immediateTasks.add(entry);
          }
          default -> throw new SchedulerException(
              "Can not add immediate tasks to an already completed day (state = %s).",
              state.name());
        }
      }
    }
    
    /**
     * Executes all of the tasks for the day.
     */
    void run() {
      if (state != State.INIT) {
        throw new SchedulerException(
            "Can not invoke the daily task while it is already running or completed (state = %s).", 
            state.name());
      }
      runImmediateTasks();

      state = State.START_OF_DAY;
      runTasks(startOfDayTasks);
      state = State.WITHIN_DAY;
      runTasks(dayTasks);
      state = State.END_OF_DAY;
      runTasks(endOfDayTasks);
      state = State.DAY_COMPLETED;
    }
    
    void runTasks(Iterable<TaskEntry<?>> tasks) {
      for (TaskEntry<?> entry : tasks) {
        runTask(date, entry);
        runImmediateTasks();
      }
    }

    void runImmediateTasks() {
      if (immediateTasks != null) {
        while (!immediateTasks.isEmpty()) {
          runTask(date, immediateTasks.poll());
        }
      }
    }
    
    TimeOfDay getCurrentTimeOfDay() {
      return switch (state) {
        case INIT, START_OF_DAY -> TimeOfDay.START_OF_DAY;
        case WITHIN_DAY -> TimeOfDay.WITHIN_DAY;
        case END_OF_DAY -> TimeOfDay.END_OF_DAY;
        default -> throw new SchedulerException(
            "There is no valid time of day since the daily tasks have completed");
      };
    }
    
    private static <T extends Actor<T>> void runTask(LocalDate date, TaskEntry<T> entry) {
      entry.task.run(new TaskContext<>(entry.actor, entry.sim));
    }

    @Override
    public int compareTo(DailyTasks o) {
      return date.compareTo(o.date);
    }
  }
  
  static class TaskEntry<T extends Actor<T>> {
    final Simulation sim;
    final T actor;
    final Task<T> task;
    
    TaskEntry(Simulation sim, T actor, Task<T> task) {
      this.sim = sim;
      this.actor = actor;
      this.task = task;
    }
  }
}
