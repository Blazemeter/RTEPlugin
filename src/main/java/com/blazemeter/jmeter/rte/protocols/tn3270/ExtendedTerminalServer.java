package com.blazemeter.jmeter.rte.protocols.tn3270;

import com.blazemeter.jmeter.rte.core.ConnectionClosedException;
import com.blazemeter.jmeter.rte.core.ExceptionHandler;
import com.bytezone.dm3270.streams.BufferListener;
import com.bytezone.dm3270.streams.TelnetSocket.Source;
import com.bytezone.dm3270.streams.TerminalServer;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.time.LocalDateTime;
import javax.net.SocketFactory;

/*
 * Performs the same as {@link TerminalServer}, but in this case uses a socket that supports SSL and
 * connection timeout.
 * <p/>
 * Additionally, instead of handling the exceptions on the class itself, they
 * are thrown to Tn3270Client, and throws a ConnectionClosedException when connection is
 * closed by remote end to be able to report and error in such case.
 */
public class ExtendedTerminalServer extends TerminalServer {

  private ExceptionHandler exceptionHandler;
  private int connectionTimeoutMillis;
  private SocketFactory socketFactory;

  private final int serverPort;
  private final String serverURL;
  private Socket socket = new Socket();
  private OutputStream serverOut;

  private final byte[] buffer = new byte[4096];
  private volatile boolean running;

  private final BufferListener telnetListener;

  public ExtendedTerminalServer(String serverURL, int serverPort, BufferListener listener,
      SocketFactory socketFactory, int connectionTimeoutMillis, ExceptionHandler exceptionHandler) {
    super(serverURL, serverPort, listener);
    this.serverPort = serverPort;
    this.socketFactory = socketFactory;
    this.serverURL = serverURL;
    this.connectionTimeoutMillis = connectionTimeoutMillis;
    this.telnetListener = listener;
    this.exceptionHandler = exceptionHandler;
  }

  @Override
  public void run() {
    try {
      socket = socketFactory.createSocket();
      socket.connect(new InetSocketAddress(serverURL, serverPort), connectionTimeoutMillis);
    } catch (IOException ex) {
      exceptionHandler.setPendingError(ex);
      return;
    }
    try {
      InputStream serverIn = socket.getInputStream();
      serverOut = socket.getOutputStream();
      running = true;
      while (running) {
        int bytesRead = serverIn.read(buffer);
        if (bytesRead < 0) {
          close();
          exceptionHandler.setPendingError(new ConnectionClosedException());
          break;
        }

        byte[] message = new byte[bytesRead];
        System.arraycopy(buffer, 0, message, 0, bytesRead);
        telnetListener.listen(Source.SERVER, message, LocalDateTime.now(), true);
      }
    } catch (IOException ex) {
      if (running) {
        close();
        exceptionHandler.setPendingError(ex);
      }
    }
  }

  public synchronized void write(byte[] buffer) {
    if (!running) {
      // the no-op may come here if socket is closed from remote end and client has not been closed
      if (buffer != ExtendedTelnetState.NO_OP) {
        exceptionHandler.setPendingError(new ConnectionClosedException());
      }
      return;
    }

    try {
      serverOut.write(buffer);
      serverOut.flush();
    } catch (IOException e) {
      exceptionHandler.setPendingError(e);
    }
  }

  @Override
  public void close() {
    try {
      running = false;

      if (socket != null) {
        socket.close();
      }

      if (telnetListener != null) {
        telnetListener.close();
      }
    } catch (IOException ex) {
      exceptionHandler.setPendingError(ex);
    }
  }
}
