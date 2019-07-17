package com.blazemeter.jmeter.rte.recorder;

import static org.assertj.swing.fixture.Containers.showInFrame;
import static org.assertj.swing.timing.Pause.pause;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

import com.blazemeter.jmeter.rte.JMeterTestUtils;
import com.blazemeter.jmeter.rte.core.Protocol;
import com.blazemeter.jmeter.rte.core.TerminalType;
import com.blazemeter.jmeter.rte.core.ssl.SSLType;
import java.awt.Dimension;
import org.assertj.core.api.JUnitSoftAssertions;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.fixture.JComboBoxFixture;
import org.assertj.swing.fixture.JRadioButtonFixture;
import org.assertj.swing.fixture.JTextComponentFixture;
import org.assertj.swing.timing.Condition;
import org.junit.After;
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
public class RTERecorderPanelIT {

  private static final String PORT = "80";
  private static final String SERVER = "server";
  private static final String CONNECTION_TIMEOUT = "5000";
  private static final Protocol PROTOCOL = Protocol.TN5250;
  private static final SSLType SSL_TYPE = SSLType.NONE;
  private static final TerminalType TERMINAL_TYPE = new TerminalType("IBM-3179-2",
      new Dimension(80, 24));
  private static final String WAIT_TIMEOUT = "10000";
  private static final String START_BUTTON_TEXT = "start";
  private static final String STOP_BUTTON_TEXT = "stop";
  private static final String RESTART_BUTTON_TEXT = "restart";
  private static final long VERIFY_TIMEOUT_MILLIS = 10000;

  @Rule
  public VerificationCollector collector = MockitoJUnit.collector();

  @Rule
  public JUnitSoftAssertions softly = new JUnitSoftAssertions();

  @Mock
  public RecordingStateListener listener;

  private FrameFixture frame;
  private RTERecorderPanel panel;

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
  public void shouldNotifyStartRecordingListenerWhenStartRecording() {
    clickButton(START_BUTTON_TEXT);
    verify(listener, timeout(VERIFY_TIMEOUT_MILLIS)).onRecordingStart();
  }

  public void clickButton(String name) {
    frame.button(name).click();
  }

  @Test
  public void shouldNotifyStopRecordingListenerWhenStopRecording() {
    clickButton(START_BUTTON_TEXT);
    waitButtonEnabled(STOP_BUTTON_TEXT, true);
    clickButton(STOP_BUTTON_TEXT);

    verify(listener, timeout(VERIFY_TIMEOUT_MILLIS)).onRecordingStop();
  }

  @Test
  public void shouldNotifyStopAndStartRecordingListenerWhenRestartRecording() {
    clickButton(START_BUTTON_TEXT);
    waitButtonEnabled(RESTART_BUTTON_TEXT, true);
    clickButton(RESTART_BUTTON_TEXT);

    verify(listener, timeout(VERIFY_TIMEOUT_MILLIS)).onRecordingStop();
    verify(listener, timeout(VERIFY_TIMEOUT_MILLIS).times(2)).onRecordingStart();
  }

