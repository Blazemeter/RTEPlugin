package com.blazemeter.jmeter.rte.sampler.gui;

import com.blazemeter.jmeter.rte.core.AttentionKey;
import com.blazemeter.jmeter.rte.core.Protocol;
import com.blazemeter.jmeter.rte.core.TerminalType;
import com.blazemeter.jmeter.rte.core.ssl.SSLType;
import com.blazemeter.jmeter.rte.recorder.RTERecorder;
import com.blazemeter.jmeter.rte.recorder.RTERecorderGui;
import com.blazemeter.jmeter.rte.sampler.RTESampler;
import kg.apc.emulators.TestJMeterUtils;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RTERecorderGuiTest {

  private final String server = "Server";
  private final int port = 80;
  private final Protocol protocol = Protocol.TN5250;
  private final TerminalType terminalType = protocol.createProtocolClient().getDefaultTerminalType();
  private final SSLType sslType = SSLType.NONE;
  private final long timeout = 10000;

  private RTERecorderGui rteRecorderGui;
  private RTERecorder testElement;


  @Rule
  public final JUnitSoftAssertions softly = new JUnitSoftAssertions();

  @Before
  public void setup() {
    testElement = new RTERecorder();
    rteRecorderGui = new RTERecorderGui();
  }

  @BeforeClass
  public static void setupClass() {
    TestJMeterUtils.createJmeterEnv();
  }


  public void testMe(String name, String text) {
    System.out.println("<" + name + ">");
    System.out.println(text);
    System.out.println("</" + name + ">");
  }

  @Test
  public void shouldNotifyRecorderWhenOnRecordingStop(){
    rteRecorderGui.onRecordingStop();
  }

  @Test
  public void shouldNotifyRecorderOnRecordingStartWhenOnRecordingStart(){

  }

  @Test
  public void shouldConfigurePanelWithGivenTestElementWhenConfigure(){

    RTERecorder testElement = Mockito.mock(RTERecorder.class);
    when(testElement.getServer()).thenReturn(server);
    when(testElement.getPort()).thenReturn(port);
    when(testElement.getProtocol()).thenReturn(protocol);
    when(testElement.getTerminalType()).thenReturn(terminalType);
    when(testElement.getSSLType()).thenReturn(sslType);
    when(testElement.getConnectionTimeout()).thenReturn(timeout);

    rteRecorderGui.configure(testElement);
    
  }

  @Test
  public void shouldSetTestElementFromTheRecordingPanelWhenModifyTestElement() {


    RTERecorder testElement = Mockito.mock(RTERecorder.class);

    rteRecorderGui.modifyTestElement(testElement);

    testMe("getPort", Integer.toString(testElement.getPort()));



  }
}
