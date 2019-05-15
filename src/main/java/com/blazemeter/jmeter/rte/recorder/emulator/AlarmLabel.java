package com.blazemeter.jmeter.rte.recorder.emulator;

import com.helger.commons.annotation.VisibleForTesting;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import javax.swing.ImageIcon;
import javax.swing.JLabel;

class AlarmLabel extends JLabel {

  private final ScheduledExecutorService alarmExecutor;
  private ScheduledFuture future;
  private int counter;

  AlarmLabel(ImageIcon icon) {
    this(icon, Executors.newSingleThreadScheduledExecutor());
  }

  @VisibleForTesting
  AlarmLabel(ImageIcon icon, ScheduledExecutorService executor) {
    super(icon);
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
      if (counter < 10) {
        counter++;
      } else {
        future.cancel(true);
        setVisible(false);
      }
    }, 0, 500, TimeUnit.MILLISECONDS);
  }

  public void shutdown() {
    alarmExecutor.shutdown();
  }

}
