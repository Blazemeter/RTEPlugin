package com.blazemeter.jmeter.rte.protocols.tn5250.listeners;

import com.blazemeter.jmeter.rte.core.wait.SyncWaitCondition;
import com.blazemeter.jmeter.rte.protocols.tn5250.Tn5250Client;
import java.util.concurrent.ScheduledExecutorService;
import net.infordata.em.tn5250.XI5250EmulatorEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link ConditionWaiter} which allows waiting until the terminal is unlocked.
 */
public class UnlockListener extends ConditionWaiter<SyncWaitCondition> {

  private static final Logger LOG = LoggerFactory.getLogger(UnlockListener.class);
  private boolean isInputInhibited;

  public UnlockListener(SyncWaitCondition condition, Tn5250Client client,
      ScheduledExecutorService stableTimeoutExecutor) {
    super(condition, client, stableTimeoutExecutor);
    isInputInhibited = client.isInputInhibited();
    if (!isInputInhibited) {
      startStablePeriod();
    }
  }

  @Override
  public synchronized void stateChanged(XI5250EmulatorEvent event) {
    if (client.hasPendingError()) {
      cancelWait();
      return;
    }
    boolean wasInputInhibited = isInputInhibited;
    isInputInhibited = client.isInputInhibited();
    if (isInputInhibited != wasInputInhibited) {
      if (isInputInhibited) {
        LOG.debug("Cancel stable period since input has been inhibited");
        endStablePeriod();
      } else {
        LOG.debug("Start stable period since input is no longer inhibited");
        startStablePeriod();
      }
    }
  }

}
