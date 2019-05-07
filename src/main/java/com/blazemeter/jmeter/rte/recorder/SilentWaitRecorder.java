package com.blazemeter.jmeter.rte.recorder;

import com.blazemeter.jmeter.rte.core.wait.SilentWaitCondition;
import com.blazemeter.jmeter.rte.core.wait.WaitCondition;

public class SilentWaitRecorder extends WaitConditionRecorder {
  private long userThreshole;

  @Override
  public WaitCondition buildWaitCondition() {
    long maxStablePeriod = getMaxStablePeriod();
    if (maxStablePeriod > stablePeriod) {
      return new SilentWaitCondition(timeOutThreshold, maxStablePeriod + userThreshole);
    } else {
      return null;
    }
  }

}
