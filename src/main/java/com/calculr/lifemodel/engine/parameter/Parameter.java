package com.calculr.lifemodel.engine.parameter;

import java.util.function.Supplier;

/**
 * A parameter is defined outside of an actor and provides values inside an entity.
 *
 * @param <T> the type of parameter to provide
 */
public interface Parameter<T> extends Supplier<T> {

    /**
     * Returns the identifier for the parameter.
     */
    String getName();
}