  @Test
  public void shouldDisableStartButtonWhenStartRecording() {
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
  public void shouldEnableStopButtonWhenStartRecording() {
    clickButton(START_BUTTON_TEXT);
    waitButtonEnabled(STOP_BUTTON_TEXT, true);
  }

  @Test
  public void shouldEnableRestartButtonWhenStartRecording() {
    clickButton(START_BUTTON_TEXT);
    waitButtonEnabled(RESTART_BUTTON_TEXT, true);
  }

  @Test
  public void shouldHaveEnabledStartButtonWhenInitPanel() {
    waitButtonEnabled(START_BUTTON_TEXT, true);
  }

  @Test
  public void shouldHaveDisabledStopButtonWhenInitPanel() {
    waitButtonEnabled(STOP_BUTTON_TEXT, false);
  }

  @Test
  public void shouldHaveDisabledRestartButtonWhenInitPanel() {
    waitButtonEnabled(RESTART_BUTTON_TEXT, false);
  }

  @Test
  public void shouldEnableStartButtonWhenStopRecording() {
    clickButton(START_BUTTON_TEXT);
    waitButtonEnabled(STOP_BUTTON_TEXT, true);
    clickButton(STOP_BUTTON_TEXT);

    waitButtonEnabled(START_BUTTON_TEXT, true);
  }

  @Test
  public void shouldDisableStopButtonWhenStopRecording() {
    clickButton(START_BUTTON_TEXT);
    waitButtonEnabled(STOP_BUTTON_TEXT, true);
    clickButton(STOP_BUTTON_TEXT);

    waitButtonEnabled(STOP_BUTTON_TEXT, false);
  }

  @Test
  public void shouldDisableRestartButtonWhenStopRecording() {
    clickButton(START_BUTTON_TEXT);
    waitButtonEnabled(STOP_BUTTON_TEXT, true);
    clickButton(STOP_BUTTON_TEXT);

    waitButtonEnabled(RESTART_BUTTON_TEXT, false);
  }

  @Test
  public void shouldGetConfiguredPropertiesWhenFieldsAreSet() {
    setPanelFields();

    softly.assertThat(panel.getPort()).as("portField").isEqualTo(PORT);
    softly.assertThat(panel.getServer()).as("serverField").isEqualTo(SERVER);
    softly.assertThat(panel.getConnectionTimeout()).as("connectionTimeout").isEqualTo(
        CONNECTION_TIMEOUT);
    softly.assertThat(panel.getWaitConditionsTimeoutThresholdMillis())
        .as("waitConditionsTimeoutThreshold").isEqualTo(WAIT_TIMEOUT);
    softly.assertThat(panel.getProtocol()).as("protocolComboBox").isEqualTo(PROTOCOL);
    softly.assertThat(panel.getTerminalType()).as("terminalTypeComboBox")
        .isEqualTo(TERMINAL_TYPE);
    softly.assertThat(panel.getSSLType()).as("sslType").isEqualTo(SSL_TYPE);
  }

  private void setPanelFields() {
    setTextField("portField", PORT);
    setTextField("serverField", SERVER);
    setTextField("connectionTimeout", CONNECTION_TIMEOUT);
    setTextField("waitConditionsTimeoutThreshold", WAIT_TIMEOUT);
    setComboBoxField("protocolComboBox", PROTOCOL.name());
    setComboBoxField("terminalTypeComboBox", TERMINAL_TYPE.toString());
    frame.radioButton(SSL_TYPE.name()).check();
  }

  private void setComboBoxField(String fieldName, String fieldValue) {
    frame.comboBox(fieldName).selectItem(fieldValue);
  }

  private void setTextField(String fieldName, String fieldValue) {
    frame.textBox(fieldName).setText(fieldValue);
  }

  @Test
  public void shouldGetConfiguredFieldsWhenPropertiesAreSet() {
    setPanelProperties();

    softAssertTextField("portField", PORT);
    softAssertTextField("serverField", SERVER);
    softAssertTextField("connectionTimeout", CONNECTION_TIMEOUT);
    softAssertTextField("waitConditionsTimeoutThreshold", WAIT_TIMEOUT);
    softAssertComboBoxField("protocolComboBox", PROTOCOL.name());
    softAssertComboBoxField("terminalTypeComboBox", TERMINAL_TYPE.toString());
    softAssertRadioField("NONE", true);
  }

  private void setPanelProperties() {
    panel.setPort(PORT);
    panel.setServer(SERVER);
    panel.setConnectionTimeout(CONNECTION_TIMEOUT);
    panel.setWaitConditionsTimeoutThresholdMillis(WAIT_TIMEOUT);
    panel.setProtocol(PROTOCOL);
    panel.setTerminalType(TERMINAL_TYPE);
    panel.setSSLType(SSL_TYPE);
  }

  private void softAssertTextField(String name, String value) {
    JTextComponentFixture field = frame.textBox(name);
    softly.assertThat(field.text()).as(name).isEqualTo(value);
  }

  private void softAssertComboBoxField(String name, String value) {
    JComboBoxFixture combo = frame.comboBox(name);
    softly.assertThat(combo.selectedItem()).as(name).isEqualTo(value);
  }

  private void softAssertRadioField(String name, boolean value) {
    JRadioButtonFixture sslTypeRadioButton = frame.radioButton(name);
    softly.assertThat(sslTypeRadioButton.isEnabled()).as(name).isEqualTo(value);
  }

}
