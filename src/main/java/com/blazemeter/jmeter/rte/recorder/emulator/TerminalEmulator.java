package com.blazemeter.jmeter.rte.recorder.emulator;

import com.blazemeter.jmeter.rte.core.AttentionKey;
import com.blazemeter.jmeter.rte.core.RteProtocolClient;
import com.blazemeter.jmeter.rte.core.Screen;
import java.util.Set;

public interface TerminalEmulator {

  void start();

  void stop();

  void setCursor(int row, int col);

  void setScreenName(String screenName);

  void setScreen(Screen screen);

  void soundAlarm();

  void setKeyboardLock(boolean lock);

  void setScreenSize(int columns, int rows);

  void addTerminalEmulatorListener(TerminalEmulatorListener terminalEmulatorListener);

  void setSupportedAttentionKeys(Set<AttentionKey> supportedAttentionKeys);

  void setProtocolClient(RteProtocolClient terminalClient);
}
