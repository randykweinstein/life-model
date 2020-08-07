package com.calculr.lifemodel.engine.parameter;

public class ConstantParameter<T> implements Parameter<T> {
    private final String name;
    private final T value;

    public ConstantParameter(String name, T value) {
        this.name = name;
        this.value = value;
    }

    @Override
    public T get() {
        return value;
    }

    @Override
    public String getName() {
        return name;
    }
}
