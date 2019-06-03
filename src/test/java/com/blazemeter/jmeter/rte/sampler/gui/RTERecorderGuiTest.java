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

  @BeforeClass
  public static void setupClass() {
    TestJMeterUtils.createJmeterEnv();
  }

  @Before
  public void setup() {
    prepareTestElement();
    preparePanel();
    rteRecorderGui = new RTERecorderGui(panel);
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

  @Test
  public void shouldNotifyRecorderOnRecordingStartWhenOnRecordingStart() throws Exception {
    rteRecorderGui.configure(testElement);
    rteRecorderGui.modifyTestElement(testElement);
    rteRecorderGui.onRecordingStart();

    verify(testElement).onRecordingStart();
  }

  @Test
  public void shouldNotifyRecorderWhenOnRecordingStop() throws Exception {
    rteRecorderGui.configure(testElement);
    rteRecorderGui.modifyTestElement(testElement);
    rteRecorderGui.onRecordingStart();
    rteRecorderGui.onRecordingStop();

    verify(testElement).onRecordingStop();
  }

  @Test
  public void shouldConfigurePanelWithGivenTestElementWhenConfigure() {
    rteRecorderGui.configure(testElement);

    verify(panel).setServer(SERVER);
    verify(panel).setPort(Long.toString(PORT));
    verify(panel).setProtocol(PROTOCOL);
    verify(panel).setTerminalType(TERMINAL_TYPE);
    verify(panel).setSSLType(SSL_TYPE);
    verify(panel).setConnectionTimeout(Long.toString(TIMEOUT));
  }

  @Test
  public void shouldSetTestElementFromTheRecordingPanelWhenModifyTestElement() {
    RTERecorder modified = new RTERecorder();
    rteRecorderGui.modifyTestElement(modified);

    verify(testElement).setServer(SERVER);
    verify(testElement).setPort(Long.toString(PORT));
    verify(testElement).setProtocol(PROTOCOL);
    verify(testElement).setTerminalType(TERMINAL_TYPE);
    verify(testElement).setSSLType(SSL_TYPE);
    verify(testElement).setConnectionTimeout(Long.toString(TIMEOUT));
  }

}
