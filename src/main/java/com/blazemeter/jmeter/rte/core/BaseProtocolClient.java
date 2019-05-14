package com.blazemeter.jmeter.rte.core;

import com.blazemeter.jmeter.rte.core.exceptions.ConnectionClosedException;
import com.blazemeter.jmeter.rte.core.exceptions.RteIOException;
import com.blazemeter.jmeter.rte.core.listener.ConditionWaiter;
import com.blazemeter.jmeter.rte.core.listener.ExceptionHandler;
import com.blazemeter.jmeter.rte.core.ssl.SSLContextFactory;
import com.blazemeter.jmeter.rte.core.ssl.SSLType;
import com.blazemeter.jmeter.rte.core.wait.WaitCondition;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import javax.net.SocketFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BaseProtocolClient implements RteProtocolClient {

  private static final Logger LOG = LoggerFactory.getLogger(BaseProtocolClient.class);

  protected ExceptionHandler exceptionHandler;
  protected ScheduledExecutorService stableTimeoutExecutor;

  protected SocketFactory getSocketFactory(SSLType sslType) throws RteIOException {
    if (sslType != null && sslType != SSLType.NONE) {
      try {
        return SSLContextFactory.buildSSLContext(sslType).getSocketFactory();
      } catch (IOException | GeneralSecurityException e) {
        throw new RteIOException(e);
      }
    } else {
      return SocketFactory.getDefault();
    }
  }

  @Override
  public void send(List<Input> input, AttentionKey attentionKey) throws RteIOException {
    input.forEach(this::setField);
    sendAttentionKey(attentionKey);
    exceptionHandler.throwAnyPendingError();
  }

  protected abstract void setField(Input input);

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

  public void disconnect() throws RteIOException {
    if (stableTimeoutExecutor == null) {
      return;
    }
    doDisconnect();
    try {
      exceptionHandler.throwAnyPendingError();
    } catch (RteIOException e) {
      if (e.getCause() instanceof ConnectionClosedException) {
        LOG.trace("Ignoring connection closed exception when disconnecting", e);
      } else {
        throw e;
      }
    }
  }

  protected abstract void doDisconnect();

}
