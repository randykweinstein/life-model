package com.calculr.lifemodel.books;

import java.time.LocalDate;

import com.calculr.lifemodel.engine.Simulation;
import com.calculr.lifemodel.finance.Bill;
import com.calculr.lifemodel.finance.Money;
import com.calculr.lifemodel.finance.MoneyMetric;
import com.calculr.lifemodel.finance.Payee;
import com.calculr.lifemodel.finance.Payer;

/**
 * A mortgage is a loan for a tangible asset, generally a home or other property.
 * 
 * <p>There are some basic assumptions that are going into this early model.
 * <ul>
 * <li>Interest rate is constant
 * <li>Payment is monthly
 * </ul>
 */
public class Mortgage implements Payee {
  private final Simulation sim;
  private final String name;
  private final AssetAccount asset;
  private final LiabilityAccount loan;
  private final VirtualAccount equity;
  private final Money payment;
  private final MoneyMetric totalPaymentMetric;
  
  private Mortgage(Simulation sim, String name, AssetAccount asset, LiabilityAccount loan,
      VirtualAccount equity, Money payment, MoneyMetric totalPayment) {
    this.sim = sim;
    this.name = name;
    this.asset = asset;
    this.loan = loan;
    this.equity = equity;
    this.payment = payment;
    this.totalPaymentMetric = totalPayment;
  }
  
  /**
   * Computes the amortization calculation for a loan assuming equal monthly
   * payments over the lifetime of the loan.
   * 
   * @param principal
   *          the amount of the mortgage
   * @param years
   *          the number of years the loan is in effect
   * @param rate
   *          the interest rate on the loan
   * @return the amount per period (month) that is owed
   */
  public static Money amortizationPayment(Money principal, int years, double rate) {
    int n = years * 12; // monthly payments
    double totalInterest = Math.pow(1 + rate/12, n);
    return principal.scale((rate/12) * totalInterest/(totalInterest - 1));
  }
  
  /**
   * Returns the {@link AssetAccount} holding the property that has been mortgaged.
   */
  public AssetAccount getAsset() {
    return asset;
  }
  
  /**
   * Returns the {@link LiabilityAccount} holding the loan for the mortgaged property.
   */
  public LiabilityAccount getLoan() {
    return loan;
  }
  
  /**
   * Returns a virtual {@link Account} combining both the asset and liability into one view.
   */
  public VirtualAccount getEquity() {
    return equity;
  }
  
  /**
   * Returns the L2V rate of the loan, calculated as the total of the
   * outstanding loan over the value of the asset.
   */
  public double getLoanToValue() {    
    return Money.ratio(loan.getPostedBalance().negate(), asset.getPostedBalance());
  }
  
  static MortgageBuilder newBuilder(Simulation sim, BalanceSheet sheet, Payer payer, String name,
      Money salePrice) {
    return new MortgageBuilder(sim, sheet, payer, name, salePrice);
  }

  /**
   * Builds a mortgage based on a set of parameters.
   */
  public static class MortgageBuilder {
    private final Simulation sim;
    private final BalanceSheet sheet;
    private final String name;
    private LocalDate closingDate;
    private Money salePrice;
    private Money downPayment = Money.zero();
    private AssetAccount downPaymentAccount;
    private Money closingCosts = Money.zero();
    private Payer payer;
    private int years = 0;
    private double rate = 0.04;
    private double originationRateDifference = 0;
    private Money originationFee = Money.zero();

    MortgageBuilder(Simulation sim, BalanceSheet sheet, Payer payer, String name, Money salePrice) {
      this.sim = sim;
      this.sheet = sheet;
      this.payer = payer;
      this.name =  name;
      this.salePrice = salePrice;
    }
    
    public MortgageBuilder setClosingDate(LocalDate date) {
      this.closingDate = date;
      return this;
    }
    
    public MortgageBuilder setRate(double rate) {
      this.rate = rate;
      return this;
    }
    
