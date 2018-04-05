package com.blazemeter.jmeter.rte.protocols.tn3270;

import com.bytezone.dm3270.streams.BufferListener;
import com.bytezone.dm3270.streams.TerminalServer;
import com.bytezone.dm3270.streams.TelnetSocket.Source;
import com.bytezone.dm3270.utilities.Dm3270Utility;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;

import com.blazemeter.jmeter.rte.core.ssl.SSLSocketFactory;
import com.blazemeter.jmeter.rte.core.ssl.SSLType;

public class ExtendedTerminalServer extends TerminalServer {

    private int connectionTimeoutMillis;
    private SSLType sslType;

    private final int serverPort;
    private final String serverURL;
    private Socket socket = new Socket();
    private InputStream serverIn;
    private OutputStream serverOut;

    private final byte[] buffer = new byte[4096];
    private int bytesRead;
    private volatile boolean running;

    private final boolean debug = false;
    private final BufferListener telnetListener;

    public ExtendedTerminalServer(String serverURL, int serverPort, BufferListener listener,
                                  SSLType sslType, int connectionTimeoutMillis) {
        super(serverURL, serverPort, listener);
        this.serverPort = serverPort;
        this.sslType = sslType;
        this.serverURL = serverURL;
        this.connectionTimeoutMillis = connectionTimeoutMillis;
        this.telnetListener = listener;
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

    @Override
    public void run() {
        try {
            socket = createSocket();
            serverIn = socket.getInputStream();
            serverOut = socket.getOutputStream();
            running = true;

            while (running) {
                if (Thread.interrupted()) {
                    System.out.println("TerminalServer interrupted");
                    break;
                }

                bytesRead = serverIn.read(buffer);
                if (bytesRead < 0) {
                    close();
                    break;
                }

                if (Thread.currentThread().isInterrupted())
                    System.out.println("TerminalServer was interrupted!");

                if (debug) {
                    System.out.println(toString());
                    System.out.println("reading:");
                    System.out.println(Dm3270Utility.toHex(buffer, 0, bytesRead));
                }

                byte[] message = new byte[bytesRead];
                System.arraycopy(buffer, 0, message, 0, bytesRead);
                telnetListener.listen(Source.SERVER, message, LocalDateTime.now(), true);
            }
        } catch (IOException ex) {
            if (running) {
                ex.printStackTrace();
                close();
            }
        } catch (GeneralSecurityException ex) {
            if (running) {
                ex.printStackTrace();
                close();
            }
        }
    }

    @Override
    public void close() {
        try {
            running = false;
            serverIn = null;
            serverOut = null;

            if (socket != null)
                socket.close();

            if (telnetListener != null)
                telnetListener.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

}