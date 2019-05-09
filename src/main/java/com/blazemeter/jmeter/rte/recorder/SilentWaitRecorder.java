package com.blazemeter.jmeter.rte.recorder;

import com.blazemeter.jmeter.rte.core.wait.WaitCondition;

public class SilentWaitRecorder extends WaitConditionRecorder {

  @Override
  public WaitCondition buildWaitCondition() {
    return null;
  }

  @Override
  public void start() {

  }

}
