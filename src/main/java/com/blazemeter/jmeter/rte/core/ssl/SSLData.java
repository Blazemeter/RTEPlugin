package com.blazemeter.jmeter.rte.core.ssl;

public class SSLData {
  private SSLType sslType;
  private String password;
  private String keyStorePath;

  public SSLData(SSLType sslType, String password, String keyStorePath) {
    this.keyStorePath = keyStorePath;
    this.password = password;
    this.sslType = sslType;
  }

  public SSLType getSslType() {
    return sslType;
  }

  public String getPassword() {
    return password;
  }

  public String getKeyStorePath() {
    return keyStorePath;
  }

}
