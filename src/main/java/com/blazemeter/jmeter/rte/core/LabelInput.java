package com.blazemeter.jmeter.rte.core;

import java.util.Objects;

public class LabelInput extends Input {

  private String label;

  // Provided for proper deserialization of sample results
  public LabelInput() {
  }

  public LabelInput(String label, String input) {
    super(input);
    this.label = label;
  }

  public String getLabel() {
    return label;
  }
  
  @Override
  public String getCsv() {
    return label + "," + getInput();
  }
  
  @Override
  public String toString() {
    return "(" + label + "): " + getInput();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    LabelInput that = (LabelInput) o;
    return Objects.equals(label, that.label) && Objects.equals(input, that.input);
  }

  @Override
  public int hashCode() {
    return Objects.hash(label, input);
  }
  
}
