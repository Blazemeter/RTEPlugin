package com.blazemeter.jmeter.rte.recorder.emulator;

import com.blazemeter.jmeter.rte.core.AttentionKey;
import com.blazemeter.jmeter.rte.core.CoordInput;
import com.blazemeter.jmeter.rte.core.Input;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Test {

  private static class TestListener implements TerminalEmulatorListener {

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

  public static void main(String[] args) {
    TerminalEmulator terminalEmulator = new Xtn5250TerminalEmulator();
    terminalEmulator.start();
    TestListener listener = new TestListener();
    terminalEmulator.addTerminalEmulatorListener(listener);
    boolean locked = false;
    terminalEmulator.setKeyboardLock(locked);

    try {
      terminalEmulator.setScreen(printHome("TEST"));
    } catch (Exception e) {
      e.printStackTrace();
    }

    while (true) {
      try {
        Thread.sleep(500);
        if (listener.isEvent()) {
          if (listener.getAttentionKey() == AttentionKey.ENTER) {
            terminalEmulator.setScreen(printFields(listener.getInputs()));
          } else if (listener.getAttentionKey() == AttentionKey.SYSRQ) {
            if (locked) {
              terminalEmulator.setScreen(printHome("KEYBOARD LOCKED: " + locked));
              locked = false;
              terminalEmulator.setKeyboardLock(locked);
            } else {
              terminalEmulator.setScreen(printHome("KEYBOARD LOCKED: " + locked));
              locked = true;
              terminalEmulator.setKeyboardLock(locked);
            }
          } else if (listener.getAttentionKey() == AttentionKey.F1) {
            Random r = new Random();
            int col = r.nextInt(81);
            int row = r.nextInt(25);
            terminalEmulator.setCursor(col, row);
            terminalEmulator.setStatusMessage("Cursor Moved");
            terminalEmulator
                .setScreen(printHome("YOUR CURSOR WAS MOVED TO col: " + col + " row: " + row));
          } else if (listener.getAttentionKey() == AttentionKey.F2) {
            terminalEmulator.setScreen(printHome("ALARM ALARM !!!"));
            terminalEmulator.soundAlarm();
          } else {
            terminalEmulator
                .setScreen(printHome("ATTENTION KEY: " + listener.getAttentionKey().toString()));
          }
        }
      } catch (InterruptedException ex) {
        break;
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  private static List<Segment> printHome(String message) {
    List<Segment> segments = new ArrayList<>();
    segments.add(new Segment("*****************************************", 20, 1));
    segments.add(new Segment("*", 20, 2));
    segments.add(new Segment("*", 20, 3));
    segments.add(new Segment("*", 20, 4));
    segments.add(new Segment("*", 60, 2));
    segments.add(new Segment("*", 60, 3));
    segments.add(new Segment("*", 60, 4));
    segments.add(new Segment(message, 25, 3));
    segments.add(new Segment("*****************************************", 20, 5));

    segments.add(new Segment("SYSRQ - Lock Keyboard", 1, 7));
    segments.add(new Segment("ENTER - Print Fields", 1, 8));
    segments.add(new Segment("F1 - Set Cursor", 1, 9));
    segments.add(new Segment("F2 - Sound Alarm", 1, 10));

    segments.add(new Segment("Field1: ", 1, 13));
    segments.add(new Field("", 15, 8, 13));
    segments.add(new Segment("Field2: ", 1, 14));
    segments.add(new Field("", 15, 8, 14));
    segments.add(new Segment("Field3: ", 1, 15));
    segments.add(new Field("", 15, 8, 15));

    return segments;
  }

  private static List<Segment> printFields(
      List<Input> inputs) {
    List<Segment> segments = new ArrayList<>();
    segments.add(new Segment("*******************************************************", 20, 1));
    int i = 2;
    for (Input in : inputs) {
      segments.add(new Segment(
          "Row: " + ((CoordInput) in).getPosition().getRow() + " Column: " + ((CoordInput) in)
              .getPosition().getColumn() + " Value: " + in.getInput(), 35, i));
      i++;
    }
    segments.add(new Segment("*******************************************************", 20, i));

    segments.add(new Segment("SYSRQ - Lock Keyboard", 1, 7));
    segments.add(new Segment("ENTER - Print Fields", 1, 8));
    segments.add(new Segment("F1 - Set Cursor", 1, 9));
    segments.add(new Segment("F2 - Sound Alarm", 1, 10));

    segments.add(new Segment("Field1: ", 1, 13));
    segments.add(new Field("", 15, 8, 13));
    segments.add(new Segment("Field2: ", 1, 14));
    segments.add(new Field("", 15, 8, 14));
    segments.add(new Segment("Field3: ", 1, 15));
    segments.add(new Field("", 15, 8, 15));

    return segments;
  }

}
