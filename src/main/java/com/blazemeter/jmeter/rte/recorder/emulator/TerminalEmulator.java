package com.blazemeter.jmeter.rte.recorder.emulator;

import java.util.List;

public interface TerminalEmulator {

  void start();

  void stop();

  void setCursor(int col, int row);

  void setScreen(List<Segment> segments);

  void soundAlarm();

  void setStatusMessage(String message);

  void setKeyboardLock(boolean lock);

  void addTerminalEmulatorListener(TerminalEmulatorListener terminalEmulatorListener);

}
