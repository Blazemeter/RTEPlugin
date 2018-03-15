package com.blazemeter.jmeter.rte.core.ssl;

public enum SSLType {
  NONE("NONE"),
  SSLv2("SSLv2"),
  SSLv3("SSLv3"),
  TLS("TLS");

  private final String name;

  SSLType(String name) {
    this.name = name;
  }

  public String toString() {
    return name;
  }
}
