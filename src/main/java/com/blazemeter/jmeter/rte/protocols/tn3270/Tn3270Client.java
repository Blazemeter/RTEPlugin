package com.blazemeter.jmeter.rte.protocols.tn3270;

import com.blazemeter.jmeter.rte.core.Action;
import com.blazemeter.jmeter.rte.core.CoordInput;
import com.blazemeter.jmeter.rte.core.InvalidFieldPositionException;
import com.blazemeter.jmeter.rte.core.Position;
import com.blazemeter.jmeter.rte.core.RequestListener;
import com.blazemeter.jmeter.rte.core.RteIOException;
import com.blazemeter.jmeter.rte.core.RteProtocolClient;
import com.blazemeter.jmeter.rte.core.TerminalType;
import com.blazemeter.jmeter.rte.core.ssl.SSLType;
import com.blazemeter.jmeter.rte.core.wait.WaitCondition;
import com.bytezone.dm3270.application.Console.Function;
import com.bytezone.dm3270.commands.AIDCommand;
import com.bytezone.dm3270.display.Cursor;
import com.bytezone.dm3270.display.Field;
import com.bytezone.dm3270.display.Screen;
import com.bytezone.dm3270.display.ScreenDimensions;
import com.bytezone.dm3270.plugins.PluginsStage;
import com.bytezone.dm3270.streams.TelnetState;
import com.bytezone.dm3270.utilities.Site;
import java.awt.Dimension;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;

//TODO: implement proper tests
public class Tn3270Client implements RteProtocolClient {

  private static final DeviceModel DEFAULT_MODEL = DeviceModel.M2;
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
      /* TODO(rabelenda): complete
      put(Action.ATTN, new KeyEventMap(0, KeyEvent.VK_ESCAPE));
      put(Action.CLEAR, new KeyEventMap(0, KeyEvent.VK_PAUSE));
      put(Action.SYSRQ, new KeyEventMap(KeyEvent.SHIFT_MASK, KeyEvent.VK_ESCAPE));
      put(Action.RESET, new KeyEventMap(KeyEvent.CTRL_MASK, KeyEvent.VK_CONTROL));
      put(Action.ROLL_UP, new KeyEventMap(KeyEvent.CTRL_MASK, KeyEvent.VK_PAGE_UP));
      put(Action.ROLL_DN, new KeyEventMap(KeyEvent.CTRL_MASK, KeyEvent.VK_PAGE_DOWN));*/
    }
  };

  private static final Map<TerminalType, DeviceType> DEVICE_TYPES =
      new EnumMap<TerminalType, DeviceType>(TerminalType.class) {
        {
          put(TerminalType.IBM_3278_2, new DeviceType(DeviceModel.M2, false));
          put(TerminalType.IBM_3278_2_E, new DeviceType(DeviceModel.M2, true));
          put(TerminalType.IBM_3278_3, new DeviceType(DeviceModel.M3, false));
          put(TerminalType.IBM_3278_3_E, new DeviceType(DeviceModel.M3, true));
          put(TerminalType.IBM_3278_4, new DeviceType(DeviceModel.M4, false));
          put(TerminalType.IBM_3278_4_E, new DeviceType(DeviceModel.M4, true));
          put(TerminalType.IBM_3278_5, new DeviceType(DeviceModel.M5, false));
          put(TerminalType.IBM_3278_5_E, new DeviceType(DeviceModel.M5, true));
        }
      };

  private Screen screen;
  private ConnectibleConsolePane consolePane;

  private enum DeviceModel {
    M2(2, 24, 80),
    M3(3, 32, 80),
    M4(4, 43, 80),
    M5(5, 27, 132);

    private final int id;
    private final ScreenDimensions screenDimensions;

    DeviceModel(int id, int rows, int columns) {
      this.id = id;
      this.screenDimensions = new ScreenDimensions(rows, columns);
    }

  }

  private static class DeviceType {

    private final DeviceModel model;
    private final boolean extended;

    private DeviceType(DeviceModel model, boolean extended) {
      this.model = model;
      this.extended = extended;
    }

  }

  @Override
  public void connect(String server, int port, SSLType sslType, TerminalType terminalType,
      long timeoutMillis, long stableTimeout) {
    //TODO: check how exceptions are treated/handled
    //TODO: see if we want to support buffering input (and if lib supports it)
    //TODO: the lib does not use logging (just stdout)
    DeviceType deviceType = DEVICE_TYPES.get(terminalType);
    Site serverSite = new Site("", server, port, deviceType.extended, deviceType.model.id, false,
        "");
    TelnetState telnetState = new TelnetState();
    telnetState.setDoDeviceType(deviceType.model.id);
    Preferences prefs = Preferences.userNodeForPackage(getClass());
    PluginsStage pluginsStage = new PluginsStage(prefs);
    screen = new Screen(DEFAULT_MODEL.screenDimensions, deviceType.model.screenDimensions, prefs,
        Function.TERMINAL, pluginsStage, serverSite, telnetState);
    consolePane = new ConnectibleConsolePane(screen, serverSite, pluginsStage);
    consolePane.connect();
  }

  @Override
  public void await(List<WaitCondition> waitConditions) {
    //TODO: implement
  }

  @Override
  public RequestListener buildRequestListener() {
    //TODO: implement
    return null;
  }

  @Override
  public void send(List<CoordInput> input, Action action) throws RteIOException {
    input.forEach(i -> {
      Field field = screen.getFieldManager()
          .getFieldAt(buildLinealPosition(i))
          .orElseThrow(() -> new InvalidFieldPositionException(i.getPosition()));
      field.setText(i.getInput());
    });
    consolePane.sendAID(AID_COMMANDS.get(action), action.name());
  }

  private int buildLinealPosition(CoordInput i) {
    return (i.getPosition().getRow() - 1) * getScreenSize().width + i.getPosition().getColumn() - 1;
  }

  @Override
  public String getScreen() {
    return screen.getPen().getScreenText();
  }

  @Override
  public Dimension getScreenSize() {
    ScreenDimensions dimensions = screen.getScreenDimensions();
    return new Dimension(dimensions.rows, dimensions.columns);
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
    return cursor.isVisible() ? new Position(location % columns + 1, location / columns + 1) : null;
  }

  @Override
  public void disconnect() {
    consolePane.disconnect();
    screen.close();
  }

}
