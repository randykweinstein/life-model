package com.calculr.lifemodel.books;

import com.calculr.lifemodel.engine.Repeating;
import com.calculr.lifemodel.engine.Simulation;
import com.calculr.lifemodel.engine.parameter.Parameter;
import com.calculr.lifemodel.finance.Money;

import java.time.LocalDate;

public class Salary extends IncomeProvider {

    protected Salary(Simulation simulation, AssetAccount account, Parameter<Money> annualSalary, Repeating repeating) {
        super(simulation, account);
    }

    @Override
    public void onRegister(LocalDate date) {

    }
}
