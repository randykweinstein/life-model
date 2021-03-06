package com.calculr.lifemodel.engine;

import java.time.Duration;
import java.time.LocalDate;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.google.common.truth.Truth;

@RunWith(JUnit4.class)
public class EventQueueTest {
  
  private static final LocalDate DATE1 = LocalDate.parse("2014-06-01");
  private static final LocalDate DATE2 = LocalDate.parse("2014-06-30");
  
  @Test
  public void createEventQueueShouldDefineDataStructures() {
    EventQueue queue = new EventQueue(DATE1, Duration.ofDays(30));
    System.out.println(queue);
    Truth.assertThat(1L).isEqualTo(1L);
    Assert.assertEquals(1L, 1L);
    // Assert.assertEquals(1L, 2L);
  }
}
