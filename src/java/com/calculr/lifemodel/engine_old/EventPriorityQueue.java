package com.calculr.lifemodel.engine_old;

import java.time.LocalDate;
import java.util.Optional;
import java.util.PriorityQueue;

class EventPriorityQueue<T extends State<T>> {

  private final PriorityQueue<ScheduledEvent<T>> queue;
 
  EventPriorityQueue() {
    this.queue = new PriorityQueue<>();
  }
  
  void add(ScheduledEvent<T> e) {
    queue.add(e);
  }
  
  void add(Event<T> event, LocalDate date) {
    add(new ScheduledEvent<>(event, date));
  }
  
  boolean isEmpty() {
    return queue.isEmpty();
  }
  
  Optional<LocalDate> getNextDate() {
    ScheduledEvent<T> event = queue.peek();
    if (event == null) {
      return Optional.empty();
    }
    return Optional.of(event.getDate());
  }
  
  ScheduledEvent<T> getNext() {
    // Throws an exception if an element doesn't exist.
    return queue.remove();
  }
}
