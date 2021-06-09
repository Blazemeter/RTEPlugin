package com.blazemeter.jmeter.rte.protocols.tn5250;

import com.blazemeter.jmeter.rte.core.AttentionKey;
import com.blazemeter.jmeter.rte.core.BaseProtocolClient;
import com.blazemeter.jmeter.rte.core.CoordInput;
import com.blazemeter.jmeter.rte.core.Input;
import com.blazemeter.jmeter.rte.core.LabelInput;
import com.blazemeter.jmeter.rte.core.NavigationInput;
import com.blazemeter.jmeter.rte.core.Position;
import com.blazemeter.jmeter.rte.core.Screen;
import com.blazemeter.jmeter.rte.core.TerminalType;
import com.blazemeter.jmeter.rte.core.exceptions.InvalidFieldLabelException;
import com.blazemeter.jmeter.rte.core.exceptions.InvalidFieldPositionException;
import com.blazemeter.jmeter.rte.core.exceptions.RteIOException;
import com.blazemeter.jmeter.rte.core.exceptions.ScreenWithoutFieldException;
import com.blazemeter.jmeter.rte.core.listener.ExceptionHandler;
import com.blazemeter.jmeter.rte.core.listener.TerminalStateListener;
import com.blazemeter.jmeter.rte.core.ssl.SSLType;
import com.blazemeter.jmeter.rte.core.wait.ConditionWaiter;
import com.blazemeter.jmeter.rte.core.wait.ConnectionEndWaiter;
import com.blazemeter.jmeter.rte.core.wait.CursorWaitCondition;
import com.blazemeter.jmeter.rte.core.wait.DisconnectWaitCondition;
import com.blazemeter.jmeter.rte.core.wait.SilentWaitCondition;
import com.blazemeter.jmeter.rte.core.wait.SyncWaitCondition;
import com.blazemeter.jmeter.rte.core.wait.TextWaitCondition;
import com.blazemeter.jmeter.rte.core.wait.WaitCondition;
import com.blazemeter.jmeter.rte.protocols.tn5250.listeners.ConnectionEndTerminalListener;
import com.blazemeter.jmeter.rte.protocols.tn5250.listeners.DisconnectListener;
import com.blazemeter.jmeter.rte.protocols.tn5250.listeners.ScreenTextListener;
import com.blazemeter.jmeter.rte.protocols.tn5250.listeners.SilenceListener;
import com.blazemeter.jmeter.rte.protocols.tn5250.listeners.Tn5250TerminalStateListenerProxy;
import com.blazemeter.jmeter.rte.protocols.tn5250.listeners.UnlockListener;
import com.blazemeter.jmeter.rte.protocols.tn5250.listeners.VisibleCursorListener;
import com.blazemeter.jmeter.rte.sampler.NavigationType;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;
import javax.naming.OperationNotSupportedException;
import net.infordata.em.TerminalClient;
import net.infordata.em.crt5250.XI5250Field;
import net.infordata.em.tn5250.XI5250EmulatorListener;

public class Tn5250Client extends BaseProtocolClient {

