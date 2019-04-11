package com.blazemeter.jmeter.rte.emulator;

import java.util.List;

public interface TerminalEmulator {

  void start();

  void stop();

  void setCursor(int col, int row);

  void setScreen(List<Segment> segments);

  void soundAlarm();

  void setKeyboardLock(boolean lock);

  void addGUITerminalListener(TerminalEmulatorListener terminalEmulatorListener);

}
