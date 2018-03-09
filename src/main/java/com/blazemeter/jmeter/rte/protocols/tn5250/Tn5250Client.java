package com.blazemeter.jmeter.rte.protocols.tn5250;

import com.blazemeter.jmeter.rte.core.Action;
import com.blazemeter.jmeter.rte.core.CoordInput;
import com.blazemeter.jmeter.rte.core.RteIOException;
import com.blazemeter.jmeter.rte.core.RteProtocolClient;
import com.blazemeter.jmeter.rte.core.TerminalType;
import com.blazemeter.jmeter.rte.core.wait.SyncWaitCondition;
import com.blazemeter.jmeter.rte.core.wait.TextWaitCondition;
import com.blazemeter.jmeter.rte.core.wait.WaitCondition;
import com.blazemeter.jmeter.rte.protocols.tn5250.listeners.ConditionWaiter;
import com.blazemeter.jmeter.rte.protocols.tn5250.listeners.ScreenTextListener;
import com.blazemeter.jmeter.rte.protocols.tn5250.listeners.UnlockListener;
import com.blazemeter.jmeter.rte.protocols.tn5250.ssl.SSLData;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import net.infordata.em.crt5250.XI5250Field;

public class Tn5250Client implements RteProtocolClient {

  private static final Map<Action, KeyEventMap> KEY_EVENTS = new HashMap<Action, KeyEventMap>() {
    {
      put(Action.F1, new KeyEventMap(0, KeyEvent.VK_F1));
      put(Action.F2, new KeyEventMap(0, KeyEvent.VK_F2));
      put(Action.F3, new KeyEventMap(0, KeyEvent.VK_F3));
      put(Action.F4, new KeyEventMap(0, KeyEvent.VK_F4));
      put(Action.F5, new KeyEventMap(0, KeyEvent.VK_F5));
      put(Action.F6, new KeyEventMap(0, KeyEvent.VK_F6));
      put(Action.F7, new KeyEventMap(0, KeyEvent.VK_F7));
      put(Action.F8, new KeyEventMap(0, KeyEvent.VK_F8));
      put(Action.F9, new KeyEventMap(0, KeyEvent.VK_F9));
      put(Action.F10, new KeyEventMap(0, KeyEvent.VK_F10));
      put(Action.F11, new KeyEventMap(0, KeyEvent.VK_F11));
      put(Action.F12, new KeyEventMap(0, KeyEvent.VK_F12));
      put(Action.F13, new KeyEventMap(0, KeyEvent.VK_F13));
      put(Action.F14, new KeyEventMap(0, KeyEvent.VK_F14));
      put(Action.F15, new KeyEventMap(0, KeyEvent.VK_F15));
      put(Action.F16, new KeyEventMap(0, KeyEvent.VK_F16));
      put(Action.F17, new KeyEventMap(0, KeyEvent.VK_F17));
      put(Action.F18, new KeyEventMap(0, KeyEvent.VK_F18));
      put(Action.F19, new KeyEventMap(0, KeyEvent.VK_F19));
      put(Action.F20, new KeyEventMap(0, KeyEvent.VK_F20));
      put(Action.F21, new KeyEventMap(0, KeyEvent.VK_F21));
      put(Action.F22, new KeyEventMap(0, KeyEvent.VK_F22));
      put(Action.F23, new KeyEventMap(0, KeyEvent.VK_F23));
      put(Action.F24, new KeyEventMap(0, KeyEvent.VK_F24));
      put(Action.ENTER, new KeyEventMap(0, KeyEvent.VK_ENTER));
      put(Action.ATTN, new KeyEventMap(0, KeyEvent.VK_ESCAPE));
      put(Action.CLEAR, new KeyEventMap(0, KeyEvent.VK_PAUSE));
      put(Action.SYSRQ, new KeyEventMap(KeyEvent.SHIFT_MASK, KeyEvent.VK_ESCAPE));
      put(Action.RESET, new KeyEventMap(KeyEvent.CTRL_MASK, KeyEvent.VK_CONTROL));
      put(Action.ROLL_UP, new KeyEventMap(KeyEvent.CTRL_MASK, KeyEvent.VK_PAGE_UP));
      put(Action.ROLL_DN, new KeyEventMap(KeyEvent.CTRL_MASK, KeyEvent.VK_PAGE_DOWN));
    }
  };

