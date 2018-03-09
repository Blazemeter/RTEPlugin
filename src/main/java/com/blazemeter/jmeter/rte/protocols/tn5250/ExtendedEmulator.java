package com.blazemeter.jmeter.rte.protocols.tn5250;

import com.blazemeter.jmeter.rte.core.RteIOException;
import com.blazemeter.jmeter.rte.core.SSLType;

import com.blazemeter.jmeter.rte.protocols.tn5250.ssl.SSLData;
import com.blazemeter.jmeter.rte.protocols.tn5250.ssl.SSLTelnet;
import java.io.IOException;
import java.lang.reflect.Field;
import net.infordata.em.tn5250.XI5250Emulator;
import net.infordata.em.tn5250.XI5250EmulatorEvent;
import net.infordata.em.tnprot.XITelnet;
import net.infordata.em.tnprot.XITelnetEmulator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Allows configuring a port to be used in {@link XI5250Emulator} connections, capture
 * exceptions to later on throw them to client code, and handle SSL connections.
 */
public class ExtendedEmulator extends XI5250Emulator {

  private static final Logger LOG = LoggerFactory.getLogger(ExtendedEmulator.class);

  private int port;
  private SSLData sslData;

  /**
   * Since we need to make aware the sampler of any communication problem raised in xtn5250 library
   * we need to capture any exception on it and re throw it when an operation is used. If we had
   * just thrown an encapsulated error in catchedIOException and catchedException we could break
   * some background thread, and the library has no simple way to capture such errors and rethrow
   * them to client code.
   */
  private Throwable pendingError;

  public synchronized void setPort(int port) {
    if (port == this.port) {
      return;
    }

    setActive(false);
    this.port = port;
  }

  @Override
  public void setActive(boolean activate) {
    boolean wasActive;
    synchronized (this) {
      wasActive = isActive();
      if (activate == wasActive) {
        return;
      }
      if (activate) {
        XITelnet ivTelnet = (sslData.getSslType() == SSLType.NONE)
            ? new XITelnet(getHost(), port)
            : new SSLTelnet(getHost(), port, sslData);
        setIvTelnet(ivTelnet);
        ivTelnet.setEmulator(new TelnetEmulator());
        ivTelnet.connect();
      } else {
        XITelnet ivTelnet = getIvTelnet();
        ivTelnet.disconnect();
        ivTelnet.setEmulator(null);
        setIvTelnet(null);
      }
    }
    firePropertyChange(ACTIVE, wasActive, isActive());
  }

  /*
  It was necessary to use reflection because XI520Emulator class
  has ivTelnet as a private attribute without set and get methods
  */
  private XITelnet getIvTelnet() {
    try {
      return (XITelnet) getAccessibleIvTelnetField().get(this);
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }

  private void setIvTelnet(XITelnet ivTelnet) {
    Field target = getAccessibleIvTelnetField();
    try {
      target.set(this, ivTelnet);
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }

  private Field getAccessibleIvTelnetField() {
    try {
      Field target = XI5250Emulator.class.getDeclaredField("ivTelnet");
      target.setAccessible(true);
      return target;
    } catch (NoSuchFieldException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  protected void catchedIOException(IOException ex) {
    setPendingError(ex);
  }

  private synchronized void setPendingError(Throwable ex) {
    if (pendingError == null) {
      pendingError = ex;
      processEmulatorEvent(new XI5250EmulatorEvent(XI5250EmulatorEvent.STATE_CHANGED, this));
    } else {
      LOG.error("Exception ignored in step result due to previously thrown exception", ex);
    }
  }

  @Override
  protected void catchedException(Throwable ex) {
    setPendingError(ex);
  }

  public synchronized void throwAnyPendingError() throws RteIOException {
    if (pendingError != null) {
      Throwable ret = pendingError;
      pendingError = null;
      throw new RteIOException(ret);
    }
  }

  public void setSslData(SSLData sslData) {
    this.sslData = sslData;
  }

  public synchronized boolean hasPendingError() {
    return pendingError != null;
  }

  private class TelnetEmulator implements XITelnetEmulator {

    public final void connecting() {
      ExtendedEmulator.this.connecting();
    }

    public final void connected() {
      ExtendedEmulator.this.connected();
    }

    public final void disconnected() {
      ExtendedEmulator.this.disconnected();
    }

    public final void catchedIOException(IOException ex) {
      ExtendedEmulator.this.catchedIOException(ex);
    }

    public final void receivedData(byte[] buf, int len) {
      ExtendedEmulator.this.receivedData(buf, len);
    }

    public final void receivedEOR() {
      ExtendedEmulator.this.receivedEOR();
    }

    public final void unhandledRequest(byte aIACOpt, String aIACStr) {
      ExtendedEmulator.this.unhandledRequest(aIACOpt, aIACStr);
    }

    public final void localFlagsChanged(byte aIACOpt) {
      ExtendedEmulator.this.localFlagsChanged(aIACOpt);
    }

    public final void remoteFlagsChanged(byte aIACOpt) {
      ExtendedEmulator.this.remoteFlagsChanged(aIACOpt);
    }
  }

}
