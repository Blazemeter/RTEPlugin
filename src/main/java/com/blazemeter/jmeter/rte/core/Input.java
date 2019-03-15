package com.blazemeter.jmeter.rte.core;

public abstract class Input {

  protected final String input;

  public Input(String input) {
    this.input = input;
  }
  
  public String getInput() {
    return input;
  }

  public abstract String getCsv();
  
}
