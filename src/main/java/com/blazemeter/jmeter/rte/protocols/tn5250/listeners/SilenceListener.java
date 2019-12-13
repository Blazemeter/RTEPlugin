package com.blazemeter.jmeter.rte.protocols.tn5250.listeners;

import com.blazemeter.jmeter.rte.core.listener.ExceptionHandler;
import com.blazemeter.jmeter.rte.core.wait.SilentWaitCondition;
import com.blazemeter.jmeter.rte.protocols.tn5250.Tn5250Client;
import java.util.concurrent.ScheduledExecutorService;
import net.infordata.em.tn5250.XI5250EmulatorEvent;

/**
 * A {@link Tn5250ConditionWaiter} which allows waiting until the terminal does not receive events
 * for a given period of time.
 */
public class SilenceListener extends Tn5250ConditionWaiter<SilentWaitCondition> {

  public SilenceListener(SilentWaitCondition condition, Tn5250Client client,
      ScheduledExecutorService stableTimeoutExecutor, ExceptionHandler exceptionHandler) {
    super(condition, client, stableTimeoutExecutor, exceptionHandler);
  }

  @Override
  public void connecting(XI5250EmulatorEvent event) {
    handleReceivedEvent(event);
  }

  @Override
  public void connected(XI5250EmulatorEvent event) {
    handleReceivedEvent(event);
  }

  @Override
  public void disconnected(XI5250EmulatorEvent event) {
    handleReceivedEvent(event);
  }

  @Override
  public void stateChanged(XI5250EmulatorEvent event) {
    handleReceivedEvent(event);
  }

  @Override
  public void newPanelReceived(XI5250EmulatorEvent event) {
    handleReceivedEvent(event);
  }

  @Override
  public void fieldsRemoved(XI5250EmulatorEvent event) {
    handleReceivedEvent(event);
  }

  @Override
  public void dataSended(XI5250EmulatorEvent event) {
    handleReceivedEvent(event);
  }

  @Override
  protected boolean getCurrentConditionState() {
    return !lastConditionState;
  }

  private void handleReceivedEvent(XI5250EmulatorEvent event) {
    updateConditionState(event.toString());
  }
}
