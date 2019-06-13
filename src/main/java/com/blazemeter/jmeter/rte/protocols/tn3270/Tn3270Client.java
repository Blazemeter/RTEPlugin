package com.blazemeter.jmeter.rte.protocols.tn3270;

import com.blazemeter.jmeter.rte.core.AttentionKey;
import com.blazemeter.jmeter.rte.core.BaseProtocolClient;
import com.blazemeter.jmeter.rte.core.CoordInput;
import com.blazemeter.jmeter.rte.core.Input;
import com.blazemeter.jmeter.rte.core.LabelInput;
import com.blazemeter.jmeter.rte.core.Position;
import com.blazemeter.jmeter.rte.core.Screen;
import com.blazemeter.jmeter.rte.core.TerminalType;
import com.blazemeter.jmeter.rte.core.exceptions.ConnectionClosedException;
import com.blazemeter.jmeter.rte.core.exceptions.InvalidFieldLabelException;
import com.blazemeter.jmeter.rte.core.exceptions.InvalidFieldPositionException;
import com.blazemeter.jmeter.rte.core.exceptions.RteIOException;
import com.blazemeter.jmeter.rte.core.listener.ExceptionHandler;
import com.blazemeter.jmeter.rte.core.listener.TerminalStateListener;
import com.blazemeter.jmeter.rte.core.ssl.SSLType;
import com.blazemeter.jmeter.rte.core.wait.ConditionWaiter;
import com.blazemeter.jmeter.rte.core.wait.ConnectionEndWaiter;
import com.blazemeter.jmeter.rte.core.wait.CursorWaitCondition;
import com.blazemeter.jmeter.rte.core.wait.SilentWaitCondition;
import com.blazemeter.jmeter.rte.core.wait.SyncWaitCondition;
import com.blazemeter.jmeter.rte.core.wait.TextWaitCondition;
import com.blazemeter.jmeter.rte.core.wait.WaitCondition;
import com.blazemeter.jmeter.rte.protocols.tn3270.Tn3270TerminalType.DeviceModel;
import com.blazemeter.jmeter.rte.protocols.tn3270.listeners.ScreenTextListener;
import com.blazemeter.jmeter.rte.protocols.tn3270.listeners.SilenceListener;
import com.blazemeter.jmeter.rte.protocols.tn3270.listeners.Tn3270TerminalStateListenerProxy;
import com.blazemeter.jmeter.rte.protocols.tn3270.listeners.UnlockListener;
import com.blazemeter.jmeter.rte.protocols.tn3270.listeners.VisibleCursorListener;
import com.bytezone.dm3270.TerminalClient;
import com.bytezone.dm3270.application.KeyboardStatusListener;
import com.bytezone.dm3270.commands.AIDCommand;
import com.bytezone.dm3270.display.CursorMoveListener;
import com.bytezone.dm3270.display.Field;
import com.bytezone.dm3270.display.ScreenChangeListener;
import com.bytezone.dm3270.display.ScreenDimensions;
import java.awt.Dimension;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Tn3270Client extends BaseProtocolClient {

  private static final Logger LOG = LoggerFactory.getLogger(Tn3270Client.class);

  private static final List<TerminalType> TERMINAL_TYPES = buildTerminalTypes();
  private static final Map<AttentionKey, Byte> AID_COMMANDS = buildAIdCommandsKeysMapping();

  private TerminalClient client;
  private Map<TerminalStateListener, Tn3270TerminalStateListenerProxy> listenersProxies =
      new ConcurrentHashMap<>();

  private static List<TerminalType> buildTerminalTypes() {
    return Arrays.asList(
        new Tn3270TerminalType(DeviceModel.M2, false),
        new Tn3270TerminalType(DeviceModel.M2, true),
        new Tn3270TerminalType(DeviceModel.M3, false),
        new Tn3270TerminalType(DeviceModel.M3, true),
        new Tn3270TerminalType(DeviceModel.M4, false),
        new Tn3270TerminalType(DeviceModel.M4, true),
        new Tn3270TerminalType(DeviceModel.M5, false),
        new Tn3270TerminalType(DeviceModel.M5, true)
    );
  }

  private static EnumMap<AttentionKey, Byte> buildAIdCommandsKeysMapping() {
    return new EnumMap<AttentionKey, Byte>(AttentionKey.class) {
      {
        put(AttentionKey.F1, AIDCommand.AID_PF1);
        put(AttentionKey.F2, AIDCommand.AID_PF2);
        put(AttentionKey.F3, AIDCommand.AID_PF3);
        put(AttentionKey.F4, AIDCommand.AID_PF4);
        put(AttentionKey.F5, AIDCommand.AID_PF5);
        put(AttentionKey.F6, AIDCommand.AID_PF6);
        put(AttentionKey.F7, AIDCommand.AID_PF7);
        put(AttentionKey.F8, AIDCommand.AID_PF8);
        put(AttentionKey.F9, AIDCommand.AID_PF9);
        put(AttentionKey.F10, AIDCommand.AID_PF10);
        put(AttentionKey.F11, AIDCommand.AID_PF11);
        put(AttentionKey.F12, AIDCommand.AID_PF12);
        put(AttentionKey.F13, AIDCommand.AID_PF13);
        put(AttentionKey.F14, AIDCommand.AID_PF14);
        put(AttentionKey.F15, AIDCommand.AID_PF15);
        put(AttentionKey.F16, AIDCommand.AID_PF16);
        put(AttentionKey.F17, AIDCommand.AID_PF17);
        put(AttentionKey.F18, AIDCommand.AID_PF18);
        put(AttentionKey.F19, AIDCommand.AID_PF19);
        put(AttentionKey.F20, AIDCommand.AID_PF20);
        put(AttentionKey.F21, AIDCommand.AID_PF21);
        put(AttentionKey.F22, AIDCommand.AID_PF22);
        put(AttentionKey.F23, AIDCommand.AID_PF23);
        put(AttentionKey.F24, AIDCommand.AID_PF24);
        put(AttentionKey.ENTER, AIDCommand.AID_ENTER);
        put(AttentionKey.PA1, AIDCommand.AID_PA1);
        put(AttentionKey.PA2, AIDCommand.AID_PA2);
        put(AttentionKey.PA3, AIDCommand.AID_PA3);
        put(AttentionKey.SYSRQ, AIDCommand.AID_SYSREQ);
        put(AttentionKey.CLEAR, AIDCommand.AID_CLEAR);
      }
    };
  }

  @Override
  public List<TerminalType> getSupportedTerminalTypes() {
    return TERMINAL_TYPES;
  }

  @Override
  public void connect(String server, int port, SSLType sslType, TerminalType terminalType,
      long timeoutMillis) throws RteIOException, InterruptedException, TimeoutException {
    stableTimeoutExecutor = Executors.newSingleThreadScheduledExecutor();
    Tn3270TerminalType termType = (Tn3270TerminalType) terminalType;
    client = new TerminalClient(termType.getModel(), termType.getScreenDimensions());
    client.setUsesExtended3270(termType.isExtended());
    client.setConnectionTimeoutMillis((int) timeoutMillis);
    client.setSocketFactory(getSocketFactory(sslType, server));
    ConnectionEndWaiter connectionEndWaiter = new ConnectionEndWaiter(timeoutMillis);
    exceptionHandler = new ExceptionHandler(server);
    client.setConnectionListener(new com.bytezone.dm3270.ConnectionListener() {

      @Override
      public void onConnection() {
        connectionEndWaiter.stop();
      }

      @Override
      public void onException(Exception e) {
        exceptionHandler.setPendingError(e);
        connectionEndWaiter.stop();
      }

      @Override
      public void onConnectionClosed() {
        exceptionHandler.setPendingError(new ConnectionClosedException());
      }
    });
    for (TerminalStateListener listener : listenersProxies.keySet()) {
      addListener(listener);
    }
    client.connect(server, port);
    connectionEndWaiter.await();
    exceptionHandler.throwAnyPendingError();
  }

  private void addListener(TerminalStateListener listener) {
    Tn3270TerminalStateListenerProxy listenerProxy = listenersProxies.get(listener);
    client.addScreenChangeListener(listenerProxy);
    client.addKeyboardStatusListener(listenerProxy);
    client.addCursorMoveListener(listenerProxy);
    exceptionHandler.addListener(listener);
  }

  @Override
  public void addTerminalStateListener(TerminalStateListener listener) {
    Tn3270TerminalStateListenerProxy listenerProxy = new Tn3270TerminalStateListenerProxy(listener);
    listenersProxies.put(listener, listenerProxy);
    if (client != null) {
      addListener(listener);
    }
  }

  @Override
  public void removeTerminalStateListener(TerminalStateListener listener) {
    Tn3270TerminalStateListenerProxy listenerProxy = listenersProxies.remove(listener);
    if (client != null) {
      exceptionHandler.removeListener(listener);
      client.removeScreenChangeListener(listenerProxy);
      client.removeKeyboardStatusListener(listenerProxy);
      client.removeCursorMoveListener(listenerProxy);
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

  private void setFieldByLabel(LabelInput i) {
    try {
      client.setFieldTextByLabel(i.getLabel(), i.getInput());
    } catch (IllegalArgumentException e) {
      throw new InvalidFieldLabelException(i.getLabel(), e);
    }
  }

  @Override
  protected void sendAttentionKey(AttentionKey attentionKey) {
    Byte actionCommand = AID_COMMANDS.get(attentionKey);
    if (actionCommand == null) {
      throw new UnsupportedOperationException(
          attentionKey.name() + " attentionKey is unsupported for protocol TN3270.");
    }
    client.sendAID(actionCommand, attentionKey.name());
  }

  @Override
  protected ConditionWaiter buildWaiter(WaitCondition waitCondition) {
    if (waitCondition instanceof SyncWaitCondition) {
      return new UnlockListener((SyncWaitCondition) waitCondition, this, stableTimeoutExecutor,
          exceptionHandler);
    } else if (waitCondition instanceof CursorWaitCondition) {
      return new VisibleCursorListener((CursorWaitCondition) waitCondition, this,
          stableTimeoutExecutor, exceptionHandler);
    } else if (waitCondition instanceof SilentWaitCondition) {
      return new SilenceListener((SilentWaitCondition) waitCondition, this, stableTimeoutExecutor,
          exceptionHandler);
    } else if (waitCondition instanceof TextWaitCondition) {
      return new ScreenTextListener((TextWaitCondition) waitCondition, this, stableTimeoutExecutor,
          exceptionHandler);
    } else {
      throw new UnsupportedOperationException(
          "We still don't support " + waitCondition.getClass().getName() + " waiters");
    }
  }

  @Override
  public Screen getScreen() {
    // when sscp lu data screen or screens without explicit fields
    if (client.getFields().isEmpty()) {
      return buildScreenFromText(client.getScreenText().replace("\n", ""));
    } else {
      return buildScreenFromFields(client.getFields());
    }
  }

  private Screen buildScreenFromText(String screenText) {
    Dimension size = getScreenSize();
    Screen ret = new Screen(size);
    int lastNonBlankPosition = screenText.length() - 1;
    while (lastNonBlankPosition >= 0 && screenText.charAt(lastNonBlankPosition) == ' ') {
      lastNonBlankPosition--;
    }
    int segmentEndPosition = lastNonBlankPosition + 1;
    if (segmentEndPosition <= 0) {
      ret.addField(0, screenText);
    } else if (segmentEndPosition >= screenText.length()) {
      ret.addSegment(0, screenText);
    } else {
      ret.addSegment(0, screenText.substring(0, segmentEndPosition + 1));
      ret.addField(segmentEndPosition + 1, screenText.substring(segmentEndPosition + 1));
    }
    return ret;
  }

  private Screen buildScreenFromFields(List<Field> fields) {
    Dimension size = getScreenSize();
    Screen ret = new Screen(size);
    for (Field f : fields) {
      int linealPosition =
          (f.getFirstLocation() != 0 ? f.getFirstLocation() : size.height * size.width) - 1;
      String text = f.isVisible() ? f.getText() : StringUtils.repeat(' ', f.getDisplayLength());
      if (f.isProtected()) {
        ret.addSegment(linealPosition, " " + text);
      } else {
        ret.addSegment(linealPosition, " ");
        if (linealPosition + 1 < size.height * size.width) {
          ret.addField(linealPosition + 1, text);
        }
      }
    }
    return ret;
  }

  public Dimension getScreenSize() {
    ScreenDimensions dimensions = client.getScreenDimensions();
    return new Dimension(dimensions.columns, dimensions.rows);
  }

  @Override
  public boolean isInputInhibited() {
    return client == null || client.isKeyboardLocked();
  }

  @Override
  public Optional<Position> getCursorPosition() {
    return client.getCursorPosition()
        .map(p -> new Position(p.y, p.x));
  }

  @Override
  public boolean resetAlarm() {
    return client.resetAlarm();
  }

  @Override
  public boolean isAlarmOn() {
    return client.isAlarmOn();
  }

  public void addScreenChangeListener(ScreenChangeListener listener) {
    client.addScreenChangeListener(listener);
  }

  public void removeScreenChangeListener(ScreenChangeListener listener) {
    client.removeScreenChangeListener(listener);
  }

  public void addKeyboardStatusListener(KeyboardStatusListener listener) {
    client.addKeyboardStatusListener(listener);
  }

  public void removeKeyboardStatusListener(KeyboardStatusListener listener) {
    client.removeKeyboardStatusListener(listener);
  }

  public void addCursorMoveListener(CursorMoveListener listener) {
    client.addCursorMoveListener(listener);
  }

  public void removeCursorMoveListener(CursorMoveListener listener) {
    client.removeCursorMoveListener(listener);
  }

  @Override
  protected void doDisconnect() {
    stableTimeoutExecutor.shutdownNow();
    stableTimeoutExecutor = null;
    try {
      client.disconnect();
    } catch (InterruptedException ex) {
      Thread.currentThread().interrupt();
      LOG.warn("Disconnection process was interrupted");
    }
  }

}
