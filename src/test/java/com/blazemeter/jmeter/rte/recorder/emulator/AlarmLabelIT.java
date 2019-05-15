package com.blazemeter.jmeter.rte.recorder.emulator;

import static org.assertj.swing.fixture.Containers.showInFrame;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.swing.ImageIcon;
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

  private TestScheduledExecutorService executorService;

  @Mock
  private ImageIcon icon;

  @Before
  public void setup() {
    executorService = new TestScheduledExecutorService(future);
    alarmLabel = new AlarmLabel(icon, executorService);
    frame = showInFrame(alarmLabel);
  }

  @After
  public void teardown() {
    frame.cleanUp();
  }

  @Test
  public void shouldBlinkForAPeriodWhenSoundAlarm() {
    boolean visible = false;
    softly.assertThat(alarmLabel.isVisible()).isEqualTo(visible);
    alarmLabel.soundAlarm();
    for (int i = 0; i < 10; i++) {
      executorService.tock();
      visible = !visible;
      softly.assertThat(alarmLabel.isVisible()).isEqualTo(visible);
    }
    executorService.tock();
    softly.assertThat(alarmLabel.isVisible()).isEqualTo(false);
  }

  @Test
  public void shouldStartNewBlinkPeriodWhenAlarmIsSoundedAndSoundAlarm() {
    boolean visible = false;
    softly.assertThat(alarmLabel.isVisible()).isEqualTo(visible);
    alarmLabel.soundAlarm();
    for (int i = 0; i < 2; i++) {
      executorService.tock();
      visible = !visible;
      softly.assertThat(alarmLabel.isVisible()).isEqualTo(visible);
    }
    visible = false;
    alarmLabel.soundAlarm();
    for (int i = 0; i < 10; i++) {
      executorService.tock();
      visible = !visible;
      softly.assertThat(alarmLabel.isVisible()).isEqualTo(visible);
    }
    executorService.tock();
    softly.assertThat(alarmLabel.isVisible()).isEqualTo(false);
  }

  private static class TestScheduledExecutorService implements ScheduledExecutorService {

    private Runnable command;
    private ScheduledFuture future;

    TestScheduledExecutorService(ScheduledFuture future) {
      this.future = future;
    }

    @Override
    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay,
        long delay, TimeUnit unit) {
      return null;
    }

    void tock() {
      command.run();
    }

    @Override
    public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
      return null;
    }

    @Override
    public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit) {
      return null;
    }

    @Override
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period,
        TimeUnit unit) {
      this.command = command;
      return this.future;
    }

    @Override
    public void shutdown() {

    }

    @Override
    public List<Runnable> shutdownNow() {
      return null;
    }

    @Override
    public boolean isShutdown() {
      return false;
    }

    @Override
    public boolean isTerminated() {
      return false;
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
      return false;
    }

    @Override
    public <T> Future<T> submit(Callable<T> task) {
      return null;
    }

    @Override
    public <T> Future<T> submit(Runnable task, T result) {
      return null;
    }

    @Override
    public Future<?> submit(Runnable task) {
      return null;
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks)
        throws InterruptedException {
      return null;
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout,
        TimeUnit unit) throws InterruptedException {
      return null;
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks)
        throws InterruptedException, ExecutionException {
      return null;
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
        throws InterruptedException, ExecutionException, TimeoutException {
      return null;
    }

    @Override
    public void execute(Runnable command) {

    }
  }

}
