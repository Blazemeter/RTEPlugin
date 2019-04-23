package com.blazemeter.jmeter.rte.recorder.emulator;

import com.blazemeter.jmeter.rte.core.AttentionKey;
import com.blazemeter.jmeter.rte.core.CoordInput;
import com.blazemeter.jmeter.rte.core.Input;
import com.blazemeter.jmeter.rte.core.Position;
import com.blazemeter.jmeter.rte.core.Screen;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.LayoutStyle.ComponentPlacement;
import net.infordata.em.crt5250.XI5250Crt;
import net.infordata.em.crt5250.XI5250Field;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
          put(new KeyEventMap(0, KeyEvent.VK_PAUSE), AttentionKey.CLEAR);
          put(new KeyEventMap(KeyEvent.SHIFT_MASK, KeyEvent.VK_ESCAPE), AttentionKey.SYSRQ);
          put(new KeyEventMap(KeyEvent.CTRL_MASK, KeyEvent.VK_CONTROL), AttentionKey.RESET);
          put(new KeyEventMap(0, KeyEvent.VK_PAGE_DOWN), AttentionKey.ROLL_UP);
          put(new KeyEventMap(0, KeyEvent.VK_PAGE_UP), AttentionKey.ROLL_DN);
        }
      };

  //private static final Logger LOG = LoggerFactory.getLogger(Xtn5250TerminalEmulator.class);
  private static final String TITLE = "Recorder";
  private static final Color BACKGROUND = Color.black;
  private static final int DEFAULT_FONT_SIZE = 14;

  private List<TerminalEmulatorListener> terminalEmulatorListeners = new ArrayList<>();
  private boolean locked = false;
  private JFrame frame;
  private XI5250Crt xi5250Crt;
  private StatusPanel statusPanel;

  @Override
  public void start(int columns, int rows) {
    xi5250Crt = new XI5250Crt() {
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
          statusPanel
              .updateStatusBarCursorPosition(this.getCursorRow() + 1, this.getCursorCol() + 1);
        }
      }
    };
    xi5250Crt.setCrtSize(columns, rows);
    xi5250Crt.setDefBackground(BACKGROUND);
    xi5250Crt.setBlinkingCursor(true);
    xi5250Crt.setEnabled(true);

    frame = new JFrame(TITLE);
    frame.setLayout(new BorderLayout());
    frame.add(xi5250Crt, BorderLayout.CENTER);
    statusPanel = new StatusPanel();
    frame.add(statusPanel, BorderLayout.SOUTH);
    frame.addWindowListener(new WindowAdapter() {

      @Override
      public void windowOpened(WindowEvent e) {
        FontMetrics fm = xi5250Crt.getFontMetrics(
            new Font(xi5250Crt.getFont().getName(), xi5250Crt.getFont().getStyle(),
                DEFAULT_FONT_SIZE));
        Dimension testSize = new Dimension(fm.charWidth('W') * xi5250Crt.getCrtSize().width,
            fm.getHeight() * xi5250Crt.getCrtSize().height);
        xi5250Crt.setSize(testSize.width, testSize.height);
        frame.pack();
        xi5250Crt.requestFocus();
      }

      @Override
      public void windowClosed(WindowEvent e) {
        for (TerminalEmulatorListener g : terminalEmulatorListeners) {
          g.onCloseTerminal();
        }
        statusPanel.dispose();
      }
    });

    frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    frame.setVisible(true);
  }

  @Override
  public void stop() {
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
      if (s instanceof Screen.Field) {
        Screen.Field f = (Screen.Field) s;
        XI5250Field xi5250Field = new XI5250Field(xi5250Crt, f.getColumn() - 1, f.getRow() - 1,
            f.getText().length(), 32);
        xi5250Field.setString(f.getText());
        xi5250Field.resetMDT();
        xi5250Crt.addField(xi5250Field);
      } else {
        xi5250Crt.drawString(s.getText(), s.getColumn() - 1, s.getRow() - 1);
      }
    }
    xi5250Crt.initAllFields();
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

  private static class StatusPanel extends JPanel {

    private static final ImageIcon ALARM_ICON = new ImageIcon(
        StatusPanel.class.getResource("/alarm.png"));
    private static final ImageIcon KEYBOARD_LOCKED_ICON = new ImageIcon(
        StatusPanel.class.getResource("/keyboard-locked.png"));
    private static final ImageIcon KEYBOARD_UNLOCKED_ICON = new ImageIcon(
        StatusPanel.class.getResource("/keyboard-unlocked.png"));
    private static final ImageIcon HELP_ICON = new ImageIcon(
        StatusPanel.class.getResource("/help.png"));

    private JLabel positionLabel = new JLabel("row: 00 / column: 00");
    private JLabel messageLabel = new JLabel("");
    private AlarmLabel alarmLabel = new AlarmLabel(ALARM_ICON);
    private JLabel keyboardLabel = new JLabel(KEYBOARD_UNLOCKED_ICON);
    private JLabel helpLabel = new JLabel(HELP_ICON);

    private HelpFrame helpFrame;

    private StatusPanel() {
      alarmLabel.setVisible(false);
      helpLabel.addMouseListener(new MouseListener() {
        @Override
        public void mouseClicked(MouseEvent mouseEvent) {
          if (helpFrame == null) {
            helpFrame = new HelpFrame();
          } else {
            helpFrame.requestFocus();
            helpFrame.setVisible(true);
          }
        }

        @Override
        public void mousePressed(MouseEvent mouseEvent) {
        }

        @Override
        public void mouseReleased(MouseEvent mouseEvent) {
        }

        @Override
        public void mouseEntered(MouseEvent mouseEvent) {
        }

        @Override
        public void mouseExited(MouseEvent mouseEvent) {
        }

      });

      GroupLayout layout = new GroupLayout(this);
      layout.setAutoCreateGaps(true);
      setLayout(layout);

      layout.setHorizontalGroup(layout.createSequentialGroup()
          .addGap(5)
          .addComponent(positionLabel, 133, 133,
              133)
          .addPreferredGap(ComponentPlacement.UNRELATED)
          .addComponent(messageLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE,
              Short.MAX_VALUE)
          .addPreferredGap(ComponentPlacement.UNRELATED)
          .addComponent(alarmLabel, 16, 16, 16)
          .addPreferredGap(ComponentPlacement.UNRELATED)
          .addComponent(keyboardLabel, 22, 22, 22)
          .addPreferredGap(ComponentPlacement.UNRELATED)
          .addComponent(helpLabel, 19, 19, 19)
          .addGap(5));
      layout.setVerticalGroup(layout.createParallelGroup(Alignment.BASELINE)
          .addComponent(positionLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
              GroupLayout.PREFERRED_SIZE)
          .addComponent(messageLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
              GroupLayout.PREFERRED_SIZE)
          .addComponent(alarmLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
              GroupLayout.PREFERRED_SIZE)
          .addComponent(keyboardLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
              GroupLayout.PREFERRED_SIZE)
          .addComponent(helpLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
              GroupLayout.PREFERRED_SIZE));
    }

    private void updateStatusBarCursorPosition(int row, int col) {
      this.positionLabel.setText("row: " + row + " / column: " + col);
      repaint();
    }

    public void setStatusMessage(String message) {
      this.messageLabel.setText(message);
      repaint();
    }

    public void soundAlarm() {
      alarmLabel.soundAlarm();
    }

    public void setKeyboardStatus(boolean locked) {
      if (locked) {
        this.keyboardLabel.setIcon(KEYBOARD_LOCKED_ICON);
      } else {
        this.keyboardLabel.setIcon(KEYBOARD_UNLOCKED_ICON);
      }
    }

    public void dispose() {
      alarmLabel.shutdown();
    }

    private static class AlarmLabel extends JLabel {

      private ScheduledExecutorService alarmExecutor = Executors.newSingleThreadScheduledExecutor();
      private ScheduledFuture future;
      private int contador;

      private AlarmLabel(ImageIcon icon) {
        super(icon);
      }

      private synchronized void soundAlarm() {
        if (future != null) {
          future.cancel(true);
          setVisible(false);
        }
        contador = 0;
        setVisible(true);
        future = alarmExecutor.scheduleAtFixedRate(() -> {
          setVisible(!isVisible());
          if (contador < 10) {
            contador++;
          } else {
            future.cancel(true);
            setVisible(false);
          }
        }, 0, 500, TimeUnit.MILLISECONDS);
      }

      private void shutdown() {
        shutdown();
      }
    }

    private static class HelpFrame extends JFrame {

      private static final String HELP_FRAME_TITLE = "Help";

      private HelpFrame() {
        setTitle(HELP_FRAME_TITLE);
        setLayout(new CardLayout());
        JLabel helpLabel = null;
        try {
          helpLabel = new JLabel(
              IOUtils.toString(HelpFrame.class.getResourceAsStream("/recorder-help.html")));
        } catch (IOException e) {
          //LOG.error("Error when loading help panel", e);
        }
        add(helpLabel);
        addWindowListener(new WindowAdapter() {
          @Override
          public void windowOpened(WindowEvent e) {
            requestFocus();
          }
        });
        setVisible(true);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setBounds(0, 0, 600, 300);
      }

    }
  }
}
