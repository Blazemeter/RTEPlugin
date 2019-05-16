package com.blazemeter.jmeter.rte.waitsRecorder;

import com.blazemeter.jmeter.rte.core.RteProtocolClient;
import com.blazemeter.jmeter.rte.recorder.wait.SyncWaitRecorder;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.mockito.Mockito.when;

public class SyncWaitConditionIT extends WaitConditionRecorderIT {

  private long stablePeriodMillis;
  private long timeoutThresholdMillis;
  private SyncWaitRecorder syncWaitRecorder;
  
  @Mock
  private RteProtocolClient rteProtocolClientMock;
  
  @Before
  public void setup(){
    stablePeriodMillis = 1000;
    timeoutThresholdMillis = 10000;
    syncWaitRecorder = new SyncWaitRecorder(rteProtocolClientMock, timeoutThresholdMillis, stablePeriodMillis, stablePeriodMillis);
    when(rteProtocolClientMock.isInputInhibited()).thenReturn(true, false);
  }
  
}
