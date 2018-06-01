package com.blazemeter.jmeter.rte.protocols.tn5250;

import com.blazemeter.jmeter.rte.core.ConnectionClosedException;
import com.blazemeter.jmeter.rte.protocols.ReflectionUtils;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import javax.net.SocketFactory;
import net.infordata.em.tnprot.XITelnet;
import net.infordata.em.tnprot.XITelnetEmulator;

/**
 * Handle telnet connection by providing connection timeout and security on top of {@link
 * XITelnet}.
 * <p/>
 * Additionally, solves concurrency issue when closing connection which may cause IOExceptions,
 * improves communication with server by sending entire packets instead of potentially splitting
 * them before they reach TCP layer, throws a ConnectionClosedException to be able to properly
 * identify this scenario, and does not close connection on exception to avoid missing fields when
 * sending and instead fail with an IOException.
 */
public class ExtendedTelnet extends XITelnet {

  private static final Field SOCKET_FIELD = ReflectionUtils
      .getAccessibleField(XITelnet.class, "ivSocket");
  private static final Field INPUT_STREAM_FIELD = ReflectionUtils
      .getAccessibleField(XITelnet.class, "ivIn");
  private static final Field OUTPUT_STREAM_FIELD = ReflectionUtils
      .getAccessibleField(XITelnet.class, "ivOut");
  private static final Field USED_FIELD = ReflectionUtils
      .getAccessibleField(XITelnet.class, "ivUsed");
  private static final Field IAC_PARSER_STATUS_FIELD = ReflectionUtils
      .getAccessibleField(XITelnet.class, "ivIACParserStatus");
  private static final Method CLOSE_SOCKET_METHOD = ReflectionUtils
      .getAccessibleMethod(XITelnet.class, "closeSocket");
  private static final int READ_BUFFER_SIZE_BYTES = 1024;

  private final int connectTimeoutMillis;
  private final SocketFactory socketFactory;
  private RxThread readThread;
  private BufferedOutputStream ivOut;
  private XITelnetEmulator ivEmulator;

  public ExtendedTelnet(String aHost, int aPort, int connectTimeoutMillis,
      SocketFactory socketFactory) {
    super(aHost, aPort);
    this.connectTimeoutMillis = connectTimeoutMillis;
    this.socketFactory = socketFactory;
  }

  @Override
  public synchronized void connect() {
    if (getIvUsed()) {
      throw new IllegalStateException("XITelnet cannot be recycled");
    } else {
      this.disconnect();
      this.connecting();
      try {
        Socket ivSocket = socketFactory.createSocket();
        setIvSocket(ivSocket);
        /*
        In XITelnet is used ivFirstHost instead of getHost(), but we are not supposed to use hosts
        with firstHostIp#SecondHostIp format in JMeter
        */
        ivSocket
            .connect(new InetSocketAddress(this.getHost(), this.getPort()), connectTimeoutMillis);
        InputStream ivIn = getIvSocket().getInputStream();
        setIvIn(ivIn);
        /*
         we use a BufferedOutputStream to avoid sending many small packets and ease tracing with
         Wireshark by keeping packets unaltered
          */
        ivOut = new BufferedOutputStream(getIvSocket().getOutputStream());
        setIvOut(ivOut);
        readThread = new ExtendedTelnet.RxThread();
        readThread.start();
        setIvUsed(true);
        this.connected();
      } catch (IOException e) {
        catchedIOException(e);
      }
    }
  }

  /*
  It was required to use reflection on the following attributes as they are private in XITelnet
  class.
   */
  private Socket getIvSocket() {
    return ReflectionUtils.getFieldValue(SOCKET_FIELD, Socket.class, this);
  }

  private void setIvSocket(Socket ivSocket) {
    ReflectionUtils.setFieldValue(SOCKET_FIELD, ivSocket, this);
  }

  private InputStream getIvIn() {
    return ReflectionUtils.getFieldValue(INPUT_STREAM_FIELD, InputStream.class, this);
  }

  private void setIvIn(InputStream ivIn) {
    ReflectionUtils.setFieldValue(INPUT_STREAM_FIELD, ivIn, this);
  }

  private void setIvOut(OutputStream ivOut) {
    ReflectionUtils.setFieldValue(OUTPUT_STREAM_FIELD, ivOut, this);
  }

