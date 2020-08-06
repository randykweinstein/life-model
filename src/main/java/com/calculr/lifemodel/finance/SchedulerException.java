package com.calculr.lifemodel.finance;

/**
 * An exception raised from the scheduler. 
 */
public class SchedulerException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  /**
   * Constructs a new {@link SchedulerException} based on a formatted string.
   */
  public SchedulerException(String format, Object...args) {
    super(String.format(format, args));
  }
}
