package com.blazemeter.jmeter.rte.protocols.tn5250.listeners;

import com.blazemeter.jmeter.rte.core.listener.ExceptionHandler;
import com.blazemeter.jmeter.rte.core.wait.ConditionWaiter;
import com.blazemeter.jmeter.rte.core.wait.WaitCondition;
import com.blazemeter.jmeter.rte.protocols.tn5250.Tn5250Client;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;
import net.infordata.em.tn5250.XI5250EmulatorEvent;
import net.infordata.em.tn5250.XI5250EmulatorListener;

/**
 * An {@link XI5250EmulatorListener} which allows waiting for certain condition, and keeps in such
 * state for a given period of time.
 */
public abstract class Tn5250ConditionWaiter<T extends WaitCondition> extends
    ConditionWaiter<T> implements XI5250EmulatorListener {

  protected final Tn5250Client client;

  public Tn5250ConditionWaiter(T condition, Tn5250Client client,
      ScheduledExecutorService stableTimeoutExecutor, ExceptionHandler exceptionHandler) {
    super(condition, stableTimeoutExecutor, exceptionHandler);
    client.addEmulatorListener(this);
    this.client = client;
    initialVerificationOfCondition();
  }

  protected static List<String> getEventNames() {
    return Arrays.stream(XI5250EmulatorEvent.class.getDeclaredFields())
        .filter(f -> Modifier.isStatic(f.getModifiers()) && Modifier.isPublic(f.getModifiers()))
        .map(Field::getName)
        .collect(Collectors.toList());
  }

  @Override
  public void connecting(XI5250EmulatorEvent event) {
  }

  @Override
  public void connected(XI5250EmulatorEvent event) {
  }

  @Override
  public void disconnected(XI5250EmulatorEvent event) {
  }

  @Override
  public void stateChanged(XI5250EmulatorEvent event) {
  }

  @Override
  public void newPanelReceived(XI5250EmulatorEvent event) {
  }

  @Override
  public void fieldsRemoved(XI5250EmulatorEvent event) {
  }

  @Override
  public void dataSended(XI5250EmulatorEvent event) {
  }

  @Override
  public void stop() {
    super.stop();
    client.removeEmulatorListener(this);
  }
}
