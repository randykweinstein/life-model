package com.calculr.lifemodel.engine;

import static java.time.temporal.ChronoUnit.DAYS;

import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class EventQueue {
  private final LocalDate startDate;
  private final LocalDate endDate;
  private final List<Collection<Event>> events;
  private final State state;

  public EventQueue(LocalDate startDate, Duration duration) {
    this.startDate = startDate;
    this.endDate = startDate.plusDays(duration.toDays());
    int total = (int) duration.toDays() + 1;
    events = new ArrayList<>(total);
    for (int i = 0; i < total; i++) {
      events.add(new LinkedList<>());
    }
    state = new State(this, startDate);
  }

  void add(Event event, LocalDate date) {
    int days = (int) startDate.until(date, DAYS);
    events.get(days).add(event);
  }
}
