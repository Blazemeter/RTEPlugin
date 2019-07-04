package com.blazemeter.jmeter.rte.core.ssl;

import com.helger.commons.annotation.VisibleForTesting;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.SecureRandom;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import org.apache.jmeter.util.CustomX509TrustManager;

public class SSLContextFactory {

  private static final String KEY_STORE_PROPERTY = "javax.net.ssl.keyStore";
  private static final String KEY_STORE_PASSWORD_PROPERTY = "javax.net.ssl.keyStorePassword";
  private static final String TRUST_STORE_PROPERTY = "javax.net.ssl.trustStore";
  private static final String TRUST_STORE_PASSWORD_PROPERTY = "javax.net.ssl.trustStorePassword";

  private static final SecureRandom RAND = new SecureRandom();

  private SSLContextFactory() {

  }

  private static String getKeyStore() {
    return System.getProperty(KEY_STORE_PROPERTY);
  }

  @VisibleForTesting
  public static void setKeyStore(String keyStore) {
    System.setProperty(KEY_STORE_PROPERTY, keyStore);
  }

  private static String getKeyStorePassword() {
    return System.getProperty(KEY_STORE_PASSWORD_PROPERTY);
  }

  @VisibleForTesting
  public static void setKeyStorePassword(String keyStorePassword) {
    System.setProperty(KEY_STORE_PASSWORD_PROPERTY, keyStorePassword);
  }

  private static String getTrustStore() {
    return System.getProperty(TRUST_STORE_PROPERTY);
  }

  private static String getTrustStorePassword() {
    return System.getProperty(TRUST_STORE_PASSWORD_PROPERTY);
  }

  public static SSLContext buildSSLContext(SSLType sslType)
      throws GeneralSecurityException, IOException {
    SSLContext context = SSLContext.getInstance(sslType.toString());
    KeyManager[] keyManagers = buildKeyManagerFactory().getKeyManagers();
    TrustManager[] trustManagers = buildTrustManagerFactory().getTrustManagers();
    for (int i = 0; i < trustManagers.length; i++) {
      if (trustManagers[i] instanceof X509TrustManager) {
        trustManagers[i] = new CustomX509TrustManager((X509TrustManager) trustManagers[i]);
      }
    }
    context.init(keyManagers, trustManagers, RAND);
    return context;
  }

  private static KeyManagerFactory buildKeyManagerFactory()
      throws GeneralSecurityException, IOException {
    KeyManagerFactory factory = KeyManagerFactory
        .getInstance(KeyManagerFactory.getDefaultAlgorithm());
    String pass = getKeyStorePassword();
    factory.init(buildKeyStore(getKeyStore(), pass), pass == null ? null : pass.toCharArray());
    return factory;
  }

  private static KeyStore buildKeyStore(String storeFileName, String pass)
      throws GeneralSecurityException, IOException {
    FileInputStream storeInputStream = storeFileName != null && new File(storeFileName).exists()
        ? new FileInputStream(storeFileName)
        : null;
    KeyStore store = KeyStore.getInstance(KeyStore.getDefaultType());
    store.load(storeInputStream, pass == null ? null : pass.toCharArray());
    return store;
  }

  private static TrustManagerFactory buildTrustManagerFactory()
      throws GeneralSecurityException, IOException {
    TrustManagerFactory factory = TrustManagerFactory
        .getInstance(TrustManagerFactory.getDefaultAlgorithm());
    String trustStore = getTrustStore();
    factory.init(trustStore == null ? null : buildKeyStore(trustStore, getTrustStorePassword()));
    return factory;
  }

}
