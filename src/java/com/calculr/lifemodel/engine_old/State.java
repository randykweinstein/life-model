package com.calculr.lifemodel.engine_old;

/**
 * A state that is passed into the simulator and is available at the invocation of each event.
 */
public interface State<T extends State<T>> {

  void addActor(Actor<T> actor);
  
  void init(Scheduler<T> scheduler);
  
  void reset(Scheduler<T> scheduler);
}
