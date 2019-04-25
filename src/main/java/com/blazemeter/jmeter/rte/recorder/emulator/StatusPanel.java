package com.blazemeter.jmeter.rte.recorder.emulator;

import java.awt.CardLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
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
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class StatusPanel extends JPanel {

  private static final Logger LOG = LoggerFactory.getLogger(StatusPanel.class);

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

  private HelpFrame helpFrame;

  StatusPanel() {
    alarmLabel.setVisible(false);
    JLabel helpLabel = new JLabel(HELP_ICON);
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

    int messageLabelWidth = 133;
    int alarmLabelWidth = 16;
    int keyboardLabelWidth = 22;
    int helpLabelWidth = 19;

    layout.setHorizontalGroup(layout.createSequentialGroup()
        .addGap(5)
        .addComponent(positionLabel, messageLabelWidth, messageLabelWidth,
            messageLabelWidth)
        .addPreferredGap(ComponentPlacement.UNRELATED)
        .addComponent(messageLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE,
            Short.MAX_VALUE)
        .addPreferredGap(ComponentPlacement.UNRELATED)
        .addComponent(alarmLabel, alarmLabelWidth, alarmLabelWidth, alarmLabelWidth)
        .addPreferredGap(ComponentPlacement.UNRELATED)
        .addComponent(keyboardLabel, keyboardLabelWidth, keyboardLabelWidth, keyboardLabelWidth)
        .addPreferredGap(ComponentPlacement.UNRELATED)
        .addComponent(helpLabel, helpLabelWidth, helpLabelWidth, helpLabelWidth)
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

  public void updateStatusBarCursorPosition(int row, int col) {
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
    private int counter;

    private AlarmLabel(ImageIcon icon) {
      super(icon);
    }

    private synchronized void soundAlarm() {
      if (future != null) {
        future.cancel(true);
        setVisible(false);
      }
      counter = 0;
      setVisible(true);
      future = alarmExecutor.scheduleAtFixedRate(() -> {
        setVisible(!isVisible());
        if (counter < 10) {
          counter++;
        } else {
          future.cancel(true);
          setVisible(false);
        }
      }, 0, 500, TimeUnit.MILLISECONDS);
    }

    private void shutdown() {
      alarmExecutor.shutdown();
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
            IOUtils.toString(HelpFrame.class.getResourceAsStream("/recorder-help.html"), "UTF-8"));
      } catch (IOException e) {
        LOG.error("Error when loading help panel", e);
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
