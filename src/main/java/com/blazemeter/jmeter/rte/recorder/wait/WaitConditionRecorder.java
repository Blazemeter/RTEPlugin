package com.blazemeter.jmeter.rte.recorder.wait;

import com.blazemeter.jmeter.rte.core.RteProtocolClient;
import com.blazemeter.jmeter.rte.core.listener.TerminalStateListener;
import com.blazemeter.jmeter.rte.core.wait.WaitCondition;
import com.blazemeter.jmeter.rte.recorder.RTERecorder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Optional;

public abstract class WaitConditionRecorder implements TerminalStateListener {

  protected static final Logger LOG = LoggerFactory.getLogger(RTERecorder.class);
  protected RteProtocolClient rteProtocolClient;
  protected long maxStablePeriodMillis;
  protected long stablePeriodThresholdMillis;
  protected Instant lastStatusChangeTime;
  private Instant startTime;
  private long timeoutThresholdMillis;

  public WaitConditionRecorder(RteProtocolClient rteProtocolClient, long timeoutThresholdMillis,
                               long stablePeriodThresholdMillis) {
    this.rteProtocolClient = rteProtocolClient;
    this.timeoutThresholdMillis = timeoutThresholdMillis;
    this.stablePeriodThresholdMillis = stablePeriodThresholdMillis;
  }
  
  public void onTerminalStateChange() {
    Instant currentTime = Instant.now();
    if (lastStatusChangeTime != null &&
        currentTime.getEpochSecond() - lastStatusChangeTime.getEpochSecond() > maxStablePeriodMillis) {
      maxStablePeriodMillis = currentTime.getEpochSecond() - lastStatusChangeTime.getEpochSecond();
    }
    lastStatusChangeTime = currentTime;
  }

  public abstract Optional<WaitCondition> buildWaitCondition();

  protected long buildTimeout() {
    long maxTimeMillis = lastStatusChangeTime != null
        ? lastStatusChangeTime.getEpochSecond() - startTime.getEpochSecond() : 0;
    return maxTimeMillis + timeoutThresholdMillis;
  }

  public void start() {
    startTime = Instant.now();
    rteProtocolClient.addTerminalStateListener(this);
    lastStatusChangeTime = null;
    maxStablePeriodMillis = 0;
  }

  public Optional<WaitCondition> stop() {
    rteProtocolClient.removeTerminalStateListener(this);
    return buildWaitCondition();
  }

  public Optional<Instant> getLastStatusChangeTime() {
    return Optional.ofNullable(lastStatusChangeTime);
  }
}
