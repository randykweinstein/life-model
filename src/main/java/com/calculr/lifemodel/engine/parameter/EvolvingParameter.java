package com.calculr.lifemodel.engine.parameter;

import com.calculr.lifemodel.engine.Actor;
import com.calculr.lifemodel.engine.Simulation;

/**
 * A {@link Parameter} that evolves as an {@link Actor}.
 *
 * <p>The value can be updated according to the simulation.
 *
 * @param <T> the type of parameter to return
 */
public abstract class EvolvingParameter<T> extends Actor<EvolvingParameter<T>> implements Parameter<T> {
    private final String name;
    private T value;

    protected EvolvingParameter(Simulation simulation, String name, T initialValue) {
        super(simulation);
        this.name = name;
        value = initialValue;
    }

    protected void update(T newValue) {
        this.value = newValue;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public T get() {
        return value;
    }
}
