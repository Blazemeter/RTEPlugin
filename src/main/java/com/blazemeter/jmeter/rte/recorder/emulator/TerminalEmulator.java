package com.blazemeter.jmeter.rte.recorder.emulator;

import com.blazemeter.jmeter.rte.core.AttentionKey;
import com.blazemeter.jmeter.rte.core.Screen;

public interface TerminalEmulator {

  void start();

  void stop();

  void setCursor(int row, int col);

  void setScreen(Screen screen);

  void soundAlarm();

  void setKeyboardLock(boolean lock);

  void setScreenSize(int columns, int rows);

  void addTerminalEmulatorListener(TerminalEmulatorListener terminalEmulatorListener);

  void setSupportedAttentionKeys(AttentionKey[] supportedAttentionKeys);
  
  void setStateMessageUpdate(String message);
}
