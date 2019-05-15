package com.blazemeter.jmeter.rte.recorder.emulator;

import com.blazemeter.jmeter.rte.core.AttentionKey;
import com.blazemeter.jmeter.rte.core.CoordInput;
import com.blazemeter.jmeter.rte.core.Input;
import com.blazemeter.jmeter.rte.core.Position;
import com.blazemeter.jmeter.rte.core.Screen;
import com.helger.commons.annotation.VisibleForTesting;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.swing.JFrame;
import net.infordata.em.crt5250.XI5250Crt;
import net.infordata.em.crt5250.XI5250Field;

public class Xtn5250TerminalEmulator implements TerminalEmulator {

  private static final Map<KeyEventMap, AttentionKey> KEY_EVENTS =
      new HashMap<KeyEventMap, AttentionKey>() {
        {
          put(new KeyEventMap(0, KeyEvent.VK_F1), AttentionKey.F1);
          put(new KeyEventMap(0, KeyEvent.VK_F2), AttentionKey.F2);
          put(new KeyEventMap(0, KeyEvent.VK_F3), AttentionKey.F3);
          put(new KeyEventMap(0, KeyEvent.VK_F4), AttentionKey.F4);
          put(new KeyEventMap(0, KeyEvent.VK_F5), AttentionKey.F5);
          put(new KeyEventMap(0, KeyEvent.VK_F6), AttentionKey.F6);
          put(new KeyEventMap(0, KeyEvent.VK_F7), AttentionKey.F7);
          put(new KeyEventMap(0, KeyEvent.VK_F8), AttentionKey.F8);
          put(new KeyEventMap(0, KeyEvent.VK_F9), AttentionKey.F9);
          put(new KeyEventMap(0, KeyEvent.VK_F10), AttentionKey.F10);
          put(new KeyEventMap(0, KeyEvent.VK_F11), AttentionKey.F11);
          put(new KeyEventMap(0, KeyEvent.VK_F12), AttentionKey.F12);
          put(new KeyEventMap(KeyEvent.SHIFT_MASK, KeyEvent.VK_F1), AttentionKey.F13);
          put(new KeyEventMap(KeyEvent.SHIFT_MASK, KeyEvent.VK_F2), AttentionKey.F14);
          put(new KeyEventMap(KeyEvent.SHIFT_MASK, KeyEvent.VK_F3), AttentionKey.F15);
          put(new KeyEventMap(KeyEvent.SHIFT_MASK, KeyEvent.VK_F4), AttentionKey.F16);
          put(new KeyEventMap(KeyEvent.SHIFT_MASK, KeyEvent.VK_F5), AttentionKey.F17);
          put(new KeyEventMap(KeyEvent.SHIFT_MASK, KeyEvent.VK_F6), AttentionKey.F18);
          put(new KeyEventMap(KeyEvent.SHIFT_MASK, KeyEvent.VK_F7), AttentionKey.F19);
          put(new KeyEventMap(KeyEvent.SHIFT_MASK, KeyEvent.VK_F8), AttentionKey.F20);
          put(new KeyEventMap(KeyEvent.SHIFT_MASK, KeyEvent.VK_F9), AttentionKey.F21);
          put(new KeyEventMap(KeyEvent.SHIFT_MASK, KeyEvent.VK_F10), AttentionKey.F22);
          put(new KeyEventMap(KeyEvent.SHIFT_MASK, KeyEvent.VK_F11), AttentionKey.F23);
          put(new KeyEventMap(KeyEvent.SHIFT_MASK, KeyEvent.VK_F12), AttentionKey.F24);
          put(new KeyEventMap(0, KeyEvent.VK_ENTER), AttentionKey.ENTER);
          put(new KeyEventMap(0, KeyEvent.VK_ESCAPE), AttentionKey.ATTN);
          put(new KeyEventMap(KeyEvent.META_MASK, KeyEvent.VK_F4), AttentionKey.CLEAR);
          put(new KeyEventMap(0, KeyEvent.VK_PAUSE), AttentionKey.CLEAR);
          put(new KeyEventMap(KeyEvent.SHIFT_MASK, KeyEvent.VK_ESCAPE), AttentionKey.SYSRQ);
          put(new KeyEventMap(KeyEvent.CTRL_MASK, KeyEvent.VK_CONTROL), AttentionKey.RESET);
          put(new KeyEventMap(0, KeyEvent.VK_PAGE_DOWN), AttentionKey.ROLL_UP);
          put(new KeyEventMap(0, KeyEvent.VK_PAGE_UP), AttentionKey.ROLL_DN);
          put(new KeyEventMap(KeyEvent.META_MASK, KeyEvent.VK_F1), AttentionKey.PA1);
          put(new KeyEventMap(KeyEvent.META_MASK, KeyEvent.VK_F2), AttentionKey.PA2);
          put(new KeyEventMap(KeyEvent.META_MASK, KeyEvent.VK_F3), AttentionKey.PA3);
          put(new KeyEventMap(KeyEvent.ALT_MASK, KeyEvent.VK_INSERT), AttentionKey.PA1);
          put(new KeyEventMap(KeyEvent.ALT_MASK, KeyEvent.VK_HOME), AttentionKey.PA1);
          put(new KeyEventMap(KeyEvent.SHIFT_MASK, KeyEvent.VK_PAGE_UP), AttentionKey.PA3);
        }
      };

