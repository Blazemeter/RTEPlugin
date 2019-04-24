package com.blazemeter.jmeter.rte.core;

public abstract class Input {

  protected String input;

  // Provided for proper deserialization of sample results
  public Input(){
  }

  public Input(String input) {
    this.input = input;
  }
  
  public String getInput() {
    return input;
  }

  public abstract String getCsv();
  
}
