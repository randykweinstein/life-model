package com.calculr.lifemodel.books;

import java.time.LocalDate;
import java.util.Random;

import com.calculr.lifemodel.engine.Actor;
import com.calculr.lifemodel.engine.Simulation;
import com.calculr.lifemodel.engine.Simulator;
import com.calculr.lifemodel.finance.Money;

public class TestBooks {
  
  static class TransactionActor extends Actor<TransactionActor> {
    private final Random random = new Random();
    private final AssetAccount account;
    
    TransactionActor(Simulation simulation, AssetAccount account) {
      super(simulation);
      this.account = account;
    }

    @Override
    public void onRegister(LocalDate date) {
      onSchedule()
          .startingToday()
          .runDaily()
          .schedule(context -> account.deposit(Transaction.create(context.getDate(), "deposit",
              Money.dollars(random.nextInt(11) - 3))));
    }    
  }
  
  public static void main(String[] args) {
    Simulator simulator = Simulator.create(LocalDate.now());
    BalanceSheet sheet = BalanceSheet.create(simulator);
    AssetAccount savingsAccount = sheet.createInterestAccount("savings", 0.03);
    new TransactionActor(simulator, savingsAccount);
    AssetAccount greatInvestmentAccount = sheet.createInterestAccount("investment", 0.10);
    greatInvestmentAccount
        .deposit(Transaction.create(LocalDate.now(), "Initial investment", Money.dollars(1000)));

    simulator.runUntil(LocalDate.now().plusYears(30));
    System.out.format("Total assets: %s\n\n", sheet.get(BalanceSheet.ALL_ASSETS).getBalance());
    System.out.println(sheet.toString());
  }
}