  private static final String TITLE = "Recorder";
  private static final Color BACKGROUND = Color.black;
  private static final int DEFAULT_FONT_SIZE = 14;

  private List<TerminalEmulatorListener> terminalEmulatorListeners = new ArrayList<>();
  private boolean locked = false;
  private JFrame frame;
  private XI5250Crt xi5250Crt;
  private StatusPanel statusPanel = new StatusPanel();
  private boolean stopping;

  @Override
  public void start(int columns, int rows) {
    xi5250Crt = new XI5250Crt() {
      @Override
      protected void processKeyEvent(KeyEvent e) {
        synchronized (Xtn5250TerminalEmulator.this) {
          AttentionKey attentionKey = null;
          if (e.getID() == KeyEvent.KEY_PRESSED) {
            attentionKey = KEY_EVENTS
                .get(new KeyEventMap(e.getModifiers(), e.getKeyCode()));
            if (attentionKey != null) {

              List<Input> fields = getInputFields();
              for (TerminalEmulatorListener listener : terminalEmulatorListeners) {
                listener.onAttentionKey(attentionKey, fields);
              }
            }
          }
          if (!locked || attentionKey != null) {
            //By default XI5250Crt only move the cursor when the backspace key is pressed and delete
            // when shift mask is enabled, in this way allways delete
            if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
              e.setModifiers(KeyEvent.SHIFT_MASK);
            }
            super.processKeyEvent(e);
            statusPanel
                .updateStatusBarCursorPosition(this.getCursorRow() + 1, this.getCursorCol() + 1);
          }
        }
      }

      @Override
      public void paintComponent(Graphics g) {
        synchronized (Xtn5250TerminalEmulator.this) {
          super.paintComponent(g);
        }
      }
    };
    xi5250Crt.setName("Terminal");
    xi5250Crt.setCrtSize(columns, rows);
    xi5250Crt.setDefBackground(BACKGROUND);
    xi5250Crt.setBlinkingCursor(true);
    xi5250Crt.setEnabled(true);

