package com.blazemeter.jmeter.rte.terminal;

import java.util.List;

public interface GUITerminal {

  void start();

  void stop();

  void setCursor(int col, int row);

  void setScreen(List<Segment> segments);

  void soundAlarm();

  void setKeyboardLock(boolean lock);

  void addGUITerminalListener(GUITerminalListener guiTerminalListener);

}
