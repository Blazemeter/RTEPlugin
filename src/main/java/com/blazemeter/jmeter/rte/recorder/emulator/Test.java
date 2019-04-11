package com.blazemeter.jmeter.rte.recorder.emulator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Test {

  public static void main(String[] args) {
    TerminalEmulator terminalEmulator = new Xtn5250TerminalEmulator();
    terminalEmulator.start();
    TestEmulatorListener listener = new TestEmulatorListener();
    terminalEmulator.addGUITerminalListener(listener);
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
            segments.add(new Segment("Segment" + i, col, row));
          } else {
            segments.add(new Field("Field" + i, 15, col, row));
          }
        }
        terminalEmulator.setScreen(segments);
        Thread.sleep(100000000);
      } catch (InterruptedException ex) {
        break;
      }
    }
  }
}
