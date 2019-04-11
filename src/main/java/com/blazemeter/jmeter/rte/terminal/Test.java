package com.blazemeter.jmeter.rte.terminal;

import com.blazemeter.jmeter.rte.core.AttentionKey;
import com.blazemeter.jmeter.rte.core.CoordInput;
import com.blazemeter.jmeter.rte.core.Input;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Test {

  public static void main(String[] args) {
    GUITerminal guiTerminal = new Xtn5250TerminalGUI();
    guiTerminal.start();
    TestListener listener = new TestListener();
    guiTerminal.addGUITerminalListener(listener);
    boolean locked = false;
    guiTerminal.setKeyboardLock(locked);

    try {
      guiTerminal.setScreen(printHome("TEST"));
    } catch (Exception e) {
      e.printStackTrace();
    }

    while (true) {
      try {
        Thread.sleep(500);
        if (listener.isEvent()) {
          if (listener.getAttentionKey() == AttentionKey.ENTER) {
            guiTerminal.setScreen(printFields(listener.getInputs()));
          } else if (listener.getAttentionKey() == AttentionKey.SYSRQ) {
            if (locked) {
              guiTerminal.setScreen(printHome("KEYBOARD LOCKED: " + locked));
              locked = false;
              guiTerminal.setKeyboardLock(locked);
            } else {
              guiTerminal.setScreen(printHome("KEYBOARD LOCKED: " + locked));
              locked = true;
              guiTerminal.setKeyboardLock(locked);
            }
          } else if (listener.getAttentionKey() == AttentionKey.F1) {
            Random r = new Random();
            int col = r.nextInt(81);
            int row = r.nextInt(25);
            guiTerminal.setCursor(col, row);
            guiTerminal.setStatusMessage("Cursor Moved");
            guiTerminal
                .setScreen(printHome("YOUR CURSOR WAS MOVED TO col: " + col + " row: " + row));
          } else if (listener.getAttentionKey() == AttentionKey.F2) {
            guiTerminal
                .setScreen(printHome("ALARM ALARM !!!"));
            guiTerminal.soundAlarm();
          } else {
            guiTerminal.setScreen(printHome("ATTENTION KEY: " + listener.getAttentionKey().toString()));
          }
        }
      } catch (InterruptedException ex) {
        break;
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  public static List<Segment> printHome(String message) {
    List<Segment> segments = new ArrayList<>();
    segments.add(new Segment("*****************************************", true, 20, 1));
    segments.add(new Segment("*", true, 20, 2));
    segments.add(new Segment("*", true, 20, 3));
    segments.add(new Segment("*", true, 20, 4));
    segments.add(new Segment("*", true, 60, 2));
    segments.add(new Segment("*", true, 60, 3));
    segments.add(new Segment("*", true, 60, 4));
    segments.add(new Segment(message, true, 25, 3));
    segments.add(new Segment("*****************************************", true, 20, 5));

    segments.add(new Segment("SYSRQ - Lock Keyboard", true, 1, 7));
    segments.add(new Segment("ENTER - Print Fields", true, 1, 8));
    segments.add(new Segment("F1 - Set Cursor", true, 1, 9));
    segments.add(new Segment("F2 - Sound Alarm", true, 1, 10));

    segments.add(new Segment("Field1: ", true, 1, 13));
    segments.add(new Field("", true, 15, 8, 13));
    segments.add(new Segment("Field2: ", true, 1, 14));
    segments.add(new Field("", true, 15, 8, 14));
    segments.add(new Segment("Field3: ", true, 1, 15));
    segments.add(new Field("", true, 15, 8, 15));

    return segments;
  }


  public static List<Segment> printFields(
      List<Input> inputs) {
    List<Segment> segments = new ArrayList<>();
    segments
        .add(new Segment("*******************************************************", true, 20, 1));
    int i = 2;
    for (Input in : inputs) {
      segments
          .add(new Segment(
              "Row: " + ((CoordInput) in).getPosition().getRow() + " Column: " + ((CoordInput) in)
                  .getPosition().getColumn() + " Value: " + in.getInput(), true, 35, i));
      i++;
    }
    segments
        .add(new Segment("*******************************************************", true, 20, i));

    segments.add(new Segment("SYSRQ - Lock Keyboard", true, 1, 7));
    segments.add(new Segment("ENTER - Print Fields", true, 1, 8));
    segments.add(new Segment("F1 - Set Cursor", true, 1, 9));
    segments.add(new Segment("F2 - Sound Alarm", true, 1, 10));

    segments.add(new Segment("Field1: ", true, 1, 13));
    segments.add(new Field("", true, 15, 8, 13));
    segments.add(new Segment("Field2: ", true, 1, 14));
    segments.add(new Field("", true, 15, 8, 14));
    segments.add(new Segment("Field3: ", true, 1, 15));
    segments.add(new Field("", true, 15, 8, 15));

    return segments;
  }
}
