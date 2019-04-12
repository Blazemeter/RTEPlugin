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
    //TODO change terminalEmulator to use 1 indexed row and column
    terminalClient.getCursorPosition().ifPresent(cursorPosition -> terminalEmulator
        .setCursor(cursorPosition.getRow() - 1, cursorPosition.getColumn() - 1));
    terminalEmulator.setKeyboardLock(terminalClient.isInputInhibited());
    if (terminalClient.isAlarmOn()) {
      terminalEmulator.soundAlarm();
    }
  }

}
