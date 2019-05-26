package com.blazemeter.jmeter.rte.recorder;

import org.junit.Test;
import org.mockito.Mockito;

import static org.mockito.Mockito.verify;

public class RTERecorderPanelTest {

  @Test
  public void shouldNotifyStartRecordingListenerWhenStartRecording() throws Exception {

    RecordingStateListener listener = Mockito.mock(RecordingStateListener.class);
    RTERecorderPanel panel = new RTERecorderPanel(listener);

    panel.onRecordingStart();

    verify(listener.onRecordingStart()).times(1);
  }

  @Test
  public void shouldNotifyStopRecordingListenerWhenStopRecording(){}

  @Test
  public void shouldNotifyStopAndStartRecordingListenerWhenRestartRecording(){}

  @Test
  public void shouldDisableStartButtonWhenStartRecording(){}

  @Test
  public void shouldEnableStopButtonWhenStartRecording(){}

  @Test
  public void shouldEnableRestartButtonWhenStartRecording(){}

  @Test
  public void shouldHaveEnabledStartButtonWhenInitPanel(){}

  @Test
  public void shouldHaveDisabledStopButtonWhenInitPanel(){}

  @Test
  public void shouldHaveDisabledRestartButtonWhenInitPanel(){}

  @Test
  public void shouldEnableStartButtonWhenStopRecording(){}

  @Test
  public void shouldDisableStopButtonWhenStopRecording(){}

  @Test
  public void shouldDisableRestartButtonWhenStopRecording(){}

  /**
   * Roger: los ultimos 2 van a quedar como varios asserts,
   * si queres podes usar soft asserts ahi. Este tiene que
   * ser un test IT con interfaz grafica (assertjswing)
   * */
  @Test
  public void shouldGetConfiguredProperiesWhenFieldsAreSet(){}

  @Test
  public void shouldGetConfiguredFieldsWhenPropertiesAreSet(){}



}
