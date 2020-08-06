package com.calculr.lifemodel.books;

import java.time.LocalDate;

import com.calculr.lifemodel.engine.Simulation;
import com.calculr.lifemodel.finance.Bill;
import com.calculr.lifemodel.finance.Payee;
import com.calculr.lifemodel.finance.Payer;

/**
 * A {@link Payer} that does not check if the money is in the account before paying.
 */
public class BlindPayer extends Payer {
  private static final int DAYS_BEFORE_DUE_DATE = 1;
  
  private final AssetAccount bankAccount; 
  
  public BlindPayer(Simulation simulation, AssetAccount bankAccount) {
    super(simulation);
    this.bankAccount = bankAccount;
  }

  @Override
  public void onBill(Payee payee, Bill bill) {
    schedule(bill.getDueDate().minusDays(DAYS_BEFORE_DUE_DATE), context -> {
      bill.pay(bankAccount, payee.getPeriodPayment());
    });
  }

  @Override
  public void onRegister(LocalDate date) {
    // TODO Auto-generated method stub

  }

}
