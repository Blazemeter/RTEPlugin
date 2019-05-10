package com.blazemeter.jmeter.rte.recorder;

import com.blazemeter.jmeter.rte.core.RteProtocolClient;
import com.blazemeter.jmeter.rte.core.listener.TerminalStateListener;
import com.blazemeter.jmeter.rte.core.wait.WaitCondition;

import java.util.Date;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class WaitConditionRecorder implements TerminalStateListener {

  protected static final Logger LOG = LoggerFactory.getLogger(RTERecorder.class);
  protected RteProtocolClient rteProtocolClient;
  private Date lastStatusChangeTime;
  private Date startTime;
  private long maxStablePeriodMillis;
  private long timeoutThresholdMillis;
  private long stablePeriodThresholdMillis;
  
  public WaitConditionRecorder(RteProtocolClient rteProtocolClient, long timeoutThresholdMillis, 
                               long stablePeriodThresholdMillis) {
    this.rteProtocolClient = rteProtocolClient;
    this.timeoutThresholdMillis = timeoutThresholdMillis;
    this.stablePeriodThresholdMillis = stablePeriodThresholdMillis;
  }

  public void onTerminalStateChange() {
    Date currentTime = new Date();
    if (currentTime.getTime() - lastStatusChangeTime.getTime() > maxStablePeriodMillis) {
      maxStablePeriodMillis = currentTime.getTime() - lastStatusChangeTime.getTime();
    }
    lastStatusChangeTime = currentTime;
  }

  public abstract Optional<WaitCondition> buildWaitCondition();
  
  protected long buildTimeout() {
    return lastStatusChangeTime.getTime() - startTime.getTime() + timeoutThresholdMillis;
  }
  
  protected long buildStablePeriodMillis() {
    return maxStablePeriodMillis + stablePeriodThresholdMillis;
  } 
  
  public void start() {
    startTime = new Date();
    rteProtocolClient.addTerminalStateListener(this);
  }
  
  public Optional<WaitCondition> stop() {
    rteProtocolClient.removeTerminalStateListener(this);
    return buildWaitCondition();
  }
  
  public Date getLastStatusChangeTime() {
    return lastStatusChangeTime;
  }
}
