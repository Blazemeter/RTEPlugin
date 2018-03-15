package com.blazemeter.jmeter.rte.core.ssl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManagerFactory;

public class SSLConnection {

  private final SSLType sslType;
  private final char[] ksPwd;
  private SSLContext sslctx;
  private String keyStorePath;

  public SSLConnection(SSLType sslType, String pwd, String keyStorePath) {
    this.sslType = sslType;
    this.ksPwd = pwd.toCharArray();
    this.keyStorePath = keyStorePath;
  }

  public void start() throws GeneralSecurityException, IOException {
    KeyStore keystore = buildkeyStore();
    KeyManagerFactory keymf = buildkeyManagerFactory(keystore);
    KeyStore trustedStore = buildtrustedStore();
    TrustManagerFactory trustmf = buildtrustManagerFactory(trustedStore);
    buildSSLContext(sslType, keymf, trustmf);
  }

  private void buildSSLContext(SSLType sslType,
                               KeyManagerFactory keymf, TrustManagerFactory trustmf)
      throws GeneralSecurityException {
    sslctx = SSLContext.getInstance(sslType.toString());
    sslctx.init(keymf.getKeyManagers(), trustmf.getTrustManagers(), null);
  }

  private TrustManagerFactory buildtrustManagerFactory(KeyStore trustedStore)
      throws GeneralSecurityException {
    TrustManagerFactory trustmf =
        TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
    trustmf.init(trustedStore);
    return trustmf;
  }

  private KeyStore buildtrustedStore() throws GeneralSecurityException, IOException {
    File tsFile = new File(keyStorePath); //Theoretically it's ok to use the same file
    // for keystore and truststore
    KeyStore trustedStore = KeyStore.getInstance(KeyStore.getDefaultType());
    trustedStore.load(new FileInputStream(tsFile), ksPwd);
    return trustedStore;
  }

  private KeyManagerFactory buildkeyManagerFactory(KeyStore keystore)
      throws GeneralSecurityException {
    KeyManagerFactory keymf =
        KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
    keymf.init(keystore, ksPwd);
    return keymf;
  }

  private KeyStore buildkeyStore() throws GeneralSecurityException, IOException {
    File ksFile = new File(keyStorePath);
    KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
    if (ksFile.exists()) {
      keystore.load(new FileInputStream(ksFile), ksPwd);
    } else {
      keystore.load(null, ksPwd);
    }
    return keystore;
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
