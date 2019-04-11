package com.blazemeter.jmeter.rte.terminal;

import com.blazemeter.jmeter.rte.core.AttentionKey;
import com.blazemeter.jmeter.rte.core.Input;
import java.util.List;

public interface GUITerminalListener {

  void onCloseTerminal();

  void onAttentionKey(AttentionKey attentionKey, List<Input> inputs);

}
