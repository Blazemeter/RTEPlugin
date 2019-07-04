package com.blazemeter.jmeter.rte.recorder;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.blazemeter.jmeter.rte.core.Protocol;
import com.blazemeter.jmeter.rte.core.TerminalType;
import com.blazemeter.jmeter.rte.core.ssl.SSLType;
import kg.apc.emulators.TestJMeterUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.VerificationCollector;

@RunWith(MockitoJUnitRunner.class)
public class RTERecorderGuiTest {

  @Rule
  public VerificationCollector collector = MockitoJUnit.collector();

  private static final String SERVER = "localhost";
  private static final int PORT = 23;
  private static final Protocol PROTOCOL = Protocol.TN5250;
  private static final TerminalType TERMINAL_TYPE = PROTOCOL.createProtocolClient()
      .getDefaultTerminalType();
  private static final SSLType SSL_TYPE = SSLType.NONE;
  private static final long CONNECTION_TIMEOUT_MILLIS = 10000;

  private RTERecorderGui rteRecorderGui;

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

  private void prepareTestElement() {
    when(testElement.getServer()).thenReturn(SERVER);
    when(testElement.getPort()).thenReturn(PORT);
    when(testElement.getProtocol()).thenReturn(PROTOCOL);
    when(testElement.getSSLType()).thenReturn(SSL_TYPE);
    when(testElement.getTerminalType()).thenReturn(TERMINAL_TYPE);
    when(testElement.getConnectionTimeout()).thenReturn(CONNECTION_TIMEOUT_MILLIS);
  }

  private void preparePanel() {
    when(panel.getServer()).thenReturn(SERVER);
    when(panel.getPort()).thenReturn(Integer.toString(PORT));
    when(panel.getProtocol()).thenReturn(PROTOCOL);
    when(panel.getSSLType()).thenReturn(SSL_TYPE);
    when(panel.getTerminalType()).thenReturn(TERMINAL_TYPE);
    when(panel.getConnectionTimeout()).thenReturn(Long.toString(CONNECTION_TIMEOUT_MILLIS));
  }

  @Test
  public void shouldNotifyRecorderOnRecordingStartWhenOnRecordingStart() {
    rteRecorderGui.configure(testElement);
    rteRecorderGui.modifyTestElement(testElement);
    rteRecorderGui.onRecordingStart();

    verify(testElement).onRecordingStart();
  }

  @Test
  public void shouldNotifyRecorderWhenOnRecordingStop() {
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
    verify(panel).setConnectionTimeout(Long.toString(CONNECTION_TIMEOUT_MILLIS));
  }

  @Test
  public void shouldSetTestElementFromTheRecordingPanelWhenModifyTestElement() {
    rteRecorderGui.modifyTestElement(testElement);

    verify(testElement).setServer(SERVER);
    verify(testElement).setPort(Long.toString(PORT));
    verify(testElement).setProtocol(PROTOCOL);
    verify(testElement).setTerminalType(TERMINAL_TYPE);
    verify(testElement).setSSLType(SSL_TYPE);
    verify(testElement).setConnectionTimeout(Long.toString(CONNECTION_TIMEOUT_MILLIS));
  }

}
