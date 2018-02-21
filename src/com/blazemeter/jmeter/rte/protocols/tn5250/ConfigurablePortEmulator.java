package com.blazemeter.jmeter.rte.protocols.tn5250;

import net.infordata.em.tn5250.XI5250Emulator;
import net.infordata.em.tnprot.XITelnet;
import net.infordata.em.tnprot.XITelnetEmulator;

import java.io.IOException;
import java.lang.reflect.Field;

public class ConfigurablePortEmulator extends XI5250Emulator {

    private int port;

    @Override
    public void setActive(boolean activate) {
        boolean wasActive;
        synchronized (this) {
            wasActive = isActive();
            if (activate == wasActive)
                return;

            //We have to use reflection because XI520Emulator class has ivTelnet as a private attribute without set and
            // get methods
            if (activate) {
                XITelnet ivTelnet = new XITelnet(getHost(),port);
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
        if (port == this.port )
            return;

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

        public final void catchedIOException(IOException ex) {
            ConfigurablePortEmulator.this.catchedIOException(ex);
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
