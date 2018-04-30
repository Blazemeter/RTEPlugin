package com.blazemeter.jmeter.rte.protocols.tn3270;

import com.bytezone.dm3270.application.Console.Function;
import com.bytezone.dm3270.display.Screen;
import com.bytezone.dm3270.display.ScreenDimensions;
import com.bytezone.dm3270.plugins.PluginsStage;
import com.bytezone.dm3270.streams.TelnetState;
import com.bytezone.dm3270.utilities.Site;
import java.util.prefs.Preferences;

public class SilentScreen extends Screen {

  private boolean soundAlarm = false;

  SilentScreen(ScreenDimensions defaultScreenDimensions,
      ScreenDimensions alternateScreenDimensions, Preferences prefs, Function function,
      PluginsStage pluginsStage, Site serverSite, TelnetState telnetState) {
    super(defaultScreenDimensions, alternateScreenDimensions, prefs, function, pluginsStage,
        serverSite, telnetState);
  }

  @Override
  public void soundAlarm() {
    soundAlarm = true;
  }

  public boolean resetAlarm() {
    boolean ret = soundAlarm;
    soundAlarm = false;
    return ret;
  }
}
