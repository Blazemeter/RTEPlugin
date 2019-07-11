package com.blazemeter.jmeter.rte.recorder.emulator;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import javax.swing.JLabel;

public class MessageLabel extends JLabel {

  private static final int TIMEOUT_MILLIS = 4500;
  private final ScheduledExecutorService messageExecutor;
  private ScheduledFuture future;

  public MessageLabel(ScheduledExecutorService scheduledExecutorService) {
    setVisible(false);
    messageExecutor = scheduledExecutorService;
    this.setFont(this.getFont().deriveFont(10.0F));
  }

  public synchronized void showMessage(String message) {
    if (future != null) {
      future.cancel(true);
      setVisible(false);
    }
    setText(message);
    setVisible(true);
    future = messageExecutor
        .schedule(() -> setVisible(false), TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);
  }
  
}
