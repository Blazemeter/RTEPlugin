package com.blazemeter.jmeter.rte.extractor;

import com.blazemeter.jmeter.rte.sampler.gui.SwingUtils;
import java.awt.BorderLayout;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;

public class RTEExtractorPanel extends JPanel {

  private JRadioButton cursorPosition = new JRadioButton("Extract cursor position");
  private JRadioButton nextFieldPosition = new JRadioButton(
      "Extract next field from position");

  private JTextField row = SwingUtils.createComponent("fieldRow", new JTextField());
  private JTextField column = SwingUtils.createComponent("fieldColumn", new JTextField());
  private JTextField offset = SwingUtils.createComponent("fieldOffset", new JTextField());
  private JTextField variablePrefix = SwingUtils
      .createComponent("variablePrefix", new JTextField());

  public RTEExtractorPanel() {
    GroupLayout layout = new GroupLayout(this);
    layout.setAutoCreateGaps(true);
    this.setLayout(layout);
    JPanel fieldPanel = buildFieldPanel();
    setRadioButtonConfiguration(fieldPanel);

    JLabel variableLabel = new JLabel("Create variable prefix");

    layout.setHorizontalGroup(layout.createParallelGroup()
        .addGap(GroupLayout.PREFERRED_SIZE, 10, GroupLayout.PREFERRED_SIZE)
        .addGroup(layout.createSequentialGroup()
            .addComponent(variableLabel)
            .addPreferredGap(ComponentPlacement.RELATED)
            .addComponent(variablePrefix, GroupLayout.PREFERRED_SIZE, 150,
                GroupLayout.PREFERRED_SIZE)
        )
        .addComponent(cursorPosition)
        .addComponent(nextFieldPosition)
        .addComponent(fieldPanel, Alignment.CENTER)
    );

    layout.setVerticalGroup(layout.createSequentialGroup()
        .addGap(GroupLayout.PREFERRED_SIZE, 10, GroupLayout.PREFERRED_SIZE)
        .addGroup(layout.createParallelGroup()
            .addComponent(variableLabel, Alignment.LEADING)
            .addComponent(variablePrefix, GroupLayout.PREFERRED_SIZE, 20,
                GroupLayout.PREFERRED_SIZE)
        )
        .addComponent(cursorPosition)
        .addComponent(nextFieldPosition)
        .addComponent(fieldPanel)
    );
  }

  private void setRadioButtonConfiguration(JPanel fieldPanel) {
    SwingUtils.setEnabledRecursively(fieldPanel, false);
    cursorPosition.setEnabled(true);
    cursorPosition.setName("cursorPosition");
    nextFieldPosition.setName("nextFieldPosition");
    ButtonGroup group = new ButtonGroup();
    group.add(cursorPosition);
    group.add(nextFieldPosition);
    cursorPosition.addItemListener(l -> {
      if (cursorPosition.isSelected()) {
        SwingUtils.setEnabledRecursively(fieldPanel, false);
      }
    });

    nextFieldPosition.addItemListener(l -> {
      if (nextFieldPosition.isSelected()) {
        SwingUtils.setEnabledRecursively(fieldPanel, true);
      }
    });
  }

  private JPanel buildFieldPanel() {
    JPanel panel = new JPanel(new BorderLayout());
    panel.setName("fieldPanel");
    GroupLayout layout = new GroupLayout(panel);
    layout.setAutoCreateGaps(true);
    layout.setAutoCreateContainerGaps(true);
    panel.setBorder(BorderFactory.createEtchedBorder());
    panel.setLayout(layout);

    JLabel rowLabel = new JLabel("row");
    JLabel columnLabel = new JLabel("column");
    JLabel skippedFields = new JLabel("tab offset");

    layout.setHorizontalGroup(layout.createSequentialGroup()
        .addGroup(layout.createParallelGroup()
            .addGroup(layout.createSequentialGroup()
                .addComponent(rowLabel)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(row, GroupLayout.PREFERRED_SIZE, 30,
                    GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(ComponentPlacement.UNRELATED)
                .addComponent(columnLabel)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(column, GroupLayout.PREFERRED_SIZE, 30,
                    GroupLayout.PREFERRED_SIZE)
                .addGap(GroupLayout.PREFERRED_SIZE, 25, GroupLayout.PREFERRED_SIZE)
                .addComponent(skippedFields)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(offset, GroupLayout.PREFERRED_SIZE, 30, GroupLayout.PREFERRED_SIZE)
            ))
    );

    layout.setVerticalGroup(layout.createSequentialGroup()
        .addGroup(layout.createParallelGroup(Alignment.BASELINE)
            .addComponent(rowLabel)
            .addComponent(row)
            .addComponent(columnLabel)
            .addComponent(column)
            .addComponent(skippedFields)
            .addComponent(offset))
    );

    return panel;
  }

  public PositionType getPositionType() {
    return cursorPosition.isSelected() ? PositionType.CURSOR_POSITION
        : PositionType.NEXT_FIELD_POSITION;
  }

  public void setPositionType(PositionType positionType) {
    if (positionType.equals(PositionType.CURSOR_POSITION)) {
      cursorPosition.setSelected(true);
    } else {
      nextFieldPosition.setSelected(true);
    }
  }

  public String getRow() {
    return row.getText();
  }

  public void setRow(String text) {
    row.setText(text);
  }

  public String getColumn() {
    return column.getText();
  }

  public void setColumn(String column) {
    this.column.setText(column);
  }

  public String getOffset() {
    return offset.getText();
  }

  public void setOffset(String offset) {
    this.offset.setText(offset);
  }

  public String getVariablePrefix() {
    return variablePrefix.getText();
  }

  public void setVariablePrefix(String prefix) {
    variablePrefix.setText(prefix);
  }
}
