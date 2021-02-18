package com.blazemeter.jmeter.rte.recorder.emulator;

import com.blazemeter.jmeter.rte.sampler.gui.SwingUtils;
import com.blazemeter.jmeter.rte.sampler.gui.ThemedIconLabel;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.GroupLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class StatusPanel extends JPanel {

  private static final String KEYBOARD_LOCKED_RESOURCE_NAME = "keyboard-locked.png";
  private static final String KEYBOARD_UNLOCKED_RESOURCE_NAME = "keyboard-unlocked.png";
  private static final String VISIBLE_CREDENTIALS_ICON = "visible-credentials.png";
  private static final String NOT_VISIBLE_CREDENTIAL_ICON = "not-visible-credentials.png";
  private static final String UNLOCKED_CURSOR_RESOURCE_NAME = "cursor.png";
  private static final String LOCKED_CURSOR_RESOURCE_NAME = "blocked-cursor.png";
  private static final String ALARM_RESOURCE_NAME = "alarm.png";

  private JLabel positionLabel = SwingUtils
      .createComponent("positionLabel", new JLabel("row: 00 / column: 00"));

  private IntermittentLabel alarmLabel = SwingUtils.createComponent("alarmLabel",
      new IntermittentLabel(ALARM_RESOURCE_NAME));

  private ThemedIconLabel keyboardLabel = SwingUtils
      .createComponent("keyboardLabel", new ThemedIconLabel(KEYBOARD_LOCKED_RESOURCE_NAME));

  private ThemedIconLabel showCredentials = SwingUtils
      .createComponent("showCredentials", new ThemedIconLabel(NOT_VISIBLE_CREDENTIAL_ICON));
  private IntermittentLabel blockedCursor = SwingUtils.createComponent("blockedCursor",
      new IntermittentLabel(LOCKED_CURSOR_RESOURCE_NAME));
  private HelpFrame helpFrame;

  public StatusPanel() {
    JLabel helpLabel = SwingUtils
        .createComponent("helpLabel", new ThemedIconLabel("help.png"));
    helpLabel.addMouseListener(buildShowHelpOnMouseClickListener());
    initIntermittentLabels();
    GroupLayout layout = new GroupLayout(this);
    layout.setAutoCreateContainerGaps(true);
    layout.setAutoCreateGaps(true);
    setLayout(layout);

    alarmLabel.setToolTipText("Alarm status");
    keyboardLabel.setToolTipText("Keyboard status");
    helpLabel.setToolTipText("Help");
    positionLabel.setToolTipText("Cursor Position");
    blockedCursor.setToolTipText("Cursor movements using\nmouse are disable");
    layout.setHorizontalGroup(layout.createSequentialGroup()
        .addComponent(positionLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE,
            Short.MAX_VALUE)
        .addComponent(blockedCursor)
        .addComponent(alarmLabel)
        .addComponent(showCredentials)
        .addComponent(keyboardLabel)
        .addComponent(helpLabel));
    layout.setVerticalGroup(layout.createParallelGroup()
        .addComponent(blockedCursor)
        .addComponent(positionLabel)
        .addComponent(alarmLabel)
        .addComponent(showCredentials)
        .addComponent(keyboardLabel)
        .addComponent(helpLabel));
  }

  private void initIntermittentLabels() {
    blockedCursor.setOnBlinkTask(() -> blockedCursor.setIconResourceName(
        blockedCursor.getLabelState() ? UNLOCKED_CURSOR_RESOURCE_NAME
            : LOCKED_CURSOR_RESOURCE_NAME));
    blockedCursor
        .setDefaultTask(() -> blockedCursor.setIconResourceName(LOCKED_CURSOR_RESOURCE_NAME));
    alarmLabel.setDefaultTask(() -> alarmLabel.setVisible(false));
    alarmLabel.setOnBlinkTask(() -> alarmLabel.setVisible(!alarmLabel.isVisible()));
  }

  private MouseListener buildShowHelpOnMouseClickListener() {
    return new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        if (helpFrame == null) {
          helpFrame = new HelpFrame();
        }
        helpFrame.open();
      }
    };
  }

  public void updateStatusBarCursorPosition(int row, int col) {
    this.positionLabel.setText("row: " + row + " / column: " + col);
    repaint();
  }

  public void updateShowCredentials(boolean visible) {
    this.showCredentials.setIconResourceName(
        visible ? VISIBLE_CREDENTIALS_ICON : NOT_VISIBLE_CREDENTIAL_ICON
    );
  }

  public void soundAlarm() {
    alarmLabel.blink();
  }

  public JLabel getShowCredentials() {
    return showCredentials;
  }

  public void setKeyboardStatus(boolean locked) {
    keyboardLabel.setIconResourceName(
        locked ? KEYBOARD_LOCKED_RESOURCE_NAME : KEYBOARD_UNLOCKED_RESOURCE_NAME);
  }

  public void setBlockedCursorVisible() {
    blockedCursor.setVisible(true);
  }

  public void blinkBlockedCursor() {
    blockedCursor.blink();
  }

  public void dispose() {
    blockedCursor.shutdown();
    alarmLabel.shutdown();
    if (helpFrame != null) {
      helpFrame.close();
    }
  }

}