  private final ExtendedEmulator em = new ExtendedEmulator();
  private ScheduledExecutorService stableTimeoutExecutor;

  @Override
  public boolean isConnected() {
    return em.isActive();
  }

  @Override
  public void connect(String server, int port, SSLData sslData,
      TerminalType terminalType, long timeoutMillis,
      long stableTimeoutMillis)
      throws RteIOException, InterruptedException, TimeoutException {
    stableTimeoutExecutor = Executors.newSingleThreadScheduledExecutor();
    em.setHost(server);
    em.setPort(port);
    em.setTerminalType(terminalType.getType());
    em.setSslData(sslData);
    UnlockListener unlock = new UnlockListener(
        new SyncWaitCondition(timeoutMillis, stableTimeoutMillis),
        stableTimeoutExecutor);
    em.addEmulatorListener(unlock);
    try {
      em.setActive(true);
      unlock.await();
    } finally {
      em.removeEmulatorListener(unlock);
      em.throwAnyPendingError();
    }
  }

  @Override
  public void send(List<CoordInput> input, Action action, List<WaitCondition> waitConditions)
      throws InterruptedException, RteIOException, TimeoutException {
    List<ConditionWaiter> listeners = buildWaitersList(waitConditions);
    listeners.forEach(em::addEmulatorListener);
    input.forEach(this::setField);
    sendActionKey(action);
    for (ConditionWaiter listener : listeners) {
      listener.await();
    }
    listeners.forEach(em::removeEmulatorListener);
    em.throwAnyPendingError();
  }

  private List<ConditionWaiter> buildWaitersList(List<WaitCondition> waiters) {
    return waiters.stream()
        .map(w -> {
          if (w instanceof SyncWaitCondition) {
            return new UnlockListener((SyncWaitCondition) w, stableTimeoutExecutor);
          } else if (w instanceof TextWaitCondition) {
            return new ScreenTextListener((TextWaitCondition) w, stableTimeoutExecutor);
          } else {
            throw new UnsupportedOperationException(
                "We still don't support " + w.getClass().getName() + " waiters");
          }
        })
        .collect(Collectors.toList());
  }

  private void setField(CoordInput s) {
    /*
    The values for row and column in getFieldFromPos are zero-indexed so we need to translate the
    core input values which are one-indexed.
   */
    XI5250Field field = em.getFieldFromPos(s.getPosition().getColumn() - 1,
        s.getPosition().getRow() - 1);
    if (field == null) {
      throw new IllegalArgumentException(
          "No field at row " + s.getPosition().getRow() + " and column " + s.getPosition()
              .getColumn());
    }
    field.setString(s.getInput());
  }

  private void sendActionKey(Action action) {
    KeyEvent keyEvent = new KeyEvent(em, KeyEvent.KEY_PRESSED, 0,
        getKeyEvent(action).modifier, getKeyEvent(action).specialKey,
        KeyEvent.CHAR_UNDEFINED);
    em.processRawKeyEvent(keyEvent);
  }

  public String getScreen() {
    int height = em.getCrtSize().height;
    int width = em.getCrtSize().width;
    StringBuilder screen = new StringBuilder();
    for (int i = 0; i < height; i++) {
      screen.append(em.getString(0, i, width));
      screen.append("\n");
    }
    return screen.toString();
  }

  @Override
  public void disconnect() throws RteIOException {
    if (stableTimeoutExecutor == null) {
      return;
    }
    stableTimeoutExecutor.shutdownNow();
    em.setActive(false);
    stableTimeoutExecutor = null;
    em.throwAnyPendingError();
  }

  @Override
  public Dimension getScreenSize() {
    return em.getCrtSize();
  }

  private KeyEventMap getKeyEvent(Action action) {
    return KEY_EVENTS.get(action);
  }

  private static class KeyEventMap {

    private final int modifier;
    private final int specialKey;

    KeyEventMap(int modifier, int specialKey) {
      this.modifier = modifier;
      this.specialKey = specialKey;
    }
  }

}
