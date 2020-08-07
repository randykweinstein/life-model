package com.calculr.lifemodel.engine;

/**
 * A task is a block of executable code that runs according to a specified schedule.
 * 
 * @param <T> the type of the {@link Actor}
 */
public interface Task<T extends Actor<T>> {
  
  /**
   * Runs the specified code block providing the {@link TaskContext} as an argument.
   */
  void run(TaskContext<T> context); 
}
