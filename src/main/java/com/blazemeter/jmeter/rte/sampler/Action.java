package com.blazemeter.jmeter.rte.sampler;

public enum Action {
  CONNECT("Connect"), SEND_INPUT("Send keys"), DISCONNECT("Disconnect");

  private final String label;

  Action(String label) {
    this.label = label;
  }

  public String getLabel() {
    return label;
  }

}
