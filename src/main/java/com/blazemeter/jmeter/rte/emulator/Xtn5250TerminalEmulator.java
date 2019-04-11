package com.blazemeter.jmeter.rte.emulator;

import com.blazemeter.jmeter.rte.core.AttentionKey;
import com.blazemeter.jmeter.rte.core.CoordInput;
import com.blazemeter.jmeter.rte.core.Input;
import com.blazemeter.jmeter.rte.core.Position;
import java.awt.Color;
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

public class Xtn5250TerminalEmulator extends XI5250Crt implements TerminalEmulator {

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
          put(new KeyEventMap(0, KeyEvent.VK_PAUSE), AttentionKey.CLEAR);
          put(new KeyEventMap(KeyEvent.SHIFT_MASK, KeyEvent.VK_ESCAPE), AttentionKey.SYSRQ);
          put(new KeyEventMap(KeyEvent.CTRL_MASK, KeyEvent.VK_CONTROL), AttentionKey.RESET);
          put(new KeyEventMap(0, KeyEvent.VK_PAGE_DOWN), AttentionKey.ROLL_UP);
          put(new KeyEventMap(0, KeyEvent.VK_PAGE_UP), AttentionKey.ROLL_DN);
        }
      };

  private static final int ST_NULL = -2;
  private static final int ST_NORMAL_UNLOCKED = 2;
  private static final String TITLE = "Recorder";
  private static final int COLUMN = 60;
  private static final int ROWS = 30;
  private static final int WIDTH = 728;
  private static final int HEIGHT = 512;
  private static final Color BACKGROUND = Color.black;

  private List<TerminalEmulatorListener> terminalEmulatorListeners = new ArrayList<>();

  @Override
  public void start() {
    setCrtSize(COLUMN, ROWS);
    setDefBackground(BACKGROUND);
    setReferenceCursor(true);
    setBlinkingCursor(true);
    setEnabled(true);
    JFrame frm = new JFrame(TITLE);
    frm.add(this);
    frm.addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosed(WindowEvent e) {
        for (TerminalEmulatorListener g : terminalEmulatorListeners) {
          g.onCloseTerminal();
        }
        System.exit(0);
      }
    });

    frm.setBounds(0, 0, WIDTH, HEIGHT);
    frm.setVisible(true);

  }

  @Override
  public void stop() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setCursor(int col, int row) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setKeyboardLock(boolean locked) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setScreen(List<Segment> segments) {
    clear();
    for (Segment s : segments) {
      if (s instanceof Field) {
        Field f = (Field) s;
        XI5250Field xi5250Field = new XI5250Field(this, f.getColumn(), f.getRow(),
            f.getLength(), -1);
        xi5250Field.setString(f.getText());
        addField(xi5250Field);
      } else {
        drawString(s.getText(), s.getColumn(), s.getRow());
      }

    }
    initAllFields();
  }

  @Override
  public void soundAlarm() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void addGUITerminalListener(TerminalEmulatorListener terminalEmulatorListener) {
    terminalEmulatorListeners.add(terminalEmulatorListener);
  }

  @Override
  protected synchronized void processKeyEvent(KeyEvent e) {
    if (e.getID() == KeyEvent.KEY_PRESSED) {
      AttentionKey attentionKey = KEY_EVENTS
          .get(new KeyEventMap(e.getModifiers(), e.getKeyCode()));
      if (attentionKey != null) {
        for (TerminalEmulatorListener g : terminalEmulatorListeners) {
          List<Input> fields = new ArrayList<>();
          for (XI5250Field f : getFields()) {
            fields.add(new CoordInput(new Position(f.getRow(), f.getCol()), f.getString()));
          }
          g.onAttentionKey(attentionKey, fields);
        }
      }
    }
    super.processKeyEvent(e);
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
