package com.blazemeter.jmeter.rte.protocols.tn5250;

import com.blazemeter.jmeter.rte.core.SSLType;

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

  public void setSslType(SSLType sslType) {
    this.sslType = sslType;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getKeyStorePath() {
    return keyStorePath;
  }

  public void setKeyStorePath(String keyStorePath) {
    this.keyStorePath = keyStorePath;
  }
}
