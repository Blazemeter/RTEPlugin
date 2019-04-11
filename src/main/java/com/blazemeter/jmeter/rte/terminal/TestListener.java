package com.blazemeter.jmeter.rte.terminal;

import com.blazemeter.jmeter.rte.core.AttentionKey;
import com.blazemeter.jmeter.rte.core.Input;
import java.util.List;

public class TestListener implements GUITerminalListener {

  private boolean closed = false;
  private AttentionKey attentionKey;
  private List<Input> inputs;
  private boolean event = false;

  public AttentionKey getAttentionKey() {
    return attentionKey;
  }

  public List<Input> getInputs() {
    return inputs;
  }

  public boolean isEvent() {
    boolean ret = event;
    event = false;
    return ret;
  }

  @Override
  public void onCloseTerminal() {
    closed = true;
    System.out.println("Terminal Closed");
  }

  @Override
  public void onAttentionKey(AttentionKey attentionKey, List<Input> inputs) {
    this.attentionKey = attentionKey;
    this.inputs = inputs;
    event = true;
  }

  public boolean isClosed() {
    return closed;
  }
}
