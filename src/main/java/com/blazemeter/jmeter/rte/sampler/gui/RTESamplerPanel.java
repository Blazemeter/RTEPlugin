package com.blazemeter.jmeter.rte.sampler.gui;

import com.blazemeter.jmeter.rte.core.Action;
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
import javax.swing.GroupLayout.Group;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;

public class RTESamplerPanel extends JPanel {

  private static final long serialVersionUID = 4739160923223292835L;
  private static final int INDEX_WIDTH = 30;
  private static final int TIME_WIDTH = 60;

  private CoordInputPanel payloadPanel;
  private ButtonGroup actionsGroup = new ButtonGroup();
  private Map<Action, JRadioButton> actions = new HashMap<>();
  private JCheckBox disconnect = new JCheckBox("Disconnect?");
  private JCheckBox sendInputs = new JCheckBox("Send Inputs");
  private JCheckBox waitSync = new JCheckBox("Sync?");
  private JTextField waitSyncTimeout = new JTextField();
  private JCheckBox waitCursor = new JCheckBox("Cursor?");
  private JTextField waitCursorRow = new JTextField();
  private JTextField waitCursorColumn = new JTextField();
  private JTextField waitCursorTimeout = new JTextField();
  private JCheckBox waitSilent = new JCheckBox("Silent?");
  private JTextField waitSilentTime = new JTextField();
  private JTextField waitSilentTimeout = new JTextField();
  private JCheckBox waitText = new JCheckBox("Text?");
  private JTextField waitTextRegex = new JTextField();
  private JTextField waitTextTimeout = new JTextField();
  private JTextField waitTextAreaTop = new JTextField();
  private JTextField waitTextAreaLeft = new JTextField();
  private JTextField waitTextAreaBottom = new JTextField();
  private JTextField waitTextAreaRight = new JTextField();

  public RTESamplerPanel() {
    GroupLayout layout = new GroupLayout(this);
    this.setLayout(layout);

    Group horizontalGroup = layout.createParallelGroup(Alignment.LEADING);
    layout.setHorizontalGroup(layout.createParallelGroup(Alignment.LEADING)
        .addGroup(layout.createSequentialGroup()
            .addContainerGap()
            .addGroup(horizontalGroup)
            .addContainerGap()));
    SequentialGroup verticalGroup = layout.createSequentialGroup()
        .addContainerGap();
    layout.setVerticalGroup(layout.createParallelGroup(Alignment.LEADING)
        .addGroup(verticalGroup));

    addRequestPanel(horizontalGroup, verticalGroup);
    addWaitPanel(horizontalGroup, verticalGroup);
  }

  private void addRequestPanel(Group horizontalGroup, SequentialGroup verticalGroup) {
    JPanel panel = new JPanel();
    panel.setBorder(BorderFactory.createTitledBorder("RTE Message"));

    horizontalGroup.addComponent(panel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE,
        Short.MAX_VALUE);
    verticalGroup.addComponent(panel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE,
        GroupLayout.DEFAULT_SIZE)
        .addContainerGap();

    GroupLayout layout = new GroupLayout(panel);
    Group requestHorizontalGroup = layout
        .createParallelGroup(Alignment.LEADING);
    layout.setHorizontalGroup(layout.createParallelGroup(Alignment.LEADING)
        .addGroup(layout.createSequentialGroup()
            .addGroup(requestHorizontalGroup)));
    SequentialGroup requestVerticalGroup = layout.createSequentialGroup()
        .addGap(8);
    layout.setVerticalGroup(layout.createParallelGroup(Alignment.LEADING)
        .addGroup(requestVerticalGroup));
    panel.setLayout(layout);

    addPayLoadPanel(requestHorizontalGroup, requestVerticalGroup);
    verticalGroup.addPreferredGap(ComponentPlacement.UNRELATED);
    addActionsPanel(requestHorizontalGroup, requestVerticalGroup);
    verticalGroup.addPreferredGap(ComponentPlacement.UNRELATED);
    addDisconnectAndSendInputs(requestHorizontalGroup, requestVerticalGroup, layout);

    requestVerticalGroup.addGap(8)
        .addContainerGap();
  }

  private void addPayLoadPanel(Group horizontalGroup, SequentialGroup verticalGroup) {
    payloadPanel = new CoordInputPanel("Payload");
    horizontalGroup.addComponent(payloadPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE,
        Short.MAX_VALUE);
    verticalGroup.addPreferredGap(ComponentPlacement.UNRELATED)
        .addComponent(payloadPanel, GroupLayout.DEFAULT_SIZE, 102, Short.MAX_VALUE);
  }