  private static final int SECRET_FIELD_MASK = 0x01;
  private static final List<TerminalType> TERMINAL_TYPES = Arrays.asList(
      new TerminalType("IBM-3179-2", new Dimension(80, 24)),
      new TerminalType("IBM-3477-FC", new Dimension(132, 27))
  );
  private static final Map<AttentionKey, KeyEventMap> KEY_EVENTS =
      new EnumMap<AttentionKey, KeyEventMap>(
          AttentionKey.class) {
        {
          put(AttentionKey.F1, new KeyEventMap(0, KeyEvent.VK_F1));
          put(AttentionKey.F2, new KeyEventMap(0, KeyEvent.VK_F2));
          put(AttentionKey.F3, new KeyEventMap(0, KeyEvent.VK_F3));
          put(AttentionKey.F4, new KeyEventMap(0, KeyEvent.VK_F4));
          put(AttentionKey.F5, new KeyEventMap(0, KeyEvent.VK_F5));
          put(AttentionKey.F6, new KeyEventMap(0, KeyEvent.VK_F6));
          put(AttentionKey.F7, new KeyEventMap(0, KeyEvent.VK_F7));
          put(AttentionKey.F8, new KeyEventMap(0, KeyEvent.VK_F8));
          put(AttentionKey.F9, new KeyEventMap(0, KeyEvent.VK_F9));
          put(AttentionKey.F10, new KeyEventMap(0, KeyEvent.VK_F10));
          put(AttentionKey.F11, new KeyEventMap(0, KeyEvent.VK_F11));
          put(AttentionKey.F12, new KeyEventMap(0, KeyEvent.VK_F12));
          put(AttentionKey.F13, new KeyEventMap(KeyEvent.SHIFT_MASK, KeyEvent.VK_F1));
          put(AttentionKey.F14, new KeyEventMap(KeyEvent.SHIFT_MASK, KeyEvent.VK_F2));
          put(AttentionKey.F15, new KeyEventMap(KeyEvent.SHIFT_MASK, KeyEvent.VK_F3));
          put(AttentionKey.F16, new KeyEventMap(KeyEvent.SHIFT_MASK, KeyEvent.VK_F4));
          put(AttentionKey.F17, new KeyEventMap(KeyEvent.SHIFT_MASK, KeyEvent.VK_F5));
          put(AttentionKey.F18, new KeyEventMap(KeyEvent.SHIFT_MASK, KeyEvent.VK_F6));
          put(AttentionKey.F19, new KeyEventMap(KeyEvent.SHIFT_MASK, KeyEvent.VK_F7));
          put(AttentionKey.F20, new KeyEventMap(KeyEvent.SHIFT_MASK, KeyEvent.VK_F8));
          put(AttentionKey.F21, new KeyEventMap(KeyEvent.SHIFT_MASK, KeyEvent.VK_F9));
          put(AttentionKey.F22, new KeyEventMap(KeyEvent.SHIFT_MASK, KeyEvent.VK_F10));
          put(AttentionKey.F23, new KeyEventMap(KeyEvent.SHIFT_MASK, KeyEvent.VK_F11));
          put(AttentionKey.F24, new KeyEventMap(KeyEvent.SHIFT_MASK, KeyEvent.VK_F12));
          put(AttentionKey.ENTER, new KeyEventMap(0, KeyEvent.VK_ENTER));
          put(AttentionKey.ATTN, new KeyEventMap(0, KeyEvent.VK_ESCAPE));
          put(AttentionKey.CLEAR, new KeyEventMap(0, KeyEvent.VK_PAUSE));
          put(AttentionKey.SYSRQ, new KeyEventMap(KeyEvent.SHIFT_MASK, KeyEvent.VK_ESCAPE));
          put(AttentionKey.RESET, new KeyEventMap(KeyEvent.CTRL_MASK, KeyEvent.VK_CONTROL));
          put(AttentionKey.ROLL_UP, new KeyEventMap(0, KeyEvent.VK_PAGE_DOWN));
          put(AttentionKey.ROLL_DN, new KeyEventMap(0, KeyEvent.VK_PAGE_UP));
        }
      };

  private TerminalClient client;
  private Map<TerminalStateListener, Tn5250TerminalStateListenerProxy> listenersProxies =
      new ConcurrentHashMap<>();

  @Override
  public List<TerminalType> getSupportedTerminalTypes() {
    return TERMINAL_TYPES;
  }

