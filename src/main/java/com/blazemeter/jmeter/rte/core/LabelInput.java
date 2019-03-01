package com.blazemeter.jmeter.rte.core;

public class LabelInput extends Input {

  private final String label;

  public LabelInput(String label, String input) {
    super(input);
    this.label = label;
  }

  public String getLabel() {
    return label;
  }

}
