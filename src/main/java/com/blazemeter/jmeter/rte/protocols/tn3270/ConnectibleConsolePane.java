package com.blazemeter.jmeter.rte.protocols.tn3270;

import com.blazemeter.jmeter.rte.protocols.ReflectionUtils;
import com.bytezone.dm3270.application.ConsolePane;
import com.bytezone.dm3270.display.Screen;
import com.bytezone.dm3270.plugins.PluginsStage;
import com.bytezone.dm3270.utilities.Site;
import java.lang.reflect.Method;

/**
 * This is an extension of {@link ConsolePane} but making connect method public.
 */
public class ConnectibleConsolePane extends ConsolePane {

  private static final Method CONNECT_METHOD = ReflectionUtils
      .getAccessibleMethod(ConsolePane.class, "connect");

  public ConnectibleConsolePane(Screen screen, Site server, PluginsStage pluginsStage) {
    super(screen, server, pluginsStage);
  }

  public void connect() {
    ReflectionUtils.invokeMethod(CONNECT_METHOD, this);
  }

}
