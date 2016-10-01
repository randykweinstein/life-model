package com.calculr.lifemodel.books;

import java.time.LocalDate;

import com.calculr.lifemodel.engine.Actor;
import com.calculr.lifemodel.engine.Task;
import com.calculr.lifemodel.engine.Simulation;
import com.calculr.lifemodel.engine.TaskContext;

/**
 * An {@link Actor} that deposits interest into an {@link AssetAccount}.
 */
public class InterestActor extends Actor<InterestActor> {
  private final Account account;
  private final double interestRate;
  
  InterestActor(Simulation simulation, Account account, double interestRate) {
    super(simulation);
    this.account = account;
    this.interestRate = interestRate;
  }

  @Override
  public void onRegister(LocalDate date) {
    onSchedule().starting(date.plusMonths(1)).runMonthly().atStartOfDay()
        .schedule(new InterestTask(interestRate / 12));
  }
  
  private static class InterestTask implements Task<InterestActor> {
    private final double monthlyRate;
    
    InterestTask(double monthlyRate) {
      this.monthlyRate = monthlyRate;
    }
    
    @Override
    public void run(TaskContext<InterestActor> context) {
      LocalDate date = context.getDate();
      Account account = context.getActor().account;
      if (account instanceof AssetAccount) {
        AssetAccount asset = (AssetAccount) account;
        if (monthlyRate > 0) {
          Transaction transaction = Transaction.create(date, "interest",
              account.getPostedBalance().scale(monthlyRate));
          asset.deposit(transaction);
        } else {
          Transaction transaction = Transaction.create(date, "interest",
              account.getPostedBalance().scale(-monthlyRate));
          asset.withdraw(transaction);
        }
      } else if (account instanceof LiabilityAccount) {
        LiabilityAccount liability = (LiabilityAccount) account;
        if (monthlyRate > 0) {
          if (account.getPostedBalance().sign() < 0) {
            Transaction transaction = Transaction.create(date, "interest",
                account.getPostedBalance().scale(monthlyRate).negate());
            liability.purchase(transaction);
          }
        } else {
          if (account.getPostedBalance().sign() > 0) {
            Transaction transaction = Transaction.create(date, "interest",
                account.getPostedBalance().scale(-monthlyRate));
            liability.payment(transaction);
          }
        }
      } else {
        throw new IllegalArgumentException(
            "Account must be an asset or liability for an interest calculation");
      }
    }
  }
}