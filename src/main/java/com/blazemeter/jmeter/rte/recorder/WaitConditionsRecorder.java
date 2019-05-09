package com.blazemeter.jmeter.rte.recorder;

import com.blazemeter.jmeter.rte.core.RteProtocolClient;
import com.blazemeter.jmeter.rte.core.wait.WaitCondition;

import java.util.ArrayList;
import java.util.List;

public class WaitConditionsRecorder {
  private List<WaitConditionRecorder> waitConditionRecorders = new ArrayList<>();

  WaitConditionsRecorder(RteProtocolClient rteProtocolClient,
                         long timeoutThresholdMillis, long stablePeriod) {
    waitConditionRecorders.add(new SyncWaitRecorder(rteProtocolClient,
            timeoutThresholdMillis, stablePeriod));
    waitConditionRecorders.add(new SilentWaitRecorder());
  }

  public void start() {

    for (WaitConditionRecorder waits : waitConditionRecorders) {
      waits.start();
    }
  }

  public void stop() {

    buildWaitConditions();
  }

  public List<WaitCondition> buildWaitConditions() {
    List<WaitCondition> waitConditionRecordersResult = new ArrayList<>();
    for (WaitConditionRecorder waits : waitConditionRecorders) {
      waitConditionRecordersResult.add(waits.buildWaitCondition());
    }
    return waitConditionRecordersResult;
  }

}
