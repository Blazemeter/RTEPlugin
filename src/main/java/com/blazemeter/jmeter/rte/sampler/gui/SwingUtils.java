package com.blazemeter.jmeter.rte.sampler.gui;

import com.blazemeter.jmeter.rte.recorder.emulator.ThemedIconButton;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import org.apache.jmeter.util.JMeterUtils;

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

  public static JButton createButton(String name, String text, String action,
      ActionListener listener) {
    JButton button = SwingUtils
        .createComponent(name, new JButton(JMeterUtils.getResString(text)));
    button.setActionCommand(action);
    button.addActionListener(listener);
    return button;
  }

  public static JButton createIconButton(String name, String iconResource) {
    return SwingUtils.createComponent(name, new ThemedIconButton(iconResource));
  }

  public static JLabel createLabelWithWarningStyle(String name, String text) {
    JLabel warningLabel = createComponent(name, new JLabel(text));
    warningLabel.setFont(warningLabel.getFont().deriveFont(Font.ITALIC, 11));
    return warningLabel;
  }
}
