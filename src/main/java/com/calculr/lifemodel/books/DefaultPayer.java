package com.calculr.lifemodel.books;

import java.time.LocalDate;

import com.calculr.lifemodel.engine.Simulation;
import com.calculr.lifemodel.finance.Bill;
import com.calculr.lifemodel.finance.Payee;
import com.calculr.lifemodel.finance.Payer;

/**
 * A minimal implementation of a {@link Payer}.
 */
public class DefaultPayer extends Payer {
  private static final int DAYS_BEFORE_DUE_DATE = 1;
 
  private final AssetAccount bankAccount; 
  
  public DefaultPayer(Simulation simulation, AssetAccount bankAccount) {
    super(simulation);
    this.bankAccount = bankAccount;
  }
  
  @Override
  public void onBill(Payee payee, Bill bill) {
    schedule(bill.getDueDate().minusDays(DAYS_BEFORE_DUE_DATE), context -> {
      payPeriodPayment(bankAccount, bill);
    });
  }

  @Override
  public void onRegister(LocalDate date) {
    // TODO Auto-generated method stub
  }
}
