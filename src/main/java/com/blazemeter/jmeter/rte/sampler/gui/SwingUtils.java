package com.blazemeter.jmeter.rte.sampler.gui;

import java.awt.Component;
import java.awt.Container;
import javax.swing.JComponent;

public class SwingUtils {

  public static <T extends JComponent> T createComponent(String name, T component) {
    component.setName(name);
    return component;
  }

  public static void setEnabledRecursively(Component component, boolean enabled) {
    component.setEnabled(enabled);
    if (component instanceof Container) {
      for (Component child : ((Container) component).getComponents()) {
        setEnabledRecursively(child, enabled);
      }
    }
  }
}
