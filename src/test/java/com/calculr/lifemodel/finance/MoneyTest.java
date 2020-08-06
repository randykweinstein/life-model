package com.calculr.lifemodel.finance;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class MoneyTest {

  @Test
  public void signShouldNotMatter() {
    Money a = Money.dollarsCents(123, 45);
    Money b = Money.dollarsCents(-24, 23);
    Money c = Money.dollarsCents(0, 0);
    Money d = Money.dollarsCents(1000, 0);
    System.out.println(a);
    System.out.println(b);
    System.out.println(c);
    System.out.println(d);
    //Truth.assertThat(a);
  }
  
  @Test
  public void summationShouldBeForMoreDigits() {
    List<Money> amounts = new ArrayList<>();
    for (int i = 0; i < 10; i++) {
      amounts.add(Money.dollars(0.1));
    }
    System.out.format("Sum = %s\n", Money.sum(amounts));
  }
}
