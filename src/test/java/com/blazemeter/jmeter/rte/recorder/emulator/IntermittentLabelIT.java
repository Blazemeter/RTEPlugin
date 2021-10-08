package com.blazemeter.jmeter.rte.recorder.emulator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.swing.fixture.Containers.showInFrame;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import org.assertj.core.api.JUnitSoftAssertions;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/*
   Mainly this class tests the IntermittentLabel when used by alarm.
   When IntermittentLabel is used as blocked cursor for CharacterEmulator is tested in
   Xtn5250TerminalEmulator.
*/

@RunWith(MockitoJUnitRunner.class)
public class IntermittentLabelIT {

  @Rule
  public final JUnitSoftAssertions softly = new JUnitSoftAssertions();


  private FrameFixture frame;
  private IntermittentLabel alarmLabel;
  @Mock
  private ScheduledFuture future;

  private ScheduledExecutorServiceTest executorService;

  @Before
  public void setup() {
    executorService = new ScheduledExecutorServiceTest(future);
    alarmLabel = new IntermittentLabel("alarm.png", executorService);
    alarmLabel.setDefaultTask(() -> alarmLabel.setVisible(false));
    alarmLabel.setOnBlinkTask(() -> alarmLabel.setVisible(!alarmLabel.isVisible()));
    frame = showInFrame(alarmLabel);
  }

  @After
  public void teardown() {
    frame.cleanUp();
  }

  @Test
  public void shouldBlinkForAPeriodWhenSoundAlarm() {
    List<Boolean> expected = Arrays
        .asList(false, true, false, true, false, true, false, true, false, true, false, false);
    List<Boolean> result = new ArrayList<>();
    result.add(alarmLabel.isVisible());
    alarmLabel.blink();
    for (int i = 0; i < 10; i++) {
      executorService.tick();
      result.add(alarmLabel.isVisible());
    }
    executorService.tick();
    result.add(alarmLabel.isVisible());
    assertThat(result).isEqualTo(expected);
  }

  @Test
  public void shouldStartNewBlinkPeriodWhenAlarmIsSoundedAndSoundAlarm() {
    List<Boolean> expected = Arrays
        .asList(false, true, false, true, false, true, false, true, false, true, false, true,
            false, false);
    List<Boolean> result = new ArrayList<>();
    result.add(GuiActionRunner.execute(() -> alarmLabel.isVisible()));
    GuiActionRunner.execute(() -> alarmLabel.blink());
    for (int i = 0; i < 2; i++) {
      GuiActionRunner.execute(() -> executorService.tick());
      result.add(GuiActionRunner.execute(() -> alarmLabel.isVisible()));
    }
    GuiActionRunner.execute(() -> alarmLabel.blink());
    for (int i = 0; i < 10; i++) {
      GuiActionRunner.execute(() -> executorService.tick());
      result.add(GuiActionRunner.execute(() -> alarmLabel.isVisible()));
    }
    GuiActionRunner.execute(() -> executorService.tick());
    result.add(GuiActionRunner.execute(() -> alarmLabel.isVisible()));
    assertThat(result).isEqualTo(expected);
  }


}
