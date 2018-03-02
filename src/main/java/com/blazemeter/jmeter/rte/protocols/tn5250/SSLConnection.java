package com.blazemeter.jmeter.rte.protocols.tn5250;

import com.blazemeter.jmeter.rte.core.SSLType;
import java.io.File;
import java.io.FileInputStream;
import java.net.Socket;
import java.security.KeyStore;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManagerFactory;

public class SSLConnection {

  private static SSLType sslType;
  private  KeyStore keystore;
  private  KeyStore trustedStore;
  private  char[] ksPwd;
  private KeyManagerFactory keymf;
  private TrustManagerFactory trustmf;
  private SSLContext sslctx;
  private String keyStorePath;

  public SSLConnection(SSLType sslType, String pwd, String keyStorePath) {
    this.sslType = sslType;
    this.ksPwd = pwd.toCharArray();
    this.keyStorePath = keyStorePath;
  }

  public void start() {
    try {
      //KeyStore
      File ksFile = new File(keyStorePath);
      this.keystore = KeyStore.getInstance(KeyStore.getDefaultType());
      if (ksFile.exists()) {
        keystore.load(new FileInputStream(ksFile), ksPwd);
      } else {
        keystore.load(null, ksPwd);
      }
      //Key Manager Factory
      keymf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
      keymf.init(keystore, ksPwd);
      //trustStore
      File tsFile = new File(keyStorePath); //Theoretically it's ok to use the same file
      // for keystore and truststore
      this.trustedStore = KeyStore.getInstance(KeyStore.getDefaultType());
      this.trustedStore.load(new FileInputStream(tsFile), ksPwd);
      //Trust Manager Factory
      trustmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
      trustmf.init(trustedStore);
      //SSL Context
      sslctx = SSLContext.getInstance(sslType.toString());
      sslctx.init(keymf.getKeyManagers(), trustmf.getTrustManagers(), null);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public Socket createSocket(String host, int port) {
    SSLSocket socket = null;
    try {
      socket = (SSLSocket) sslctx.getSocketFactory().createSocket(
                    host, port);
      socket.startHandshake();
    } catch (Exception e) {
      e.printStackTrace();
    }
    return socket;
  }
}
