package com.blazemeter.jmeter.rte.core;

import com.blazemeter.jmeter.rte.core.listener.ExceptionHandler;

public abstract class ServerDisconnectHandler {

  protected boolean isExpectedDisconnection;

  protected ServerDisconnectHandler(boolean isExpectedDisconnection) {
    this.isExpectedDisconnection = isExpectedDisconnection;
  }

  public boolean isExpectedDisconnection() {
    return this.isExpectedDisconnection;
  }

  public abstract void onDisconnection(ExceptionHandler handler);

}
