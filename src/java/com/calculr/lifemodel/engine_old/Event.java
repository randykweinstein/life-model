package com.calculr.lifemodel.engine_old;

/**
 * An action that occurs as a specific point of time.
 */
public interface Event<T extends State<T>> {

  /**
   * Evaluates the event given the current model state. 
   */
	void evaluate(Scheduler<T> scheduler, T state);
}
