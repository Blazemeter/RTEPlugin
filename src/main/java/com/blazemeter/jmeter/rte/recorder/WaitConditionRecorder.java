package com.blazemeter.jmeter.rte.recorder;

import com.blazemeter.jmeter.rte.core.wait.WaitCondition;

import java.util.Date;

public abstract class WaitConditionRecorder extends WaitConditionsRecorder {
private Date startTime;

  protected Date lastStatusChangeTime;
  private long maxStablePeriod;
  private long timeOutThreshold;


private void start(){
  startTime=new Date();
}

protected void onTerminalStatusChange(){
  Date now = new Date();
  System.currentTimeMillis();
  if (now.getTime()-lastStatusChangeTime.getTime()>maxStablePeriod){
    maxStablePeriod= now.getTime()-lastStatusChangeTime.getTime();
  }
  else{
    lastStatusChangeTime=now;
  }
}

public Date getLastStatusChange() { 
  
}

  public WaitCondition buildWaitCondition(){

    return new WaitCondition(timeOutThreshold, maxStablePeriod) {
      @Override
      public String getDescription() {
        return null;
      }
    };

  }



  public void setStartTime(Date startTime) {
    this.startTime = startTime;
  }

  public void setLastStatusChangeTime(Date lastStatusChangeTime) {
    this.lastStatusChangeTime = lastStatusChangeTime;
  }

  public void setMaxStablePeriod(int maxStablePeriod) {
    this.maxStablePeriod = maxStablePeriod;
  }

  public void setTimeOutThreshold(long timeOutThreshold) {
    this.timeOutThreshold = timeOutThreshold;
  }

  public Date getStartTime() {
    return startTime;
  }

  public Date getLastStatusChangeTime() {
    return lastStatusChangeTime;
  }

  public long getMaxStablePeriod() {
    return maxStablePeriod;
  }

  public long getTimeOutThreshold() {
    return timeOutThreshold;
  }
}
