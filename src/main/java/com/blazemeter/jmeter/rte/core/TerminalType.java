package com.blazemeter.jmeter.rte.core;

import java.awt.Dimension;

public class TerminalType {

  private String id;
  private Dimension screenSize;

  // Provided for proper deserialization of sample results
  public TerminalType() {
  }

  public TerminalType(String id, Dimension screenSize) {
    this.id = id;
    this.screenSize = screenSize;
  }

  public String getId() {
    return id;
  }

  @Override
  public String toString() {
    return id + ": " + screenSize.height + "x" + screenSize.width;
  }

}
