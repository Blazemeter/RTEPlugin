package com.blazemeter.jmeter.rte.protocols.tn3270;

import com.blazemeter.jmeter.rte.core.ExceptionHandler;
import com.bytezone.dm3270.application.ConsolePane;
import com.bytezone.dm3270.display.Screen;
import com.bytezone.dm3270.streams.TelnetListener;
import com.bytezone.dm3270.utilities.Site;
import javax.net.SocketFactory;

/**
 * This is an extension of {@link ConsolePane} that provides connect method public visibility and
 * SSL / connection timeout support. Apart from that, instead of handling the exceptions on the
 * class itself, they are thrown to Tn3270Client.
 */
public class ExtendedConsolePane extends ConsolePane {

  private final Site server;
  private ExceptionHandler exceptionHandler;
  private ExtendedTerminalServer terminalServer;
  private final Screen screen;
  private Thread terminalServerThread;
  private SocketFactory socketFactory;
  private int connectionTimeoutMillis;

  public ExtendedConsolePane(Screen screen, Site server,
      ExceptionHandler exceptionHandler) {
    super(screen, server);
    this.server = server;
    this.screen = screen;
    this.exceptionHandler = exceptionHandler;
  }

  public void setSocketFactory(SocketFactory socketFactory) {
    this.socketFactory = socketFactory;
  }

  public void setConnectionTimeoutMillis(int connectionTimeoutMillis) {
    this.connectionTimeoutMillis = connectionTimeoutMillis;
  }

  public void connect() {
    if (server == null) {
      throw new IllegalArgumentException("Server must not be null");
    }

    // set preferences for this session
    ExtendedTelnetState telnetState = (ExtendedTelnetState) screen.getTelnetState();
    telnetState.setDo3270Extended(server.getExtended());
    telnetState.setDoTerminalType(true);

    TelnetListener telnetListener = new TelnetListener(screen, telnetState);
    terminalServer =
        new ExtendedTerminalServer(server.getURL(), server.getPort(), telnetListener, socketFactory,
            connectionTimeoutMillis, exceptionHandler);
    telnetState.setTerminalServer(terminalServer);

    terminalServerThread = new Thread(terminalServer);
    terminalServerThread.start();
  }

  public void doDisconnect() throws InterruptedException {
    screen.getTelnetState().close();

    if (terminalServer != null) {
      terminalServer.close();
    }

    if (terminalServerThread != null) {
      terminalServerThread.interrupt();
      terminalServerThread.join();
    }
  }
}