  @Override
  public void connect(String server, int port, SSLType sslType, TerminalType terminalType,
      long timeoutMillis) throws RteIOException, TimeoutException, InterruptedException {
    stableTimeoutExecutor = Executors.newSingleThreadScheduledExecutor(NAMED_THREAD_FACTORY);
    /*
     we need create terminalClient instance on connect instead of 
     constructor to avoid leaving keyboard thread running when 
     instance of this class is created for getting supported terminal types in JMeter
    */
    client = new TerminalClient();
    client.setConnectionTimeoutMillis((int) timeoutMillis);
    client.setTerminalType(terminalType.getId());
    client.setSocketFactory(getSocketFactory(sslType, server));
    exceptionHandler = new ExceptionHandler(server);
    ConnectionEndWaiter connectionEndWaiter = new ConnectionEndWaiter(timeoutMillis);
    client.setExceptionHandler(new net.infordata.em.ExceptionHandler() {

      @Override
      public void onException(Throwable e) {
        exceptionHandler.setPendingError(e);
        connectionEndWaiter.stop();
      }

      @Override
      public void onConnectionClosed() {
        handleServerDisconnection();
      }
    });
    for (TerminalStateListener listener : listenersProxies.keySet()) {
      addListener(listener);
    }
    ConnectionEndTerminalListener connectionEndListener = new ConnectionEndTerminalListener(
        connectionEndWaiter);
    client.addEmulatorListener(connectionEndListener);
    try {
      client.connect(server, port);
      connectionEndWaiter.await();
      exceptionHandler.throwAnyPendingError();
    } finally {
      client.removeEmulatorListener(connectionEndListener);
    }
  }

  @Override
  protected void setField(Input i, long echoTimeoutMillis) {
    if (i instanceof CoordInput) {
      setFieldByCoord((CoordInput) i);
    } else if (i instanceof LabelInput) {
      setFieldByLabel((LabelInput) i);
    } else if (i instanceof NavigationInput) {
      setFieldByNavigationType((NavigationInput) i);
    } else {
      throw new IllegalArgumentException("Invalid input type: " + i.getClass());
    }
  }

  private void setFieldByCoord(CoordInput i) {
    if (i.getInput().isEmpty()) {
      client.setCursorPosition(i.getPosition().getRow() - 1, i.getPosition().getColumn() - 1);
      return;
    }
    try {
      client.setFieldTextByCoord(i.getPosition().getRow(),
          i.getPosition().getColumn(), i.getInput());
    } catch (IllegalArgumentException e) {
      throw new InvalidFieldPositionException(i.getPosition(), e);
    }
  }

  private void setFieldByLabel(LabelInput i) {
    try {
      client.setFieldTextByLabel(i.getLabel(), i.getInput());
    } catch (IllegalArgumentException e) {
      throw new InvalidFieldLabelException(i.getLabel(), e);
    }
  }

  private void setFieldByNavigationType(NavigationInput i) {
    if (i.getNavigationType().equals(NavigationType.TAB)) {
      try {
        client
            .setFieldTextByTabulator(i.getRepeat(), i.getInput());
      } catch (NoSuchElementException e) {
        throw new ScreenWithoutFieldException();
      }
    } else {
      if (Arrays.asList(NavigationType.values()).contains(i.getNavigationType())) {
        Position currentPos =
            getCursorPosition().orElseThrow(() -> new NoSuchElementException("No position "
                + "available"));

        Position finalPosition = i
            .calculateInputFinalPosition(currentPos,
                new Dimension(client.getScreenDimensions()));
        client
            .setFieldTextByCoord(finalPosition.getRow(), finalPosition.getColumn(),
                i.getInput());
      } else {
        exceptionHandler
            .setPendingError(new OperationNotSupportedException(
                "The navigation type \'" + i.getNavigationType() + "\' is not supported"));
      }
    }
  }

  @Override
  protected void sendAttentionKey(AttentionKey attentionKey) {
    client.sendKeyEvent(getKeyEvent(attentionKey).specialKey, getKeyEvent(attentionKey).modifier);
  }

  private KeyEventMap getKeyEvent(AttentionKey attentionKey) {
    KeyEventMap actionCommand = KEY_EVENTS.get(attentionKey);
    if (actionCommand == null) {
      throw new UnsupportedOperationException(
          attentionKey.name() + " attentionKey is unsupported " +
              "for protocol TN5250.");
    } else {
      return actionCommand;
    }
  }

