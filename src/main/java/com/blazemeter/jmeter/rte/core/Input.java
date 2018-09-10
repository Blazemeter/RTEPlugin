package com.blazemeter.jmeter.rte.core;

public abstract class Input {

  private final String input;

  public Input(String in) {
    input = in;
  }

  public String getInput() {
    return input;
  }

}
