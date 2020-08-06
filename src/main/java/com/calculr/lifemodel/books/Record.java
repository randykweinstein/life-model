package com.calculr.lifemodel.books;

import java.util.Objects;

/**
 * A object that can contain arbitrary data but is uniquified by an identifier.
 */
public abstract class Record {

  private static long maxId = 0;
  private final long id;
  
  protected Record() {
    id = createUniqueIdentifier();
  }
  
  /**
   * Returns a guaranteed unique identifier. 
   */
  private synchronized static final long createUniqueIdentifier() {
    maxId++;
    return maxId;
  }
  
  /**
   * Returns the unique identifier for the {@link Record}.
   */
  public final long getId() {
    return id;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(id);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Record other = (Record) obj;
    if (id != other.id)
      return false;
    return true;
  }
  
  @Override
  public String toString() {
    return getClass().getSimpleName() + "#" + id;
  }
}
