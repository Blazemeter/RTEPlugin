package com.blazemeter.jmeter.rte.recorder.wait;

import com.blazemeter.jmeter.rte.core.RteProtocolClient;
import com.blazemeter.jmeter.rte.core.listener.TerminalStateListener;
import com.blazemeter.jmeter.rte.core.wait.WaitCondition;
import com.helger.commons.annotation.VisibleForTesting;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class WaitConditionRecorder implements TerminalStateListener {

  protected static final Logger LOG = LoggerFactory.getLogger(WaitConditionRecorder.class);
  protected RteProtocolClient rteProtocolClient;
  protected long maxStablePeriodMillis;
  protected long stablePeriodThresholdMillis;
  private Instant lastStatusChangeTime;
  private Instant startTime;
  private long timeoutThresholdMillis;
  private Clock clock;

  public WaitConditionRecorder(RteProtocolClient rteProtocolClient, long timeoutThresholdMillis,
                               long stablePeriodThresholdMillis) {
    this(rteProtocolClient, timeoutThresholdMillis,
        stablePeriodThresholdMillis, Clock.systemUTC());
  }

  @VisibleForTesting
  public WaitConditionRecorder(RteProtocolClient rteProtocolClient, long timeoutThresholdMillis,
                               long stablePeriodThresholdMillis, Clock clock) {
    this.rteProtocolClient = rteProtocolClient;
    this.timeoutThresholdMillis = timeoutThresholdMillis;
    this.stablePeriodThresholdMillis = stablePeriodThresholdMillis;
    this.clock = clock;
  }

  @Override
  public void onTerminalStateChange() {
    Instant currentTime = clock.instant();
    if (lastStatusChangeTime != null &&
        ChronoUnit.MILLIS.between(lastStatusChangeTime, currentTime) > maxStablePeriodMillis) {
      maxStablePeriodMillis = ChronoUnit.MILLIS.between(lastStatusChangeTime, currentTime);
    }
    lastStatusChangeTime = currentTime;
  }

  public abstract Optional<WaitCondition> buildWaitCondition();

  protected long buildTimeout() {
    long maxTimeMillis = lastStatusChangeTime != null
        ? ChronoUnit.MILLIS.between(startTime, lastStatusChangeTime) : 0;
    return maxTimeMillis + timeoutThresholdMillis;
  }

  public void start() {
    startTime = clock.instant();
    lastStatusChangeTime = null;
    maxStablePeriodMillis = 0;
    rteProtocolClient.addTerminalStateListener(this);
  }

  public Optional<WaitCondition> stop() {
    rteProtocolClient.removeTerminalStateListener(this);
    return buildWaitCondition();
  }

  public Optional<Instant> getLastStatusChangeTime() {
    return Optional.ofNullable(lastStatusChangeTime);
  }
  
  @Override
  public void onException(Throwable e){
    
  }
}
