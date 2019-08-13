package com.blazemeter.jmeter.rte.extractor;

import com.blazemeter.jmeter.rte.core.Position;
import com.blazemeter.jmeter.rte.sampler.gui.SwingUtils;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import org.apache.jmeter.util.JMeterUtils;

public class RTEExtractorPanel extends JPanel implements ActionListener {

  private JRadioButton cursorPosition = new JRadioButton("Extract cursor position");
  private JRadioButton nextFieldPosition = new JRadioButton(
      "Extract next field from position");

  private JTextField row = SwingUtils.createComponent("fieldRow", new JTextField());
  private JTextField column = SwingUtils.createComponent("fieldColumn", new JTextField());
  private JTextField offset = SwingUtils.createComponent("fieldOffset", new JTextField());
  private JTextField variablePrefix = SwingUtils
      .createComponent("variablePrefix", new JTextField());

  private Position maxPosition = new Position(24,
      80); // this should be obtained from xtnTerminalEmulator 

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
    ButtonGroup radios = new ButtonGroup();
    radios.add(cursorPosition);
    radios.add(nextFieldPosition);
    
    cursorPosition.setEnabled(true);
    cursorPosition.addItemListener(l -> {
      if (cursorPosition.isSelected()) {
        setEnabled(fieldPanel, false);

      }
    });

    nextFieldPosition.addItemListener(l -> {
      if (nextFieldPosition.isSelected()) {
        setEnabled(fieldPanel, true);
      }
    });
  }

  private JPanel buildFieldPanel() {
    JPanel panel = new JPanel(new BorderLayout());
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

  private void setEnabled(Component component, boolean enabled) {
    component.setEnabled(enabled);
    if (component instanceof Container) {
      for (Component child : ((Container) component).getComponents()) {
        setEnabled(child, enabled);
      }
    }
  }

  @Override
  public void actionPerformed(ActionEvent e) {
  }

  public Boolean isCursorPosition() {
    return cursorPosition.isSelected();
  }

  public void setCursorPosition(boolean cursorPosition) {
    this.cursorPosition.setEnabled(cursorPosition);
  }

  public Boolean isFieldPosition() {
    return nextFieldPosition.isSelected();
  }

  public void setNextFieldPosition(boolean fieldPosition) {
    this.nextFieldPosition.setEnabled(fieldPosition);
  }

  public int getRow() {
    int row = -1;
    try {
      row = Integer.parseInt(this.row.getText());
      if (row > 0 && row < maxPosition.getRow()) {
        return row;
      }
    } catch (NumberFormatException e) {
      JMeterUtils.reportErrorToUser("Incorrect row value", "Invalid Input");
      setColumn("");
    }
    return row;
  }

  public void setRow(String text) {
    row.setText(text);
  }

  public int getColumn() {
    int column = -1;
    try {
      column = Integer.parseInt(this.column.getText());
      if (column > 0 && column < maxPosition.getColumn()) {
        return column;
      }
    } catch (NumberFormatException e) {
      JMeterUtils.reportErrorToUser("Incorrect column value", "Invalid Input");
      setColumn("");
    }
    return column;

  }

  public void setColumn(String column) {
    this.column.setText(column);
  }

  public int getOffset() {
    int offset = -1;
    try {
      offset = Integer.parseInt(this.offset.getText());
    } catch (NumberFormatException e) {
      JMeterUtils.reportErrorToUser("Incorrect tab offset value", "Invalid Input");
      setOffset("");
    }
    return offset;
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
