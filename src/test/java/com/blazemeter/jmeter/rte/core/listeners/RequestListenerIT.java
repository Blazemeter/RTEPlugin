package com.blazemeter.jmeter.rte.core.listeners;

import static org.assertj.core.api.Assertions.assertThat;

import com.blazemeter.jmeter.rte.core.listener.RequestListener;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public abstract class RequestListenerIT<T extends RequestListener<?>> {

  protected T listener;

  @Before
  public void setup() throws Exception {
    listener = buildRequestListener();
  }

  protected abstract void eventGenerator();

  public abstract T buildRequestListener();

  @Test
  public void shouldReturnGreaterLatencyThanTheElapsedTime() throws Exception {
    Thread.sleep(500);
    eventGenerator();
    assertThat(listener.getLatency()).isGreaterThanOrEqualTo(500);
  }

  @Test
  public void shouldReturnGreaterEndTimeThanTheStartTime() throws Exception {
    long startTime = System.currentTimeMillis();
    /*This loop was included to simulate multiple screens sent by the server.
    The end time must be the time where the last screen came.*/
    for (int i = 0; i < 3; i++) {
      Thread.sleep(100);
      eventGenerator();
    }
    assertThat(listener.getEndTime()).isGreaterThanOrEqualTo(startTime + 300);
  }

  @After
  public void teardown() {
    listener.stop();
  }
}
