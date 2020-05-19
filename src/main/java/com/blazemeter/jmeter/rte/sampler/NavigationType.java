package com.blazemeter.jmeter.rte.sampler;

import java.util.Arrays;

public enum NavigationType {
  TAB("Tabulator"),
  UP("Up Arrow"),
  DOWN("Down Arrow"),
  RIGHT("Right Arrow"),
  LEFT("Left Arrow");

  private final String label;

  NavigationType(String label) {
    this.label = label;
  }

  public static NavigationType fromLabel(String label) {
    return Arrays.stream(NavigationType.values())
        .filter(t -> t.getLabel().equals(label))
        .findAny()
        .orElseThrow(() -> new IllegalArgumentException("Label \'" + label + "\' does not "
            + "match with any Navigation Type."));
  }

  public String getLabel() {
    return label;
  }
}
