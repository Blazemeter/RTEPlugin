package com.blazemeter.jmeter.rte.protocols.tn5250.listeners;

import com.blazemeter.jmeter.rte.core.listener.ExceptionHandler;
import com.blazemeter.jmeter.rte.core.wait.TextWaitCondition;
import com.blazemeter.jmeter.rte.protocols.tn5250.Tn5250Client;
import java.util.concurrent.ScheduledExecutorService;
import net.infordata.em.tn5250.XI5250EmulatorEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScreenTextListener extends Tn5250ConditionWaiter<TextWaitCondition> {

  private static final Logger LOG = LoggerFactory.getLogger(ScreenTextListener.class);

  public ScreenTextListener(TextWaitCondition condition, Tn5250Client client,
      ScheduledExecutorService stableTimeoutExecutor, ExceptionHandler exceptionHandler) {
    super(condition, client, stableTimeoutExecutor, exceptionHandler);
    if (getCurrentConditionState()) {
      startStablePeriod();
    }
  }

  @Override
  public void connecting(XI5250EmulatorEvent event) {
    validateCondition();
  }

  @Override
  public void connected(XI5250EmulatorEvent event) {
    validateCondition();
  }

  @Override
  public void disconnected(XI5250EmulatorEvent event) {
    validateCondition();
  }

  @Override
  public void stateChanged(XI5250EmulatorEvent event) {
    validateCondition();
  }

  @Override
  public void newPanelReceived(XI5250EmulatorEvent event) {
    validateCondition();
  }

  @Override
  public void fieldsRemoved(XI5250EmulatorEvent event) {
    validateCondition();
  }

  @Override
  public void dataSended(XI5250EmulatorEvent event) {
    validateCondition();
  }

  @Override
  protected boolean getCurrentConditionState() {
    return condition.matchesScreen(client.getScreen());
  }
}
