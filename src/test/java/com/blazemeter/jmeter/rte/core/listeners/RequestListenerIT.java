package com.blazemeter.jmeter.rte.core.listeners;

import static org.assertj.core.api.Assertions.assertThat;

import com.blazemeter.jmeter.rte.core.RteProtocolClient;
import com.blazemeter.jmeter.rte.core.RteSampleResultBuilder;
import com.blazemeter.jmeter.rte.core.listener.RequestListener;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class RequestListenerIT {

  private RteSampleResultBuilder resultBuilder;
  private RequestListener listener;

  @Mock
  private RteProtocolClient client;

  @Before
  public void setup() {
    resultBuilder = new RteSampleResultBuilder();
    listener = new RequestListener<>(resultBuilder, client);
  }

  @Test
  public void shouldReturnGreaterLatencyThanTheElapsedTime() throws Exception {
    Thread.sleep(500);
    listener.onTerminalStateChange();
    listener.stop();
    assertThat(resultBuilder.build().getLatency()).isGreaterThanOrEqualTo(500);
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
    assertThat(resultBuilder.build().getEndTime()).isGreaterThanOrEqualTo(startTime + 300);
  }

}
