package com.blazemeter.jmeter.rte.protocols.tn5250;

import com.blazemeter.jmeter.rte.core.SSLType;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.net.Socket;
import net.infordata.em.tnprot.XITelnet;

public class SSLTelnet extends XITelnet {

  private SSLType sslType;
  private String password;
  private String keyStorePath;

  public SSLTelnet(String aHost, int aPort, SSLType sslType,
                   String password, String keyStorePath) {
        super(aHost, aPort);
    this.sslType = sslType;
    this.password = password;
    this.keyStorePath = keyStorePath;
  }

  public synchronized void connect() {
    if (getIvUsed()) {
      throw new IllegalArgumentException("XITelnet cannot be recycled");
    } else {
      this.disconnect();
      this.connecting();

      try {
        SSLConnection sslConnection = new SSLConnection(sslType, password, keyStorePath);
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
      } catch (IOException vvar) {
        this.catchedIOException(vvar);
      }

    }
  }

  //It was required to implement reflection on the following attributes as
  // there are private in XITelnet class.
  private Field getAccessibleIvField(String fieldName) {
    try {
      Field target = XITelnet.class.getDeclaredField(fieldName);
      target.setAccessible(true);
      return target;
    } catch (NoSuchFieldException e) {
      throw new RuntimeException(e);
    }
  }

  private void setIvSocket(Socket ivSocket) {
    Field target = getAccessibleIvField("ivSocket");
    try {
      target.set(this, ivSocket);
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }

  private Socket getIvSocket() {
    try {
      return (Socket) getAccessibleIvField("ivSocket").get(this);
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }

  private void setIvIn(InputStream ivIn) {
    Field target = getAccessibleIvField("ivIn");
    try {
      target.set(this, ivIn);
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }

  private InputStream getIvIn() {
    try {
      return (InputStream) getAccessibleIvField("ivIn").get(this);
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }

  private void setIvOut(OutputStream ivOut) {
    Field target = getAccessibleIvField("ivOut");
    try {
      target.set(this, ivOut);
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }

  private void setivUsed(boolean ivUsed) {
    Field target = getAccessibleIvField("ivUsed");
    try {
      target.set(this, ivUsed);
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }

  private boolean getIvUsed() {
    try {
      return (boolean) getAccessibleIvField("ivUsed").get(this);
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }

  private int getivIACParserStatus() {
    try {
      return (int) getAccessibleIvField("ivIACParserStatus").get(this);
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }

  class RxThread extends Thread {
    private boolean ivTerminate = false;

    RxThread() {
      super("XITelnet rx thread");
    }

    public void terminate() {
      this.ivTerminate = true;
      if (this != Thread.currentThread()) {
        this.interrupt();
      }
    }

    public void run() {
      byte[] buf = new byte[1024];
      byte[] rBuf = new byte[1024];
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
