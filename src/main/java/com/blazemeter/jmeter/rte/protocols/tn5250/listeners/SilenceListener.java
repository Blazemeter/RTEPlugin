package com.blazemeter.jmeter.rte.protocols.tn5250.listeners;

import com.blazemeter.jmeter.rte.core.wait.SilentWaitCondition;
import java.util.concurrent.ScheduledExecutorService;
import net.infordata.em.tn5250.XI5250EmulatorEvent;

public class SilenceListener extends ConditionWaiter<SilentWaitCondition> {

  public SilenceListener(SilentWaitCondition condition,
      ScheduledExecutorService stableTimeoutExecutor) {
    super(condition, stableTimeoutExecutor);
    startStablePeriod();
  }

  @Override
  public void connecting(XI5250EmulatorEvent event) {
    startStablePeriod();
  }

  @Override
  public void connected(XI5250EmulatorEvent event) {
    startStablePeriod();
  }

  @Override
  public void disconnected(XI5250EmulatorEvent event) {
    startStablePeriod();
  }

  @Override
  public void stateChanged(XI5250EmulatorEvent event) {
    if (hasPendingError(event)) {
      cancelWait();
    } else {
      startStablePeriod();
    }
  }

  @Override
  public void newPanelReceived(XI5250EmulatorEvent event) {
    startStablePeriod();
  }

  @Override
  public void fieldsRemoved(XI5250EmulatorEvent event) {
    startStablePeriod();
  }

  @Override
  public void dataSended(XI5250EmulatorEvent event) {
    startStablePeriod();
  }

}
