package com.calculr.lifemodel.engine_old;

public interface Actor<T extends State<T>> {

  void onStart(Scheduler<T> scheduler, State<T> state);
  void onFinish(Scheduler<T> scheduler, State<T> state);
}
