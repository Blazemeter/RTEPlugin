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

  @Before
  public void setup() {
    panel = new RTERecorderPanel(listener);
    frame = showInFrame(panel);
  }

  @After
  public void tearDown() {
    frame.cleanUp();
  }

  @Test
  public void shouldNotifyStartRecordingListenerWhenStartRecording() throws Exception {
    clickButton(START_BUTTON_TEXT);

    verify(listener, timeout(VERIFY_TIMEOUT)).onRecordingStart();
  }

  public void clickButton(String name){
    JButtonFixture start = frame.button(name);
    start.click();
  }

  @Test
  public void shouldNotifyStopRecordingListenerWhenStopRecording(){
    clickButton(START_BUTTON_TEXT);
    waitButtonEnabled(STOP_BUTTON_TEXT, true);

    clickButton(STOP_BUTTON_TEXT);

    verify(listener, timeout(VERIFY_TIMEOUT)).onRecordingStop();
  }

  @Test
  public void shouldNotifyStopAndStartRecordingListenerWhenRestartRecording() throws Exception {
    clickButton(START_BUTTON_TEXT);

    waitButtonEnabled(RESTART_BUTTON_TEXT, true);
    clickButton(RESTART_BUTTON_TEXT);

    verify(listener, timeout(VERIFY_TIMEOUT)).onRecordingStop();
    verify(listener, timeout(VERIFY_TIMEOUT).times(2)).onRecordingStart();
  }

  @Test
  public void shouldDisableStartButtonWhenStartRecording(){
    clickButton(START_BUTTON_TEXT);

    waitButtonEnabled(START_BUTTON_TEXT, false);
  }

  public void waitButtonEnabled(String buttonName, boolean enable) {
    pause(new Condition("button " + buttonName + " to be " + (enable ? "enabled" : "disabled")) {
      @Override
      public boolean test() {
        return frame.button(buttonName).isEnabled() == enable;
      }
    });
  }

  @Test
  public void shouldEnableStopButtonWhenStartRecording(){
    clickButton(START_BUTTON_TEXT);

    waitButtonEnabled(STOP_BUTTON_TEXT, true);
  }

  @Test
  public void shouldEnableRestartButtonWhenStartRecording(){
    clickButton(START_BUTTON_TEXT);

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
    clickButton(START_BUTTON_TEXT);

    waitButtonEnabled(STOP_BUTTON_TEXT, true);
    clickButton(STOP_BUTTON_TEXT);

    waitButtonEnabled(START_BUTTON_TEXT, true);
  }

  @Test
  public void shouldDisableStopButtonWhenStopRecording(){
    clickButton(START_BUTTON_TEXT);

    waitButtonEnabled(STOP_BUTTON_TEXT, true);
    clickButton(STOP_BUTTON_TEXT);

    waitButtonEnabled(STOP_BUTTON_TEXT, false);
  }

  @Test
  public void shouldDisableRestartButtonWhenStopRecording(){
    clickButton(START_BUTTON_TEXT);

    waitButtonEnabled(STOP_BUTTON_TEXT, true);
    clickButton(STOP_BUTTON_TEXT);

    waitButtonEnabled(RESTART_BUTTON_TEXT, false);
  }

  @Test
  public void shouldGetConfiguredPropertiesWhenFieldsAreSet(){
    settingFields();

    softly.assertThat(panel.getPort()).as("portField").isEqualTo(PORT);
    softly.assertThat(panel.getServer()).as("serverField").isEqualTo(SERVER);
    softly.assertThat(panel.getConnectionTimeout()).as("connectionTimeout").isEqualTo(TIMEOUT);
    softly.assertThat(panel.getWaitConditionsTimeoutThresholdMillis()).as("waitConditionsTimeoutThreshold").isEqualTo(WAIT_TIMEOUT);

    softly.assertThat(panel.getProtocol().name()).as("protocolComboBox").isEqualTo(PROTOCOL_TEXT).toString();
    softly.assertThat(panel.getTerminalType().toString()).as("terminalTypeComboBox").isEqualTo(TERMINAL_TYPE_TEXT);

    softly.assertThat(panel.getSSLType().toString()).as("sslType_NONE").isEqualTo(SSL_TYPE.toString());
  }

  private void settingFields(){

    JTextComponentFixture portField = frame.textBox("portField");
    JTextComponentFixture serverField = frame.textBox(("serverField"));
    JTextComponentFixture connectionTimeoutField = frame.textBox(("connectionTimeout"));
    JTextComponentFixture waitConditionsTimeoutThresholdField = frame.textBox(("waitConditionsTimeoutThreshold"));

    JComboBoxFixture protocolComboBox = frame.comboBox("protocolComboBox");
    JComboBoxFixture terminalTypeComboBox = frame.comboBox("terminalTypeComboBox");

    JRadioButtonFixture sslTypeRadioButton = frame.radioButton("NONE");

    portField.enterText(PORT);
    serverField.enterText(SERVER);
    connectionTimeoutField.enterText(TIMEOUT);
    waitConditionsTimeoutThresholdField.setText(WAIT_TIMEOUT);
    protocolComboBox.selectItem(PROTOCOL_TEXT);
    terminalTypeComboBox.selectItem(TERMINAL_TYPE_TEXT);
    sslTypeRadioButton.check();
  }

  @Test
  public void shouldGetConfiguredFieldsWhenPropertiesAreSet(){
    configureProperties();

    softAssertJText("portField", PORT);
    softAssertJText("serverField", SERVER);
    softAssertJText("connectionTimeout", TIMEOUT);
    softAssertJText("waitConditionsTimeoutThreshold", WAIT_TIMEOUT);

    softAssertJComboBox("protocolComboBox", PROTOCOL_TEXT);
    softAssertJComboBox("terminalTypeComboBox", TERMINAL_TYPE_TEXT);

    softAssertJRadio("NONE", true);
  }

  private void configureProperties(){
    panel.setPort(PORT);
    panel.setServer(SERVER);
    panel.setConnectionTimeout(TIMEOUT);
    panel.setWaitConditionsTimeoutThresholdMillis(WAIT_TIMEOUT);
    panel.setProtocol(PROTOCOL);
    panel.setTerminalType(TERMINAL_TYPE);
    panel.setSSLType(SSL_TYPE);
  }

  private void softAssertJText(String name, String value) {
    JTextComponentFixture field = frame.textBox(name);
    softly.assertThat(field.text()).as(name).isEqualTo(value);
  }

  private void softAssertJComboBox(String name, String value) {
    JComboBoxFixture combo = frame.comboBox(name);
    softly.assertThat(combo.selectedItem()).as(name).isEqualTo(value);
  }

  private void softAssertJRadio(String name, boolean value) {
    JRadioButtonFixture sslTypeRadioButton = frame.radioButton(name);
    softly.assertThat(sslTypeRadioButton.isEnabled()).as(name).isEqualTo(value);
  }


}
