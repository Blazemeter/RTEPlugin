package com.blazemeter.jmeter.rte.recorder;

import com.blazemeter.jmeter.rte.JMeterTestUtils;
import com.blazemeter.jmeter.rte.core.Protocol;
import com.blazemeter.jmeter.rte.core.TerminalType;
import com.blazemeter.jmeter.rte.core.ssl.SSLType;
import org.assertj.core.api.JUnitSoftAssertions;
import org.assertj.swing.core.BasicComponentFinder;
import org.assertj.swing.core.ComponentFinder;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.fixture.JButtonFixture;
import org.junit.Rule;
import org.junit.Test;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.VerificationCollector;

import javax.swing.*;
import java.awt.*;

import static org.assertj.swing.fixture.Containers.showInFrame;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class RTERecorderPanelIT {

  @Rule
  public VerificationCollector collector = MockitoJUnit.collector();

  @Rule
  public final JUnitSoftAssertions softly = new JUnitSoftAssertions();

  @Mock
  public RecordingStateListener listener;

  private FrameFixture frame;
  private RTERecorderPanel panel;

  private final String PORT = "80";
  private final String SERVER = "server";
  private final String TIMEOUT = "5000";

  private final String PROTOCOL_TEXT = "TN5250";
  private final Protocol PROTOCOL = Protocol.TN5250;

  private final SSLType SSL_TYPE = SSLType.NONE;
  private final String TERMINAL_TYPE_TEXT = "IBM-3179-2: 24x80";
  private final TerminalType TERMINAL_TYPE = new TerminalType("IBM-3179-2", new Dimension(80, 24));
  private final String WAIT_TIMEOUT = "10000";

  private final String START_BUTTON_TEXT = "start";
  private final String STOP_BUTTON_TEXT = "stop";
  private final String RESTART_BUTTON_TEXT = "restart";

  @BeforeClass
  public static void setupClass() {
    JMeterTestUtils.setupJmeterEnv();
  }

  private RTERecorderGui rteRecorderGui;

  @Before
  public void setup() {

    RTERecorder configurationElement = buildRTERecorderForConfiguration();
    panel = new RTERecorderPanel(listener);
    rteRecorderGui = new RTERecorderGui(panel);
    rteRecorderGui.configure(configurationElement);

    frame = showInFrame(rteRecorderGui);
  }

  private RTERecorder buildRTERecorderForConfiguration(){
    RTERecorder configurationElement = new RTERecorder();
    configurationElement.setPort(PORT);
    configurationElement.setConnectionTimeout(TIMEOUT);
    configurationElement.setProtocol(PROTOCOL);
    configurationElement.setServer(SERVER);
    configurationElement.setSSLType(SSL_TYPE);
    configurationElement.setTerminalType(TERMINAL_TYPE);
    configurationElement.setTimeoutThresholdMillis(WAIT_TIMEOUT);

    return configurationElement;
  }

  @After
  public void tearDown() {
    frame.cleanUp();
  }

  @Test
  public void shouldNotifyStartRecordingListenerWhenStartRecording() throws Exception {
    JButtonFixture start = frame.button(START_BUTTON_TEXT);
    start.click();

    verify(listener).onRecordingStart();
  }

  @Test
  public void shouldNotifyStopRecordingListenerWhenStopRecording(){

    JButtonFixture start = frame.button(START_BUTTON_TEXT);
    JButtonFixture stop  = frame.button(STOP_BUTTON_TEXT);
    start.click();
    stop.click();

    verify(listener).onRecordingStop();
  }

  @Test
  public void shouldNotifyStopAndStartRecordingListenerWhenRestartRecording() throws Exception {
    JButtonFixture start = frame.button(START_BUTTON_TEXT);
    start.click();

    JButtonFixture restart = frame.button(RESTART_BUTTON_TEXT);
    restart.click();

    verify(listener).onRecordingStop();
    verify(listener, times(2)).onRecordingStart();
  }

  @Test
  public void shouldDisableStartButtonWhenStartRecording(){
    JButtonFixture start = frame.button(START_BUTTON_TEXT);
    start.click();

    assertEquals(false, start.isEnabled());
  }

  @Test
  public void shouldEnableStopButtonWhenStartRecording(){
    JButtonFixture start = frame.button(START_BUTTON_TEXT);
    start.click();

    JButtonFixture stop = frame.button(STOP_BUTTON_TEXT);

    assertEquals(true, stop.isEnabled());
  }

  @Test
  public void shouldEnableRestartButtonWhenStartRecording(){
    JButtonFixture start = frame.button(START_BUTTON_TEXT);
    start.click();

    JButtonFixture restart = frame.button(RESTART_BUTTON_TEXT);

    assertEquals(true, restart.isEnabled());
  }

  @Test
  public void shouldHaveEnabledStartButtonWhenInitPanel(){
    JButtonFixture start = frame.button(START_BUTTON_TEXT);

    assertEquals(true, start.isEnabled());
  }

  @Test
  public void shouldHaveDisabledStopButtonWhenInitPanel(){
    JButtonFixture stop = frame.button(STOP_BUTTON_TEXT);
    assertEquals(false, stop.isEnabled());
  }

  @Test
  public void shouldHaveDisabledRestartButtonWhenInitPanel(){
    JButtonFixture restart = frame.button(RESTART_BUTTON_TEXT);
    assertEquals(false, restart.isEnabled());
  }

  @Test
  public void shouldEnableStartButtonWhenStopRecording(){
    JButtonFixture start = frame.button(START_BUTTON_TEXT);
    JButtonFixture stop = frame.button(STOP_BUTTON_TEXT);
    start.click();
    stop.click();

    assertEquals(true, start.isEnabled());
  }

  @Test
  public void shouldDisableStopButtonWhenStopRecording(){
    JButtonFixture start = frame.button(START_BUTTON_TEXT);
    JButtonFixture stop = frame.button(STOP_BUTTON_TEXT);
    start.click();
    stop.click();

    assertEquals(false, stop.isEnabled());
  }

  @Test
  public void shouldDisableRestartButtonWhenStopRecording(){
    JButtonFixture start = frame.button(START_BUTTON_TEXT);
    JButtonFixture stop = frame.button(STOP_BUTTON_TEXT);
    start.click();
    stop.click();

    JButtonFixture restart = frame.button(RESTART_BUTTON_TEXT);

    assertEquals(false, restart.isEnabled());
  }

  @Test
  public void shouldGetConfiguredPropertiesWhenFieldsAreSet(){

  }

  @Test
  public void shouldGetConfiguredFieldsWhenPropertiesAreSet(){
    ComponentFinder finder = BasicComponentFinder.finderWithCurrentAwtHierarchy();

    /**
     * The true at the end of finder.findByName("name") was necessary since,
     * when running the whole set of tests, founded multiple instances
     * of the same element with all but one of then with visibility = true.
     * */
    /*Text Fields*/
    JTextField portField = ( JTextField ) finder.findByName("portField", true);
    JTextField serverField = ( JTextField ) finder.findByName("serverField", true);
    JTextField connectionTimeoutField = ( JTextField ) finder.findByName("connectionTimeout", true);
    JTextField waitConditionsTimeoutThresholdField = ( JTextField ) finder.findByName("waitConditionsTimeoutThreshold", true);

    /*ComboBoxes*/
    JComboBox protocolComboBox = (JComboBox) finder.findByName("protocolComboBox", true);
    JComboBox terminalTypeComboBox = (JComboBox) finder.findByName("terminalTypeComboBox", true);

    /*RadioButton*/
    JRadioButton sslTypeRadioButton = (JRadioButton) finder.findByName("NONE", true);

    softly.assertThat(portField.getText()).as("portField").isEqualTo(PORT);
    softly.assertThat(serverField.getText()).as("serverField").isEqualTo(SERVER);
    softly.assertThat(connectionTimeoutField.getText()).as("connectionTimeout").isEqualTo(TIMEOUT);
    softly.assertThat(waitConditionsTimeoutThresholdField.getText()).as("waitConditionsTimeoutThreshold").isEqualTo(WAIT_TIMEOUT);

    softly.assertThat(protocolComboBox.getSelectedItem().toString()).as("protocolComboBox").isEqualTo(PROTOCOL_TEXT);
    softly.assertThat(terminalTypeComboBox.getSelectedItem().toString()).as("terminalTypeComboBox").isEqualTo(TERMINAL_TYPE_TEXT);

    softly.assertThat(sslTypeRadioButton.isEnabled()).as("sslType_NONE").isEqualTo(true);
  }
}
