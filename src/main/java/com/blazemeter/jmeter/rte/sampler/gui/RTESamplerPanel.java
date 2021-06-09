package com.blazemeter.jmeter.rte.sampler.gui;

import com.blazemeter.jmeter.rte.core.AttentionKey;
import com.blazemeter.jmeter.rte.sampler.Action;
import com.blazemeter.jmeter.rte.sampler.RTESampler;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
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
  private static final String TIMEOUT_LABEL = "Timeout (millis): ";
  private static final int LABEL_TABULATION_SPACE = 50;
  private final JPanel requestPanel;
  private final JPanel waitPanel;
  private final ButtonGroup actionsGroup = new ButtonGroup();
  private final Map<Action, JRadioButton> actions = new EnumMap<>(Action.class);
  private InputPanel payloadPanel;
  private final ButtonGroup attentionKeysGroup = new ButtonGroup();
  private final Map<AttentionKey, JRadioButton> attentionKeys = new EnumMap<>(AttentionKey.class);
  private JPanel waitSyncPanel;
  private final JCheckBox waitSync = SwingUtils.createComponent("waitSync", new JCheckBox("Sync?"));
  private final JTextField waitSyncTimeout = SwingUtils
      .createComponent("waitSyncTimeout", new JTextField());
  private JPanel waitCursorPanel;
  private final JCheckBox waitCursor = SwingUtils
      .createComponent("waitCursor", new JCheckBox("Cursor?"));
  private final JTextField waitCursorRow = SwingUtils
      .createComponent("waitCursorRow", new JTextField());
  private final JTextField waitCursorColumn = SwingUtils
      .createComponent("waitCursorColumn", new JTextField());
  private final JTextField waitCursorTimeout = SwingUtils
      .createComponent("waitCursorTimeout", new JTextField());
  private JPanel waitSilentPanel;
  private final JCheckBox waitSilent = SwingUtils
      .createComponent("waitSilent", new JCheckBox("Silent?"));
  private final JTextField waitSilentTime = SwingUtils
      .createComponent("waitSilentTime", new JTextField());
  private final JTextField waitSilentTimeout = SwingUtils
      .createComponent("waitSilentTimeout", new JTextField());
  private JPanel waitTextPanel;
  private final JCheckBox waitText = SwingUtils.createComponent("waitText", new JCheckBox("Text?"));
  private final JTextField waitTextRegex = SwingUtils
      .createComponent("waitTextRegex", new JTextField());
  private final JTextField waitTextTimeout = SwingUtils
      .createComponent("waitTextTimeout", new JTextField());
  private final JTextField waitTextAreaTop = SwingUtils
      .createComponent("waitTextAreaTop", new JTextField());
  private final JTextField waitTextAreaLeft = SwingUtils
      .createComponent("waitTextAreaLeft", new JTextField());
  private final JTextField waitTextAreaBottom = SwingUtils
      .createComponent("waitTextAreaBottom", new JTextField());
  private final JTextField waitTextAreaRight = SwingUtils
      .createComponent("waitTextAreaRight", new JTextField());
  private final JCheckBox waitDisconnect = SwingUtils.createComponent("waitDisconnect",
      new JCheckBox("Disconnect?"));
  private final JTextField waitDisconnectTimeout = SwingUtils.createComponent(
      "waitDisconnectTimeout", new JTextField());
  private JPanel waitDisconnectPanel;

  public RTESamplerPanel() {
    GroupLayout layout = new GroupLayout(this);
    layout.setAutoCreateGaps(true);
    this.setLayout(layout);

    JPanel modePanel = buildModePanel();
    requestPanel = buildRequestPanel();
    waitPanel = buildWaitsPanel();
    BlazemeterLabsLogo blazemeterLabsLogo = new BlazemeterLabsLogo();

    layout.setHorizontalGroup(layout.createParallelGroup()
        .addComponent(modePanel, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE,
            Short.MAX_VALUE)
        .addComponent(requestPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE,
            Short.MAX_VALUE)
        .addComponent(waitPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE,
            Short.MAX_VALUE)
        .addComponent(blazemeterLabsLogo, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE,
            Short.MAX_VALUE));
    layout.setVerticalGroup(layout.createSequentialGroup()
        .addComponent(modePanel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
            GroupLayout.PREFERRED_SIZE)
        .addComponent(requestPanel)
        .addComponent(waitPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
            GroupLayout.PREFERRED_SIZE)
        .addComponent(blazemeterLabsLogo, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
            GroupLayout.PREFERRED_SIZE)
    );
  }

  private JPanel buildModePanel() {
    JPanel panel = SwingUtils.createComponent("modePanel", new JPanel());
    panel.setBorder(BorderFactory.createTitledBorder("Action"));
    panel.setLayout(new FlowLayout(FlowLayout.LEFT));

    Arrays.stream(Action.values()).forEach(t -> {
      JRadioButton r = SwingUtils.createComponent(t.toString(), new JRadioButton(t.getLabel()));
      r.setActionCommand(t.name());
      panel.add(r);
      actions.put(t, r);
      actionsGroup.add(r);
    });

    actions.forEach((a, r) ->
        r.addItemListener(e -> {
          if (e.getStateChange() == ItemEvent.SELECTED) {
            updateActionPanels(a);
          }
        })
    );

    return panel;
  }

  private void updateActionPanels(Action action) {
    waitPanel.setVisible(Action.DISCONNECT != action);
    requestPanel.setVisible(Action.SEND_INPUT == action);
    validate();
    repaint();
  }

  private JPanel buildRequestPanel() {
    JPanel panel = SwingUtils.createComponent("requestPanel", new JPanel());
    panel.setBorder(BorderFactory.createTitledBorder("RTE Message"));
    GroupLayout layout = new GroupLayout(panel);
    layout.setAutoCreateContainerGaps(true);
    panel.setLayout(layout);

    JLabel payloadLabel = SwingUtils.createComponent("payloadLabel", new JLabel("Payload: "));
    payloadPanel = SwingUtils.createComponent("payloadPanel", new InputPanel());
    JPanel attentionKeysPanel = buildAttentionKeysPanel();

    JLabel warningLabel = SwingUtils
        .createLabelWithWarningStyle("warningLabel", "Warning: AttentionKey buttons ATTN and " +
            "RESET are only supported for TN5250 protocol. " +
            "AttentionKey buttons PA1, PA2 and PA3 are only supported for TN3270 protocol.");
    JLabel warningLabelContinuation = SwingUtils
        .createLabelWithWarningStyle("warningLabelContinuation",
            "ROLL_UP and ROLL_DN are supported for VT420 and TN5250 protocols. VT420"
                + " also supports ENTER and all function keys.");

    layout.setHorizontalGroup(layout.createParallelGroup()
        .addComponent(payloadLabel)
        .addComponent(payloadPanel)
        .addComponent(attentionKeysPanel)
        .addComponent(warningLabel)
        .addGroup(layout.createSequentialGroup()
            .addGap(LABEL_TABULATION_SPACE)
            .addComponent(warningLabelContinuation)
        ));

    layout.setVerticalGroup(layout.createSequentialGroup()
        .addComponent(payloadLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
            GroupLayout.PREFERRED_SIZE)
        .addComponent(payloadPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE,
            Short.MAX_VALUE)
        .addComponent(attentionKeysPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
            GroupLayout.PREFERRED_SIZE)
        .addComponent(warningLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
            GroupLayout.PREFERRED_SIZE)
        .addComponent(warningLabelContinuation, GroupLayout.PREFERRED_SIZE,
            GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE));

    return panel;
  }

  private JPanel buildAttentionKeysPanel() {
    JPanel panel = SwingUtils.createComponent("attentionKeysPanel", new JPanel());
    panel.setBorder(BorderFactory.createTitledBorder("Attention keys"));
    panel.setLayout(new GridLayout(0, 12));

    Arrays.stream(AttentionKey.values()).forEach(t -> {
      JRadioButton r = SwingUtils.createComponent(t.toString(), new JRadioButton(t.toString()));
      r.setActionCommand(t.toString());
      panel.add(r);
      attentionKeys.put(t, r);
      attentionKeysGroup.add(r);
    });

    return panel;
  }

  private JPanel buildWaitsPanel() {
    JPanel panel = SwingUtils.createComponent("waitsPanel", new JPanel());
    panel.setBorder(BorderFactory.createTitledBorder("Wait for:"));
    GroupLayout layout = new GroupLayout(panel);
    layout.setAutoCreateContainerGaps(true);
    layout.setAutoCreateGaps(true);
    panel.setLayout(layout);

    waitSyncPanel = buildSimpleWaitPanel("waitSyncPanel", waitSync, waitSyncTimeout);
    waitCursorPanel = buildWaitCursorPanel();
    waitSilentPanel = buildWaitSilentPanel();
    waitTextPanel = buildWaitTextPanel();
    waitDisconnectPanel = buildSimpleWaitPanel("waitDisconnectPanel", waitDisconnect,
        waitDisconnectTimeout);

    JLabel warningLabel = SwingUtils
        .createComponent("warningLabel", new JLabel("Warning: if Timeout value " +
            "is shorter than Stable time, or Silent interval, " +
            "the sampler will return a Timeout exception. " +
            "For more information see sampler documentation."));
    warningLabel.setFont(new Font(null, Font.ITALIC, 11));

    layout.setHorizontalGroup(layout.createParallelGroup()
        .addComponent(waitSyncPanel)
        .addComponent(waitCursorPanel)
        .addComponent(waitSilentPanel)
        .addComponent(waitTextPanel)
        .addComponent(waitDisconnectPanel)
        .addComponent(warningLabel));
    layout.setVerticalGroup(layout.createSequentialGroup()
        .addComponent(waitSyncPanel)
        .addComponent(waitCursorPanel)
        .addComponent(waitSilentPanel)
        .addComponent(waitTextPanel)
        .addComponent(waitDisconnectPanel)
        .addComponent(warningLabel));

    return panel;
  }

  private JPanel buildSimpleWaitPanel(String panelName, JCheckBox waitCheck,
      JTextField waitTimeout) {
    JPanel panel = SwingUtils.createComponent(panelName, new JPanel());
    GroupLayout layout = new GroupLayout(panel);
    panel.setLayout(layout);

    waitCheck.addItemListener(e -> {
      updateWait(waitCheck, panel, e.getStateChange() == ItemEvent.SELECTED);
      validate();
      repaint();
    });
    JPanel labeledTimeoutPanel = buildLabeledTimeoutPanel(waitTimeout);
    layout.setHorizontalGroup(layout.createSequentialGroup()
        .addComponent(waitCheck)
        .addPreferredGap(ComponentPlacement.UNRELATED)
        .addComponent(labeledTimeoutPanel));
    layout.setVerticalGroup(layout.createParallelGroup(Alignment.BASELINE)
        .addComponent(waitCheck)
        .addComponent(labeledTimeoutPanel));

    return panel;
  }

  private JPanel buildLabeledTimeoutPanel(JTextField waitTimeout) {
    JPanel panel = SwingUtils.createComponent("labeledTimeout", new JPanel());
    GroupLayout layout = new GroupLayout(panel);
    panel.setLayout(layout);
    JLabel timeoutLabel = SwingUtils.createComponent("timeoutLabel",
        new JLabel(TIMEOUT_LABEL));
    layout.setHorizontalGroup(layout.createSequentialGroup()
        .addComponent(timeoutLabel)
        .addPreferredGap(ComponentPlacement.RELATED)
        .addComponent(waitTimeout, GroupLayout.PREFERRED_SIZE, TIME_WIDTH,
            GroupLayout.PREFERRED_SIZE));
    layout.setVerticalGroup(layout.createParallelGroup(Alignment.BASELINE)
        .addComponent(timeoutLabel)
        .addComponent(waitTimeout));
    return panel;
  }

  private void updateWait(JCheckBox waitCheck, JPanel panel, boolean checked) {
    SwingUtils.setEnabledRecursively(panel, checked);
    waitCheck.setEnabled(true);
  }

  private JPanel buildWaitCursorPanel() {
    JPanel panel = SwingUtils.createComponent("waitCursorPanel", new JPanel());
    GroupLayout layout = new GroupLayout(panel);
    panel.setLayout(layout);

    waitCursor.addItemListener(e -> {
      updateWait(waitCursor, panel, e.getStateChange() == ItemEvent.SELECTED);
      validate();
      repaint();
    });

    JLabel rowLabel = SwingUtils.createComponent("rowLabel", new JLabel("Row: "));
    JLabel columnLabel = SwingUtils.createComponent("columnLabel", new JLabel("Column: "));
    JPanel labeledTimeoutPanel = buildLabeledTimeoutPanel(waitCursorTimeout);
    layout.setHorizontalGroup(layout.createSequentialGroup()
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
        .addComponent(labeledTimeoutPanel));
    layout.setVerticalGroup(layout.createParallelGroup(Alignment.BASELINE)
        .addComponent(waitCursor)
        .addComponent(rowLabel)
        .addComponent(waitCursorRow)
        .addComponent(columnLabel)
        .addComponent(waitCursorColumn)
        .addComponent(labeledTimeoutPanel));

    return panel;
  }

  private JPanel buildWaitSilentPanel() {
    JPanel panel = SwingUtils.createComponent("waitSilentPanel", new JPanel());
    GroupLayout layout = new GroupLayout(panel);
    panel.setLayout(layout);

    waitSilent.addItemListener(e -> {
      updateWait(waitSilent, panel, e.getStateChange() == ItemEvent.SELECTED);
      validate();
      repaint();
    });

    JLabel timeLabel = SwingUtils
        .createComponent("timeLabel", new JLabel("Silent interval (millis): "));
    JPanel labeledTimeoutPanel = buildLabeledTimeoutPanel(waitSilentTimeout);
    layout.setHorizontalGroup(layout.createSequentialGroup()
        .addComponent(waitSilent)
        .addPreferredGap(ComponentPlacement.UNRELATED)
        .addComponent(timeLabel)
        .addPreferredGap(ComponentPlacement.RELATED)
        .addComponent(waitSilentTime, GroupLayout.PREFERRED_SIZE, TIME_WIDTH,
            GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(ComponentPlacement.UNRELATED)
        .addComponent(labeledTimeoutPanel));
    layout.setVerticalGroup(layout.createParallelGroup(Alignment.BASELINE)
        .addComponent(waitSilent)
        .addComponent(timeLabel)
        .addComponent(waitSilentTime)
        .addComponent(labeledTimeoutPanel));
    return panel;
  }

  private JPanel buildWaitTextPanel() {
    JPanel panel = SwingUtils.createComponent("waitTextPanel", new JPanel());
    GroupLayout layout = new GroupLayout(panel);
    panel.setLayout(layout);

    waitText.addItemListener(e -> {
      updateWait(waitText, panel, e.getStateChange() == ItemEvent.SELECTED);
      validate();
      repaint();
    });

    JLabel regexLabel = SwingUtils.createComponent("regexLabel", new JLabel("Regex: "));
    JPanel searchAreaPanel = buildSearchAreaPanel();
    JPanel labeledTimeoutPanel = buildLabeledTimeoutPanel(waitTextTimeout);
    layout.setHorizontalGroup(layout.createSequentialGroup()
        .addComponent(waitText)
        .addPreferredGap(ComponentPlacement.UNRELATED)
        .addGroup(layout.createParallelGroup()
            .addGroup(layout.createSequentialGroup()
                .addComponent(regexLabel)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(waitTextRegex, GroupLayout.PREFERRED_SIZE, 200,
                    GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(ComponentPlacement.UNRELATED)
                .addComponent(labeledTimeoutPanel))
            .addComponent(searchAreaPanel))
    );
    layout.setVerticalGroup(layout.createSequentialGroup()
        .addGroup(layout.createParallelGroup(Alignment.BASELINE)
            .addComponent(waitText)
            .addComponent(regexLabel)
            .addComponent(waitTextRegex)
            .addComponent(labeledTimeoutPanel))
        .addPreferredGap(ComponentPlacement.RELATED)
        .addComponent(searchAreaPanel));

    return panel;
  }

  private JPanel buildSearchAreaPanel() {
    JPanel panel = SwingUtils.createComponent("searchAreaPanel", new JPanel());
    panel.setBorder(BorderFactory.createTitledBorder("Search area: "));
    GroupLayout layout = new GroupLayout(panel);
    layout.setAutoCreateContainerGaps(true);
    panel.setLayout(layout);

    JLabel topLabel = SwingUtils.createComponent("topLabel", new JLabel("Top row: "));
    JLabel leftLabel = SwingUtils.createComponent("leftLabel", new JLabel("Left column: "));
    JLabel bottomLabel = SwingUtils.createComponent("bottomLabel", new JLabel("Bottom row: "));
    JLabel rightLabel = SwingUtils.createComponent("rightLabel", new JLabel("Right column: "));
    layout.setHorizontalGroup(
        layout.createSequentialGroup()
            .addComponent(leftLabel)
            .addPreferredGap(ComponentPlacement.RELATED)
            .addComponent(waitTextAreaLeft, GroupLayout.PREFERRED_SIZE, INDEX_WIDTH,
                GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(ComponentPlacement.UNRELATED)
            .addGroup(layout.createParallelGroup(Alignment.TRAILING)
                .addGroup(layout.createSequentialGroup()
                    .addComponent(topLabel)
                    .addPreferredGap(ComponentPlacement.RELATED)
                    .addComponent(waitTextAreaTop, GroupLayout.PREFERRED_SIZE, INDEX_WIDTH,
                        GroupLayout.PREFERRED_SIZE))
                .addGroup(layout.createSequentialGroup()
                    .addComponent(bottomLabel)
                    .addPreferredGap(ComponentPlacement.RELATED)
                    .addComponent(waitTextAreaBottom, GroupLayout.PREFERRED_SIZE, INDEX_WIDTH,
                        GroupLayout.PREFERRED_SIZE)
                )
            )
            .addPreferredGap(ComponentPlacement.UNRELATED)
            .addComponent(rightLabel)
            .addPreferredGap(ComponentPlacement.RELATED)
            .addComponent(waitTextAreaRight, GroupLayout.PREFERRED_SIZE, INDEX_WIDTH,
                GroupLayout.PREFERRED_SIZE));
    layout.setVerticalGroup(layout.createSequentialGroup()
        .addGroup(layout.createParallelGroup(Alignment.BASELINE, false)
            .addComponent(topLabel)
            .addComponent(waitTextAreaTop))
        .addPreferredGap(ComponentPlacement.UNRELATED)
        .addGroup(layout.createParallelGroup(Alignment.BASELINE, false)
            .addComponent(leftLabel)
            .addComponent(waitTextAreaLeft)
            .addComponent(rightLabel)
            .addComponent(waitTextAreaRight))
        .addPreferredGap(ComponentPlacement.UNRELATED)
        .addGroup(layout.createParallelGroup(Alignment.BASELINE, false)
            .addComponent(bottomLabel)
            .addComponent(waitTextAreaBottom)));

    return panel;
  }

  public void resetFields() {
    payloadPanel.clear();
  }

  public Action getAction() {
    String mode = actionsGroup.getSelection().getActionCommand();
    return Action.valueOf(mode);
  }

  public void setAction(Action action) {
    action = actions.containsKey(action) ? action : RTESampler.DEFAULT_ACTION;
    actions.get(action).setSelected(true);
    updateActionPanels(action);
  }

  public InputPanel getPayload() {
    return this.payloadPanel;
  }

  public AttentionKey getAttentionKey() {
    String attentionKey = attentionKeysGroup.getSelection().getActionCommand();
    return AttentionKey.valueOf(attentionKey);
  }

  public void setAttentionKey(AttentionKey attentionKey) {
    if (attentionKeys.containsKey(attentionKey)) {
      attentionKeys.get(attentionKey).setSelected(true);
    } else {
      attentionKeys.get(RTESampler.DEFAULT_ATTENTION_KEY).setSelected(true);
    }
  }

  public boolean getWaitSync() {
    return this.waitSync.isSelected();
  }

  public void setWaitSync(boolean waitSync) {
    this.waitSync.setSelected(waitSync);
    updateWait(this.waitSync, waitSyncPanel, waitSync);
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
    updateWait(this.waitCursor, waitCursorPanel, waitCursor);
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
    updateWait(this.waitSilent, waitSilentPanel, waitSilent);
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
    updateWait(this.waitText, waitTextPanel, waitText);
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

  public boolean getWaitDisconnect() {
    return this.waitDisconnect.isSelected();
  }

  public void setWaitDisconnect(boolean waitDisconnect) {
    this.waitDisconnect.setSelected(waitDisconnect);
    updateWait(this.waitDisconnect, waitDisconnectPanel, waitDisconnect);
  }

  public String getWaitDisconnectTimeout() {
    return this.waitDisconnectTimeout.getText();
  }

  public void setWaitDisconnectTimeout(String waitDisconnectTimeout) {
    this.waitDisconnectTimeout.setText(waitDisconnectTimeout);
  }
}
