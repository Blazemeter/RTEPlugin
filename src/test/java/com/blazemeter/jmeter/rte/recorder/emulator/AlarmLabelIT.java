package com.blazemeter.jmeter.rte.recorder.emulator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.swing.fixture.Containers.showInFrame;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.assertj.core.api.JUnitSoftAssertions;
import org.assertj.swing.fixture.FrameFixture;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AlarmLabelIT {

  private FrameFixture frame;
  private AlarmLabel alarmLabel;

  @Rule
  public final JUnitSoftAssertions softly = new JUnitSoftAssertions();

  @Mock
  private ScheduledFuture future;

  private ScheduledExecutorServiceTest executorService;

  @Before
  public void setup() {
    executorService = new ScheduledExecutorServiceTest(future);
    alarmLabel = new AlarmLabel(executorService);
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
    alarmLabel.soundAlarm();
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
    result.add(alarmLabel.isVisible());
    alarmLabel.soundAlarm();
    for (int i = 0; i < 2; i++) {
      executorService.tick();
      result.add(alarmLabel.isVisible());
    }
    alarmLabel.soundAlarm();
    for (int i = 0; i < 10; i++) {
      executorService.tick();
      result.add(alarmLabel.isVisible());
    }
    executorService.tick();
    result.add(alarmLabel.isVisible());
    assertThat(result).isEqualTo(expected);
  }

  

}
