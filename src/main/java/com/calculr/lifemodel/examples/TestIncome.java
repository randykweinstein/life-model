package com.calculr.lifemodel.examples;

import com.calculr.lifemodel.books.BalanceSheet;
import com.calculr.lifemodel.engine.Simulator;
import com.calculr.lifemodel.engine.parameter.EvolvingParameter;
import com.calculr.lifemodel.engine.parameter.Parameter;
import com.calculr.lifemodel.finance.Money;

import java.time.LocalDate;
import java.time.Month;

public class TestIncome {

    public static void main(String[] args) {
        Simulator simulator = Simulator.create(LocalDate.now());
        BalanceSheet sheet = BalanceSheet.create(simulator);

        Money initialSalary = Money.dollars(161_000);
        double raise = 0.04;
        Parameter<Money> annualIncome = new EvolvingParameter<>(simulator, "annual income",
            initialSalary) {
            @Override
            public void onRegister(LocalDate date) {
                onSchedule()
                    .starting(Month.JANUARY, 1)
                    .runAnnually()
                    .schedule(context -> update(get().scale(1 + raise)));
            }
        };

        System.out.format("Initial Salary: %s\n", annualIncome.get());
        simulator.runUntil(LocalDate.now().plusYears(10));
        System.out.format("Final Salary: %s\n", annualIncome.get());

    }
}
