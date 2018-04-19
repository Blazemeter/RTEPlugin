package com.blazemeter.jmeter.rte.protocols.tn3270;

import com.blazemeter.jmeter.rte.core.Action;
import com.blazemeter.jmeter.rte.core.BaseProtocolClient;
import com.blazemeter.jmeter.rte.core.CoordInput;
import com.blazemeter.jmeter.rte.core.InvalidFieldPositionException;
import com.blazemeter.jmeter.rte.core.Position;
import com.blazemeter.jmeter.rte.core.listener.RequestListener;
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
import com.bytezone.dm3270.application.Console.Function;
import com.bytezone.dm3270.commands.AIDCommand;
import com.bytezone.dm3270.display.Cursor;
import com.bytezone.dm3270.display.Field;
import com.bytezone.dm3270.display.Screen;
import com.bytezone.dm3270.display.ScreenDimensions;
import com.bytezone.dm3270.display.ScreenPosition;
import com.bytezone.dm3270.plugins.PluginsStage;
import com.bytezone.dm3270.utilities.Site;
import java.awt.Dimension;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;
import java.util.prefs.Preferences;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
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

  private static final Map<Action, Byte> AID_COMMANDS = new EnumMap<Action, Byte>(
      Action.class) {
    {
      put(Action.F1, AIDCommand.AID_PF1);
      put(Action.F2, AIDCommand.AID_PF2);
      put(Action.F3, AIDCommand.AID_PF3);
      put(Action.F4, AIDCommand.AID_PF4);
      put(Action.F5, AIDCommand.AID_PF5);
      put(Action.F6, AIDCommand.AID_PF6);
      put(Action.F7, AIDCommand.AID_PF7);
      put(Action.F8, AIDCommand.AID_PF8);
      put(Action.F9, AIDCommand.AID_PF9);
      put(Action.F10, AIDCommand.AID_PF10);
      put(Action.F11, AIDCommand.AID_PF11);
      put(Action.F12, AIDCommand.AID_PF12);
      put(Action.F13, AIDCommand.AID_PF13);
      put(Action.F14, AIDCommand.AID_PF14);
      put(Action.F15, AIDCommand.AID_PF15);
      put(Action.F16, AIDCommand.AID_PF16);
      put(Action.F17, AIDCommand.AID_PF17);
      put(Action.F18, AIDCommand.AID_PF18);
      put(Action.F19, AIDCommand.AID_PF19);
      put(Action.F20, AIDCommand.AID_PF20);
      put(Action.F21, AIDCommand.AID_PF21);
      put(Action.F22, AIDCommand.AID_PF22);
      put(Action.F23, AIDCommand.AID_PF23);
      put(Action.F24, AIDCommand.AID_PF24);
      put(Action.ENTER, AIDCommand.AID_ENTER);
    }
  };

  private static final Tn3270TerminalType DEFAULT_TERMINAL_TYPE =
      (Tn3270TerminalType) TERMINAL_TYPES.get(0);

  private static final Method FIELD_POSITION_GET_CHAR_STRING_METHOD =
      ReflectionUtils.getAccessibleMethod(ScreenPosition.class, "getCharString");

  private Screen screen;
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
    //we need to initialize javafx and be able to use dm3270 classes
    //TODO: check if this is needed per thread or can/must be done just once
    initializeJavafx();
    Tn3270TerminalType termType = (Tn3270TerminalType) terminalType;
    Site serverSite = new Site("", server, port, termType.isExtended(), termType.getModel(), false,
        "");
    ExtendedTelnetState telnetState = new ExtendedTelnetState();
    telnetState.setDoDeviceType(termType.getModel());
    Preferences prefs = Preferences.userNodeForPackage(getClass());
    // we need to build instances in JavaFx thread due to JavaFx limitations
    PluginsStage pluginsStage = buildInJavaFxThread(() -> new PluginsStage(prefs));
    /*
    Creating empty dm3270/files temporary directory and pointing user home directory to it to
    avoid system.out log from dm3270 emulator
    */
    String homeDir = System.getProperty("user.home");
    System.setProperty("user.home", buildDm3270TemporaryHomeDirectory());
    screen = buildInJavaFxThread(() -> new Screen(DEFAULT_TERMINAL_TYPE.getScreenDimensions(),
        termType.getScreenDimensions(), prefs, Function.TERMINAL, pluginsStage, serverSite,
        telnetState));
    screen.lockKeyboard("connect");
    System.setProperty("user.home", homeDir);
    consolePane = new ExtendedConsolePane(screen, serverSite, pluginsStage);
    consolePane.connect();
    stableTimeoutExecutor = Executors.newSingleThreadScheduledExecutor();
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

  private String buildDm3270TemporaryHomeDirectory() throws RteIOException{
    try{
      Path tmpDir = Files.createTempDirectory("dm3270-files");
      Files.createDirectories(tmpDir.resolve("dm3270/files"));
      return tmpDir.toString();
    }
    catch(IOException ex){
      throw new RteIOException(ex);
    }
  }

  private void initializeJavafx() {
    new JFXPanel();
  }

  private <T> T buildInJavaFxThread(Supplier<T> supplier)
      throws InterruptedException {
    CompletableFuture<T> ret = new CompletableFuture<>();
    Platform.runLater(() -> ret.complete(supplier.get()));
    try {
      return ret.get();
    } catch (ExecutionException e) {
      throw new RuntimeException(e); //NOSONAR
    }
  }

  @Override
  public RequestListener buildRequestListener() {
    Tn3270RequestListener listener = new Tn3270RequestListener(this, screen);
    screen.getFieldManager().addScreenChangeListener(listener);
    return listener;
  }

  @Override
  public void send(List<CoordInput> input, Action action) throws RteIOException {
    input.forEach(i -> {
      int linearPosition = buildLinealPosition(i);
      Field field = screen.getFieldManager()
          .getFieldAt(linearPosition)
          .orElseThrow(() -> new InvalidFieldPositionException(i.getPosition()));
      field.setText(i.getInput());
      screen.getScreenCursor().moveTo(linearPosition + i.getInput().length());
    });
    consolePane.sendAID(AID_COMMANDS.get(action), action.name());
    consolePane.throwAnyPendingError();
  }

  private int buildLinealPosition(CoordInput i) {
    return (i.getPosition().getRow() - 1) * getScreenSize().width + i.getPosition().getColumn() - 1;
  }

  @Override
  protected ConditionWaiter buildWaiter(WaitCondition waitCondition) {
    if (waitCondition instanceof SyncWaitCondition) {
      UnlockListener unlock = new UnlockListener((SyncWaitCondition) waitCondition, this,
          stableTimeoutExecutor, screen);
      screen.addKeyboardStatusChangeListener(unlock);
      return unlock;
    } else if (waitCondition instanceof CursorWaitCondition) {
      VisibleCursorListener unlock = new VisibleCursorListener((CursorWaitCondition) waitCondition,
          this, stableTimeoutExecutor, screen.getScreenCursor());
      screen.getScreenCursor().addCursorMoveListener(unlock);
      return unlock;
    } else if (waitCondition instanceof SilentWaitCondition) {
      SilenceListener silence = new SilenceListener((SilentWaitCondition) waitCondition,
          stableTimeoutExecutor, screen);
      screen.getScreenCursor().addCursorMoveListener(silence);
      screen.addKeyboardStatusChangeListener(silence);
      screen.getFieldManager().addScreenChangeListener(silence);
      return silence;
    } else if (waitCondition instanceof TextWaitCondition) {
      ScreenTextListener text = new ScreenTextListener((TextWaitCondition) waitCondition, this,
          stableTimeoutExecutor, screen);
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
  public void disconnect() throws RteIOException{
    if (stableTimeoutExecutor == null) {
      return;
    }
    doDisconnect();
  }

  private void doDisconnect() throws RteIOException{
    stableTimeoutExecutor.shutdownNow();
    stableTimeoutExecutor = null;
    try {
      consolePane.doDisconnect();
    } catch (InterruptedException ex) {
      Thread.currentThread().interrupt();
      LOG.warn("Disconnection process was interrupted");
    }
    consolePane.throwAnyPendingError();
    //TODO: at some point we need to call Platform.exit()
  }

}
