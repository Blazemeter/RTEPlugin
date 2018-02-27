package com.blazemeter.jmeter.rte.sampler.gui;

import com.blazemeter.jmeter.rte.core.Trigger;
import com.blazemeter.jmeter.rte.sampler.RTESampler;
import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;

public class RTESamplerPanel extends JPanel {

  private static final long serialVersionUID = 4739160923223292835L;

  private static final String[] TYPING_STYLE = {RTESampler.TYPING_STYLE_FAST,
      RTESampler.TYPING_STYLE_HUMAN};

  private JPanel requestPanel = new JPanel();
  private JLabel typingStyleLabel = new JLabel();
  private JComboBox<String> typingStyleComboBox = new JComboBox<String>(TYPING_STYLE);
  private JPanel triggerPanel = new JPanel();
  private ButtonGroup triggersGroup = new ButtonGroup();
  private Map<Trigger, JRadioButton> triggers = new HashMap<>();
  private CoordInputPanel payloadPanel;
  private JCheckBox disconnect = new JCheckBox("Disconnect?");
  private JPanel waitPanel = new JPanel();
  private JCheckBox waitSync = new JCheckBox("Sync?");
  private JCheckBox waitCursor = new JCheckBox("Cursor?");
  private JCheckBox waitSilent = new JCheckBox("Silent?");
  private JCheckBox waitText = new JCheckBox("Text?");
  private JLabel waitTimeoutLableSync = new JLabel();
  private JLabel waitTimeoutLableCursor = new JLabel();
  private JLabel waitTimeoutLableSilent = new JLabel();
  private JLabel waitForLableSilent = new JLabel();
  private JLabel waitTimeoutLableText = new JLabel();
  private JTextField waitTimeoutSync = new JTextField();
  private JTextField waitTimeoutCursor = new JTextField();
  private JTextField waitTimeoutSilent = new JTextField();
  private JTextField waitForSilent = new JTextField();
  private JTextField waitTimeoutText = new JTextField();
  private JTextField textWait = new JTextField();
  private JLabel coordXWaitLabel = new JLabel();
  private JTextField coordXWait = new JTextField();
  private JLabel coordYWaitLabel = new JLabel();
  private JTextField coordYWait = new JTextField();

  public RTESamplerPanel() {
    payloadPanel = new CoordInputPanel("Payload");
    initComponents();
  }

