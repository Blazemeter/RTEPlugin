package com.blazemeter.jmeter.rte.protocols.tn3270;

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
import com.blazemeter.jmeter.rte.protocols.tn3270.Tn3270TerminalType.DeviceModel;
import com.blazemeter.jmeter.rte.protocols.tn3270.listeners.ScreenTextListener;
import com.blazemeter.jmeter.rte.protocols.tn3270.listeners.SilenceListener;
import com.blazemeter.jmeter.rte.protocols.tn3270.listeners.Tn3270RequestListener;
import com.blazemeter.jmeter.rte.protocols.tn3270.listeners.UnlockListener;
import com.blazemeter.jmeter.rte.protocols.tn3270.listeners.VisibleCursorListener;
import com.bytezone.dm3270.TerminalClient;
import com.bytezone.dm3270.application.KeyboardStatusListener;
import com.bytezone.dm3270.commands.AIDCommand;
import com.bytezone.dm3270.display.CursorMoveListener;
import com.bytezone.dm3270.display.ScreenChangeListener;
import com.bytezone.dm3270.display.ScreenDimensions;

import java.awt.Dimension;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;

import org.apache.jmeter.samplers.SampleResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Tn3270Client extends BaseProtocolClient {

  private static final Logger LOG = LoggerFactory.getLogger(Tn3270Client.class);

  private static final List<TerminalType> TERMINAL_TYPES = Arrays.asList(
      new Tn3270TerminalType(DeviceModel.M2, false),
      new Tn3270TerminalType(DeviceModel.M2, true),
      new Tn3270TerminalType(DeviceModel.M3, false),
      new Tn3270TerminalType(DeviceModel.M3, true),
      new Tn3270TerminalType(DeviceModel.M4, false),
      new Tn3270TerminalType(DeviceModel.M4, true),
      new Tn3270TerminalType(DeviceModel.M5, false),
      new Tn3270TerminalType(DeviceModel.M5, true)
  );

  private static final Map<AttentionKey, Byte> AID_COMMANDS = new EnumMap<AttentionKey, Byte>(
      AttentionKey.class) {
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

  private TerminalClient client;

  @Override
  public List<TerminalType> getSupportedTerminalTypes() {
    return TERMINAL_TYPES;
  }

  @Override
  public void connect(String server, int port, SSLType sslType, TerminalType terminalType,
      long timeoutMillis, long stableTimeout)
      throws InterruptedException, TimeoutException, RteIOException {
    stableTimeoutExecutor = Executors.newSingleThreadScheduledExecutor();
    client = new TerminalClient();
    Tn3270TerminalType termType = (Tn3270TerminalType) terminalType;
    client.setModel(termType.getModel());
    client.setScreenDimensions(termType.getScreenDimensions());
    client.setUsesExtended3270(termType.isExtended());
    exceptionHandler = new ExceptionHandler();
    client.setExceptionHandler(new com.bytezone.dm3270.ExceptionHandler() {

      @Override
      public void onException(Exception e) {
        exceptionHandler.setPendingError(e);
      }

      @Override
      public void onConnectionClosed() {
        exceptionHandler.setPendingError(new ConnectionClosedException());
      }
    });
    client.setConnectionTimeoutMillis((int) timeoutMillis);
    client.setSocketFactory(getSocketFactory(sslType));
    client.connect(server, port);
    ConditionWaiter unlock = buildWaiter(new SyncWaitCondition(timeoutMillis, stableTimeout));
    try {
      unlock.await();
    } catch (TimeoutException | InterruptedException | RteIOException e) {
      doDisconnect();
      throw e;
    } finally {
      unlock.stop();
    }
  }

  @Override
  public RequestListener buildRequestListener(SampleResult result) {
    return new Tn3270RequestListener(result, this);
  }

  private void setFieldByCoord(CoordInput i) {
    try {
      Position pos = i.getPosition();
      client.setFieldTextByCoord(pos.getRow(), pos.getColumn(), i.getInput());
    } catch (IllegalArgumentException e) {
      Position pos = i.getPosition();
      throw new InvalidFieldPositionException(pos, e);
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
  public String getScreen() {
    return client.getScreenText();
  }

  @Override
  public Dimension getScreenSize() {
    ScreenDimensions dimensions = client.getScreenDimensions();
    return new Dimension(dimensions.columns, dimensions.rows);
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
