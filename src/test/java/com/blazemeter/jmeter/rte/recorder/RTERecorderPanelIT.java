package com.blazemeter.jmeter.rte.recorder;

import com.blazemeter.jmeter.rte.JMeterTestUtils;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.VerificationCollector;

import javax.swing.*;
import java.awt.event.ActionEvent;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;

public class RTERecorderPanelIT {

  @Rule
  public VerificationCollector collector = MockitoJUnit.collector();

  @Rule
  public final JUnitSoftAssertions softly = new JUnitSoftAssertions();

  private static final String ADD_ACTION_START = "addActionStart";
  private static final String ADD_ACTION_STOP = "addActionStop";
  private static final String ADD_ACTION_RESTART = "addActionRestart";

  private RTERecorderPanel panel;
  private RecordingStateListener listener;

  @BeforeClass
  public static void setupClass() {
    JMeterTestUtils.setupJmeterEnv();
  }

  @Before
  public void setup() {
    listener = Mockito.mock(RecordingStateListener.class);
    panel = new RTERecorderPanel(listener);
  }

  @Test
  public void shouldNotifyStartRecordingListenerWhenStartRecording() throws Exception {
    ActionEvent addActionStart = new ActionEvent("", 1, ADD_ACTION_START);
    panel.actionPerformed(addActionStart);

    verify(listener).onRecordingStart();
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
