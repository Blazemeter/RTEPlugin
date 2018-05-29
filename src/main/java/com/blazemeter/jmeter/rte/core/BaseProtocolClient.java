package com.blazemeter.jmeter.rte.core;

import com.blazemeter.jmeter.rte.core.listener.ConditionWaiter;
import com.blazemeter.jmeter.rte.core.wait.WaitCondition;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

public abstract class BaseProtocolClient implements RteProtocolClient {

  protected ExceptionHandler exceptionHandler;

  @Override
  public void send(List<CoordInput> input, AttentionKey attentionKey) throws RteIOException {
    input.forEach(this::setField);
    sendAttentionKey(attentionKey);
    exceptionHandler.throwAnyPendingError();
  }

  protected abstract void setField(CoordInput coordInput);

  protected abstract void sendAttentionKey(AttentionKey attentionKey);

  @Override
  public void await(List<WaitCondition> waitConditions)
      throws InterruptedException, TimeoutException, RteIOException {
    List<ConditionWaiter> listeners = waitConditions.stream()
        .map(this::buildWaiter)
        .collect(Collectors.toList());
    try {
      for (ConditionWaiter listener : listeners) {
        listener.await();
      }
    } finally {
      listeners.forEach(ConditionWaiter::stop);
    }
  }

  protected abstract ConditionWaiter buildWaiter(WaitCondition waitCondition);
}
