package com.blazemeter.jmeter.rte.core;

public abstract class Input {

  private final String input;

  public Input(String input) {
    this.input = input;
  }

  public String getInput() {
    return input;
  }

}
