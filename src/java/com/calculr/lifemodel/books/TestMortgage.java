package com.calculr.lifemodel.books;

import java.time.Duration;
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
    private final double fractionDown;
    private final double mortgageRate;
    private final Money closingCosts;
    private final Money extraMonthlyPayment;
    private final int years;
    private final Money originationFee;
    private final double originationRateDifference;
    
    private MortgageSimulation(MortgageSimulationBuilder builder) {
      this.trial = builder.trial;
      this.totalPrice = builder.totalPrice;
      this.fractionDown = builder.fractionDown;
      this.mortgageRate = builder.mortgageRate;
      this.closingCosts = builder.closingCosts;
      this.extraMonthlyPayment = builder.extraPayment;
      this.years = builder.years;
      this.originationFee = builder.originationFee;
      this.originationRateDifference = builder.originationRateDifference;
    }
    
    static MortgageSimulationBuilder newBuilder(String trial, Money totalPrice, double mortgageRate,
        int years) {
      return new MortgageSimulationBuilder(trial, totalPrice, mortgageRate, years);
    }
    
    static class MortgageSimulationBuilder {
      private final String trial;
      private final Money totalPrice;
      private final double mortgageRate;
      private final int years;
      private double fractionDown = 0.20;
      private Money closingCosts = Money.zero();
      private Money extraPayment = Money.zero();
      private Money originationFee = Money.zero();
      private double originationRateDifference = 0; 
      
      MortgageSimulationBuilder(String trial, Money totalPrice, double mortgageRate, int years) {
        this.trial = trial;
        this.totalPrice = totalPrice;
        this.mortgageRate = mortgageRate;
        this.years = years;
      }
      
      public MortgageSimulationBuilder setDownPaymentFraction(double fraction) {
        this.fractionDown = fraction;
        return this;
      }
      
      public MortgageSimulationBuilder setClosingCosts(Money costs) {
        this.closingCosts = costs;
        return this;
      }
      
      public MortgageSimulationBuilder setExtraPayment(Money payment) {
        this.extraPayment = payment;
        return this;
      }
      
      public MortgageSimulationBuilder setOrigination(Money fee, double newRate) {
        this.originationFee = fee;
        this.originationRateDifference = newRate - mortgageRate;
        return this;
      }
      
      public MortgageSimulation build() {
        return new MortgageSimulation(this);
      }
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
          .deposit(Transaction.create(start.plusDays(15), "Transfer", Money.dollars(250000))));
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
      
      LocalDate closingDate = LocalDate.of(2016, 11, 16);
      Mortgage mortgage = sheet.createMortgage(extraPayer, "Home", totalPrice)
          .setClosingDate(closingDate)
          .setRate(mortgageRate)
          .setDownPayment(fractionDown, checking)  // 20% down
          .setClosingCosts(closingCosts)
          .setOriginationRate(originationFee, mortgageRate + originationRateDifference)
          .setFixed(years)  // years
          .build();      
      
      AssetAccount dummy = sheet.createDummyAccount();
      Payer dummyPayer = new BlindPayer(sim, dummy);
      Mortgage comparisonMortgage = sheet.createMortgage(dummyPayer, "ComparisonHome", totalPrice)
          .setClosingDate(closingDate)
          .setRate(mortgageRate)
          .setDownPayment(fractionDown, dummy)  // 20% down
          .setClosingCosts(closingCosts)
          .setFixed(years)  // years
          .build();
      mortgage.getAsset().onSchedule().starting(closingDate).runEveryNMonths(60).schedule(context -> {
         Money payment = context.<Money>getMetric("Mortgage <Home> total payment").getValue();
         Money comparison = context.<Money>getMetric("Mortgage <ComparisonHome> total payment").getValue();
            System.out.format(
                "After %s years, total payment %s vs %s (no origination fee) with a difference of %s\n",
                between(closingDate, context.getDate()).getSeconds() / (3600 * 24 * 365), payment,
                comparison, payment.add(comparison.negate()));
          });
      MoneyMetric maxChecking = MoneyMetric.max("Checking max balance");
      MoneyMetric downPayment = MoneyMetric.max("Mortgage <Home> downpayment");
      sim.update(downPayment, totalPrice.scale(fractionDown));
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
  
  private static Duration between(LocalDate date1, LocalDate date2) {
    return Duration.between(date1.atTime(12, 0), date2.atTime(12, 0));
  }
  
  public static void main(String[] args) {
    Experiment experiment = Experiment.create(
        MortgageSimulation.newBuilder("Rate 3.625%, no points", Money.dollars(1_010_000), 0.03625, 30)
            .setClosingCosts(Money.dollars(20_000))
            .build(),
        MortgageSimulation.newBuilder("Rate 3.625%, 0.125 points @ 0.5% fee", Money.dollars(1_010_000), 0.03625, 30)
            .setClosingCosts(Money.dollars(20_000))
            .setOrigination(Money.dollars(4040), 0.035)            
            .build(),          
        MortgageSimulation.newBuilder("Rate 3.625%, 0.25 points @ 1% fee", Money.dollars(1_010_000), 0.03625, 30)
            .setClosingCosts(Money.dollars(20_000))
            .setOrigination(Money.dollars(8080), 0.03375)            
            .build()          
        );
    experiment.run(LocalDate.of(2016, 10, 1));
  }
}
