package com.calculr.lifemodel.books;

import com.calculr.lifemodel.engine.Actor;
import com.calculr.lifemodel.engine.Simulation;

public interface ActorFactory<S, T extends Actor<T>> {

  Actor<T> build(Simulation sim, S entity);
}
