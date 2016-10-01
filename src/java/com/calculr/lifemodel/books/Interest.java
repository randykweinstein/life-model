package com.calculr.lifemodel.books;

import com.calculr.lifemodel.engine.Actor;
import com.calculr.lifemodel.engine.Simulation;

/**
 * An {@link ActorFactory} that deposits or withdraws interest to an {@link Account}.
 */
public class Interest implements ActorFactory<Account, InterestActor> {
  private final double rate;
  
  public Interest(double rate) {
    this.rate = rate;
  }
  
  @Override
  public Actor<InterestActor> build(Simulation sim, Account entity) {
    return new InterestActor(sim, entity, rate);
  }
}
