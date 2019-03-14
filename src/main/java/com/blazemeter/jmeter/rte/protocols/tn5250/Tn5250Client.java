package com.blazemeter.jmeter.rte.protocols.tn5250;

import com.blazemeter.jmeter.rte.core.AttentionKey;
import com.blazemeter.jmeter.rte.core.BaseProtocolClient;
import com.blazemeter.jmeter.rte.core.ConnectionClosedException;
import com.blazemeter.jmeter.rte.core.CoordInput;
import com.blazemeter.jmeter.rte.core.ExceptionHandler;
import com.blazemeter.jmeter.rte.core.Input;
import com.blazemeter.jmeter.rte.core.InvalidFieldLabelException;
import com.blazemeter.jmeter.rte.core.InvalidFieldPositionException;
import com.blazemeter.jmeter.rte.core.LabelInput;
import com.blazemeter.jmeter.rte.core.Position;
import com.blazemeter.jmeter.rte.core.RteIOException;
import com.blazemeter.jmeter.rte.core.TerminalType;
import com.blazemeter.jmeter.rte.core.listener.ConditionWaiter;
import com.blazemeter.jmeter.rte.core.listener.RequestListener;
import com.blazemeter.jmeter.rte.core.ssl.SSLType;
import com.blazemeter.jmeter.rte.core.wait.CursorWaitCondition;
import com.blazemeter.jmeter.rte.core.wait.SilentWaitCondition;
import com.blazemeter.jmeter.rte.core.wait.SyncWaitCondition;
import com.blazemeter.jmeter.rte.core.wait.TextWaitCondition;
import com.blazemeter.jmeter.rte.core.wait.WaitCondition;
import com.blazemeter.jmeter.rte.protocols.tn5250.listeners.ScreenTextListener;
import com.blazemeter.jmeter.rte.protocols.tn5250.listeners.SilenceListener;
import com.blazemeter.jmeter.rte.protocols.tn5250.listeners.Tn5250ConditionWaiter;
import com.blazemeter.jmeter.rte.protocols.tn5250.listeners.Tn5250RequestListener;
import com.blazemeter.jmeter.rte.protocols.tn5250.listeners.UnlockListener;
import com.blazemeter.jmeter.rte.protocols.tn5250.listeners.VisibleCursorListener;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;
import net.infordata.em.TerminalClient;
import net.infordata.em.tn5250.XI5250EmulatorListener;
import org.apache.jmeter.samplers.SampleResult;

public class Tn5250Client extends BaseProtocolClient {

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

  @Override
  public List<TerminalType> getSupportedTerminalTypes() {
    return TERMINAL_TYPES;
  }

  @Override
  public void connect(String server, int port, SSLType sslType,
      TerminalType terminalType, long timeoutMillis,
      long stableTimeoutMillis)
      throws RteIOException, InterruptedException, TimeoutException {
    stableTimeoutExecutor = Executors.newSingleThreadScheduledExecutor();
    /*
     we need to do this on connect to avoid leaving keyboard thread running when instance of client
     is created for getting supported terminal types in jmeter
    */
    client = new TerminalClient();
    exceptionHandler = new ExceptionHandler();
    client.setExceptionHandler(new net.infordata.em.ExceptionHandler() {

      @Override
      public void onException(Throwable e) {
        exceptionHandler.setPendingError(e);
      }

      @Override
      public void onConnectionClosed() {
        exceptionHandler.setPendingError(new ConnectionClosedException());
      }
    });
    client.setConnectionTimeoutMillis((int) timeoutMillis);
    client.setTerminalType(terminalType.getId());
    client.setSocketFactory(getSocketFactory(sslType));
    ConditionWaiter unlock = buildWaiter(new SyncWaitCondition(timeoutMillis, stableTimeoutMillis));
    try {
      client.connect(server, port);
      unlock.await();
    } catch (TimeoutException | InterruptedException | RteIOException e) {
      doDisconnect();
      throw e;
    } finally {
      unlock.stop();
    }
  }

  @Override
  protected void setField(Input i) {
    if (i instanceof CoordInput) {
      setFieldByCoord((CoordInput) i);
    } else if (i instanceof LabelInput) {
      setFieldByLabel((LabelInput) i);
    } else {
      throw new IllegalArgumentException("Invalid input type: " + i.getClass());
    }
  }

  private void setFieldByCoord(CoordInput i) {
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
  protected ConditionWaiter buildWaiter(WaitCondition waitCondition) {
    Tn5250ConditionWaiter condition;
    if (waitCondition instanceof SyncWaitCondition) {
      condition = new UnlockListener((SyncWaitCondition) waitCondition, this,
          stableTimeoutExecutor, exceptionHandler);
    } else if (waitCondition instanceof CursorWaitCondition) {
      condition = new VisibleCursorListener((CursorWaitCondition) waitCondition,
          this, stableTimeoutExecutor, exceptionHandler);
    } else if (waitCondition instanceof SilentWaitCondition) {
      condition = new SilenceListener((SilentWaitCondition) waitCondition,
          this, stableTimeoutExecutor, exceptionHandler);
    } else if (waitCondition instanceof TextWaitCondition) {
      condition = new ScreenTextListener((TextWaitCondition) waitCondition, this,
          stableTimeoutExecutor, exceptionHandler);
    } else {
      throw new UnsupportedOperationException(
          "We still don't support " + waitCondition.getClass().getName() + " waiters");
    }
    return condition;
  }

  @Override
  public RequestListener buildRequestListener(SampleResult result) {
    return new Tn5250RequestListener(result, this);
  }

  @Override
  public String getScreen() {
    return client.getScreenText();
  }

  @Override
  public Dimension getScreenSize() {
    return client.getScreenDimensions();
  }

  @Override
  public boolean isInputInhibited() {
    return client.isKeyboardLocked();
  }

  @Override
  public Optional<Position> getCursorPosition() {
    return client.getCursorPosition()
        .map(p -> new Position(p.y, p.x));
  }

  @Override
  public boolean getSoundAlarm() {
    return client.resetAlarm();
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
