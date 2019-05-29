package com.blazemeter.jmeter.rte.recorder;

import com.blazemeter.jmeter.rte.JMeterTestUtils;
import com.blazemeter.jmeter.rte.core.Protocol;
import com.blazemeter.jmeter.rte.core.TerminalType;
import com.blazemeter.jmeter.rte.core.ssl.SSLType;
import org.assertj.core.api.JUnitSoftAssertions;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.fixture.JButtonFixture;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.VerificationCollector;

import javax.swing.*;
import java.awt.event.ActionEvent;

import static org.assertj.swing.fixture.Containers.showInFrame;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class RTERecorderPanelIT {
  
  @Rule
  public final JUnitSoftAssertions softly = new JUnitSoftAssertions();

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

  @Test
  public void shouldNotifyStartRecordingListenerWhenStartRecording() throws Exception {

    JButtonFixture start = frame.button("start");
    start.click();

    verify(listener).onRecordingStart();
  }

  @Test
  public void shouldNotifyStopRecordingListenerWhenStopRecording(){

    JButtonFixture start = frame.button("restart");
    JButtonFixture stop = frame.button("stop");
    start.click();
    stop.click();

    verify(listener).onRecordingStop();
  }

  @Test
  public void shouldNotifyStopAndStartRecordingListenerWhenRestartRecording() throws Exception {
    JButtonFixture start = frame.button("start");
    start.click();

    JButtonFixture restart = frame.button("restart");
    restart.click();

    verify(listener).onRecordingStop();
    verify(listener, times(2)).onRecordingStart();
  }

  @Test
  public void shouldDisableStartButtonWhenStartRecording(){
    JButtonFixture start = frame.button("start");
    start.click();

    assertEquals(false, start.isEnabled());
  }

  @Test
  public void shouldEnableStopButtonWhenStartRecording(){
    JButtonFixture start = frame.button("start");
    start.click();

    JButtonFixture stop = frame.button("stop");

    assertEquals(true, stop.isEnabled());
  }

  @Test
  public void shouldEnableRestartButtonWhenStartRecording(){
    JButtonFixture start = frame.button("start");
    start.click();

    JButtonFixture restart = frame.button("restart");

    assertEquals(true, restart.isEnabled());
  }

  @Test
  public void shouldHaveEnabledStartButtonWhenInitPanel(){
    JButtonFixture start = frame.button("start");

    assertEquals(true, start.isEnabled());
  }

  @Test
  public void shouldHaveDisabledStopButtonWhenInitPanel(){
    JButtonFixture stop = frame.button("stop");
    assertEquals(false, stop.isEnabled());
  }

  @Test
  public void shouldHaveDisabledRestartButtonWhenInitPanel(){
    JButtonFixture restart = frame.button("restart");
    assertEquals(false, restart.isEnabled());
  }

  @Test
  public void shouldEnableStartButtonWhenStopRecording(){
    JButtonFixture start = frame.button("restart");
    JButtonFixture stop = frame.button("stop");
    start.click();
    stop.click();

    assertEquals(true, start.isEnabled());
  }

  @Test
  public void shouldDisableStopButtonWhenStopRecording(){
    JButtonFixture start = frame.button("restart");
    JButtonFixture stop = frame.button("stop");
    start.click();
    stop.click();

    assertEquals(false, stop.isEnabled());
  }

  @Test
  public void shouldDisableRestartButtonWhenStopRecording(){
    JButtonFixture start = frame.button("restart");
    JButtonFixture stop = frame.button("stop");
    start.click();
    stop.click();

    JButtonFixture restart = frame.button("restart");

    assertEquals(false, restart.isEnabled());
  }

  @Test
  public void shouldGetConfiguredPropertiesWhenFieldsAreSet(){}

  @Test
  public void shouldGetConfiguredFieldsWhenPropertiesAreSet(){}
}
