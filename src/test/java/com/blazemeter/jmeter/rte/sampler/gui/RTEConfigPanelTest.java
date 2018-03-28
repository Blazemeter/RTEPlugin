package com.blazemeter.jmeter.rte.sampler.gui;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.swing.fixture.Containers.showInFrame;

import com.blazemeter.jmeter.rte.core.Protocol;
import com.blazemeter.jmeter.rte.core.TerminalType;
import java.util.ArrayList;
import java.util.Arrays;
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

  @Test
  public void shouldChangeTheValuesOfTheTerminalComboBoxWhenChangeProtocolComboBox() {
    JComboBox protocolCombo = frame.robot().finder().findByLabel("Protocol: ", JComboBox.class);
    JComboBox terminalCombo = frame.robot().finder().findByLabel("Terminal Type:", JComboBox.class);
    assertThat(Arrays.asList(TerminalType.findByProtocol(Protocol.TN5250))).containsExactlyElementsOf(getComboValues(terminalCombo));
    protocolCombo.setSelectedItem(Protocol.TN3270);
    assertThat(Arrays.asList(TerminalType.findByProtocol(Protocol.TN3270))).containsExactlyElementsOf(getComboValues(terminalCombo));
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
