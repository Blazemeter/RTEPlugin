package com.blazemeter.jmeter.rte.protocols.tn3270;

import com.blazemeter.jmeter.rte.core.RteIOException;
import com.blazemeter.jmeter.rte.core.ssl.SSLSocketFactory;
import com.blazemeter.jmeter.rte.core.ssl.SSLType;
import com.bytezone.dm3270.streams.BufferListener;
import com.bytezone.dm3270.streams.TelnetSocket.Source;
import com.bytezone.dm3270.streams.TerminalServer;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import javax.net.ssl.SSLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Performs the same as {@link TerminalServer}, but in this case uses a socket that supports SSL and
 * connection timeout. Apart from that, instead of handling the exceptions on the class itself, they
 * are thrown to tn3270class.
 */
public class ExtendedTerminalServer extends TerminalServer {

  private static final Logger LOG = LoggerFactory.getLogger(ExtendedTerminalServer.class);
  private int connectionTimeoutMillis;
  private SSLType sslType;

  private final int serverPort;
  private final String serverURL;
  private Socket socket = new Socket();
  private InputStream serverIn;
  private OutputStream serverOut;

  private final byte[] buffer = new byte[4096];
  private volatile boolean running;

  private final BufferListener telnetListener;

  private Throwable pendingError;

  public ExtendedTerminalServer(String serverURL, int serverPort, BufferListener listener,
      SSLType sslType, int connectionTimeoutMillis) {
    super(serverURL, serverPort, listener);
    this.serverPort = serverPort;
    this.sslType = sslType;
    this.serverURL = serverURL;
    this.connectionTimeoutMillis = connectionTimeoutMillis;
    this.telnetListener = listener;
  }

  @Override
  public void run() {
    try {
      socket = createSocket();
      serverIn = socket.getInputStream();
      serverOut = socket.getOutputStream();
      running = true;

      while (running) {

        int bytesRead = serverIn.read(buffer);
        if (bytesRead < 0) {
          close();
          break;
        }

        byte[] message = new byte[bytesRead];
        System.arraycopy(buffer, 0, message, 0, bytesRead);
        telnetListener.listen(Source.SERVER, message, LocalDateTime.now(), true);
      }
    } catch (GeneralSecurityException ex) {
      if (running) {
        close();
      }
      setPendingError(new SSLException(ex));
    } catch (IOException ex) {
      if (running) {
        close();
        setPendingError(ex);
      }
    }
  }

  private Socket createSocket() throws IOException, GeneralSecurityException {
    if (sslType != null && sslType != SSLType.NONE) {
      SSLSocketFactory sslSocketFactory = new SSLSocketFactory(sslType);
      sslSocketFactory.init();
      return sslSocketFactory.createSocket(serverURL, serverPort, connectionTimeoutMillis);
    } else {
      Socket socket = new Socket();
      socket.connect(new InetSocketAddress(serverURL, serverPort), connectionTimeoutMillis);
      return socket;
    }
  }

  public synchronized void write(byte[] buffer) {
    if (serverOut == null) {
      // the no-op may come here if the program is not closed after disconnection
      LOG.debug("serverOut is null in TerminalServer");
      return;
    }

    try {
      serverOut.write(buffer);
      serverOut.flush();
    } catch (IOException e) {
      setPendingError(e);
    }
  }

  private synchronized void setPendingError(Throwable ex) {
    if (pendingError == null) {
      pendingError = ex;
    } else {
      LOG.error("Exception ignored in step result due to previously thrown exception", ex);
    }
  }

  @Override
  public void close() {
    try {
      running = false;

      serverIn = null;
      serverOut = null;

      if (socket != null) {
        socket.close();
      }

      if (telnetListener != null) {
        telnetListener.close();
      }
    } catch (IOException ex) {
      setPendingError(ex);
      LOG.error("Communication with rte server failed.");
    }
  }

  public synchronized void throwAnyPendingError() throws RteIOException {
    if (pendingError != null) {
      Throwable ret = pendingError;
      pendingError = null;
      throw new RteIOException(ret);
    }
  }

}
