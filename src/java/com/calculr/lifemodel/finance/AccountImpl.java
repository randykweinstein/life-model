package com.calculr.lifemodel.finance;

import com.calculr.lifemodel.engine_old.Scheduler;
import com.calculr.lifemodel.engine_old.State;

public class AccountImpl extends OldAccount {
  
  AccountImpl(String name, Money startingBalance) {
    super(name, startingBalance);
  }

  @Override
  public void onStart(Scheduler<BalanceSheet> scheduler, State<BalanceSheet> state) {
  }

  @Override
  public void onFinish(Scheduler<BalanceSheet> scheduler, State<BalanceSheet> state) {
  }
}
