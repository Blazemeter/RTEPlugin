package com.blazemeter.jmeter.rte.recorder.emulator;

import com.blazemeter.jmeter.rte.sampler.gui.ThemedIcon;
import java.awt.Graphics;
import javax.swing.JButton;

public class ThemedIconButton extends JButton {

  private String iconResourceName;

  public ThemedIconButton(String iconResourceName) {
    super(ThemedIcon.fromResourceName(iconResourceName));
    this.iconResourceName = iconResourceName;
  }

  @Override
  public void paint(Graphics g) {
    setIcon(ThemedIcon.fromResourceName(iconResourceName));
    super.paint(g);
  }

}
