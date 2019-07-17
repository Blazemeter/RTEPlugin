package com.blazemeter.jmeter.rte.core.listeners;

import static org.assertj.core.api.Assertions.assertThat;

import com.blazemeter.jmeter.rte.core.RteProtocolClient;
import com.blazemeter.jmeter.rte.core.listener.RequestListener;
import org.apache.jmeter.samplers.SampleResult;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class RequestListenerIT {

  private SampleResult result;
  private RequestListener listener;

  @Mock
  private RteProtocolClient client;

  @Before
  public void setup() {
    result = new SampleResult();
    result.sampleStart();
    listener = new RequestListener<>(result, client);
  }

  @Test
  public void shouldReturnGreaterLatencyThanTheElapsedTime() throws Exception {
    Thread.sleep(500);
    listener.onTerminalStateChange();
    listener.stop();
    assertThat(result.getLatency()).isGreaterThanOrEqualTo(500);
  }

  @Test
  public void shouldReturnGreaterEndTimeThanTheStartTime() throws Exception {
    long startTime = System.currentTimeMillis();
    /*This loop was included to simulate multiple screens sent by the server.
    The end time must be the time where the last screen came.*/
    for (int i = 0; i < 3; i++) {
      Thread.sleep(100);
      listener.onTerminalStateChange();
    }
    listener.stop();
    assertThat(result.getEndTime()).isGreaterThanOrEqualTo(startTime + 300);
  }

}
