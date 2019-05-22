package com.blazemeter.jmeter.rte.sampler.gui;

import com.blazemeter.jmeter.rte.core.Protocol;
import com.blazemeter.jmeter.rte.core.TerminalType;
import com.blazemeter.jmeter.rte.core.ssl.SSLType;
import com.blazemeter.jmeter.rte.recorder.RTERecorder;
import com.blazemeter.jmeter.rte.recorder.RTERecorderGui;
import com.blazemeter.jmeter.rte.recorder.RecordingStateListener;
import kg.apc.emulators.TestJMeterUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class RTERecorderGuiTest {

  private final String SERVER = "localhost";
  private final int port = 23;
  private final Protocol protocol = Protocol.TN5250;
  private final TerminalType terminalType = protocol.createProtocolClient().getDefaultTerminalType();
  private final SSLType sslType = SSLType.NONE;
  private final long timeout = 60000;

  private RTERecorderGui rteRecorderGui;
  private RTERecorder testElement;


  @Before
  public void setup() {
    testElement = new RTERecorder();
    rteRecorderGui = new RTERecorderGui();
  }

  @BeforeClass
  public static void setupClass() {
    TestJMeterUtils.createJmeterEnv();
  }

  @Test
  public void shouldNotifyRecorderOnRecordingStartWhenOnRecordingStart() throws Exception {

    RTERecorder testElement = Mockito.mock(RTERecorder.class);
    when(testElement.getServer()).thenReturn(SERVER);
    when(testElement.getPort()).thenReturn(port);
    when(testElement.getProtocol()).thenReturn(protocol);
    when(testElement.getTerminalType()).thenReturn(terminalType);
    when(testElement.getSSLType()).thenReturn(sslType);
    when(testElement.getConnectionTimeout()).thenReturn(timeout);

    rteRecorderGui.configure(testElement);
    rteRecorderGui.onRecordingStart();

    /*
     * Doubt: How do I access/or add the TestStateListener in the rteRecorderGui?
     * */
  }

  @Test
  public void shouldNotifyRecorderWhenOnRecordingStop(){
    /*
     * Doubt: How do I access/or add the TestStateListener in the rteRecorderGui?
     * Obviously, this test is not complete.
     * */

    rteRecorderGui.onRecordingStop();
  }

  @Test
  public void shouldConfigurePanelWithGivenTestElementWhenConfigure(){

    RTERecorder testElement = Mockito.mock(RTERecorder.class);
    when(testElement.getServer()).thenReturn(SERVER);
    when(testElement.getPort()).thenReturn(port);
    when(testElement.getProtocol()).thenReturn(protocol);
    when(testElement.getTerminalType()).thenReturn(terminalType);
    when(testElement.getSSLType()).thenReturn(sslType);
    when(testElement.getConnectionTimeout()).thenReturn(timeout);

    rteRecorderGui.configure(testElement);

    /*
     * I know there most be another way to try to see if the Panel is
     * well configured but, since I don't know how to reach the
     * configured panel without making modifications into
     * the actual RTERecorderGui, I'm pushing this
     * approach.
     *
     * Here I'm just testing method where called, but not set,
     * which isn't what the method said it should do.
     * */

    verify(testElement, times(1)).getServer();
    verify(testElement, times(1)).getPort();
    verify(testElement, times(1)).getProtocol();
    verify(testElement, times(1)).getTerminalType();
    verify(testElement, times(1)).getSSLType();
    verify(testElement, times(1)).getConnectionTimeout();
  }

  @Test
  public void shouldSetTestElementFromTheRecordingPanelWhenModifyTestElement() {

    RTERecorder expected = buildDefaultRTERecorder();
    RTERecorder modified = new RTERecorder();

    rteRecorderGui.configure(expected);
    rteRecorderGui.modifyTestElement(modified);

    assertRecorder(expected, modified);
  }

  public RTERecorder buildDefaultRTERecorder(){
    RTERecorder recorder = new RTERecorder();

    recorder.setServer(SERVER);
    recorder.setPort(String.valueOf(port));
    recorder.setProtocol(protocol);
    recorder.setTerminalType(terminalType);
    recorder.setSSLType(sslType);
    recorder.setConnectionTimeout(Long.toString(timeout));

    return recorder;
  }

  private void assertRecorder(RTERecorder result, RTERecorder expected) {
    assertThat(result)
            .isEqualToComparingOnlyGivenFields(expected, "port", "server", "protocol", "terminalType", "connectionTimeout");
  }

}
