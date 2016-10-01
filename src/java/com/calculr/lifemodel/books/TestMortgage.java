package com.calculr.lifemodel.books;

import java.time.LocalDate;
import java.util.Collection;

import com.calculr.lifemodel.engine.Experiment;
import com.calculr.lifemodel.engine.Metric;
import com.calculr.lifemodel.engine.Simulation;
import com.calculr.lifemodel.engine.Simulator;
import com.calculr.lifemodel.engine.Trial;
import com.calculr.lifemodel.finance.Bill;
import com.calculr.lifemodel.finance.Money;
import com.calculr.lifemodel.finance.MoneyMetric;
import com.calculr.lifemodel.finance.Payee;
import com.calculr.lifemodel.finance.Payer;

public class TestMortgage {

  static class MortgageSimulation implements Trial {
    private final String trial;
    private final Money totalPrice;
    private final double mortgageRate;
    private final Money closingCosts;
    private final Money extraMonthlyPayment;
    
    MortgageSimulation(String trial, Money totalPrice, double mortgageRate, Money closingCosts, Money extraMonthlyPayment) {
      this.trial = trial;
      this.totalPrice = totalPrice;
      this.mortgageRate = mortgageRate;
      this.closingCosts = closingCosts;
      this.extraMonthlyPayment = extraMonthlyPayment;
    }
    
    @Override
    public String getTrialName() {
      return trial;
    }

    @Override
    public void run(LocalDate start, Simulation sim) {
      BalanceSheet sheet = BalanceSheet.create(sim);
      AssetAccount checking = sheet.createAssetAccount("Checking");
      checking.schedule(start.plusDays(15), context -> checking
          .deposit(Transaction.create(start.plusDays(15), "Transfer", Money.dollars(220000))));
      checking.onSchedule()
          .startingToday()
          .runEveryNWeeks(2)
          .schedule(context -> checking
              .deposit(Transaction.create(context.getDate(), "Salary", Money.dollars(7500))));
      
      // TODO: Create a much smarter payer - saves extra money, pays the most
      // important bills first..
      //Payer payer = new DefaultPayer(sim, checking);
      Payer extraPayer = new Payer(sim) {
        @Override
        public void onBill(Payee payee, Bill bill) {
          schedule(bill.getDueDate().minusDays(3), context -> {
            if (payee.getName().equals("Home")) {
              payPeriodAndExtraPayment(checking, bill, extraMonthlyPayment);
            } else {
              payPeriodPayment(checking, bill);
            }
          });
        }

        @Override
        public void onRegister(LocalDate date) {
        }
      };
      
      CreditCardAccount credit = sheet
          .createCreditCard(extraPayer, "Credit Card", Money.dollars(20000), 0.109).build();
      credit.onSchedule().startingToday().runDaily().schedule(context -> credit.purchase(
          Transaction.create(context.getDate(), "credit card purchase", Money.dollars(250))));
      
      Mortgage mortgage = sheet.createMortgage(extraPayer, "Home", totalPrice)
          .setClosingDate(LocalDate.now().plusDays(60))
          .setRate(mortgageRate)
          .setDownPayment(0.2, checking)  // 20% down
          .setClosingCosts(closingCosts)
          .setFixed(30)  // years
          .build();
      
      MoneyMetric maxChecking = MoneyMetric.max("Checking max balance");
      checking.onSchedule()
          .startingIn(100)
          .runDaily()
          .schedule(context -> context.updateMetric(maxChecking, checking.getPostedBalance()));

      //sheet.onSchedule().startingToday().runEveryNMonths(12).stopAfter(3)
      //    .schedule(date -> System.out.println("\n" + date + "\n" + sheet));
      //sheet.onSchedule().startingToday().runMonthly().stopAfter(12 * 30)
      //    .schedule(context -> System.out.format("%s: LTV: %f (asset: %s, loan: %s)\n",
      //        context.getDate(), mortgage.getLoanToValue(), mortgage.getAsset().getPostedBalance(),
      //        mortgage.getLoan().getPostedBalance()));
      sim.runUntil(start.plusYears(35));
    }  
  }
  
  public static void main(String[] args) {
    Experiment experiment = Experiment.create(
        new MortgageSimulation("Rate 3.625% (with closing costs)", Money.dollars(1_000_000),
            0.03625, Money.dollars(20_000), Money.zero()),
        new MortgageSimulation("Rate 3.625% (with no closing costs)", Money.dollars(1_020_000),
            0.03625, Money.zero(), Money.zero()),
        new MortgageSimulation("Rate 3.625% (with closing costs and extra payment)",
            Money.dollars(1_000_000), 0.03625, Money.dollars(20_000),
            Money.dollars(3731.37 - 3648.41)));
    experiment.run(LocalDate.now());
  }
}
