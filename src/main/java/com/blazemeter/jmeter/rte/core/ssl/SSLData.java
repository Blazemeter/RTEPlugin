package com.blazemeter.jmeter.rte.core.ssl;

import java.util.Objects;

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

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SSLData sslData = (SSLData) o;
    return sslType == sslData.sslType &&
        Objects.equals(password, sslData.password) &&
        Objects.equals(keyStorePath, sslData.keyStorePath);
  }

  @Override
  public int hashCode() {
    return Objects.hash(sslType, password, keyStorePath);
  }

  @Override
  public String toString() {
    return "SSLData{" +
        "sslType=" + sslType +
        ", password='" + password + '\'' +
        ", keyStorePath='" + keyStorePath + '\'' +
        '}';
  }

}