    public MortgageBuilder setDownPayment(Money downPayment, AssetAccount account) {
      this.downPayment = downPayment;
      this.downPaymentAccount = account;
      return this;
    }
    
    public MortgageBuilder setDownPayment(double fractionDown, AssetAccount account) {
      return setDownPayment(salePrice.scale(fractionDown), account);
    }

    public MortgageBuilder setFixed(int years) {
      this.years = years;
      return this;
    }
    
    public MortgageBuilder setClosingCosts(Money costs) {
      this.closingCosts = costs;
      return this;
    }
    
    public MortgageBuilder setOriginationRate(Money fee, double newRate) {
      this.originationFee = fee;
      this.originationRateDifference = newRate - rate;
      return this;
    }
    
    public Mortgage build() {
      AssetAccount asset = sheet.createAssetAccount(name + " Asset");
      Transaction mortgatgeAsset = Transaction.create(closingDate,
          "Tangible Asset <" + name + ">", salePrice);
      asset.schedule(closingDate, date -> asset.deposit(mortgatgeAsset));
      
      LiabilityAccount loan = sheet.createLiabilityAccount(name + " Loan");
      loan.schedule(closingDate, date -> loan.purchase(mortgatgeAsset));
      loan.actOn(new Interest(rate + originationRateDifference));
      
      VirtualAccount equity = sheet.createVirtualAccount(name + " Equity", asset, loan);
      MoneyMetric totalPayment = MoneyMetric.sum("Mortgage <" + name + "> total payment");
      
      if (downPayment.sign() > 0) {
        Transaction transaction = Transaction.create(closingDate,
            "Mortgage <" + name + "> Down Payment", downPayment);
        downPaymentAccount.schedule(closingDate,
            context -> {
              downPaymentAccount.makePayment(transaction, loan);
              context.updateMetric(totalPayment, downPayment);
            });
      }

      if (closingCosts.sign() > 0) {
        Transaction transaction = Transaction.create(closingDate,
            "Mortgage <" + name + "> Closing Costs", closingCosts);
        downPaymentAccount.schedule(closingDate,
            context -> {
              downPaymentAccount.withdraw(transaction);
              downPaymentAccount.makePayment(transaction, loan);
              context.updateMetric(totalPayment, closingCosts);
            });
      }
      if (originationFee.sign() > 0) {
        Transaction transaction = Transaction.create(closingDate,
            "Mortgage <" + name + "> Origination Fee", originationFee);
        downPaymentAccount.schedule(closingDate,
            context -> {
              downPaymentAccount.withdraw(transaction);
              downPaymentAccount.makePayment(transaction, loan);
              context.updateMetric(totalPayment, originationFee);
            });        
      }
      
      Money paymentAmount = amortizationPayment(salePrice.add(downPayment.negate()), years, rate + originationRateDifference); 
      MoneyMetric monthlyPayment = MoneyMetric.first("Mortgage <" + name + "> monthly payment");
      sim.update(monthlyPayment, paymentAmount);
      Mortgage mortgage = new Mortgage(sim, name, asset, loan, equity, paymentAmount, totalPayment);
      
      loan.onSchedule()
          .starting(closingDate.plusMonths(1))
          .runMonthly()
          .stopWhen(() -> loan.getBalance().isAtLeast(Money.zero()))
          .schedule(context -> Bill.issue(sim, mortgage, payer, 20 /* days */));
      return mortgage;
    }
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public Money getMinimumPayment() {
    if (getTotalLiability().isAtLeast(payment)) {
      return payment;
    }
    return getTotalLiability();
  }

  @Override
  public Money getPeriodPayment() {
    return getMinimumPayment();
  }

  @Override
  public Money getTotalLiability() {
    return getLoan().getBalance().negate();
  }

  @Override
  public void remit(AssetAccount account, Transaction transaction) {
    sim.update(totalPaymentMetric, transaction.getAmount());
    account.makePayment(transaction, getLoan());
  }
}
