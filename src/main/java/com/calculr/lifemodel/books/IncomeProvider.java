package com.calculr.lifemodel.books;

import com.calculr.lifemodel.engine.Actor;
import com.calculr.lifemodel.engine.Simulation;

/**
 * An income provider deposits earned revenue into an asset account.
 */
public abstract class IncomeProvider extends Actor<IncomeProvider> {
    private final Journal journal;
    private final AssetAccount account;

    protected IncomeProvider(Simulation simulation, AssetAccount account) {
        super(simulation);
        journal = Journal.create();
        this.account = account;
    }

    protected void pay(Transaction transaction) {
        journal.deposit(transaction);
        account.deposit(transaction);
    }
}
