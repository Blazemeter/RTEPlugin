package com.blazemeter.jmeter.rte.recorder;

import com.blazemeter.jmeter.rte.core.RteProtocolClient;
import com.blazemeter.jmeter.rte.core.wait.WaitCondition;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class WaitConditionsRecorder {
  public final long stablePeriod = 1000L;
  public long timeOutThreshold;
  private Date currentDate;
  private RteProtocolClient terminalClient;

  public WaitConditionsRecorder(RteProtocolClient terminalClient) {
    this.terminalClient = terminalClient;
  }

  public WaitConditionsRecorder() {

  }

  public List<WaitCondition> buildWaitConditions() {
    List<WaitCondition> waiters = new ArrayList<>();
    return waiters;
  }

  public void start() {
    currentDate = new Date();
  }

  public Date getStartTime() {
    return currentDate;
  }

  public boolean getInputInhibitedStatus() {
    return terminalClient.isInputInhibited();
  }
}
