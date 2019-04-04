package com.blazemeter.jmeter.rte.terminal;

import com.blazemeter.jmeter.rte.core.AttentionKey;
import com.blazemeter.jmeter.rte.core.CoordInput;
import com.blazemeter.jmeter.rte.core.Input;
import java.util.List;

public class TestListener implements GUITerminalListener {

  private boolean closed = false;

  @Override
  public void onCloseTerminal() {
    closed = true;
    System.out.println("Terminal Closed");
  }

  @Override
  public void onAttentionKey(AttentionKey attentionKey, List<Input> inputs) {
    System.out.println("Attention Key = " + attentionKey);
    for (Input i : inputs) {
      CoordInput c = (CoordInput) i;
      System.out.println(
          "Row: " + c.getPosition().getRow() + "Column: " + c.getPosition().getColumn() + "Value: "
              + c.getInput());
    }
  }

  public boolean isClosed() {
    return closed;
  }
}
