package com.blazemeter.jmeter.rte.recorder.emulator;

import com.blazemeter.jmeter.rte.sampler.gui.SwingUtils;
import com.blazemeter.jmeter.rte.sampler.gui.ThemedIconLabel;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.LayoutStyle.ComponentPlacement;

public class StatusPanel extends JPanel {

  private static final String KEYBOARD_LOCKED_RESOURCE_NAME = "keyboard-locked.png";
  private static final String KEYBOARD_UNLOCKED_RESOURCE_NAME = "keyboard-unlocked.png";

  private JLabel positionLabel = SwingUtils
      .createComponent("positionLabel", new JLabel("row: 00 / column: 00"));
  private JLabel messageLabel = SwingUtils
      .createComponent("messageLabel", new JLabel(""));
  private AlarmLabel alarmLabel = SwingUtils
      .createComponent("alarmLabel", new AlarmLabel());
  private ThemedIconLabel keyboardLabel = SwingUtils
      .createComponent("keyboardLabel", new ThemedIconLabel(KEYBOARD_LOCKED_RESOURCE_NAME));

  private HelpFrame helpFrame;

  public StatusPanel() {
    JLabel helpLabel = SwingUtils
        .createComponent("helpLabel", new ThemedIconLabel("help.png"));
    helpLabel.addMouseListener(buildShowHelpOnMouseClickListener());

    GroupLayout layout = new GroupLayout(this);
    layout.setAutoCreateContainerGaps(true);
    layout.setAutoCreateGaps(true);
    setLayout(layout);

    int messageLabelWidth = 133;
    int alarmLabelWidth = 16;
    int keyboardLabelWidth = 22;
    int helpLabelWidth = 19;

    layout.setHorizontalGroup(layout.createSequentialGroup()
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
        .addComponent(helpLabel, helpLabelWidth, helpLabelWidth, helpLabelWidth));
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

  private MouseListener buildShowHelpOnMouseClickListener() {
    return new MouseListener() {
      @Override
      public void mouseClicked(MouseEvent mouseEvent) {
        if (helpFrame == null) {
          helpFrame = new HelpFrame();
        }
        helpFrame.open();
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

    };
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
    keyboardLabel.setIconResourceName(
        locked ? KEYBOARD_LOCKED_RESOURCE_NAME : KEYBOARD_UNLOCKED_RESOURCE_NAME);
  }

  public void dispose() {
    alarmLabel.shutdown();
    if (helpFrame != null) {
      helpFrame.close();
    }
  }

}
