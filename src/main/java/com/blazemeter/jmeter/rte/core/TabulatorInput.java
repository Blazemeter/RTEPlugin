package com.blazemeter.jmeter.rte.core;

import java.util.Objects;

public class TabulatorInput extends Input {

  //offset is the number of tabs that will be sent 
  private final int offset;

  public TabulatorInput(int offset, String value) {
    super(value);
    this.offset = offset;
  }

  @Override
  public String getCsv() {
    return "<TAB*" + offset + ">\t" + getInput();
  }

  @Override
  public String toString() {
    return offset + " tab/s:" + getInput();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TabulatorInput that = (TabulatorInput) o;
    return Objects.equals(offset, that.offset) &&
        Objects.equals(getInput(), that.getInput());
  }

  public int getOffset() {
    return offset;
  }

  @Override
  public int hashCode() {
    return Objects.hash(offset, getInput());
  }
}
