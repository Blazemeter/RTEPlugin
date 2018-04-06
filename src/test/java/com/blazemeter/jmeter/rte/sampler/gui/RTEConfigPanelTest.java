package com.blazemeter.jmeter.rte.sampler.gui;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.swing.fixture.Containers.showInFrame;

import com.blazemeter.jmeter.rte.core.Protocol;
import com.blazemeter.jmeter.rte.core.TerminalType;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComboBox;
import kg.apc.emulators.TestJMeterUtils;
import org.assertj.swing.fixture.FrameFixture;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class RTEConfigPanelTest {

  private FrameFixture frame;

  @BeforeClass
  public static void setUpOnce() {
    TestJMeterUtils.createJmeterEnv();
  }

  @Before
  public void setup() {
    RTEConfigPanel panel = new RTEConfigPanel();
    frame = showInFrame(panel);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void shouldChangeTheValuesOfTheTerminalComboBoxWhenChangeProtocolComboBox() {
    JComboBox protocolCombo = frame.robot().finder()
        .findByName("protocolComboBox", JComboBox.class);
    JComboBox<TerminalType> terminalCombo = (JComboBox<TerminalType>) frame.robot().finder()
        .findByName("terminalTypeComboBox", JComboBox.class);
    protocolCombo.setSelectedItem(Protocol.TN3270);
    assertThat(Protocol.TN3270.createProtocolClient().getSupportedTerminalTypes())
        .containsExactlyElementsOf(getComboValues(terminalCombo));
  }

  @After
  public void tearDown() {
    frame.cleanUp();
  }

  private <T> List<T> getComboValues(JComboBox<T> combo) {
    List<T> ret = new ArrayList<>();
    for (int i = 0; i < combo.getItemCount(); i++) {
      ret.add(combo.getItemAt(i));
    }
    return ret;
  }
}