  private void initComponents() {

    requestPanel.setBorder(BorderFactory.createTitledBorder("RTE Message"));

    typingStyleLabel.setText("Typing Style: ");
    disconnect.setText("Disconnect?");

    waitSync.addItemListener(e -> {
      if (e.getStateChange() == ItemEvent.SELECTED) {
        waitTimeoutSync.setEnabled(true);
      } else if (e.getStateChange() == ItemEvent.DESELECTED) {
        waitTimeoutSync.setEnabled(false);
      }
      validate();
      repaint();
    });

    waitCursor.addItemListener(e -> {
      if (e.getStateChange() == ItemEvent.SELECTED) {
        waitTimeoutCursor.setEnabled(true);
      } else if (e.getStateChange() == ItemEvent.DESELECTED) {
        waitTimeoutCursor.setEnabled(false);
      }
      validate();
      repaint();
    });

    waitSilent.addItemListener(e -> {
      if (e.getStateChange() == ItemEvent.SELECTED) {
        waitTimeoutSilent.setEnabled(true);
      } else if (e.getStateChange() == ItemEvent.DESELECTED) {
        waitTimeoutSilent.setEnabled(false);
      }
      validate();
      repaint();
    });

    waitText.addItemListener(e -> {
      if (e.getStateChange() == ItemEvent.SELECTED) {
        waitTimeoutText.setEnabled(true);
        textWait.setEnabled(true);
        coordXWait.setEnabled(true);
        coordYWait.setEnabled(true);
      } else if (e.getStateChange() == ItemEvent.DESELECTED) {
        waitTimeoutText.setEnabled(false);
        textWait.setEnabled(false);
        coordXWait.setEnabled(false);
        coordYWait.setEnabled(false);
      }
      validate();
      repaint();
    });

    triggerPanel.setBorder(BorderFactory.createTitledBorder("Trigger"));
    triggerPanel.setLayout(new GridLayout((int) Math.ceil(Trigger.values().length / 12), 12));

    Arrays.stream(Trigger.values()).forEach(t -> {
      JRadioButton r = new JRadioButton(t.toString());
      r.setActionCommand(t.toString());
      triggerPanel.add(r);
      triggers.put(t, r);
      triggersGroup.add(r);
    });

    GroupLayout requestPanelLayout = new GroupLayout(requestPanel);
    requestPanel.setLayout(requestPanelLayout);
    requestPanelLayout.setHorizontalGroup(requestPanelLayout
        .createParallelGroup(Alignment.LEADING)
        .addGroup(requestPanelLayout.createSequentialGroup()
            .addGroup(requestPanelLayout.createParallelGroup(Alignment.LEADING)
                .addGroup(requestPanelLayout.createSequentialGroup().addComponent(typingStyleLabel)
                    .addPreferredGap(ComponentPlacement.RELATED)
                    .addComponent(typingStyleComboBox))
                .addComponent(payloadPanel, GroupLayout.DEFAULT_SIZE,
                    GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(triggerPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE,
                    Short.MAX_VALUE)
                .addComponent(disconnect))));

    requestPanelLayout.setVerticalGroup(requestPanelLayout
        .createParallelGroup(Alignment.LEADING)
        .addGroup(requestPanelLayout.createSequentialGroup().addGap(8, 8, 8)
            .addGroup(requestPanelLayout.createParallelGroup(Alignment.BASELINE)
                .addComponent(typingStyleLabel)
                .addComponent(typingStyleComboBox))
            .addPreferredGap(ComponentPlacement.UNRELATED)
            .addComponent(payloadPanel, GroupLayout.DEFAULT_SIZE, 102, Short.MAX_VALUE)
            .addPreferredGap(ComponentPlacement.UNRELATED)
            .addComponent(triggerPanel, GroupLayout.DEFAULT_SIZE, 102, Short.MAX_VALUE)
            .addPreferredGap(ComponentPlacement.UNRELATED)
            .addGroup(requestPanelLayout.createParallelGroup(Alignment.BASELINE)
                .addComponent(disconnect))
            .addGap(8, 8, 8).addContainerGap()));

    waitPanel.setBorder(BorderFactory.createTitledBorder("Wait for:"));
    waitTimeoutLableSync.setText("Timeout: ");
    waitTimeoutLableCursor.setText("Timeout: ");
    waitTimeoutLableSilent.setText("Timeout: ");
    waitForLableSilent.setText("Wait for Silent: ");
    waitTimeoutLableText.setText("Timeout: ");
    coordXWaitLabel.setText("Coord X: ");
    coordYWaitLabel.setText("Coord Y: ");

    GroupLayout waitPanelLayout = new GroupLayout(waitPanel);
    waitPanel.setLayout(waitPanelLayout);
    waitPanelLayout
        .setHorizontalGroup(waitPanelLayout.createParallelGroup(Alignment.LEADING)
            .addGroup(waitPanelLayout.createSequentialGroup()
                .addGroup(waitPanelLayout.createParallelGroup(Alignment.LEADING)
                    .addGroup(waitPanelLayout.createSequentialGroup().addComponent(waitSync)
                        .addPreferredGap(ComponentPlacement.UNRELATED)
                        .addComponent(waitTimeoutLableSync)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(waitTimeoutSync, GroupLayout.PREFERRED_SIZE,
                            100, GroupLayout.PREFERRED_SIZE))
                    .addGroup(waitPanelLayout.createSequentialGroup().addComponent(waitCursor)
                        .addPreferredGap(ComponentPlacement.UNRELATED)
                        .addComponent(waitTimeoutLableCursor)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(waitTimeoutCursor, GroupLayout.PREFERRED_SIZE,
                            100, GroupLayout.PREFERRED_SIZE))
                    .addGroup(waitPanelLayout.createSequentialGroup().addComponent(waitSilent)
                        .addPreferredGap(ComponentPlacement.UNRELATED)
                        .addComponent(waitForLableSilent)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(waitForSilent, GroupLayout.PREFERRED_SIZE,
                            100, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(ComponentPlacement.UNRELATED)
                        .addComponent(waitTimeoutLableSilent)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(waitTimeoutSilent, GroupLayout.PREFERRED_SIZE,
                            100, GroupLayout.PREFERRED_SIZE))
                    .addGroup(waitPanelLayout.createSequentialGroup().addComponent(waitText)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(textWait, GroupLayout.PREFERRED_SIZE, 100,
                            GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(ComponentPlacement.UNRELATED)
                        .addComponent(coordXWaitLabel)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(coordXWait, GroupLayout.PREFERRED_SIZE, 50,
                            GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(coordYWaitLabel)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(coordYWait, GroupLayout.PREFERRED_SIZE, 50,
                            GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(ComponentPlacement.UNRELATED)
                        .addComponent(waitTimeoutLableText)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(waitTimeoutText, GroupLayout.PREFERRED_SIZE,
                            100, GroupLayout.PREFERRED_SIZE)))));

    waitPanelLayout.setVerticalGroup(waitPanelLayout.createParallelGroup(Alignment.LEADING)
        .addGroup(waitPanelLayout.createSequentialGroup().addGap(8, 8, 8)
            .addGroup(waitPanelLayout.createParallelGroup(Alignment.BASELINE)
                .addComponent(waitSync).addComponent(waitTimeoutLableSync)
                .addComponent(waitTimeoutSync))
            .addPreferredGap(ComponentPlacement.UNRELATED)
            .addGroup(waitPanelLayout.createParallelGroup(Alignment.BASELINE)
                .addComponent(waitCursor).addComponent(waitTimeoutLableCursor)
                .addComponent(waitTimeoutCursor))
            .addPreferredGap(ComponentPlacement.UNRELATED)
            .addGroup(waitPanelLayout.createParallelGroup(Alignment.BASELINE)
                .addComponent(waitSilent).addComponent(waitTimeoutLableSilent)
                .addComponent(waitTimeoutSilent)
                .addComponent(waitForLableSilent)
                .addComponent(waitForSilent))
            .addPreferredGap(ComponentPlacement.UNRELATED)
            .addGroup(waitPanelLayout.createParallelGroup(Alignment.BASELINE)
                .addComponent(waitText).addComponent(textWait).addComponent(coordXWaitLabel)
                .addComponent(coordXWait).addComponent(coordYWaitLabel).addComponent(coordYWait)
                .addComponent(waitTimeoutLableText).addComponent(waitTimeoutText))
            .addGap(8, 8, 8).addContainerGap()));

    GroupLayout layout = new GroupLayout(this);
    this.setLayout(layout);
    layout.setHorizontalGroup(layout.createParallelGroup(Alignment.LEADING)
        .addGroup(layout.createSequentialGroup().addContainerGap()
            .addGroup(layout.createParallelGroup(Alignment.LEADING)
                .addComponent(requestPanel, GroupLayout.DEFAULT_SIZE,
                    GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(waitPanel, GroupLayout.DEFAULT_SIZE,
                    GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addContainerGap()));
    layout.setVerticalGroup(layout.createParallelGroup(Alignment.LEADING)
        .addGroup(layout.createSequentialGroup().addContainerGap()
            .addComponent(requestPanel, GroupLayout.DEFAULT_SIZE,
                GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE)
            .addContainerGap()
            .addComponent(waitPanel, GroupLayout.DEFAULT_SIZE,
                GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE)
            .addContainerGap()));
  }

  public void initFields() {
    payloadPanel.clear();
    disconnect.setSelected(false);
    waitCursor.setSelected(false);
    waitSilent.setSelected(false);
    waitSync.setSelected(false);
    waitText.setSelected(false);
    waitTimeoutSync.setText("");
    waitTimeoutCursor.setText("");
    waitTimeoutSilent.setText("");
    waitTimeoutText.setText("");
    textWait.setText("");
    coordXWait.setText("");
    coordYWait.setText("");
    waitTimeoutSync.setEnabled(false);
    waitTimeoutCursor.setEnabled(false);
    waitTimeoutSilent.setEnabled(false);
    waitTimeoutText.setEnabled(false);
    textWait.setEnabled(false);
    coordXWait.setEnabled(false);
    coordYWait.setEnabled(false);
    typingStyleComboBox.setSelectedItem(RTESampler.TYPING_STYLE_FAST);
    triggers.get(RTESampler.DEFAULT_TRIGGER).setSelected(true);
  }

  public CoordInputPanel getPayload() {
    return this.payloadPanel;
  }

  public String getCoordYWait() {
    return this.coordYWait.getText();
  }

  public void setCoordYWait(String coordY) {
    this.coordYWait.setText(coordY);
  }

  public String getCoordXWait() {
    return this.coordXWait.getText();
  }

  public void setCoordXWait(String coordX) {
    this.coordXWait.setText(coordX);
  }

  public String getTextWait() {
    return this.textWait.getText();
  }

  public void setTextWait(String textWait) {
    this.textWait.setText(textWait);
  }

  public boolean getDisconnect() {
    return this.disconnect.isSelected();
  }

  public void setDisconnect(boolean disconnect) {
    this.disconnect.setSelected(disconnect);
  }

  public boolean getWaitSync() {
    return this.waitSync.isSelected();
  }

  public void setWaitSync(boolean waitSync) {
    this.waitSync.setSelected(waitSync);
  }

  public boolean getWaitCursor() {
    return this.waitCursor.isSelected();
  }

  public void setWaitCursor(boolean waitCursor) {
    this.waitCursor.setSelected(waitCursor);
  }

  public boolean getWaitSilent() {
    return this.waitSilent.isSelected();
  }

  public void setWaitSilent(boolean waitSilent) {
    this.waitSilent.setSelected(waitSilent);
  }

  public boolean getWaitText() {
    return this.waitText.isSelected();
  }

  public void setWaitText(boolean waitText) {
    this.waitText.setSelected(waitText);
  }

  public String getWaitTimeoutSync() {
    return this.waitTimeoutSync.getText();
  }

  public void setWaitTimeoutSync(String waitTimeoutSync) {
    this.waitTimeoutSync.setText(waitTimeoutSync);
  }

  public String getWaitTimeoutCursor() {
    return this.waitTimeoutCursor.getText();
  }

  public void setWaitTimeoutCursor(String waitTimeoutCursor) {
    this.waitTimeoutCursor.setText(waitTimeoutCursor);
  }

  public String getWaitTimeoutSilent() {
    return this.waitTimeoutSilent.getText();
  }

  public void setWaitTimeoutSilent(String waitTimeoutSilent) {
    this.waitTimeoutSilent.setText(waitTimeoutSilent);
  }

  public String getWaitForSilent() {
    return this.waitForSilent.getText();
  }

  public void setWaitForSilent(String waitForutSilent) {
    this.waitForSilent.setText(waitForutSilent);
  }

  public String getWaitTimeoutText() {
    return this.waitTimeoutText.getText();
  }

  public void setWaitTimeoutText(String waitTimeoutText) {
    this.waitTimeoutText.setText(waitTimeoutText);
  }

  public void setTypingStyle(String typingStyle) {
    typingStyleComboBox.setSelectedItem(typingStyle);
  }

  public String getTypingStyle() {
    return (String) typingStyleComboBox.getSelectedItem();
  }

  public void setTrigger(Trigger trigger) {
    if (triggers.containsKey(trigger)) {
      triggers.get(trigger).setSelected(true);
    } else {
      triggers.get(RTESampler.DEFAULT_TRIGGER).setSelected(true);
    }
  }

  public Trigger getTrigger() {
    String trigger = triggersGroup.getSelection().getActionCommand();
    return Trigger.valueOf(trigger);
  }
}
