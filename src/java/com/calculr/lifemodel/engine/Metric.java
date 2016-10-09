package com.calculr.lifemodel.engine;

import java.util.Objects;

/**
 * A type of metric taken from a {@link Simulation}.
 * 
 * @param T type of return value
 */
public abstract class Metric<T> {
  private final String name;
  
  protected Metric(String name) {
    this.name = name;
  }
  
  /**
   * Returns the name of the metric.
   */
  public String getName() {
    return name;
  }
  
  /**
   * Returns the value stored in the metric.
   */
  public abstract T getValue();
  
  @Override
  public String toString() {
    return name + ": " + getValue();
  }

  /**
   * Updates the embedded state based on the new value.
   */
  protected abstract void add(T value);
  
  @Override
  public int hashCode() {
    return Objects.hashCode(name);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Metric<?> other = (Metric<?>) obj;
    if (name == null) {
      if (other.name != null)
        return false;
    } else if (!name.equals(other.name))
      return false;
    return true;
  }
}
