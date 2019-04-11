package com.blazemeter.jmeter.rte.terminal;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Test {

  public static void main(String[] args) {
    GUITerminal guiTerminal = new Xtn5250TerminalGUI();
    guiTerminal.start();
    TestListener listener = new TestListener();
    guiTerminal.addGUITerminalListener(listener);
    while (true) {
      try {
        Random rand = new Random();
        List<Segment> segments = new ArrayList<>();
        if (listener.isClosed()) {
          System.exit(0);
        }
        for (int i = 0; i < 15; i++) {
          int col = rand.nextInt(40);
          int row = rand.nextInt(15);
          int type = rand.nextInt(2);
          if (type == 0) {
            segments.add(new Segment("Segment" + i, true, col, row));
          } else {
            segments.add(new Field("Field" + i, true, 15, col, row));
          }
        }
        guiTerminal.setScreen(segments);
        Thread.sleep(100000000);
      } catch (InterruptedException ex) {
        break;
      }
    }
  }
}
