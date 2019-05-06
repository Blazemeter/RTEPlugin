package com.blazemeter.jmeter.rte.recorder;

import com.blazemeter.jmeter.rte.core.wait.WaitCondition;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class WaitConditionsRecorder {
  private long timeOutThreshold;
  private Date currentDate;
 
  public void startTime() {
          this.currentDate = new Date();
  }
  
  public List<WaitCondition> buildWaitConditios() {
    List<WaitCondition> waiters = new ArrayList<>();
    
    waiters.add(new WaitCondition() {
      @Override
      public String getDescription() {
        return null;
      }
    });
return waiters;
  }
}
