package com.blazemeter.jmeter.rte.terminal;

import com.blazemeter.jmeter.rte.core.AttentionKey;
import com.blazemeter.jmeter.rte.core.CoordInput;
import com.blazemeter.jmeter.rte.core.Input;
import com.blazemeter.jmeter.rte.core.Position;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
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
import javax.swing.JLabel;
import javax.swing.JPanel;
import net.infordata.em.crt5250.XI5250Crt;
import net.infordata.em.crt5250.XI5250Field;

public class Xtn5250TerminalGUI extends XI5250Crt implements GUITerminal {

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

  private static final String TITLE = "Recorder";
  private static final int COLUMN = 81;
  private static final int ROWS = 25;
  private static final int WIDTH = 728;
  private static final int HEIGHT = 512;
  private static final Color BACKGROUND = Color.black;

  private JLabel column = new JLabel();
  private JLabel row = new JLabel();
  private JLabel message = new JLabel();
  private JPanel status;

  private List<GUITerminalListener> guiTerminalListeners = new ArrayList<>();
  private boolean locked = false;

  @Override
  public void start() {
    setCrtSize(COLUMN, ROWS);
    setDefBackground(BACKGROUND);
    setReferenceCursor(true);
    setBlinkingCursor(true);
    setEnabled(true);
    JFrame frm = new JFrame(TITLE);
    frm.setLayout(new BorderLayout());
    frm.add(this, BorderLayout.CENTER);
    status = new JPanel();
    status.setLayout(new FlowLayout());
    column.setText("0");
    row.setText("0");
    message.setText("");
    status.add(column);
    status.add(row);
    status.add(message);
    frm.add(status, BorderLayout.SOUTH);
    frm.addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosed(WindowEvent e) {
        for (GUITerminalListener g : guiTerminalListeners) {
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

  }

  @Override
  public void setCursor(int col, int row) {
    this.setCursorPos(col, row);
    updateStatusBarCursorPosition(col, row);
  }

  private void updateStatusBarCursorPosition(int col, int row) {
    this.column.setText(Integer.toString(col));
    this.row.setText(Integer.toString(row));
    this.status.repaint();
  }

  @Override
  public void setScreen(List<Segment> segments) {
    clear();
    for (Segment s : segments) {
      if (s instanceof Field) {
        Field f = (Field) s;
        XI5250Field xi5250Field = new XI5250Field(this, f.getColumn(), f.getRow(),
            f.getLength(), 32);
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
    Toolkit.getDefaultToolkit().beep();
  }

  @Override
  public void setStatusMessage(String message) {
    this.message.setText(message);
    this.status.repaint();
  }

  @Override
  public void setKeyboardLock(boolean lock) {
    this.locked = lock;
  }

  @Override
  public void addGUITerminalListener(GUITerminalListener guiTerminalListener) {
    guiTerminalListeners.add(guiTerminalListener);

  }

  @Override
  protected synchronized void processKeyEvent(KeyEvent e) {
    AttentionKey attentionKey = null;
    if (e.getID() == KeyEvent.KEY_PRESSED) {
      attentionKey = KEY_EVENTS
          .get(new KeyEventMap(e.getModifiers(), e.getKeyCode()));
      if (attentionKey != null) {
        for (GUITerminalListener g : guiTerminalListeners) {
          List<Input> fields = new ArrayList<>();
          for (XI5250Field f : getFields()) {
            fields.add(new CoordInput(new Position(f.getRow(), f.getCol()), f.getString()));
          }
          g.onAttentionKey(attentionKey, fields);
        }
      } else if (e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_DOWN
          || e.getKeyCode() == KeyEvent.VK_LEFT || e.getKeyCode() == KeyEvent.VK_RIGHT
          || e.getKeyCode() == KeyEvent.VK_TAB) {

      }
    }
    if (!locked || attentionKey != null) {
      super.processKeyEvent(e);
      updateStatusBarCursorPosition(this.getCursorCol(), this.getCursorRow());
    }
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
