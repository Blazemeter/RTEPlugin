package com.blazemeter.jmeter.rte.protocols.tn3270;

import com.blazemeter.jmeter.rte.core.ssl.SSLType;
import com.blazemeter.jmeter.rte.protocols.ReflectionUtils;
import com.bytezone.dm3270.application.ConsolePane;
import com.bytezone.dm3270.display.Screen;
import com.bytezone.dm3270.plugins.PluginsStage;
import com.bytezone.dm3270.streams.TelnetListener;
import com.bytezone.dm3270.streams.TelnetState;
import com.bytezone.dm3270.streams.TerminalServer;
import com.bytezone.dm3270.utilities.Site;
import java.lang.reflect.Method;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is an extension of {@link ConsolePane} but making connect method public.
 */
public class ConnectibleConsolePane extends ConsolePane {

  private final Logger LOG = LoggerFactory.getLogger(ConnectibleConsolePane.class);
  private TelnetListener telnetListener;
  private final TelnetState telnetState;
  private final Site server;
  private TerminalServer terminalServer;
  private final Screen screen;
  private Thread terminalServerThread;
  private SSLType sslType;
  private int connectionTimeoutMillis;

  private static final Method CONNECT_METHOD = ReflectionUtils
      .getAccessibleMethod(ConsolePane.class, "connect");

  public ConnectibleConsolePane(Screen screen, Site server, PluginsStage pluginsStage, TelnetState telnetState) {
    super(screen, server, pluginsStage);
    this.server = server;
    this.telnetState = telnetState;
    this.screen = screen;
  }

  public void setSslType(SSLType sslType){
      this.sslType = sslType;
  }

  public void setConnectionTimeoutMillis(int connectionTimeoutMillis){
      this.connectionTimeoutMillis = connectionTimeoutMillis;
  }

  public void connect () {
      if (server == null)
          throw new IllegalArgumentException ("Server must not be null");

      // set preferences for this session
      telnetState.setDo3270Extended (server.getExtended ());
      telnetState.setDoTerminalType (true);

      telnetListener = new TelnetListener(screen, telnetState);
      terminalServer =
              new ExtendedTerminalServer(server.getURL (), server.getPort (), telnetListener, sslType, connectionTimeoutMillis);
      telnetState.setTerminalServer (terminalServer);

      terminalServerThread = new Thread (terminalServer);
      terminalServerThread.start ();
  }

  @Override
  public void disconnect()
  {
      if (terminalServer != null)
          terminalServer.close ();

      telnetState.close ();

      if (terminalServerThread != null)
      {
          terminalServerThread.interrupt ();
          try
          {
              terminalServerThread.join ();
          }
          catch (InterruptedException ex)
          {
              terminalServerThread.interrupt();
              LOG.info("Communication error with Rte server, the disconnection process was interrupted.");
          }
      }
  }

}
