package com.blazemeter.jmeter.rte.recorder;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import com.blazemeter.jmeter.rte.JMeterTestUtils;
import com.blazemeter.jmeter.rte.core.Protocol;
import com.blazemeter.jmeter.rte.core.TerminalType;
import com.blazemeter.jmeter.rte.core.ssl.SSLType;

import org.assertj.core.api.JUnitSoftAssertions;
import org.assertj.swing.fixture.*;
import org.assertj.swing.timing.Condition;
import org.junit.Rule;
import org.junit.After;
import org.junit.BeforeClass;
import org.mockito.Mock;

import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.VerificationCollector;

import java.awt.*;

import static org.assertj.swing.fixture.Containers.showInFrame;
import static org.assertj.swing.timing.Pause.pause;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class RTERecorderPanelIT {

  @Rule
  public VerificationCollector collector = MockitoJUnit.collector();

  @Rule
  public final JUnitSoftAssertions softly = new JUnitSoftAssertions();

  @Mock
  public RecordingStateListener listener;

  @Mock
  RTERecorder configurationElement;

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

  private final long VERIFY_TIMEOUT = 100l;

  @BeforeClass
  public static void setupClass() {
    JMeterTestUtils.setupJmeterEnv();
  }

  @Mock
  private RTERecorderGui rteRecorderGui;

  @Before
  public void setup() {
    buildRTERecorderForConfiguration();
    panel = new RTERecorderPanel(listener);
    rteRecorderGui = new RTERecorderGui(panel);
    rteRecorderGui.configure(configurationElement);

    frame = showInFrame(rteRecorderGui);
  }

  private void buildRTERecorderForConfiguration(){
    when(configurationElement.getPort()).thenReturn(Integer.parseInt(PORT));
    when(configurationElement.getConnectionTimeout()).thenReturn(Long.parseLong(TIMEOUT));
    when(configurationElement.getProtocol()).thenReturn(PROTOCOL);
    when(configurationElement.getServer()).thenReturn(SERVER);
    when(configurationElement.getSSLType()).thenReturn(SSL_TYPE);
    when(configurationElement.getTerminalType()).thenReturn(TERMINAL_TYPE);
    when(configurationElement.getTimeoutThresholdMillis()).thenReturn(Long.parseLong(WAIT_TIMEOUT));
  }

  @After
  public void tearDown() {
    frame.cleanUp();
  }

  @Test
  public void shouldNotifyStartRecordingListenerWhenStartRecording() throws Exception {
    JButtonFixture start = frame.button(START_BUTTON_TEXT);
    start.click();

    verify(listener, timeout(VERIFY_TIMEOUT)).onRecordingStart();
  }

  @Test
  public void shouldNotifyStopRecordingListenerWhenStopRecording(){

    JButtonFixture start = frame.button(START_BUTTON_TEXT);
    JButtonFixture stop  = frame.button(STOP_BUTTON_TEXT);
    start.click();
    stop.click();

    verify(listener, timeout(VERIFY_TIMEOUT)).onRecordingStop();
  }

  @Test
  public void shouldNotifyStopAndStartRecordingListenerWhenRestartRecording() throws Exception {
    JButtonFixture start = frame.button(START_BUTTON_TEXT);
    start.click();

    JButtonFixture restart = frame.button(RESTART_BUTTON_TEXT);
    restart.click();

    verify(listener, timeout(VERIFY_TIMEOUT)).onRecordingStop();
    verify(listener, timeout(VERIFY_TIMEOUT).times(2)).onRecordingStart();
  }

  @Test
  public void shouldDisableStartButtonWhenStartRecording(){
    JButtonFixture start = frame.button(START_BUTTON_TEXT);
    start.click();

    waitButtonEnabled(START_BUTTON_TEXT, false);
  }

  private void waitButtonEnabled(String buttonName, boolean enable) {
    pause(new Condition("button " + buttonName + " to be " + (enable ? "enabled" : "disabled")) {
      @Override
      public boolean test() {
        return frame.button(buttonName).isEnabled() == enable;
      }
    });
  }

  @Test
  public void shouldEnableStopButtonWhenStartRecording(){
    JButtonFixture start = frame.button(START_BUTTON_TEXT);
    start.click();

    waitButtonEnabled(STOP_BUTTON_TEXT, true);
  }

  @Test
  public void shouldEnableRestartButtonWhenStartRecording(){
    JButtonFixture start = frame.button(START_BUTTON_TEXT);
    start.click();

    waitButtonEnabled(RESTART_BUTTON_TEXT, true);
  }

  @Test
  public void shouldHaveEnabledStartButtonWhenInitPanel(){
    waitButtonEnabled(START_BUTTON_TEXT, true);
  }

  @Test
  public void shouldHaveDisabledStopButtonWhenInitPanel(){
    waitButtonEnabled(STOP_BUTTON_TEXT, false);
  }

  @Test
  public void shouldHaveDisabledRestartButtonWhenInitPanel(){
    waitButtonEnabled(RESTART_BUTTON_TEXT, false);
  }

  @Test
  public void shouldEnableStartButtonWhenStopRecording(){
    JButtonFixture start = frame.button(START_BUTTON_TEXT);
    JButtonFixture stop = frame.button(STOP_BUTTON_TEXT);
    start.click();
    stop.click();

    waitButtonEnabled(START_BUTTON_TEXT, true);
  }

  @Test
  public void shouldDisableStopButtonWhenStopRecording(){
    JButtonFixture start = frame.button(START_BUTTON_TEXT);
    JButtonFixture stop = frame.button(STOP_BUTTON_TEXT);
    start.click();
    stop.click();

    waitButtonEnabled(STOP_BUTTON_TEXT, false);
  }

  @Test
  public void shouldDisableRestartButtonWhenStopRecording(){
    JButtonFixture start = frame.button(START_BUTTON_TEXT);
    JButtonFixture stop = frame.button(STOP_BUTTON_TEXT);
    start.click();
    stop.click();

    waitButtonEnabled(RESTART_BUTTON_TEXT, false);
  }

  @Test
  public void shouldGetConfiguredPropertiesWhenFieldsAreSet(){}

  @Test
  public void shouldGetConfiguredFieldsWhenPropertiesAreSet(){
    /*Text Fields*/
    JTextComponentFixture portField = frame.textBox("portField");
    JTextComponentFixture serverField = frame.textBox(("serverField"));
    JTextComponentFixture connectionTimeoutField = frame.textBox(("connectionTimeout"));
    JTextComponentFixture waitConditionsTimeoutThresholdField = frame.textBox(("waitConditionsTimeoutThreshold"));

    /*ComboBoxes*/
    JComboBoxFixture protocolComboBox = frame.comboBox("protocolComboBox");
    JComboBoxFixture terminalTypeComboBox = frame.comboBox("terminalTypeComboBox");

    /*RadioButton*/
    JRadioButtonFixture sslTypeRadioButton = frame.radioButton("NONE");

    softly.assertThat(portField.text()).as("portField").isEqualTo(PORT);
    softly.assertThat(serverField.text()).as("serverField").isEqualTo(SERVER);
    softly.assertThat(connectionTimeoutField.text()).as("connectionTimeout").isEqualTo(TIMEOUT);
    softly.assertThat(waitConditionsTimeoutThresholdField.text()).as("waitConditionsTimeoutThreshold").isEqualTo(WAIT_TIMEOUT);

    softly.assertThat(protocolComboBox.selectedItem()).as("protocolComboBox").isEqualTo(PROTOCOL_TEXT);
    softly.assertThat(terminalTypeComboBox.selectedItem()).as("terminalTypeComboBox").isEqualTo(TERMINAL_TYPE_TEXT);

    softly.assertThat(sslTypeRadioButton.isEnabled()).as("sslType_NONE").isEqualTo(true);
  }
}
