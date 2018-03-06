package com.blazemeter.jmeter.rte.protocols.tn5250;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.net.Socket;
import java.security.GeneralSecurityException;
import net.infordata.em.tnprot.XITelnet;

public class SSLTelnet extends XITelnet {

  private static final int READ_BUFFER_SIZE_BYTES = 1024;
  private SSLData sslData;

  public SSLTelnet(String aHost, int aPort, SSLData sslData) {
    super(aHost, aPort);
    this.sslData = sslData;
  }

  public synchronized void connect() {
    if (getIvUsed()) {
      throw new IllegalStateException("XITelnet cannot be recycled");
    } else {
      this.disconnect();
      this.connecting();
      SSLConnection sslConnection = new SSLConnection(sslData.getSslType(),
          sslData.getPassword(), sslData.getKeyStorePath());
      try {
        sslConnection.start();
        Socket ivSocket = sslConnection.createSocket(this.getHost(), this.getPort());
        //In XITelnet is used ivFirstHost but we are not supposed to use hosts with
        // firstHostIp#SecondHostIp format in JMeter
        setIvSocket(ivSocket);
        InputStream ivIn = getIvSocket().getInputStream();
        setIvIn(ivIn);
        OutputStream ivOut = getIvSocket().getOutputStream();
        setIvOut(ivOut);
        SSLTelnet.RxThread ivReadTh = new SSLTelnet.RxThread();
        ivReadTh.start();
        setivUsed(true);
        this.connected();
      } catch (GeneralSecurityException | IOException e) {
        e.printStackTrace();
      }
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
      throw new RuntimeException(e);
    }
  }

  private <T> T getField(String fieldName, Class<T> clazz) {
    try {
      return clazz.cast(getAccessibleField(fieldName).get(this));
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }

  private void setField(String fieldName, Object value) {
    Field target = getAccessibleField(fieldName);
    try {
      target.set(this, value);
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }

  private void setIvSocket(Socket ivSocket) {
    setField("ivSocket", ivSocket);
  }

  private Socket getIvSocket() {
    return getField("ivSocket", Socket.class);
  }

  private void setIvIn(InputStream ivIn) {
    setField("ivIn", ivIn);
  }

  private InputStream getIvIn() {
    return getField("ivIn", InputStream.class);
  }

  private void setIvOut(OutputStream ivOut) {
    setField("ivOut", ivOut);
  }

  private void setivUsed(boolean ivUsed) {
    setField("ivUsed", ivUsed);
  }

  private boolean getIvUsed() {
    return getField("ivUsed", boolean.class);
  }

  private int getivIACParserStatus() {
    return getField("ivIACParserStatus", int.class);
  }

  //This class implements the Receptor Thread of the SSL Telnet connection.
  //It's a copy of XITelnet().RxThread() class.
  class RxThread extends Thread {
    private boolean ivTerminate = false;

    RxThread() {
      super("SSLTelnet rx thread");
    }

    public void terminate() {
      this.ivTerminate = true;
      if (this != Thread.currentThread()) {
        this.interrupt();
      }
    }

    public void run() {
      byte[] buf = new byte[READ_BUFFER_SIZE_BYTES];
      byte[] rBuf = new byte[READ_BUFFER_SIZE_BYTES];
      boolean var3 = false;

      try {
        while (!this.ivTerminate) {
          int len;
          try {
            len = getIvIn().read(buf);
          } catch (InterruptedIOException varvii) {
            len = 0;
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
                  SSLTelnet.this.receivedData(rBuf, j);
                }

                j = 0;
              }

              j += SSLTelnet.this.processIAC(buf[i]);
            }
          }

          if (j > 0) {
            SSLTelnet.this.receivedData(rBuf, j);
          }
        }
      } catch (IOException varviii) {
        if (!this.ivTerminate) {
          SSLTelnet.this.catchedIOException(varviii);
        }
      }

    }
  }
}
