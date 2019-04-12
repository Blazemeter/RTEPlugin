package com.blazemeter.jmeter.rte.recorder.emulator;

import com.blazemeter.jmeter.rte.core.AttentionKey;
import com.blazemeter.jmeter.rte.core.CoordInput;
import com.blazemeter.jmeter.rte.core.Input;
import com.blazemeter.jmeter.rte.core.Position;
import com.blazemeter.jmeter.rte.core.Screen;
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

  private static final String TITLE = "Recorder";
  private static final int COLUMNS = 80;
  private static final int ROWS = 24;
  private static final int WIDTH = 728;
  private static final int HEIGHT = 512;
  private static final Color BACKGROUND = Color.black;

  private JLabel column = new JLabel();
  private JLabel row = new JLabel();
  private JLabel message = new JLabel();
  private JPanel status;

  private List<TerminalEmulatorListener> terminalEmulatorListeners = new ArrayList<>();
  private boolean locked = false;
  private JFrame frame;

  @Override
  public void start() {
    setCrtSize(COLUMNS, ROWS);
    setDefBackground(BACKGROUND);
    setBlinkingCursor(true);
    setEnabled(true);
    frame = new JFrame(TITLE);
    frame.setLayout(new BorderLayout());
    frame.add(this, BorderLayout.CENTER);
    status = new JPanel();
    status.setLayout(new FlowLayout());
    column.setText("0");
    row.setText("0");
    message.setText("");
    status.add(column);
    status.add(row);
    status.add(message);
    frame.add(status, BorderLayout.SOUTH);
    frame.addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosed(WindowEvent e) {
        for (TerminalEmulatorListener g : terminalEmulatorListeners) {
          g.onCloseTerminal();
        }
      }
    });
    frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    frame.setBounds(0, 0, WIDTH, HEIGHT);
    frame.setVisible(true);

  }

  @Override
  public void stop() {
    frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
  }

  @Override
  public void setCursor(int row, int col) {
    this.setCursorPos(col - 1, row - 1);
    updateStatusBarCursorPosition(row, col);
  }

  private void updateStatusBarCursorPosition(int row, int col) {
    this.row.setText(Integer.toString(row));
    this.column.setText(Integer.toString(col));
    this.status.repaint();
  }

  @Override
  public void setScreen(Screen screen) {
    clear();
    for (Screen.Segment s : screen.getSegments()) {
      if (s instanceof Screen.Field) {
        Screen.Field f = (Screen.Field) s;
        XI5250Field xi5250Field = new XI5250Field(this, f.getColumn() - 1, f.getRow() - 1,
            f.getText().length(), 32);
        xi5250Field.setString(f.getText());
        xi5250Field.resetMDT();
        addField(xi5250Field);
      } else {
        drawString(s.getText(), s.getColumn() - 1, s.getRow() - 1);
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
  public void addTerminalEmulatorListener(TerminalEmulatorListener terminalEmulatorListener) {
    terminalEmulatorListeners.add(terminalEmulatorListener);
  }

  @Override
  protected synchronized void processKeyEvent(KeyEvent e) {
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
      super.processKeyEvent(e);
      updateStatusBarCursorPosition(this.getCursorCol(), this.getCursorRow());
    }
  }

  private List<Input> getInputFields() {
    List<Input> fields = new ArrayList<>();
    for (XI5250Field f : getFields()) {
      if (f.isMDTOn()) {
        fields.add(new CoordInput(new Position(f.getRow() + 1, f.getCol() + 1),
            f.getTrimmedString()));
      }
    }
    return fields;
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
