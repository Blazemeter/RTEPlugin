package com.blazemeter.jmeter.rte.recorder.emulator;

import com.blazemeter.jmeter.rte.sampler.gui.ThemedIconLabel;
import com.helger.commons.annotation.VisibleForTesting;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class AlarmLabel extends ThemedIconLabel {

  private static final int ALARM_ITERATION = 10;
  private static final int BLINK_TIME_PERIOD_MILLIS = 500;
  private final ScheduledExecutorService alarmExecutor;
  private ScheduledFuture future;
  private int counter;

  public AlarmLabel() {
    this(Executors.newSingleThreadScheduledExecutor());
  }

  @VisibleForTesting
  public AlarmLabel(ScheduledExecutorService executor) {
    super("alarm.png");
    setVisible(false);
    alarmExecutor = executor;
  }

  public synchronized void soundAlarm() {
    if (future != null) {
      future.cancel(true);
      setVisible(false);
    }
    counter = 0;
    future = alarmExecutor.scheduleAtFixedRate(() -> {
      setVisible(!isVisible());
      if (counter < ALARM_ITERATION) {
        counter++;
      } else {
        future.cancel(true);
        setVisible(false);
      }
    }, 0, BLINK_TIME_PERIOD_MILLIS, TimeUnit.MILLISECONDS);
  }

  public void shutdown() {
    alarmExecutor.shutdown();
  }

}
