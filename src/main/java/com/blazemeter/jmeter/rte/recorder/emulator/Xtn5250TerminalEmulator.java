package com.blazemeter.jmeter.rte.recorder.emulator;

import com.blazemeter.jmeter.rte.core.AttentionKey;
import com.blazemeter.jmeter.rte.core.CoordInput;
import com.blazemeter.jmeter.rte.core.Input;
import com.blazemeter.jmeter.rte.core.Position;
import com.blazemeter.jmeter.rte.core.Screen;
import com.blazemeter.jmeter.rte.sampler.gui.SwingUtils;
import com.helger.commons.annotation.VisibleForTesting;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.LayoutStyle.ComponentPlacement;
import net.infordata.em.crt5250.XI5250Crt;
import net.infordata.em.crt5250.XI5250Field;

public class Xtn5250TerminalEmulator extends JFrame implements TerminalEmulator {

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
          put(new KeyEventMap(0, KeyEvent.VK_CONTROL), AttentionKey.RESET);
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

  private static final ImageIcon COPY_ICON = new ImageIcon(
      StatusPanel.class.getResource("/keyboard-locked.png"));
  private static final ImageIcon PASTE_ICON = new ImageIcon(
      StatusPanel.class.getResource("/keyboard-locked.png"));
  private JButton copyButton = SwingUtils
      .createComponent("copyButton", new JButton(COPY_ICON));
  private JButton pasteButton = SwingUtils
      .createComponent("pasteButton", new JButton(PASTE_ICON));

  private List<TerminalEmulatorListener> terminalEmulatorListeners = new ArrayList<>();
  private boolean locked = false;
  private boolean stopping;
  private StatusPanel statusPanel = new StatusPanel();
  private XI5250Crt xi5250Crt = new CustomXI5250Crt();
  private JPanel toolsPanel = createToolsPanel();

  public Xtn5250TerminalEmulator() {
    xi5250Crt.setName("Terminal");
    xi5250Crt.setDefBackground(BACKGROUND);
    xi5250Crt.setBlinkingCursor(true);
    xi5250Crt.setEnabled(true);
    setTitle(TITLE);
    setLayout(new BorderLayout());
    add(toolsPanel, BorderLayout.NORTH);
    add(xi5250Crt, BorderLayout.CENTER);
    add(statusPanel, BorderLayout.SOUTH);
    addWindowListener(new WindowAdapter() {

      @Override
      public void windowOpened(WindowEvent e) {
        Dimension testSize = calculateCrtDefaultSize();
        xi5250Crt.setSize(testSize.width, testSize.height);
        pack();
        xi5250Crt.requestFocus();
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
    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
  }

  private JPanel createToolsPanel() {
    JPanel toolsPanel = new JPanel();
    GroupLayout layout = new GroupLayout(toolsPanel);
    layout.setAutoCreateContainerGaps(true);
    layout.setAutoCreateGaps(true);
    toolsPanel.setLayout(layout);

    layout.setHorizontalGroup(layout.createSequentialGroup()
        .addComponent(copyButton, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
            GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(ComponentPlacement.UNRELATED)
        .addComponent(pasteButton, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
            GroupLayout.PREFERRED_SIZE));
    layout.setVerticalGroup(layout.createParallelGroup(Alignment.BASELINE)
        .addComponent(copyButton, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
            GroupLayout.PREFERRED_SIZE)
        .addComponent(pasteButton, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
            GroupLayout.PREFERRED_SIZE));

    return toolsPanel;
  }

  @Override
  public void start() {
    setVisible(true);
  }

  @Override
  public void setScreenSize(int columns, int rows) {
    xi5250Crt.setCrtSize(columns, rows);
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
    dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
  }

  @Override
  public void setCursor(int row, int col) {
    xi5250Crt.setCursorPos(col - 1, row - 1);
    this.statusPanel.updateStatusBarCursorPosition(row, col);
  }

  @Override
  public synchronized void setScreen(Screen screen) {
    Dimension screenSize = screen.getSize();
    setScreenSize(screenSize.width, screenSize.height);
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

  @VisibleForTesting
  public void setSelectedArea(Rectangle rectangle) {
    xi5250Crt.setSelectedArea(rectangle);
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

  private class CustomXI5250Crt extends XI5250Crt {

    private boolean copyPaste = false;

    CustomXI5250Crt() {
      super();
      copyButton.addActionListener(e -> {
        doCopy();
        xi5250Crt.requestFocus();
      });
      pasteButton.addActionListener(e -> {
        doPaste();
        xi5250Crt.requestFocus();
      });
    }

    @Override
    protected synchronized void processKeyEvent(KeyEvent e) {
      AttentionKey attentionKey = null;
      if (e.getID() == KeyEvent.KEY_PRESSED && e.getKeyCode() == KeyEvent.VK_C
          && e.getModifiers() == KeyEvent.META_MASK) {
        doCopy();
      } else if (e.getID() == KeyEvent.KEY_PRESSED && e.getKeyCode() == KeyEvent.VK_C
          && e.getModifiers() == KeyEvent.CTRL_MASK) {
        doCopy();
        copyPaste = true;
      } else if (e.getID() == KeyEvent.KEY_PRESSED && e.getKeyCode() == KeyEvent.VK_V
          && e.getModifiers() == KeyEvent.META_MASK && !locked) {
        doPaste();
      } else if (e.getID() == KeyEvent.KEY_PRESSED && e.getKeyCode() == KeyEvent.VK_V
          && e.getModifiers() == KeyEvent.CTRL_MASK && !locked) {
        doPaste();
        copyPaste = true;
      } else if (e.getID() == KeyEvent.KEY_RELEASED && e.getKeyCode() == KeyEvent.VK_CONTROL
          && copyPaste) {
        copyPaste = false;
      } else if (isAnyKeyPressedOrControlKeyReleasedAndNotCopy(e)) {
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

    //This is implemented in this way because ctrl key is used in a shortcut to
    //copy/paste and the attention key should be triggered only if have not
    //executed a copy paste before.
    private boolean isAnyKeyPressedOrControlKeyReleasedAndNotCopy(KeyEvent e) {
      return (e.getID() == KeyEvent.KEY_PRESSED && e.getKeyCode() != KeyEvent.VK_CONTROL) || (
          e.getID() == KeyEvent.KEY_RELEASED && e.getKeyCode() == KeyEvent.VK_CONTROL
              && !copyPaste);
    }

    @Override
    protected void processMouseEvent(MouseEvent e) {
      super.processMouseEvent(e);
      statusPanel
          .updateStatusBarCursorPosition(this.getCursorRow() + 1, this.getCursorCol() + 1);
    }

    @Override
    public void paintComponent(Graphics g) {
      synchronized (Xtn5250TerminalEmulator.this) {
        super.paintComponent(g);
      }
    }
  }
}
