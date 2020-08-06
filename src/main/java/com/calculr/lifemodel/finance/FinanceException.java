package com.calculr.lifemodel.finance;

public class FinanceException extends RuntimeException {

  private static final long serialVersionUID = 9210327742579835571L;

  protected FinanceException(String msg, Object... args) {
    super(String.format(msg, args));
  }
}