  private void addActionsPanel(Group horizontalGroup, SequentialGroup verticalGroup) {
    JPanel panel = new JPanel();
    panel.setBorder(BorderFactory.createTitledBorder("Actions"));
    panel.setLayout(new GridLayout((int) Math.ceil(Action.values().length / 12), 12));

    Arrays.stream(Action.values()).forEach(t -> {
      JRadioButton r = new JRadioButton(t.toString());
      r.setActionCommand(t.toString());
      panel.add(r);
      actions.put(t, r);
      actionsGroup.add(r);
    });

    horizontalGroup.addComponent(panel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE,
        Short.MAX_VALUE);
    verticalGroup.addComponent(panel, GroupLayout.DEFAULT_SIZE, 102, Short.MAX_VALUE);
  }

  private void addDisconnectAndSendInputs(Group horizontalGroup, SequentialGroup verticalGroup,
      GroupLayout layout) {
    horizontalGroup.addGroup(layout.createSequentialGroup()
        .addComponent(disconnect)
        .addPreferredGap(ComponentPlacement.UNRELATED)
        .addComponent(sendInputs));
    verticalGroup.addGroup(layout.createParallelGroup(Alignment.BASELINE)
        .addComponent(disconnect)
        .addComponent(sendInputs));
  }

  private void addWaitPanel(Group horizontalGroup, SequentialGroup verticalGroup) {
    JPanel panel = new JPanel();
    panel.setBorder(BorderFactory.createTitledBorder("Wait for:"));

    horizontalGroup.addComponent(panel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE,
        Short.MAX_VALUE);
    verticalGroup.addComponent(panel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE,
        GroupLayout.DEFAULT_SIZE)
        .addContainerGap();

    GroupLayout layout = new GroupLayout(panel);
    panel.setLayout(layout);
    Group waitHorizontalGroup = layout.createParallelGroup(Alignment.LEADING);
    layout.setHorizontalGroup(layout.createParallelGroup(Alignment.LEADING)
        .addGroup(layout.createSequentialGroup()
            .addGroup(waitHorizontalGroup)));
    SequentialGroup waitVerticalGroup = layout.createSequentialGroup()
        .addGap(8);
    layout.setVerticalGroup(layout.createParallelGroup(Alignment.LEADING)
        .addGroup(waitVerticalGroup));

    addWaitSync(waitHorizontalGroup, waitVerticalGroup, layout);
    waitVerticalGroup.addPreferredGap(ComponentPlacement.UNRELATED);
    addWaitCursor(waitHorizontalGroup, waitVerticalGroup, layout);
    waitVerticalGroup.addPreferredGap(ComponentPlacement.UNRELATED);
    addWaitSilent(waitHorizontalGroup, waitVerticalGroup, layout);
    waitVerticalGroup.addPreferredGap(ComponentPlacement.UNRELATED);
    addWaitText(waitHorizontalGroup, waitVerticalGroup, layout);

    waitVerticalGroup.addGap(8)
        .addContainerGap();
  }

  private void addWaitSync(Group horizontalGroup, SequentialGroup verticalGroup,
      GroupLayout panelLayout) {

    waitSync.addItemListener(e -> {
      waitSyncTimeout.setEnabled(e.getStateChange() == ItemEvent.SELECTED);
      validate();
      repaint();
    });

    JLabel timeoutLabel = new JLabel("Timeout (millis): ");
    horizontalGroup.addGroup(panelLayout.createSequentialGroup()
        .addComponent(waitSync)
        .addPreferredGap(ComponentPlacement.UNRELATED)
        .addComponent(timeoutLabel)
        .addPreferredGap(ComponentPlacement.RELATED)
        .addComponent(waitSyncTimeout, GroupLayout.PREFERRED_SIZE, TIME_WIDTH,
            GroupLayout.PREFERRED_SIZE));
    verticalGroup.addGroup(panelLayout.createParallelGroup(Alignment.BASELINE)
        .addComponent(waitSync)
        .addComponent(timeoutLabel)
        .addComponent(waitSyncTimeout));
  }

