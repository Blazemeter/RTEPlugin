package com.blazemeter.jmeter.rte.sampler.gui;

import com.blazemeter.jmeter.rte.core.AttentionKey;
import com.blazemeter.jmeter.rte.core.Protocol;
import com.blazemeter.jmeter.rte.core.TerminalType;
import com.blazemeter.jmeter.rte.core.ssl.SSLType;
import com.blazemeter.jmeter.rte.recorder.RTERecorder;
import com.blazemeter.jmeter.rte.recorder.RTERecorderGui;
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

  private static final String LOCAL_SERVER = "localhost";
  private static final String SERVER = "localhost";
  private static final int PORT = 2323;
  private static final Protocol PROTOCOL = Protocol.TN3270;
  private static final SSLType SSL_TYPE = SSLType.NONE;
  private static final long CONNECTION_TIMEOUT = 30;

  private RTERecorderGui rteRecorderGui;
  private RTERecorder testElement;


  /**/
  @Rule
  /**/ public final JUnitSoftAssertions softly = new JUnitSoftAssertions();

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
    when(testElement.getServer()).thenReturn(LOCAL_SERVER);
    when(testElement.getPort()).thenReturn(PORT);
    when(testElement.getProtocol()).thenReturn(PROTOCOL);
    when(testElement.getTerminalType()).thenReturn(null);
    when(testElement.getSSLType()).thenReturn(SSL_TYPE);
    when(testElement.getConnectionTimeout()).thenReturn(CONNECTION_TIMEOUT);

    rteRecorderGui.configure(testElement);
  }

  @Test
  public void shouldSetTestElementFromTheRecordingPanelWhenModifyTestElement() {

    /*
    * I think I'm making a mistake since I aint using RecordingPanel but,
    * */



    RTERecorder testElement = Mockito.mock(RTERecorder.class);

    rteRecorderGui.modifyTestElement(testElement);

    testMe("getPort", Integer.toString(testElement.getPort()));



  }
}
