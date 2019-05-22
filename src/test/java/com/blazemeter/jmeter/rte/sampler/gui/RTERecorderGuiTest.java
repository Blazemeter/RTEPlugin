package com.blazemeter.jmeter.rte.sampler.gui;

import com.blazemeter.jmeter.rte.core.Protocol;
import com.blazemeter.jmeter.rte.core.TerminalType;
import com.blazemeter.jmeter.rte.core.ssl.SSLType;
import com.blazemeter.jmeter.rte.recorder.RTERecorder;
import com.blazemeter.jmeter.rte.recorder.RTERecorderGui;
import com.blazemeter.jmeter.rte.recorder.RecordingStateListener;
import kg.apc.emulators.TestJMeterUtils;
import org.apache.jmeter.testelement.TestElement;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;


import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class RTERecorderGuiTest {

  private final String SERVER = "localhost";
  private final int PORT = 23;
  private final Protocol PROTOCOL = Protocol.TN5250;
  private final TerminalType TERMINAL_TYPE = PROTOCOL.createProtocolClient().getDefaultTerminalType();
  private final SSLType SSL_TYPE = SSLType.NONE;
  private final long TIMEOUT = 5000;

  public RTERecorderGui rteRecorderGui;
  public RTERecorder testElement;


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
    when(testElement.getPort()).thenReturn(PORT);
    when(testElement.getProtocol()).thenReturn(PROTOCOL);
    when(testElement.getTerminalType()).thenReturn(TERMINAL_TYPE);
    when(testElement.getSSLType()).thenReturn(SSL_TYPE);
    when(testElement.getConnectionTimeout()).thenReturn(TIMEOUT);

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

  private void testME(String name, String content){
    System.out.println("<"+name+">");
    System.out.println(content);
    System.out.println("</"+name+">");
  }

  @Test
  public void shouldConfigurePanelWithGivenTestElementWhenConfigure(){
    /*
    RTERecorder testElement = Mockito.mock(RTERecorder.class);
    when(testElement.getServer()).thenReturn(SERVER);
    when(testElement.getPort()).thenReturn(PORT);
    when(testElement.getProtocol()).thenReturn(PROTOCOL);
    when(testElement.getTerminalType()).thenReturn(TERMINAL_TYPE);
    when(testElement.getSSLType()).thenReturn(SSL_TYPE);
    when(testElement.getConnectionTimeout()).thenReturn(TIMEOUT);
    */
    RTERecorder configurationElement = buildDefaultRTERecorder();
    configurationElement.setServer(SERVER);
    configurationElement.setPort(String.valueOf(PORT));
    configurationElement.setProtocol(PROTOCOL);
    configurationElement.setTerminalType(TERMINAL_TYPE);
    configurationElement.setSSLType(SSL_TYPE);
    configurationElement.setConnectionTimeout(Long.toString(TIMEOUT));

    rteRecorderGui.configure(configurationElement);

    String expected = "RTERecorderGui { \n" +
            "recordingPanel { \n" +
            "server="+SERVER+", \n" +
            "port="+PORT+", \n" +
            "protocol="+PROTOCOL+", \n" +
            "terminalType="+TERMINAL_TYPE+", \n" +
            "sslType="+SSL_TYPE+", \n" +
            "connectionTimeout="+TIMEOUT+"}, \n" +
            "recorder=null } }";

    assertEquals(expected, rteRecorderGui.toString());
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
    recorder.setPort(String.valueOf(PORT));
    recorder.setProtocol(PROTOCOL);
    recorder.setTerminalType(TERMINAL_TYPE);
    recorder.setSSLType(SSL_TYPE);
    recorder.setConnectionTimeout(Long.toString(TIMEOUT));

    return recorder;
  }

  private void assertRecorder(RTERecorder result, RTERecorder expected) {
    assertThat(result)
            .isEqualToComparingOnlyGivenFields(expected, "port", "server", "protocol", "terminalType", "connectionTimeout");
  }

}
