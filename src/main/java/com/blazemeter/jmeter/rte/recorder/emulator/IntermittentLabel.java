package com.blazemeter.jmeter.rte.recorder.emulator;

import com.blazemeter.jmeter.rte.sampler.gui.ThemedIconLabel;
import com.helger.commons.annotation.VisibleForTesting;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class IntermittentLabel extends ThemedIconLabel {

  private static final int LABEL_ITERATION = 10;
  private static final int BLINK_TIME_PERIOD_MILLIS = 500;
  private final ScheduledExecutorService executor;
  private ScheduledFuture future;
  private int counter;
  private boolean labelState = true;
  private Runnable defaultTask;
  private Runnable onBlinkTask;

  public IntermittentLabel(String iconResourceName) {
    this(iconResourceName, Executors.newSingleThreadScheduledExecutor());
  }

  @VisibleForTesting
  public IntermittentLabel(String iconResourceName, ScheduledExecutorService executor) {
    super(iconResourceName);
    setVisible(false);
    this.executor = executor;
  }

  public synchronized void blink() {
    if (future != null) {
      future.cancel(true);
      defaultTask.run();
    }
    counter = 0;
    future = executor.scheduleAtFixedRate(() -> {
      onBlinkTask.run();
      if (counter < LABEL_ITERATION) {
        labelState = !labelState;
        counter++;
      } else {
        future.cancel(true);
        defaultTask.run();
      }

    }, 0, BLINK_TIME_PERIOD_MILLIS, TimeUnit.MILLISECONDS);
  }

  public void setOnBlinkTask(Runnable onBlinkTask) {
    this.onBlinkTask = onBlinkTask;
  }

  public void setDefaultTask(Runnable defaultTask) {
    this.defaultTask = defaultTask;
  }

  public void shutdown() {
    executor.shutdown();
  }

  public boolean getLabelState() {
    return labelState;
  }
}
