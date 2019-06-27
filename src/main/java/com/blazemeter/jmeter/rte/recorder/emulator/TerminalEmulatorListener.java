package com.blazemeter.jmeter.rte.recorder.emulator;

import com.blazemeter.jmeter.rte.core.AttentionKey;
import com.blazemeter.jmeter.rte.core.Input;
import com.blazemeter.jmeter.rte.core.wait.Area;
import java.util.List;

public interface TerminalEmulatorListener {

  void onCloseTerminal();

  void onAttentionKey(AttentionKey attentionKey, List<Input> inputs);

  void onWaitForText(Area area, String text);

}