  @Override
  protected ConditionWaiter<?> buildWaiter(WaitCondition waitCondition) {
    if (waitCondition instanceof SyncWaitCondition) {
      return new UnlockListener((SyncWaitCondition) waitCondition, this,
          stableTimeoutExecutor, exceptionHandler);
    } else if (waitCondition instanceof CursorWaitCondition) {
      return new VisibleCursorListener((CursorWaitCondition) waitCondition,
          this, stableTimeoutExecutor, exceptionHandler);
    } else if (waitCondition instanceof SilentWaitCondition) {
      return new SilenceListener((SilentWaitCondition) waitCondition,
          this, stableTimeoutExecutor, exceptionHandler);
    } else if (waitCondition instanceof TextWaitCondition) {
      return new ScreenTextListener((TextWaitCondition) waitCondition, this,
          stableTimeoutExecutor, exceptionHandler);
    } else if (waitCondition instanceof DisconnectWaitCondition) {
      return new DisconnectListener((DisconnectWaitCondition) waitCondition, this,
          stableTimeoutExecutor, exceptionHandler);
    } else {
      throw new UnsupportedOperationException(
          "We still don't support " + waitCondition.getClass().getName() + " waiters");
    }
  }

  @Override
  public void addTerminalStateListener(TerminalStateListener listener) {
    Tn5250TerminalStateListenerProxy proxy = new Tn5250TerminalStateListenerProxy(listener);
    listenersProxies.put(listener, proxy);
    if (client != null) {
      addListener(listener);
    }
  }

  private void addListener(TerminalStateListener listener) {
    Tn5250TerminalStateListenerProxy listenerProxy = listenersProxies.get(listener);
    client.addEmulatorListener(listenerProxy);
    exceptionHandler.addListener(listener);
  }

  @Override
  public void removeTerminalStateListener(TerminalStateListener listener) {
    Tn5250TerminalStateListenerProxy proxy = listenersProxies.remove(listener);
    if (client != null) {
      client.removeEmulatorListener(proxy);
      exceptionHandler.removeListener(listener);
    }
  }

  @Override
  public Screen getScreen() {
    Dimension screenSize = client.getScreenDimensions();
    Screen ret = new Screen(screenSize);
    String screenText = client.getScreenText().replace("\n", "");
    int textStartPos = 0;
    for (XI5250Field f : client.getFields()) {
      int fieldLinealPosition = getFieldLinealPosition(f, screenSize);
      if (fieldLinealPosition > textStartPos) {
        ret.addSegment(textStartPos, screenText.substring(textStartPos, fieldLinealPosition)
        );
      }
      if ((SECRET_FIELD_MASK & f.getAttr()) != 0) {
        ret.addSecretField(fieldLinealPosition, Screen.replaceTrailingSpacesByNull(f.getString()));
      } else {
        ret.addField(fieldLinealPosition, Screen.replaceTrailingSpacesByNull(f.getString()));
      }
      textStartPos = fieldLinealPosition + f.getString().length();
    }
    if (textStartPos < screenText.length()) {
      ret.addSegment(textStartPos, screenText.substring(textStartPos));
    }
    return ret;
  }

  private int getFieldLinealPosition(XI5250Field field, Dimension screenSize) {
    return field.getRow() * screenSize.width + field.getCol();
  }

  @Override
  public Optional<Boolean> isInputInhibited() {
    return Optional.of(client == null || client.isKeyboardLocked());
  }

  @Override
  public Optional<Position> getCursorPosition() {
    return client.getCursorPosition()
        .map(p -> new Position(p.y, p.x));
  }

  @Override
  public boolean isAlarmOn() {
    return client.isAlarmOn();
  }

  @Override
  public boolean resetAlarm() {
    return client.resetAlarm();
  }

  @Override
  public Set<AttentionKey> getSupportedAttentionKeys() {
    return KEY_EVENTS.keySet();

  }

  public void addEmulatorListener(XI5250EmulatorListener listener) {
    client.addEmulatorListener(listener);
  }

  public void removeEmulatorListener(XI5250EmulatorListener listener) {
    client.removeEmulatorListener(listener);
  }

  @Override
  protected void doDisconnect() {
    stableTimeoutExecutor.shutdownNow();
    stableTimeoutExecutor = null;
    client.disconnect();
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
