package com.blazemeter.jmeter.rte.sampler.gui;

import javax.swing.JComponent;
import javax.swing.JLabel;

public class SwingUtils {

  public static JLabel createLabel(String text, JComponent target) {
    JLabel label = new JLabel(text);
    label.setLabelFor(target);
    return label;
  }
}
