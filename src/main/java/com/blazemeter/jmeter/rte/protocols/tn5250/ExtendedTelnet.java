package com.blazemeter.jmeter.rte.protocols.tn5250;

import com.blazemeter.jmeter.rte.core.ssl.SSLData;
import com.blazemeter.jmeter.rte.core.ssl.SSLSocketFactory;
import com.blazemeter.jmeter.rte.core.ssl.SSLType;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.security.GeneralSecurityException;
import javax.net.ssl.SSLException;
import net.infordata.em.tnprot.XITelnet;

/**
 * Handle telnet connection by providing connection timeout and security on top of {@link
 * XITelnet}.
 */
public class ExtendedTelnet extends XITelnet {

  private static final int READ_BUFFER_SIZE_BYTES = 1024;
  private final int connectTimeoutMillis;
  private SSLData sslData;
  private RxThread readThread;

  public ExtendedTelnet(String aHost, int aPort, int connectTimeoutMillis, SSLData sslData) {
    super(aHost, aPort);
    this.connectTimeoutMillis = connectTimeoutMillis;
    this.sslData = sslData;
  }

  @Override
  public synchronized void connect() {
    if (getIvUsed()) {
      throw new IllegalStateException("XITelnet cannot be recycled");
    } else {
      this.disconnect();
      this.connecting();
      try {
        Socket ivSocket = createSocket();
        setIvSocket(ivSocket);
        InputStream ivIn = getIvSocket().getInputStream();
        setIvIn(ivIn);
        OutputStream ivOut = getIvSocket().getOutputStream();
        setIvOut(ivOut);
        readThread = new ExtendedTelnet.RxThread();
        readThread.start();
        setIvUsed(true);
        this.connected();
      } catch (IOException e) {
        catchedIOException(e);
      } catch (GeneralSecurityException e) {
        catchedIOException(new SSLException(e));
      }
    }
  }

  private Socket createSocket() throws IOException, GeneralSecurityException {
    if (sslData != null && sslData.getSslType() != SSLType.NONE) {
      SSLSocketFactory sslSocketFactory = new SSLSocketFactory(sslData.getSslType(),
          sslData.getPassword(), sslData.getKeyStorePath());
      sslSocketFactory.init();
      /*
      In XITelnet is used ivFirstHost instead of getHost(), but we are not supposed to use hosts
      with firstHostIp#SecondHostIp format in JMeter
       */
      return sslSocketFactory
          .createSocket(this.getHost(), this.getPort(), connectTimeoutMillis);
    } else {
      Socket socket = new Socket();
      socket.connect(new InetSocketAddress(this.getHost(), this.getPort()), connectTimeoutMillis);
      return socket;
    }
  }

  //It was required to use reflection on the following attributes as
  // they are private in XITelnet class.
  private Field getAccessibleField(String fieldName) {
    try {
      Field target = XITelnet.class.getDeclaredField(fieldName);
      target.setAccessible(true);
      return target;
    } catch (NoSuchFieldException e) {
      throw new RuntimeException(e); //NOSONAR
    }
  }

  private <T> T getField(String fieldName, Class<T> clazz) {
    try {
      return clazz.cast(getAccessibleField(fieldName).get(this));
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e); //NOSONAR
    }
  }

  private void setField(String fieldName, Object value) {
    Field target = getAccessibleField(fieldName);
    try {
      target.set(this, value);
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e); //NOSONAR
    }
  }

  private Socket getIvSocket() {
    return getField("ivSocket", Socket.class);
  }

  private void setIvSocket(Socket ivSocket) {
    setField("ivSocket", ivSocket);
  }

  private InputStream getIvIn() {
    return getField("ivIn", InputStream.class);
  }

  private void setIvIn(InputStream ivIn) {
    setField("ivIn", ivIn);
  }

  private void setIvOut(OutputStream ivOut) {
    setField("ivOut", ivOut);
  }

  private boolean getIvUsed() {
    return getField("ivUsed", Boolean.class);
  }

  private void setIvUsed(boolean ivUsed) {
    setField("ivUsed", ivUsed);
  }

  private int getivIACParserStatus() {
    return getField("ivIACParserStatus", Integer.class);
  }

  @Override
  public synchronized void disconnect() { //!!V 03/03/98
    if (readThread != null) {
      readThread.terminate();
      readThread = null;
    }
    closeIvSocket();
  }

  private void closeIvSocket() {
    try {
      Method method = XITelnet.class.getDeclaredMethod("closeSocket");
      method.setAccessible(true);
      method.invoke(this);
    } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
      throw new RuntimeException(e); //NOSONAR
    }
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
          // the input may be null if disconnect was invoked after the while condition evaluation
          if (input == null) {
            return;
          }
          int len = input.read(buf);

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
