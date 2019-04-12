package com.blazemeter.jmeter.rte.recorder.emulator;

import com.blazemeter.jmeter.rte.core.Screen;

public interface TerminalEmulator {

  void start();

  void stop();

  void setCursor(int row, int col);

  void setScreen(Screen screen);

  void soundAlarm();

  void setStatusMessage(String message);

  void setKeyboardLock(boolean lock);

  void addTerminalEmulatorListener(TerminalEmulatorListener terminalEmulatorListener);

}