    frame = new JFrame(TITLE);
    frame.setLayout(new BorderLayout());
    frame.add(xi5250Crt, BorderLayout.CENTER);
    frame.add(statusPanel, BorderLayout.SOUTH);
    frame.addWindowListener(new WindowAdapter() {

      @Override
      public void windowOpened(WindowEvent e) {
        Dimension testSize = calculateCrtDefaultSize();
        xi5250Crt.setSize(testSize.width, testSize.height);
        frame.pack();
        xi5250Crt.requestFocus();
        System.out.println("xi5250Crt - " + xi5250Crt.getSize());
        System.out.println("statusPanel - " + statusPanel.getSize());
      }

      @Override
      public void windowClosed(WindowEvent e) {
        if (!stopping) {
          for (TerminalEmulatorListener g : terminalEmulatorListeners) {
            g.onCloseTerminal();
          }
        }
        statusPanel.dispose();
      }
    });

    frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    frame.setVisible(true);
  }

  private Dimension calculateCrtDefaultSize() {
    FontMetrics fm = xi5250Crt.getFontMetrics(
        new Font(xi5250Crt.getFont().getName(), xi5250Crt.getFont().getStyle(),
            DEFAULT_FONT_SIZE));
    return new Dimension(fm.charWidth('W') * xi5250Crt.getCrtSize().width,
        fm.getHeight() * xi5250Crt.getCrtSize().height);
  }

  @Override
  public void stop() {
    stopping = true;
    frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
  }

  @Override
  public void setCursor(int row, int col) {
    xi5250Crt.setCursorPos(col - 1, row - 1);
    this.statusPanel.updateStatusBarCursorPosition(row, col);
  }

  @Override
  public synchronized void setScreen(Screen screen) {
    xi5250Crt.clear();
    xi5250Crt.removeFields();
    for (Screen.Segment s : screen.getSegments()) {
      if (s.isEditable()) {
        XI5250Field xi5250Field = new XI5250Field(xi5250Crt, s.getColumn() - 1, s.getRow() - 1,
            s.getText().length(), 32);
        xi5250Field.setString(s.getText());
        xi5250Field.resetMDT();
        xi5250Crt.addField(xi5250Field);
      } else {
        xi5250Crt.drawString(s.getText(), s.getColumn() - 1, s.getRow() - 1);
      }
    }
    xi5250Crt.initAllFields();
  }

  @VisibleForTesting
  public String getScreen() {
    int height = xi5250Crt.getCrtSize().height;
    int width = xi5250Crt.getCrtSize().width;
    StringBuilder screen = new StringBuilder();
    for (int i = 0; i < height; i++) {
      screen.append(xi5250Crt.getString(0, i, width).replaceAll("[\\x00-\\x19]", " "));
      screen.append("\n");
    }
    return screen.toString();
  }

  @Override
  public void soundAlarm() {
    Toolkit.getDefaultToolkit().beep();
    statusPanel.soundAlarm();
  }

  @Override
  public void setStatusMessage(String message) {
    this.statusPanel.setStatusMessage(message);
  }

  @Override
  public void setKeyboardLock(boolean lock) {
    this.locked = lock;
    this.statusPanel.setKeyboardStatus(lock);
  }

  @Override
  public void addTerminalEmulatorListener(TerminalEmulatorListener terminalEmulatorListener) {
    terminalEmulatorListeners.add(terminalEmulatorListener);
  }

  private List<Input> getInputFields() {
    List<Input> fields = new ArrayList<>();
    for (XI5250Field f : xi5250Crt.getFields()) {
      if (f.isMDTOn()) {
        fields.add(new CoordInput(new Position(f.getRow() + 1, f.getCol() + 1),
            f.getTrimmedString()));
      }
    }
    return fields;
  }

  @VisibleForTesting
  public JFrame getFrame() {
    return this.frame;
  }

  private static class KeyEventMap {

    private final int modifier;
    private final int specialKey;

    KeyEventMap(int modifier, int specialKey) {
      this.modifier = modifier;
      this.specialKey = specialKey;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      KeyEventMap that = (KeyEventMap) o;
      return modifier == that.modifier &&
          specialKey == that.specialKey;
    }

    @Override
    public int hashCode() {
      return Objects.hash(modifier, specialKey);
    }

  }

}
