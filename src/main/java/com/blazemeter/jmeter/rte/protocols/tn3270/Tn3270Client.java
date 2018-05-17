package com.blazemeter.jmeter.rte.protocols.tn3270;

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
import com.blazemeter.jmeter.rte.protocols.ReflectionUtils;
import com.blazemeter.jmeter.rte.protocols.tn3270.Tn3270TerminalType.DeviceModel;
import com.blazemeter.jmeter.rte.protocols.tn3270.listeners.ScreenTextListener;
import com.blazemeter.jmeter.rte.protocols.tn3270.listeners.SilenceListener;
import com.blazemeter.jmeter.rte.protocols.tn3270.listeners.Tn3270RequestListener;
import com.blazemeter.jmeter.rte.protocols.tn3270.listeners.UnlockListener;
import com.blazemeter.jmeter.rte.protocols.tn3270.listeners.VisibleCursorListener;
import com.bytezone.dm3270.commands.AIDCommand;
import com.bytezone.dm3270.display.Cursor;
import com.bytezone.dm3270.display.Field;
import com.bytezone.dm3270.display.ScreenDimensions;
import com.bytezone.dm3270.display.ScreenPosition;
import com.bytezone.dm3270.utilities.Site;
import java.awt.Dimension;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
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

  private static final Tn3270TerminalType DEFAULT_TERMINAL_TYPE =
      (Tn3270TerminalType) TERMINAL_TYPES.get(0);

  private static final Method FIELD_POSITION_GET_CHAR_STRING_METHOD =
      ReflectionUtils.getAccessibleMethod(ScreenPosition.class, "getCharString");

  private SilentScreen screen;
  private ExtendedConsolePane consolePane;

  private ScheduledExecutorService stableTimeoutExecutor;

  @Override
  public List<TerminalType> getSupportedTerminalTypes() {
    return TERMINAL_TYPES;
  }

  @Override
  public void connect(String server, int port, SSLType sslType, TerminalType terminalType,
      long timeoutMillis, long stableTimeout)
      throws InterruptedException, TimeoutException, RteIOException {
    stableTimeoutExecutor = Executors.newSingleThreadScheduledExecutor();
    Tn3270TerminalType termType = (Tn3270TerminalType) terminalType;
    ExtendedTelnetState telnetState = new ExtendedTelnetState();
    telnetState.setDoDeviceType(termType.getModel());
    screen = new SilentScreen(DEFAULT_TERMINAL_TYPE.getScreenDimensions(),
        termType.getScreenDimensions(), telnetState);
    screen.lockKeyboard("connect");
    exceptionHandler = new ExceptionHandler();
    Site serverSite = new Site("", server, port, termType.isExtended(), termType.getModel());
    consolePane = new ExtendedConsolePane(screen, serverSite, exceptionHandler);
    consolePane.setConnectionTimeoutMillis((int) timeoutMillis);
    consolePane.setSslType(sslType);
    consolePane.connect();
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
    Tn3270RequestListener listener = new Tn3270RequestListener(result, this, screen);
    screen.getFieldManager().addScreenChangeListener(listener);
    return listener;
  }

  @Override
  public void send(List<CoordInput> input, AttentionKey attentionKey) throws RteIOException {
    input.forEach(i -> {
      int linearPosition = buildLinealPosition(i);
      Field field = screen.getFieldManager()
          .getFieldAt(linearPosition)
          .orElseThrow(() -> new InvalidFieldPositionException(i.getPosition()));
      screen.setFieldText(field, i.getInput());
      screen.getScreenCursor().moveTo(linearPosition + i.getInput().length());
    });
    Byte actionCommand = AID_COMMANDS.get(attentionKey);
    if (actionCommand == null) {
      throw new UnsupportedOperationException(
          attentionKey.name() + " attentionKey is unsupported " +
              "for protocol TN3270.");
    }
    consolePane.sendAID(actionCommand, attentionKey.name());
    exceptionHandler.throwAnyPendingError();
  }

  private int buildLinealPosition(CoordInput i) {
    return (i.getPosition().getRow() - 1) * getScreenSize().width + i.getPosition().getColumn() - 1;
  }

  @Override
  protected ConditionWaiter buildWaiter(WaitCondition waitCondition) {
    if (waitCondition instanceof SyncWaitCondition) {
      UnlockListener unlock = new UnlockListener((SyncWaitCondition) waitCondition, this,
          stableTimeoutExecutor, screen, exceptionHandler);
      screen.addKeyboardStatusChangeListener(unlock);
      return unlock;
    } else if (waitCondition instanceof CursorWaitCondition) {
      VisibleCursorListener unlock = new VisibleCursorListener((CursorWaitCondition) waitCondition,
          this, stableTimeoutExecutor, screen.getScreenCursor(), exceptionHandler);
      screen.getScreenCursor().addCursorMoveListener(unlock);
      return unlock;
    } else if (waitCondition instanceof SilentWaitCondition) {
      SilenceListener silence = new SilenceListener((SilentWaitCondition) waitCondition,
          stableTimeoutExecutor, screen, exceptionHandler);
      screen.getScreenCursor().addCursorMoveListener(silence);
      screen.addKeyboardStatusChangeListener(silence);
      screen.getFieldManager().addScreenChangeListener(silence);
      return silence;
    } else if (waitCondition instanceof TextWaitCondition) {
      ScreenTextListener text = new ScreenTextListener((TextWaitCondition) waitCondition, this,
          stableTimeoutExecutor, screen, exceptionHandler);
      screen.getScreenCursor().addCursorMoveListener(text);
      screen.addKeyboardStatusChangeListener(text);
      screen.getFieldManager().addScreenChangeListener(text);
      return text;
    } else {
      throw new UnsupportedOperationException(
          "We still don't support " + waitCondition.getClass().getName() + " waiters");
    }
  }

  //we don't just use screen.getPen().getScreenText()
  @Override
  public String getScreen() {
    StringBuilder text = new StringBuilder();
    int pos = 0;
    for (ScreenPosition sp : screen.getPen()) {
      text.append(
          ReflectionUtils.invokeMethod(FIELD_POSITION_GET_CHAR_STRING_METHOD, String.class, sp));
      if (++pos % getScreenSize().width == 0) {
        text.append("\n");
      }
    }

    return text.toString();
  }

  @Override
  public Dimension getScreenSize() {
    ScreenDimensions dimensions = screen.getScreenDimensions();
    return new Dimension(dimensions.columns, dimensions.rows);
  }

  @Override
  public boolean isInputInhibited() {
    return screen.isKeyboardLocked();
  }

  @Override
  public Position getCursorPosition() {
    Cursor cursor = screen.getScreenCursor();
    int location = cursor.getLocation();
    int columns = screen.getScreenDimensions().columns;
    return cursor.isVisible() ? new Position(location / columns + 1, location % columns + 1) : null;
  }

  @Override
  public boolean getSoundAlarm() {
    return screen.resetAlarm();
  }

  @Override
  public void disconnect() throws RteIOException {
    if (stableTimeoutExecutor == null) {
      return;
    }
    doDisconnect();
  }

  private void doDisconnect() throws RteIOException {
    stableTimeoutExecutor.shutdownNow();
    stableTimeoutExecutor = null;
    try {
      consolePane.doDisconnect();
    } catch (InterruptedException ex) {
      Thread.currentThread().interrupt();
      LOG.warn("Disconnection process was interrupted");
    }
    exceptionHandler.throwAnyPendingError();
  }

}
