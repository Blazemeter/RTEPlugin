package com.blazemeter.jmeter.rte.protocols.tn5250;

import com.blazemeter.jmeter.rte.core.AttentionKey;
import com.blazemeter.jmeter.rte.core.BaseProtocolClient;
import com.blazemeter.jmeter.rte.core.CoordInput;
import com.blazemeter.jmeter.rte.core.ExceptionHandler;
import com.blazemeter.jmeter.rte.core.InvalidFieldPositionException;
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
import com.blazemeter.jmeter.rte.sampler.RTESampler;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeoutException;
import net.infordata.em.crt5250.XI5250Field;
import net.infordata.em.tn5250.XI5250Emulator;
import net.infordata.em.tn5250.XI5250EmulatorListener;
import org.apache.jmeter.samplers.SampleResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Tn5250Client extends BaseProtocolClient {

  private static final Logger LOG = LoggerFactory.getLogger(RTESampler.class);

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
          put(AttentionKey.F13, new KeyEventMap(0, KeyEvent.VK_F13));
          put(AttentionKey.F14, new KeyEventMap(0, KeyEvent.VK_F14));
          put(AttentionKey.F15, new KeyEventMap(0, KeyEvent.VK_F15));
          put(AttentionKey.F16, new KeyEventMap(0, KeyEvent.VK_F16));
          put(AttentionKey.F17, new KeyEventMap(0, KeyEvent.VK_F17));
          put(AttentionKey.F18, new KeyEventMap(0, KeyEvent.VK_F18));
          put(AttentionKey.F19, new KeyEventMap(0, KeyEvent.VK_F19));
          put(AttentionKey.F20, new KeyEventMap(0, KeyEvent.VK_F20));
          put(AttentionKey.F21, new KeyEventMap(0, KeyEvent.VK_F21));
          put(AttentionKey.F22, new KeyEventMap(0, KeyEvent.VK_F22));
          put(AttentionKey.F23, new KeyEventMap(0, KeyEvent.VK_F23));
          put(AttentionKey.F24, new KeyEventMap(0, KeyEvent.VK_F24));
          put(AttentionKey.ENTER, new KeyEventMap(0, KeyEvent.VK_ENTER));
          put(AttentionKey.ATTN, new KeyEventMap(0, KeyEvent.VK_ESCAPE));
          put(AttentionKey.CLEAR, new KeyEventMap(0, KeyEvent.VK_PAUSE));
          put(AttentionKey.SYSRQ, new KeyEventMap(KeyEvent.SHIFT_MASK, KeyEvent.VK_ESCAPE));
          put(AttentionKey.RESET, new KeyEventMap(KeyEvent.CTRL_MASK, KeyEvent.VK_CONTROL));
          put(AttentionKey.ROLL_UP, new KeyEventMap(KeyEvent.CTRL_MASK, KeyEvent.VK_PAGE_UP));
          put(AttentionKey.ROLL_DN, new KeyEventMap(KeyEvent.CTRL_MASK, KeyEvent.VK_PAGE_DOWN));
        }
      };

  private ExtendedEmulator em;
  private ScheduledExecutorService stableTimeoutExecutor;

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
    exceptionHandler = new ExceptionHandler();
    /*
     we need to do this on connect to avoid leaving keyboard thread running when instance of client
     is created for getting supported terminal types in jmeter
    */
    em = new ExtendedEmulator(exceptionHandler);
    em.setHost(server);
    em.setPort(port);
    em.setConnectionTimeoutMillis((int) timeoutMillis);
    em.setTerminalType(terminalType.getId());
    em.setSslType(sslType);
    ConditionWaiter unlock = buildWaiter(new SyncWaitCondition(timeoutMillis, stableTimeoutMillis));
    try {
      em.setActive(true);
      unlock.await();
    } catch (TimeoutException | InterruptedException | RteIOException e) {
      doDisconnect();
      throw e;
    } finally {
      unlock.stop();
    }
  }

  @Override
  protected void setField(CoordInput s) {
    /*
    The values for row and column in getFieldFromPos are zero-indexed so we need to translate the
    core input values which are one-indexed.
   */
    int column = s.getPosition().getColumn() - 1;
    int row = s.getPosition().getRow() - 1;
    XI5250Field field = em.getFieldFromPos(column, row);
    if (field == null) {
      throw new InvalidFieldPositionException(s.getPosition());
    }
    field.setString(s.getInput());
    em.setCursorPos((column + s.getInput().length()) % getScreenSize().width,
        row + (column + s.getInput().length()) / getScreenSize().width);
  }

  @Override
  protected void sendAttentionKey(AttentionKey attentionKey) {
    KeyEvent keyEvent = new KeyEvent(em, KeyEvent.KEY_PRESSED, 0,
        getKeyEvent(attentionKey).modifier, getKeyEvent(attentionKey).specialKey,
        KeyEvent.CHAR_UNDEFINED);
    em.processRawKeyEvent(keyEvent);
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
          stableTimeoutExecutor, em, exceptionHandler);
    } else if (waitCondition instanceof CursorWaitCondition) {
      condition = new VisibleCursorListener((CursorWaitCondition) waitCondition,
          this, stableTimeoutExecutor, em, exceptionHandler);
    } else if (waitCondition instanceof SilentWaitCondition) {
      condition = new SilenceListener((SilentWaitCondition) waitCondition,
          this, stableTimeoutExecutor, em, exceptionHandler);
    } else if (waitCondition instanceof TextWaitCondition) {
      condition = new ScreenTextListener((TextWaitCondition) waitCondition, this,
          stableTimeoutExecutor, em, exceptionHandler);
    } else {
      throw new UnsupportedOperationException(
          "We still don't support " + waitCondition.getClass().getName() + " waiters");
    }
    em.addEmulatorListener(condition);
    exceptionHandler.addListener(condition);
    return condition;
  }

  @Override
  public RequestListener buildRequestListener(SampleResult result) {
    Tn5250RequestListener listener = new Tn5250RequestListener(result, this);
    em.addEmulatorListener(listener);
    return listener;
  }

  @Override
  public String getScreen() {
    int height = em.getCrtSize().height;
    int width = em.getCrtSize().width;
    StringBuilder screen = new StringBuilder();
    for (int i = 0; i < height; i++) {
      screen.append(em.getString(0, i, width).replaceAll("[\\x00-\\x19]", " "));
      screen.append("\n");
    }
    return screen.toString();
  }

  @Override
  public Dimension getScreenSize() {
    return em.getCrtSize();
  }

  @Override
  public boolean isInputInhibited() {
    int state = em.getState();
    switch (state) {
      case XI5250Emulator.ST_NULL:
      case XI5250Emulator.ST_TEMPORARY_LOCK:
      case XI5250Emulator.ST_NORMAL_LOCKED:
      case XI5250Emulator.ST_POWER_ON:
      case XI5250Emulator.ST_POWERED:
        return true;
      case XI5250Emulator.ST_HARDWARE_ERROR:
      case XI5250Emulator.ST_POST_HELP:
      case XI5250Emulator.ST_PRE_HELP:
      case XI5250Emulator.ST_SS_MESSAGE:
      case XI5250Emulator.ST_SYSTEM_REQUEST:
      case XI5250Emulator.ST_NORMAL_UNLOCKED:
        return false;
      default:
        LOG.debug("Unexpected state: {}", state);
        return false;
    }
  }

  @Override
  public Position getCursorPosition() {
    return em.isCursorVisible() ? new Position(em.getCursorRow() + 1, em.getCursorCol() + 1) : null;
  }

  @Override
  public boolean getSoundAlarm() {
    return false;
  }

  @Override
  public void disconnect() throws RteIOException {
    if (stableTimeoutExecutor == null) {
      return;
    }
    doDisconnect();
    exceptionHandler.throwAnyPendingError();
  }

  private void doDisconnect() {
    stableTimeoutExecutor.shutdownNow();
    stableTimeoutExecutor = null;
    em.setActive(false);
  }

  public void removeListener(XI5250EmulatorListener listener) {
    em.removeEmulatorListener(listener);
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
