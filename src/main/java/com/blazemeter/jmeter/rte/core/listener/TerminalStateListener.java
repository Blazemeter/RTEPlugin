package com.blazemeter.jmeter.rte.core.listener;

public interface TerminalStateListener extends ExceptionListener {

  void onTerminalStateChange();

}
