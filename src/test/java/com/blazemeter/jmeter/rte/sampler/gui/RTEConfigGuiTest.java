package com.blazemeter.jmeter.rte.sampler.gui;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.blazemeter.jmeter.rte.core.Protocol;
import com.blazemeter.jmeter.rte.core.TerminalType;
import com.blazemeter.jmeter.rte.core.ssl.SSLType;
import com.blazemeter.jmeter.rte.sampler.RTESampler;
import java.io.File;
import java.io.IOException;
import kg.apc.emulators.TestJMeterUtils;
import org.apache.commons.io.FileUtils;
import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.testelement.TestElement;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class RTEConfigGuiTest {

  private RTEConfigGui configGui;
  private TestElement testElement;

  @Mock
  private RTEConfigPanel panel;

  @Before
  public void setup() {
    configGui = new RTEConfigGui(panel);
    testElement = new ConfigTestElement();
  }

  @BeforeClass
  public static void setupClass() {
    TestJMeterUtils.createJmeterEnv();
  }

  @Test
  public void shouldSetTheTestElementFromThePanel() {
    when(panel.getServer()).thenReturn("Server");
    when(panel.getPort()).thenReturn("80");
    when(panel.getProtocol()).thenReturn(Protocol.TN5250);
    when(panel.getSSLType()).thenReturn(SSLType.NONE);
    when(panel.getTerminalType()).thenReturn(TerminalType.IBM_3179_2);
    when(panel.getConnectionTimeout()).thenReturn("10000");
    configGui.modifyTestElement(testElement);
    assertThat(testElement.getPropertyAsString(RTESampler.CONFIG_SERVER)).isEqualTo("Server");
    assertThat(testElement.getPropertyAsString(RTESampler.CONFIG_PORT)).isEqualTo("80");
    assertThat(testElement.getPropertyAsString(RTESampler.CONFIG_PROTOCOL))
        .isEqualTo(Protocol.TN5250.name());
    assertThat(testElement.getPropertyAsString(RTESampler.CONFIG_SSL_TYPE))
        .isEqualTo(SSLType.NONE.name());
    assertThat(testElement.getPropertyAsString(RTESampler.CONFIG_TERMINAL_TYPE))
        .isEqualTo(TerminalType.IBM_3179_2.name());
    assertThat(testElement.getPropertyAsString(RTESampler.CONFIG_CONNECTION_TIMEOUT))
        .isEqualTo("10000");
  }
}
