package com.blazemeter.jmeter.rte.protocols.tn5250;

import com.blazemeter.jmeter.rte.core.RteIOException;
import java.io.IOException;
import java.lang.reflect.Field;
import net.infordata.em.tn5250.XI5250Emulator;
import net.infordata.em.tnprot.XITelnet;
import net.infordata.em.tnprot.XITelnetEmulator;

/**
 * This class was created because it's necessary to have an Emulator instance in which it could be
 * possible to create an XITelnet instance with "port" attribute inside setActive method. Using
 * setActive method from XI5250Emulator class the connection will be done always through port 23.
 */
public class ConfigurablePortEmulator extends XI5250Emulator {

  private int port;

  @Override
  public void setActive(boolean activate) {
    boolean wasActive;
    synchronized (this) {
      wasActive = isActive();
      if (activate == wasActive) {
        return;
      }

      if (activate) {
        XITelnet ivTelnet = new XITelnet(getHost(), port);
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

  /*It was necessary to use reflection because XI520Emulator class has ivTelnet as a private
  attribute without set and
  get methods*/
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

  private XITelnet getIvTelnet() {
    try {
      return (XITelnet) getAccessibleIvTelnetField().get(this);
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }

  public synchronized void setPort(int port) {
    if (port == this.port) {
      return;
    }

    setActive(false);
    this.port = port;
  }

  private class TelnetEmulator implements XITelnetEmulator {

    public final void connecting() {
      ConfigurablePortEmulator.this.connecting();
    }

    public final void connected() {
      ConfigurablePortEmulator.this.connected();
    }

    public final void disconnected() {
      ConfigurablePortEmulator.this.disconnected();
    }

    public final void catchedIOException(IOException ex) throws RteIOException {
      ConfigurablePortEmulator.this.catchedIOException(ex);
      throw new RteIOException(ex);
    }

    public final void receivedData(byte[] buf, int len) {
      ConfigurablePortEmulator.this.receivedData(buf, len);
    }

    public final void receivedEOR() {
      ConfigurablePortEmulator.this.receivedEOR();
    }

    public final void unhandledRequest(byte aIACOpt, String aIACStr) {
      ConfigurablePortEmulator.this.unhandledRequest(aIACOpt, aIACStr);
    }

    public final void localFlagsChanged(byte aIACOpt) {
      ConfigurablePortEmulator.this.localFlagsChanged(aIACOpt);
    }

    public final void remoteFlagsChanged(byte aIACOpt) {
      ConfigurablePortEmulator.this.remoteFlagsChanged(aIACOpt);
    }
  }
}
