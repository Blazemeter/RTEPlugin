package com.blazemeter.jmeter.rte.protocols.tn5250.listeners;

import com.blazemeter.jmeter.rte.core.listener.ExceptionHandler;
import com.blazemeter.jmeter.rte.core.wait.TextWaitCondition;
import com.blazemeter.jmeter.rte.protocols.tn5250.Tn5250Client;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import net.infordata.em.tn5250.XI5250EmulatorEvent;

public class ScreenTextListener extends Tn5250ConditionWaiter<TextWaitCondition> {

  private static final List<String> EVENT_NAMES = getEventNames();

  public ScreenTextListener(TextWaitCondition condition, Tn5250Client client,
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
    return condition.matchesScreen(client.getScreen());
  }

  private void handleReceivedEvent(XI5250EmulatorEvent event) {
    updateConditionState(EVENT_NAMES.get(event.getID()));
  }
}
