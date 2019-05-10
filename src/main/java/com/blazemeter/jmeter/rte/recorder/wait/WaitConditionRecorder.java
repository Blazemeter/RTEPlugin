package com.blazemeter.jmeter.rte.recorder.wait;

import com.blazemeter.jmeter.rte.core.RteProtocolClient;
import com.blazemeter.jmeter.rte.core.listener.TerminalStateListener;
import com.blazemeter.jmeter.rte.core.wait.WaitCondition;
import com.blazemeter.jmeter.rte.recorder.RTERecorder;
import java.util.Date;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class WaitConditionRecorder implements TerminalStateListener {

  protected static final Logger LOG = LoggerFactory.getLogger(RTERecorder.class);
  protected RteProtocolClient rteProtocolClient;
  protected long maxStablePeriodMillis;
  protected long stablePeriodThresholdMillis;
  protected Date lastStatusChangeTime;
  private Date startTime;
  private long timeoutThresholdMillis;

  public WaitConditionRecorder(RteProtocolClient rteProtocolClient, long timeoutThresholdMillis,
                               long stablePeriodThresholdMillis) {
    this.rteProtocolClient = rteProtocolClient;
    this.timeoutThresholdMillis = timeoutThresholdMillis;
    this.stablePeriodThresholdMillis = stablePeriodThresholdMillis;
  }

  public void onTerminalStateChange() {
    Date currentTime = new Date();
    if (lastStatusChangeTime != null &&
        currentTime.getTime() - lastStatusChangeTime.getTime() > maxStablePeriodMillis) {
      maxStablePeriodMillis = currentTime.getTime() - lastStatusChangeTime.getTime();
    }
    lastStatusChangeTime = currentTime;
  }

  public abstract Optional<WaitCondition> buildWaitCondition();

  protected long buildTimeout() {
    Date lastChangeTime = lastStatusChangeTime != null ? lastStatusChangeTime : new Date();
    return lastChangeTime.getTime() - startTime.getTime() + timeoutThresholdMillis;
  }

  public void start() {
    startTime = new Date();
    rteProtocolClient.addTerminalStateListener(this);
    lastStatusChangeTime = null;
    maxStablePeriodMillis = 0;
  }

  public Optional<WaitCondition> stop() {
    rteProtocolClient.removeTerminalStateListener(this);
    return buildWaitCondition();
  }

  public Optional<Date> getLastStatusChangeTime() {
    return Optional.ofNullable(lastStatusChangeTime);
  }
}
