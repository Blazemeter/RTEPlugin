package com.blazemeter.jmeter.rte.sampler.gui;

import javax.swing.JComponent;
import javax.swing.JPanel;
import org.assertj.swing.core.GenericTypeMatcher;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.fixture.JComponentFixture;
import org.assertj.swing.fixture.JPanelFixture;

public final class AssertJUtils {

  private AssertJUtils() {
  }

  public static JPanelFixture findInvisiblePanelByName(FrameFixture frame, String name) {
    return frame.panel(new GenericTypeMatcher<JPanel>(JPanel.class) {
      @Override
      protected boolean isMatching(JPanel component) {
        return name.equals(component.getName());
      }
    });
  }

  public static JComponent findComponentByName(FrameFixture frame, String name) {
    return frame.robot().finder().find(new GenericTypeMatcher<JComponent>(JComponent.class) {
      @Override
      protected boolean isMatching(JComponent component) {
        return name.equals(component.getName());
      }
    });

  }
}
