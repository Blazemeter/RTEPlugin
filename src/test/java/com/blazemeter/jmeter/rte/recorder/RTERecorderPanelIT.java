package com.blazemeter.jmeter.rte.recorder;

import com.blazemeter.jmeter.rte.JMeterTestUtils;
import com.blazemeter.jmeter.rte.core.Protocol;
import com.blazemeter.jmeter.rte.core.TerminalType;
import com.blazemeter.jmeter.rte.core.ssl.SSLType;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.VerificationCollector;

import javax.swing.*;
import java.awt.event.ActionEvent;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RTERecorderPanelIT {

  @Rule
  public VerificationCollector collector = MockitoJUnit.collector();

  @Rule
  public final JUnitSoftAssertions softly = new JUnitSoftAssertions();

  private static final String ADD_ACTION_START = "addActionStart";
  private static final String ADD_ACTION_STOP = "addActionStop";
  private static final String ADD_ACTION_RESTART = "addActionRestart";

  private final String SERVER = "localhost";
  private final int PORT = 23;
  private final Protocol PROTOCOL = Protocol.TN5250;
  private final TerminalType TERMINAL_TYPE = PROTOCOL.createProtocolClient().getDefaultTerminalType();
  private final SSLType SSL_TYPE = SSLType.NONE;
  private final long TIMEOUT = 5000;

  private RTERecorder testElement;
  private RTERecorderPanel panel;
  private RecordingStateListener listener;
  private RTERecorderGui rteRecorderGui;

  @BeforeClass
  public static void setupClass() {
    JMeterTestUtils.setupJmeterEnv();
  }

  @Before
  public void setup() {
    preparePanel();
    rteRecorderGui = new RTERecorderGui(panel);
  }

  @Test
  public void shouldNotifyStartRecordingListenerWhenStartRecording() throws Exception {

    prepareTestElement();

    rteRecorderGui.configure(testElement);
    rteRecorderGui.modifyTestElement(testElement);
    rteRecorderGui.onRecordingStart();

    //ActionEvent addActionStart = new ActionEvent("", 1, ADD_ACTION_START);
    //panel.actionPerformed(addActionStart);

    verify(listener).onRecordingStart();
  }

  private void prepareTestElement(){
    testElement = new RTERecorder();
    testElement.setServer(SERVER);
    testElement.setPort(Integer.toString(PORT));
    testElement.setProtocol(PROTOCOL);
    testElement.setTerminalType(TERMINAL_TYPE);
    testElement.setSSLType(SSL_TYPE);
    testElement.setConnectionTimeout(Long.toString(TIMEOUT));
  }

  private void preparePanel(){
    listener = Mockito.mock(RecordingStateListener.class);
    panel = new RTERecorderPanel(listener);
/*
    when(panel.getServer()).thenReturn(SERVER);
    when(panel.getPort()).thenReturn(Integer.toString(PORT));
    when(panel.getProtocol()).thenReturn(PROTOCOL);
    when(panel.getTerminalType()).thenReturn(TERMINAL_TYPE);
    when(panel.getSSLType()).thenReturn(SSL_TYPE);
    when(panel.getConnectionTimeout()).thenReturn(Long.toString(TIMEOUT));
 */
  }



  @Test
  public void shouldNotifyStopRecordingListenerWhenStopRecording(){
    ActionEvent addActionStart = new ActionEvent("", 1, ADD_ACTION_STOP);
    panel.actionPerformed(addActionStart);

    verify(listener).onRecordingStop();
  }

  @Test
  public void shouldNotifyStopAndStartRecordingListenerWhenRestartRecording() throws Exception {
    ActionEvent addActionStart = new ActionEvent("", 1, ADD_ACTION_RESTART);
    panel.actionPerformed(addActionStart);

    verify(listener).onRecordingStop();
    verify(listener).onRecordingStart();
  }

  @Test
  public void shouldDisableStartButtonWhenStartRecording(){
    ActionEvent addActionStart = new ActionEvent("", 1, ADD_ACTION_START);
    panel.actionPerformed(addActionStart);

    JButton startButton = panel.getStartButton();
    assertEquals(false, startButton.isEnabled());
  }

  @Test
  public void shouldEnableStopButtonWhenStartRecording(){
    ActionEvent addActionStart = new ActionEvent("", 1, ADD_ACTION_START);
    panel.actionPerformed(addActionStart);

    JButton stopButton = panel.getStopButton();
    assertEquals(true, stopButton.isEnabled());
  }

  @Test
  public void shouldEnableRestartButtonWhenStartRecording(){
    ActionEvent addActionStart = new ActionEvent("", 1, ADD_ACTION_START);
    panel.actionPerformed(addActionStart);

    JButton restartButton = panel.getRestartButton();
    assertEquals(true, restartButton.isEnabled());
  }

  @Test
  public void shouldHaveEnabledStartButtonWhenInitPanel(){
    JButton startButton = panel.getStartButton();
    assertEquals(true, startButton.isEnabled());
  }

  @Test
  public void shouldHaveDisabledStopButtonWhenInitPanel(){
    JButton stopButton = panel.getStopButton();
    assertEquals(false, stopButton.isEnabled());
  }

  @Test
  public void shouldHaveDisabledRestartButtonWhenInitPanel(){
    JButton restartButton = panel.getRestartButton();
    assertEquals(false, restartButton.isEnabled());
  }

  @Test
  public void shouldEnableStartButtonWhenStopRecording(){
    ActionEvent addActionStop = new ActionEvent("", 1, ADD_ACTION_STOP);
    panel.actionPerformed(addActionStop);

    JButton startButton = panel.getStartButton();
    assertEquals(true, startButton.isEnabled());
  }

  @Test
  public void shouldDisableStopButtonWhenStopRecording(){
    ActionEvent addActionStop = new ActionEvent("", 1, ADD_ACTION_STOP);
    panel.actionPerformed(addActionStop);

    JButton stopButton = panel.getStopButton();
    assertEquals(false, stopButton.isEnabled());
  }

  @Test
  public void shouldDisableRestartButtonWhenStopRecording(){
    ActionEvent addActionStop = new ActionEvent("", 1, ADD_ACTION_STOP);
    panel.actionPerformed(addActionStop);

    JButton restartButton = panel.getStopButton();
    assertEquals(false, restartButton.isEnabled());
  }

  @Test
  public void shouldGetConfiguredPropertiesWhenFieldsAreSet(){}

  @Test
  public void shouldGetConfiguredFieldsWhenPropertiesAreSet(){}
}
