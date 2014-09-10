package com.calculr.lifemodel.engine;

/**
 * An action that occurs as a specific point of time.
 */
public interface Event {

  /**
   * Evaluates the event given the current model state.
   * 
   * @param state the {@link State} of the model, i.e. the current values of each of the underlying
   *        dynamic states
   */
	void evaluate(State state);
}
