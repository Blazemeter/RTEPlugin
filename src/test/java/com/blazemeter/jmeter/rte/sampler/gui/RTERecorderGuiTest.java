package com.blazemeter.jmeter.rte.sampler.gui;

import com.blazemeter.jmeter.rte.core.Protocol;
import com.blazemeter.jmeter.rte.core.TerminalType;
import com.blazemeter.jmeter.rte.core.ssl.SSLType;
import com.blazemeter.jmeter.rte.recorder.RTERecorder;
import com.blazemeter.jmeter.rte.recorder.RTERecorderGui;
import com.blazemeter.jmeter.rte.recorder.RTERecorderPanel;

import com.blazemeter.jmeter.rte.recorder.RecordingStateListener;
import kg.apc.emulators.TestJMeterUtils;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.VerificationCollector;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class RTERecorderGuiTest {

  @Rule
  public VerificationCollector collector = MockitoJUnit.collector();

  @Rule
  public final JUnitSoftAssertions softly = new JUnitSoftAssertions();

  private final String SERVER = "localhost";
  private final int PORT = 23;
  private final Protocol PROTOCOL = Protocol.TN5250;
  private final TerminalType TERMINAL_TYPE = PROTOCOL.createProtocolClient().getDefaultTerminalType();
  private final SSLType SSL_TYPE = SSLType.NONE;
  private final long TIMEOUT = 5000;

  public RTERecorderGui rteRecorderGui;

  @Mock
  private RTERecorder testElement;

  @Mock
  private RTERecorderPanel panel;

  @Mock
  private RecordingStateListener listener;

  @Before
  public void setup() {
    prepareTestElement();
    preparePanel();
    
    rteRecorderGui = new RTERecorderGui();
  }

  private void prepareTestElement(){
    when(testElement.getServer()).thenReturn(SERVER);
    when(testElement.getPort()).thenReturn(PORT);
    when(testElement.getProtocol()).thenReturn(PROTOCOL);
    when(testElement.getSSLType()).thenReturn(SSL_TYPE);
    when(testElement.getTerminalType()).thenReturn(TERMINAL_TYPE);
    when(testElement.getConnectionTimeout()).thenReturn(TIMEOUT);
  }

  private void preparePanel(){
    when(panel.getServer()).thenReturn(SERVER);
    when(panel.getPort()).thenReturn(Integer.toString(PORT));
    when(panel.getProtocol()).thenReturn(PROTOCOL);
    when(panel.getSSLType()).thenReturn(SSL_TYPE);
    when(panel.getTerminalType()).thenReturn(TERMINAL_TYPE);
    when(panel.getConnectionTimeout()).thenReturn(Long.toString(TIMEOUT));
  }

  @BeforeClass
  public static void setupClass() {
    TestJMeterUtils.createJmeterEnv();
  }

  @Test
  public void shouldNotifyRecorderOnRecordingStartWhenOnRecordingStart() throws Exception {
    rteRecorderGui.configure(testElement);
    rteRecorderGui.modifyTestElement(testElement);
    rteRecorderGui.onRecordingStart();

    verify(testElement).onRecordingStart();
  }

  @Test
  public void shouldNotifyRecorderWhenOnRecordingStop() throws Exception {
    prepareTestElement();

    rteRecorderGui.configure(testElement);
    rteRecorderGui.modifyTestElement(testElement);
    rteRecorderGui.onRecordingStart();
    rteRecorderGui.onRecordingStop();

    verify(testElement).onRecordingStop();
  }

  @Test
  public void shouldConfigurePanelWithGivenTestElementWhenConfigure() {
    RTERecorderPanel configuredPanel = new RTERecorderPanel(listener);
    rteRecorderGui = new RTERecorderGui(configuredPanel);

    rteRecorderGui.configure(testElement);

    softly.assertThat(configuredPanel.getServer()).as("server").isEqualTo(SERVER);
    softly.assertThat(configuredPanel.getPort()).as("port").isEqualTo(Integer.toString(PORT));
    softly.assertThat(configuredPanel.getProtocol()).as("protocol").isEqualTo(PROTOCOL);
    softly.assertThat(configuredPanel.getTerminalType()).as("terminalType").isEqualTo(TERMINAL_TYPE);
    softly.assertThat(configuredPanel.getSSLType()).as("sslType").isEqualTo(SSL_TYPE);
    softly.assertThat(configuredPanel.getConnectionTimeout()).as("timeout").isEqualTo(Long.toString(TIMEOUT));
  }

  @Test
  public void shouldSetTestElementFromTheRecordingPanelWhenModifyTestElement() {
    rteRecorderGui = new RTERecorderGui(panel);

    RTERecorder modified = new RTERecorder();
    rteRecorderGui.modifyTestElement(modified);

    softly.assertThat(modified.getServer()).as("server").isEqualTo(SERVER);
    softly.assertThat(modified.getPort()).as("port").isEqualTo(PORT);
    softly.assertThat(modified.getProtocol()).as("protocol").isEqualTo(PROTOCOL);
    softly.assertThat(modified.getTerminalType()).as("terminalType").isEqualTo(TERMINAL_TYPE);
    softly.assertThat(modified.getSSLType()).as("sslType").isEqualTo(SSL_TYPE);
    softly.assertThat(modified.getConnectionTimeout()).as("timeout").isEqualTo(TIMEOUT);
  }

}