  private void addWaitCursor(Group horizontalGroup, SequentialGroup verticalGroup,
      GroupLayout panelLayout) {

    waitCursor.addItemListener(e -> {
      boolean enabled = e.getStateChange() == ItemEvent.SELECTED;
      waitCursorRow.setEnabled(enabled);
      waitCursorColumn.setEnabled(enabled);
      waitCursorTimeout.setEnabled(enabled);
      validate();
      repaint();
    });

    JLabel rowLabel = new JLabel("Row: ");
    JLabel columnLabel = new JLabel("Column: ");
    JLabel timeoutLabel = new JLabel("Timeout (millis): ");
    horizontalGroup.addGroup(panelLayout.createSequentialGroup()
        .addComponent(waitCursor)
        .addPreferredGap(ComponentPlacement.UNRELATED)
        .addComponent(rowLabel)
        .addPreferredGap(ComponentPlacement.RELATED)
        .addComponent(waitCursorRow, GroupLayout.PREFERRED_SIZE, INDEX_WIDTH,
            GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(ComponentPlacement.UNRELATED)
        .addComponent(columnLabel)
        .addPreferredGap(ComponentPlacement.RELATED)
        .addComponent(waitCursorColumn, GroupLayout.PREFERRED_SIZE, INDEX_WIDTH,
            GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(ComponentPlacement.UNRELATED)
        .addComponent(timeoutLabel)
        .addPreferredGap(ComponentPlacement.RELATED)
        .addComponent(waitCursorTimeout, GroupLayout.PREFERRED_SIZE, TIME_WIDTH,
            GroupLayout.PREFERRED_SIZE));
    verticalGroup.addGroup(panelLayout.createParallelGroup(Alignment.BASELINE)
        .addComponent(waitCursor)
        .addComponent(rowLabel)
        .addComponent(waitCursorRow)
        .addComponent(columnLabel)
        .addComponent(waitCursorColumn)
        .addComponent(timeoutLabel)
        .addComponent(waitCursorTimeout));
  }

  private void addWaitSilent(Group horizontalGroup, SequentialGroup verticalGroup,
      GroupLayout panelLayout) {

    waitSilent.addItemListener(e -> {
      boolean selected = e.getStateChange() == ItemEvent.SELECTED;
      waitSilentTimeout.setEnabled(selected);
      waitSilentTime.setEnabled(selected);
      validate();
      repaint();
    });

    JLabel timeLabel = new JLabel("Wait for silent (millis): ");
    JLabel timeoutLabel = new JLabel("Timeout (millis): ");
    horizontalGroup.addGroup(panelLayout.createSequentialGroup()
        .addComponent(waitSilent)
        .addPreferredGap(ComponentPlacement.UNRELATED)
        .addComponent(timeLabel)
        .addPreferredGap(ComponentPlacement.RELATED)
        .addComponent(waitSilentTime, GroupLayout.PREFERRED_SIZE, TIME_WIDTH,
            GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(ComponentPlacement.UNRELATED)
        .addComponent(timeoutLabel)
        .addPreferredGap(ComponentPlacement.RELATED)
        .addComponent(waitSilentTimeout, GroupLayout.PREFERRED_SIZE, TIME_WIDTH,
            GroupLayout.PREFERRED_SIZE));
    verticalGroup.addGroup(panelLayout.createParallelGroup(Alignment.BASELINE)
        .addComponent(waitSilent)
        .addComponent(timeoutLabel)
        .addComponent(waitSilentTimeout)
        .addComponent(timeLabel)
        .addComponent(waitSilentTime));
  }

  private void addWaitText(Group horizontalGroup, SequentialGroup verticalGroup,
      GroupLayout panelLayout) {

    waitText.addItemListener(e -> {
      boolean selected = e.getStateChange() == ItemEvent.SELECTED;
      waitTextRegex.setEnabled(selected);
      waitTextAreaTop.setEnabled(selected);
      waitTextAreaLeft.setEnabled(selected);
      waitTextAreaBottom.setEnabled(selected);
      waitTextAreaRight.setEnabled(selected);
      waitTextTimeout.setEnabled(selected);
      validate();
      repaint();
    });

    JLabel regexLabel = new JLabel("Regex: ");
    SequentialGroup waitHorizontalGroup = panelLayout.createSequentialGroup();
    Group waitVerticalGroup = panelLayout.createParallelGroup(Alignment.BASELINE);
    horizontalGroup.addGroup(waitHorizontalGroup
        .addComponent(waitText)
        .addPreferredGap(ComponentPlacement.UNRELATED)
        .addComponent(regexLabel)
        .addPreferredGap(ComponentPlacement.RELATED)
        .addComponent(waitTextRegex, GroupLayout.PREFERRED_SIZE, 200,
            GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(ComponentPlacement.UNRELATED));
    verticalGroup.addGroup(waitVerticalGroup
        .addComponent(waitText)
        .addComponent(regexLabel)
        .addComponent(waitTextRegex));

    addSearchAreaPanel(waitHorizontalGroup, waitVerticalGroup);

    JLabel timeoutLabel = new JLabel("Timeout (millis): ");
    waitHorizontalGroup.addPreferredGap(ComponentPlacement.UNRELATED)
        .addComponent(timeoutLabel)
        .addPreferredGap(ComponentPlacement.RELATED)
        .addComponent(waitTextTimeout, GroupLayout.PREFERRED_SIZE, TIME_WIDTH,
            GroupLayout.PREFERRED_SIZE);
    waitVerticalGroup.addComponent(timeoutLabel)
        .addComponent(timeoutLabel)
        .addComponent(waitTextTimeout);
  }

  private void addSearchAreaPanel(SequentialGroup horizontalGroup, Group verticalGroup) {
    JLabel waitTextAreaTopLabel = new JLabel("Top row: ");
    JLabel waitTextAreaLeftLabel = new JLabel("Left column: ");
    JLabel waitTextAreaBottomLabel = new JLabel("Bottom row: ");
    JLabel waitTextAreaRightLabel = new JLabel("Right column: ");
    horizontalGroup.addComponent(waitTextAreaTopLabel)
        .addPreferredGap(ComponentPlacement.RELATED)
        .addComponent(waitTextAreaTop, GroupLayout.PREFERRED_SIZE, INDEX_WIDTH,
            GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(ComponentPlacement.UNRELATED)
        .addComponent(waitTextAreaLeftLabel)
        .addPreferredGap(ComponentPlacement.RELATED)
        .addComponent(waitTextAreaLeft, GroupLayout.PREFERRED_SIZE, INDEX_WIDTH,
            GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(ComponentPlacement.UNRELATED)
        .addComponent(waitTextAreaBottomLabel)
        .addPreferredGap(ComponentPlacement.RELATED)
        .addComponent(waitTextAreaBottom, GroupLayout.PREFERRED_SIZE, INDEX_WIDTH,
            GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(ComponentPlacement.UNRELATED)
        .addComponent(waitTextAreaRightLabel)
        .addPreferredGap(ComponentPlacement.RELATED)
        .addComponent(waitTextAreaRight, GroupLayout.PREFERRED_SIZE, INDEX_WIDTH,
            GroupLayout.PREFERRED_SIZE);
    verticalGroup.addComponent(waitTextAreaTopLabel)
        .addComponent(waitTextAreaTop)
        .addComponent(waitTextAreaLeftLabel)
        .addComponent(waitTextAreaLeft)
        .addComponent(waitTextAreaBottomLabel)
        .addComponent(waitTextAreaBottom)
        .addComponent(waitTextAreaRightLabel)
        .addComponent(waitTextAreaRight);
  }

  public void initFields() {
    payloadPanel.clear();
    disconnect.setSelected(false);
    sendInputs.setSelected(true);
    actions.get(RTESampler.DEFAULT_ACTION).setSelected(true);
    waitSync.setSelected(false);
    waitSyncTimeout.setText("");
    waitSyncTimeout.setEnabled(false);
    waitCursor.setSelected(false);
    waitCursorRow.setText("");
    waitCursorRow.setEnabled(false);
    waitCursorColumn.setText("");
    waitCursorColumn.setEnabled(false);
    waitCursorTimeout.setText("");
    waitCursorTimeout.setEnabled(false);
    waitSilent.setSelected(false);
    waitSilentTimeout.setText("");
    waitSilentTimeout.setEnabled(false);
    waitSilentTime.setText("");
    waitSilentTime.setEnabled(false);
    waitText.setSelected(false);
    waitTextTimeout.setText("");
    waitTextTimeout.setEnabled(false);
    waitTextRegex.setText("");
    waitTextRegex.setEnabled(false);
    waitTextAreaTop.setText("");
    waitTextAreaTop.setEnabled(false);
    waitTextAreaLeft.setText("");
    waitTextAreaLeft.setEnabled(false);
    waitTextAreaBottom.setText("");
    waitTextAreaBottom.setEnabled(false);
    waitTextAreaRight.setText("");
    waitTextAreaRight.setEnabled(false);
  }

  public CoordInputPanel getPayload() {
    return this.payloadPanel;
  }

  public Action getAction() {
    String action = actionsGroup.getSelection().getActionCommand();
    return Action.valueOf(action);
  }

  public void setAction(Action action) {
    if (actions.containsKey(action)) {
      actions.get(action).setSelected(true);
    } else {
      actions.get(RTESampler.DEFAULT_ACTION).setSelected(true);
    }
  }

  public boolean getDisconnect() {
    return this.disconnect.isSelected();
  }

  public void setDisconnect(boolean disconnect) {
    this.disconnect.setSelected(disconnect);
  }

  public boolean getSendInputs() {
    return this.sendInputs.isSelected();
  }

  public void setSendInputs(boolean sendInputs) {
    this.sendInputs.setSelected(sendInputs);
  }

  public boolean getWaitSync() {
    return this.waitSync.isSelected();
  }

  public void setWaitSync(boolean waitSync) {
    this.waitSync.setSelected(waitSync);
  }

  public String getWaitSyncTimeout() {
    return this.waitSyncTimeout.getText();
  }

  public void setWaitSyncTimeout(String waitSyncTimeout) {
    this.waitSyncTimeout.setText(waitSyncTimeout);
  }

  public boolean getWaitCursor() {
    return this.waitCursor.isSelected();
  }

  public void setWaitCursor(boolean waitCursor) {
    this.waitCursor.setSelected(waitCursor);
  }

  public String getWaitCursorRow() {
    return this.waitCursorRow.getText();
  }

  public void setWaitCursorRow(String waitCursorRow) {
    this.waitCursorRow.setText(waitCursorRow);
  }

  public String getWaitCursorColumn() {
    return this.waitCursorColumn.getText();
  }

  public void setWaitCursorColumn(String waitCursorColumn) {
    this.waitCursorColumn.setText(waitCursorColumn);
  }

  public String getWaitCursorTimeout() {
    return this.waitCursorTimeout.getText();
  }

  public void setWaitCursorTimeout(String waitCursorTimeout) {
    this.waitCursorTimeout.setText(waitCursorTimeout);
  }

  public boolean getWaitSilent() {
    return this.waitSilent.isSelected();
  }

  public void setWaitSilent(boolean waitSilent) {
    this.waitSilent.setSelected(waitSilent);
  }

  public String getWaitSilentTime() {
    return this.waitSilentTime.getText();
  }

  public void setWaitSilentTime(String waitSilentTime) {
    this.waitSilentTime.setText(waitSilentTime);
  }

  public String getWaitSilentTimeout() {
    return this.waitSilentTimeout.getText();
  }

  public void setWaitSilentTimeout(String waitSilentTimeout) {
    this.waitSilentTimeout.setText(waitSilentTimeout);
  }

  public boolean getWaitText() {
    return this.waitText.isSelected();
  }

  public void setWaitText(boolean waitText) {
    this.waitText.setSelected(waitText);
  }

  public String getWaitTextRegex() {
    return this.waitTextRegex.getText();
  }

  public void setWaitTextRegex(String waitTextRegex) {
    this.waitTextRegex.setText(waitTextRegex);
  }

  public String getWaitTextAreaTop() {
    return this.waitTextAreaTop.getText();
  }

  public void setWaitTextAreaTop(String row) {
    this.waitTextAreaTop.setText(row);
  }

  public String getWaitTextAreaLeft() {
    return this.waitTextAreaLeft.getText();
  }

  public void setWaitTextAreaLeft(String column) {
    this.waitTextAreaLeft.setText(column);
  }

  public String getWaitTextAreaBottom() {
    return this.waitTextAreaBottom.getText();
  }

  public void setWaitTextAreaBottom(String row) {
    this.waitTextAreaBottom.setText(row);
  }

  public String getWaitTextAreaRight() {
    return this.waitTextAreaRight.getText();
  }

  public void setWaitTextAreaRight(String column) {
    this.waitTextAreaRight.setText(column);
  }

  public String getWaitTextTimeout() {
    return this.waitTextTimeout.getText();
  }

  public void setWaitTextTimeout(String waitTextTimeout) {
    this.waitTextTimeout.setText(waitTextTimeout);
  }

}