  private boolean getIvUsed() {
    return ReflectionUtils.getFieldValue(USED_FIELD, Boolean.class, this);
  }

  private void setIvUsed(boolean ivUsed) {
    ReflectionUtils.setFieldValue(USED_FIELD, ivUsed, this);
  }

  private int getivIACParserStatus() {
    return ReflectionUtils.getFieldValue(IAC_PARSER_STATUS_FIELD, Integer.class, this);
  }

  @Override
  public synchronized void sendEOR() throws IOException {
    checkIfAlreadyClosed();
    super.sendEOR();
  }

  private void checkIfAlreadyClosed() throws IOException {
    if (getIvSocket() == null) {
      throw new SocketException("Connection already closed");
    }
  }

  @Override
  public synchronized void sendIACCmd(byte aCmd, byte aOpt) {
    try {
      checkIfAlreadyClosed();
      super.sendIACCmd(aCmd, aOpt);
    } catch (IOException e) {
      catchedIOException(e);
    }
  }

  @Override
  public synchronized void sendIACStr(byte aCmd, byte aOpt, boolean sendIS, String aString) {
    try {
      checkIfAlreadyClosed();
      super.sendIACStr(aCmd, aOpt, sendIS, aString);
    } catch (IOException e) {
      catchedIOException(e);
    }
  }

  @Override
  public synchronized void send(byte[] aBuf, int aLen) {
    try {
      checkIfAlreadyClosed();
      super.send(aBuf, aLen);
      /*
       this is the only method from XITelnet that does not invoke flush, and since we are now using
       a BufferedOutputStream we need all methods to flush.
        */
      ivOut.flush();
    } catch (IOException e) {
      catchedIOException(e);
    }
  }

  @Override
  public synchronized void flush() {
    try {
      checkIfAlreadyClosed();
      super.flush();
    } catch (IOException e) {
      catchedIOException(e);
    }
  }

  @Override
  public synchronized void disconnect() { //!!V 03/03/98
    if (readThread != null) {
      readThread.terminate();
      readThread = null;
    }
    closeIvSocket();
  }

  @Override
  public void setEmulator(XITelnetEmulator aEmulator) {
    ivEmulator = aEmulator;
    super.setEmulator(aEmulator);
  }

  @Override
  protected synchronized void catchedIOException(IOException ex) {
    if (ivEmulator != null) {
      ivEmulator.catchedIOException(ex);
    }
  }

  private void closeIvSocket() {
    ReflectionUtils.invokeMethod(CLOSE_SOCKET_METHOD, this);
  }

  //This class implements the Receptor Thread of the SSL Telnet connection.
  //It's a copy of XITelnet().RxThread() class.
  @SuppressWarnings("all")
  class RxThread extends Thread {

    private boolean ivTerminate = false;

    private RxThread() {
      super("ExtendedTelnet rx thread");
    }

    private void terminate() {
      this.ivTerminate = true;
      if (this != Thread.currentThread()) {
        this.interrupt();
      }
    }

    @Override
    public void run() {
      byte[] buf = new byte[READ_BUFFER_SIZE_BYTES];
      byte[] rBuf = new byte[READ_BUFFER_SIZE_BYTES];

      try {
        while (!this.ivTerminate) {
          InputStream input = getIvIn();
          // the input may be null if doDisconnect was invoked after the while condition evaluation
          if (input == null) {
            return;
          }
          int len = input.read(buf);
          if (len < 0) {
            throw new ConnectionClosedException();
          }
          int i = 0;
          int j;
          for (j = 0; i < len; ++i) {
            rBuf[j] = buf[i];
            if (getivIACParserStatus() == 0 && buf[i] != -1) {
              ++j;
            } else {
              if (getivIACParserStatus() == 0 && buf[i] == -1) {
                if (j > 0) {
                  ExtendedTelnet.this.receivedData(rBuf, j);
                }

                j = 0;
              }

              j += ExtendedTelnet.this.processIAC(buf[i]);
            }
          }

          if (j > 0) {
            ExtendedTelnet.this.receivedData(rBuf, j);
          }
        }
      } catch (IOException varviii) {
        if (!this.ivTerminate) {
          ExtendedTelnet.this.catchedIOException(varviii);
        }
      }

    }
  }
}
