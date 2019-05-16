package com.blazemeter.jmeter.rte.recorder;

import com.blazemeter.jmeter.rte.core.RteProtocolClient;
import com.blazemeter.jmeter.rte.core.listener.TerminalStateListener;
import com.blazemeter.jmeter.rte.recorder.emulator.TerminalEmulator;

public class TerminalEmulatorUpdater implements TerminalStateListener {

  private final TerminalEmulator terminalEmulator;
  private final RteProtocolClient terminalClient;

  public TerminalEmulatorUpdater(TerminalEmulator terminalEmulator,
      RteProtocolClient terminalClient) {
    this.terminalEmulator = terminalEmulator;
    this.terminalClient = terminalClient;
  }

  @Override
  public void onTerminalStateChange() {
    terminalEmulator.setScreen(terminalClient.getScreen());
    terminalClient.getCursorPosition().ifPresent(cursorPosition -> terminalEmulator
        .setCursor(cursorPosition.getRow(), cursorPosition.getColumn()));
    terminalEmulator.setKeyboardLock(terminalClient.isInputInhibited());
    if (terminalClient.isAlarmOn()) {
      terminalEmulator.soundAlarm();
    }
  }

  @Override
  public void onException(Throwable e) {
    terminalEmulator.setStatusMessage(e.getMessage());
  }

}
