package com.blazemeter.jmeter.rte.sampler.gui;

import com.blazemeter.jmeter.rte.sampler.CoordInputRowGUI;
import com.blazemeter.jmeter.rte.sampler.InputTestElement;
import com.blazemeter.jmeter.rte.sampler.Inputs;
import com.blazemeter.jmeter.rte.sampler.LabelInputRowGUI;
import com.blazemeter.jmeter.rte.sampler.NavigationInputRowGui;
import com.blazemeter.jmeter.rte.sampler.NavigationType;
import com.blazemeter.jmeter.rte.sampler.TabulatorInputRowGui;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JViewport;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.gui.util.HeaderAsPropertyRenderer;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.gui.GuiUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InputPanel extends JPanel implements ActionListener {

  private static final long serialVersionUID = -6184904133375045201L;
  private static final Logger LOG = LoggerFactory.getLogger(InputPanel.class);
  private static final String ADD_ACTION_LABEL = "addInputByLabel";
  private static final String ADD_ACTION_POSITION = "addInputByPosition";
  private static final String ADD_ACTION_NAVIGATION = "addInputByNavigation";
  private static final String ADD_ACTION = "addFromComboBox";
  private static final String DELETE_ACTION = "delete";
  private static final String UP_ACTION = "up";
  private static final String DOWN_ACTION = "down";
  private static final String CLIPBOARD_LINE_DELIMITERS = "\n";
  private static final String CLIPBOARD_ARG_DELIMITERS = "\t";
  private static final String INPUT_BY_LABEL = "Input by Label";
  private static final String INPUT_BY_POSITION = "Input by Position";
  private static final String INPUT_BY_NAVIGATION = "Input by Navigation";
  private static final String FROM_CLIPBOARD = "From Clipboard";

  private InputTableModel tableModel;
  private JTable table;
  private JButton deleteButton;
  private JButton upButton;
  private JButton downButton;
  private JComboBox comboType;

  public InputPanel() {
    initComponents();
    table.revalidate();
  }

  private static int getNumberOfVisibleRows(JTable table) {
    Rectangle vr = table.getVisibleRect();
    int first = table.rowAtPoint(vr.getLocation());
    vr.translate(0, vr.height);
    return table.rowAtPoint(vr.getLocation()) - first;
  }

  private void initComponents() {
    GroupLayout layout = new GroupLayout(this);
    layout.setAutoCreateGaps(true);
    setLayout(layout);
    Component mainPanel = makeMainPanel();
    JPanel buttonPanel = makeButtonPanel();
    JLabel warningLabel = warningLabel();

    layout.setVerticalGroup(layout.createSequentialGroup()
        .addComponent(mainPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
            GroupLayout.PREFERRED_SIZE)
        .addComponent(warningLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE,
            Short.MAX_VALUE)
        .addComponent(buttonPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
            GroupLayout.PREFERRED_SIZE));

    layout.setHorizontalGroup(layout.createParallelGroup()
        .addComponent(mainPanel)
        .addComponent(warningLabel)
        .addComponent(buttonPanel));
  }

  private JLabel warningLabel() {
    return SwingUtils
        .createLabelWithWarningStyle("warningInputByLabel", "Warning: Input by Position and Input "
            + "by Label are not supported for VT protocols");
  }

  private Component makeMainPanel() {
    if (tableModel == null) {
      tableModel = new InputTableModel(new Object[]{"Field", "Value"});
    }
    table = SwingUtils.createComponent("table", new JTable(tableModel));
    table.getTableHeader().setDefaultRenderer(new HeaderAsPropertyRenderer() {
      @Override
      protected String getText(Object value, int row, int column) {
        return (value == null) ? "" : value.toString();
      }
    });
    table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    TableColumn fieldColumn = table.getColumn("Field");
    fieldColumn.setCellRenderer(new FieldRenderer());
    fieldColumn.setCellEditor(new FieldEditor());
    int textFieldPreferredSize = new JTextField().getPreferredSize().height;
    table.setRowHeight(textFieldPreferredSize);
    table.setPreferredScrollableViewportSize(new Dimension(-1, textFieldPreferredSize * 5));
    JMeterUtils.applyHiDPI(table);
    return new JScrollPane(table);
  }

  private JPanel makeButtonPanel() {
    deleteButton = SwingUtils.createButton("deleteButton", "delete",
        DELETE_ACTION, this);
    upButton = SwingUtils.createButton("upButton", "up",
        UP_ACTION, this);
    downButton = SwingUtils.createButton("downButton", "down",
        DOWN_ACTION, this);
    JButton addButton = SwingUtils.createButton("addButton", "Add", ADD_ACTION, this);

    comboType = SwingUtils.createComponent("comboType", new JComboBox());

    comboType.setModel(getDefaultComboBoxModel());

    updateEnabledButtons();

    JPanel buttonPanel = SwingUtils.createComponent("buttonPanel", new JPanel());
    buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
    buttonPanel.add(comboType);
    buttonPanel.add(addButton);
    buttonPanel.add(deleteButton);
    buttonPanel.add(upButton);
    buttonPanel.add(downButton);
    return buttonPanel;
  }

  private DefaultComboBoxModel getDefaultComboBoxModel() {
    DefaultComboBoxModel model = new DefaultComboBoxModel();
    model.addElement(INPUT_BY_LABEL);
    model.addElement(INPUT_BY_NAVIGATION);
    model.addElement(INPUT_BY_POSITION);
    model.addElement(FROM_CLIPBOARD);
    return model;
  }

  private void updateEnabledButtons() {
    int rowCount = tableModel.getRowCount();
    deleteButton.setEnabled(isEnabled() && rowCount != 0);
    upButton.setEnabled(isEnabled() && rowCount > 1);
    downButton.setEnabled(isEnabled() && rowCount > 1);
  }

  public TestElement createTestElement() {
    GuiUtils.stopTableEditing(table);
    Inputs inputs = new Inputs();
    inputs.clear();
    for (InputTestElement input : tableModel) {
      inputs.addInput(input);
    }
    return inputs;
  }

  public void configure(Inputs i) {
    if (i != null) {
      tableModel.clearData();
      for (JMeterProperty jMeterProperty : i) {
        InputTestElement input = (InputTestElement) jMeterProperty.getObjectValue();
        /* 
          Backward compatibility with those TestPlans which still
          using TabulatorInput instead of NavigationInput.
          Here we are also migrating to new format (NavigationInputRowGui)
         */
        if (input instanceof TabulatorInputRowGui) {
          NavigationInputRowGui navInput = new NavigationInputRowGui();
          navInput.setType(NavigationType.TAB.getLabel());
          navInput.setRepeated(((TabulatorInputRowGui) input).getOffset());
          navInput.setInput(input.getInput());
          input = navInput;
        }
        tableModel.addRow(input);
      }
    }
    updateEnabledButtons();
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    String action = e.getActionCommand();
    switch (action) {
      case ADD_ACTION:
        String selectedType = (String) comboType.getSelectedItem();
        switch (selectedType) {
          case INPUT_BY_LABEL:
            addArgument(ADD_ACTION_LABEL);
            break;
          case INPUT_BY_POSITION:
            addArgument(ADD_ACTION_POSITION);
            break;
          case FROM_CLIPBOARD:
            addFromClipboard();
            break;
          case INPUT_BY_NAVIGATION:
            addArgument(ADD_ACTION_NAVIGATION);
            break;
          default:
            throw new UnsupportedOperationException(selectedType);
        }
        break;
      case DELETE_ACTION:
        deleteArgument();
        break;
      case UP_ACTION:
        moveUp();
        break;
      case DOWN_ACTION:
        moveDown();
        break;
      default:
        throw new UnsupportedOperationException(action);
    }
  }

  private void addArgument(String type) {
    // If a table cell is being edited, we should accept the current value
    // and stop the editing before adding a new row.
    GuiUtils.stopTableEditing(table);

    if (ADD_ACTION_POSITION.equals(type)) {
      tableModel.addRow(new CoordInputRowGUI());
    } else if (ADD_ACTION_LABEL.equals(type)) {
      tableModel.addRow(new LabelInputRowGUI());
    } else if (ADD_ACTION_NAVIGATION.equals(type)) {
      tableModel.addRow(new NavigationInputRowGui());
    }

    updateEnabledButtons();

    // Highlight (select) and scroll to the appropriate row.
    int rowToSelect = tableModel.getRowCount() - 1;
    table.setRowSelectionInterval(rowToSelect, rowToSelect);
    table.scrollRectToVisible(table.getCellRect(rowToSelect, 0, true));
  }

  private void addFromClipboard() {
    GuiUtils.stopTableEditing(table);
    int rowCount = table.getRowCount();
    try {
      String clipboardContent = GuiUtils.getPastedText();
      if (StringUtils.isEmpty(clipboardContent)) {
        return;
      }
      String[] clipboardLines = clipboardContent.split(CLIPBOARD_LINE_DELIMITERS);
      for (String clipboardLine : clipboardLines) {
        String[] clipboardCols = clipboardLine.split(CLIPBOARD_ARG_DELIMITERS);
        if (clipboardCols.length > 0) {
          InputTestElement input = buildArgumentFromClipboard(clipboardCols);
          tableModel.addRow(input);
        }
      }
      if (table.getRowCount() > rowCount) {
        updateEnabledButtons();

        // Highlight (select) and scroll to the appropriate rows.
        int rowToSelect = tableModel.getRowCount() - 1;
        table.setRowSelectionInterval(rowCount, rowToSelect);
        table.scrollRectToVisible(table.getCellRect(rowCount, 0, true));
      }
    } catch (IOException ioe) {
      JOptionPane.showMessageDialog(this,
          "Could not add read arguments from clipboard:\n" + ioe.getLocalizedMessage(), "Error",
          JOptionPane.ERROR_MESSAGE);
    } catch (UnsupportedFlavorException ufe) {
      JOptionPane
          .showMessageDialog(this,
              "Could not add retrieve " + DataFlavor.stringFlavor.getHumanPresentableName()
                  + " from clipboard" + ufe.getLocalizedMessage(),
              "Error", JOptionPane.ERROR_MESSAGE);
    }
  }

  private InputTestElement buildArgumentFromClipboard(String[] clipboardCols) {

    if (clipboardCols.length >= 3) {
      CoordInputRowGUI argument = new CoordInputRowGUI();
      argument.setRow(clipboardCols[0]);
      argument.setColumn(clipboardCols[1]);
      argument.setInput(clipboardCols[2]);
      return argument;
    } else {
      String value = "";
      if (clipboardCols.length == 2) {
        value = clipboardCols[1];
      }
      try {
        return NavigationInputRowGui.parse(clipboardCols[0], value);
      } catch (IllegalArgumentException e) {
        if (value.isEmpty()) {
          NavigationInputRowGui defaultArgument = new NavigationInputRowGui();
          defaultArgument.setRepeated("0");
          defaultArgument.setInput(clipboardCols[0]);
          defaultArgument.setType(NavigationType.TAB.getLabel());
          return defaultArgument;
        }
        LOG.info(e.getMessage());
        LOG.info("Building argument: '{}' as Input by Label",
            clipboardCols[0] + " " + value);
        LabelInputRowGUI labelArgument = new LabelInputRowGUI();
        labelArgument.setLabel(clipboardCols[0]);
        labelArgument.setInput(value);
        return labelArgument;
      }
    }

  }

  private void deleteArgument() {
    GuiUtils.cancelEditing(table);

    int[] rowsSelected = table.getSelectedRows();
    int anchorSelection = table.getSelectionModel().getAnchorSelectionIndex();
    table.clearSelection();
    if (rowsSelected.length > 0) {
      for (int i = rowsSelected.length - 1; i >= 0; i--) {
        tableModel.deleteRow(rowsSelected[i]);
      }
      // Table still contains one or more rows, so highlight (select)
      // the appropriate one.
      if (tableModel.getRowCount() > 0) {
        if (anchorSelection >= tableModel.getRowCount()) {
          anchorSelection = tableModel.getRowCount() - 1;
        }
        table.setRowSelectionInterval(anchorSelection, anchorSelection);
      }

      updateEnabledButtons();
    }
  }

  private void moveUp() {
    // get the selected rows before stopping editing
    // or the selected rows will be unselected
    int[] rowsSelected = table.getSelectedRows();
    GuiUtils.stopTableEditing(table);

    if (rowsSelected.length > 0 && rowsSelected[0] > 0) {
      table.clearSelection();
      for (int rowSelected : rowsSelected) {
        tableModel.switchRows(rowSelected, rowSelected - 1);
      }

      for (int rowSelected : rowsSelected) {
        table.addRowSelectionInterval(rowSelected - 1, rowSelected - 1);
      }

      scrollToRowIfNotVisible(rowsSelected[0] - 1);
    }
  }

  private void scrollToRowIfNotVisible(int rowIndx) {
    if (table.getParent() instanceof JViewport) {
      Rectangle visibleRect = table.getVisibleRect();
      final int cellIndex = 0;
      Rectangle cellRect = table.getCellRect(rowIndx, cellIndex, false);
      if (visibleRect.y > cellRect.y) {
        table.scrollRectToVisible(cellRect);
      } else {
        Rectangle rect2 = table
            .getCellRect(rowIndx + getNumberOfVisibleRows(table), cellIndex, true);
        int width = rect2.y - cellRect.y;
        table.scrollRectToVisible(
            new Rectangle(cellRect.x, cellRect.y, cellRect.width, cellRect.height + width));
      }
    }
  }

  private void moveDown() {
    // get the selected rows before stopping editing
    // or the selected rows will be unselected
    int[] rowsSelected = table.getSelectedRows();
    GuiUtils.stopTableEditing(table);

    if (rowsSelected.length > 0
        && rowsSelected[rowsSelected.length - 1] < table.getRowCount() - 1) {
      table.clearSelection();
      for (int i = rowsSelected.length - 1; i >= 0; i--) {
        int rowSelected = rowsSelected[i];
        tableModel.switchRows(rowSelected, rowSelected + 1);
      }
      for (int rowSelected : rowsSelected) {
        table.addRowSelectionInterval(rowSelected + 1, rowSelected + 1);
      }

      scrollToRowIfNotVisible(rowsSelected[0] + 1);
    }
  }

  public void clear() {
    GuiUtils.stopTableEditing(table);
    tableModel.clearData();
  }

  @Override
  public void setEnabled(boolean enabled) {
    super.setEnabled(enabled);
    table.setEnabled(enabled);
    updateEnabledButtons();
  }

  protected static class FieldPanel extends JPanel {

    private final GroupLayout layout;
    private JLabel label = new JLabel();
    private JTextField fieldRow = SwingUtils.createComponent("fieldRow", new JTextField());
    private JTextField fieldColumn = SwingUtils.createComponent("fieldColumn", new JTextField());
    private JTextField fieldLabel = SwingUtils.createComponent("fieldLabel", new JTextField());
    private JTextField fieldNavigation = SwingUtils
        .createComponent("fieldNavigation", new JTextField());
    private JComboBox navigationCombo = SwingUtils.createComponent("navigationCombo",
        new JComboBox());
    private JLabel navigationLabel = SwingUtils.createComponent("navigationLabel", new JLabel(
        "Repeat: "));
    private DefaultComboBoxModel model = new DefaultComboBoxModel();

    private FieldPanel() {
      layout = new GroupLayout(this);
      layout.setAutoCreateGaps(true);
      setLayout(layout);
      setupNavigationCombo();
    }

    private void updateFromField(InputTestElement value) {
      this.removeAll();
      if (value instanceof CoordInputRowGUI) {
        buildPanel((CoordInputRowGUI) value);
      } else if (value instanceof LabelInputRowGUI) {
        buildPanel((LabelInputRowGUI) value);
      } else if (value instanceof NavigationInputRowGui) {
        buildPanel((NavigationInputRowGui) value);
      }
    }

    private void updateField(InputTestElement value) {
      if (value instanceof CoordInputRowGUI) {
        CoordInputRowGUI input = (CoordInputRowGUI) value;
        input.setRow(fieldRow.getText());
        input.setColumn(fieldColumn.getText());
      }
      if (value instanceof LabelInputRowGUI) {
        LabelInputRowGUI input = (LabelInputRowGUI) value;
        input.setLabel(fieldLabel.getText());
      }
      if (value instanceof NavigationInputRowGui) {
        NavigationInputRowGui input = (NavigationInputRowGui) value;
        String offset = verifyOffsetIntegrity(fieldNavigation.getText());
        input.setRepeated(offset);
        input.setType((String) navigationCombo.getSelectedItem());
      }
    }

    private String verifyOffsetIntegrity(String text) {
      try {
        if (Integer.valueOf(text) < 0) {
          JOptionPane.showMessageDialog(this, "Tabulator offset can not be lower than zero");
          return String.valueOf(1);
        }
      } catch (NumberFormatException e) {
        return text;
      }
      return text;
    }

    private void buildPanel(CoordInputRowGUI coordInputRowGUI) {
      label.setText("Position (Row Column)");
      fieldRow.setText(coordInputRowGUI.getRow());
      fieldColumn.setText(coordInputRowGUI.getColumn());
      layout.setHorizontalGroup(layout.createSequentialGroup()
          .addComponent(label)
          .addComponent(fieldRow)
          .addComponent(fieldColumn));
      layout.setVerticalGroup(layout.createParallelGroup(Alignment.BASELINE)
          .addComponent(label)
          .addComponent(fieldRow)
          .addComponent(fieldColumn));
    }

    private void buildPanel(LabelInputRowGUI labelInputRowGUI) {
      label.setText("Label");
      fieldLabel.setText(labelInputRowGUI.getLabel());
      layout.setHorizontalGroup(layout.createSequentialGroup()
          .addComponent(label)
          .addComponent(fieldLabel));
      layout.setVerticalGroup(layout.createParallelGroup(Alignment.BASELINE)
          .addComponent(label)
          .addComponent(fieldLabel));
    }

    private void buildPanel(NavigationInputRowGui navigationInputRowGui) {
      label.setText("Navigation ");
      fieldNavigation.setText(navigationInputRowGui.getRepeated());
      navigationCombo.setSelectedItem(navigationInputRowGui.getTypeNavigation().getLabel());
      layout.setHorizontalGroup(layout.createSequentialGroup()
          .addComponent(label)
          .addComponent(navigationCombo)
          .addComponent(navigationLabel)
          .addComponent(fieldNavigation));
      layout.setVerticalGroup(layout.createParallelGroup(Alignment.BASELINE)
          .addComponent(label)
          .addComponent(navigationCombo)
          .addComponent(navigationLabel)
          .addComponent(fieldNavigation));
    }

    private void setupNavigationCombo() {
      Arrays.stream(NavigationType.values())
          .map(NavigationType::getLabel)
          .forEach(model::addElement);
      navigationCombo.setModel(model);
    }

  }

  private static class FieldRenderer implements TableCellRenderer {

    private final FieldPanel fieldPanel = new FieldPanel();

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
        boolean hasFocus, int row, int column) {
      fieldPanel.updateFromField((InputTestElement) value);
      return fieldPanel;
    }

  }

  private static class FieldEditor extends AbstractCellEditor implements TableCellEditor {

    private final FieldPanel fieldPanel = new FieldPanel();
    private InputTestElement value;

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected,
        int row, int column) {
      this.value = (InputTestElement) value;
      fieldPanel.updateFromField(this.value);
      return fieldPanel;
    }

    @Override
    public Object getCellEditorValue() {
      fieldPanel.updateField(value);
      return value;
    }

  }

  private class InputTableModel extends DefaultTableModel implements Iterable<InputTestElement> {

    private transient ArrayList<InputTestElement> inputs;

    private InputTableModel(Object[] header) {
      super(header, 0);
      inputs = new ArrayList<>();
    }

    private void clearData() {
      int size = this.getRowCount();
      this.inputs.clear();
      super.fireTableRowsDeleted(0, size);
    }

    private void deleteRow(int row) {
      LOG.debug("Removing row: {}", row);
      this.inputs.remove(row);
      fireTableRowsDeleted(row, row);
    }

    private void addRow(InputTestElement value) {
      LOG.debug("Adding row value: {}", value);
      inputs.add(value);
      int insertedRowIndex = inputs.size() - 1;
      super.fireTableRowsInserted(insertedRowIndex, insertedRowIndex);
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public Iterator<InputTestElement> iterator() {
      return this.inputs.iterator();
    }

    @Override
    public int getRowCount() {
      return this.inputs == null ? 0 : this.inputs.size();
    }

    @Override
    public Object getValueAt(int row, int col) {
      if (col == 0) {
        return this.inputs.get(row);
      } else if (col == 1) {
        return this.inputs.get(row).getInput();
      } else {
        return "";
      }
    }

    @Override
    public void setValueAt(Object cellValue, int row, int col) {
      if (row < this.inputs.size()) {
        if (col == 0) {
          if (cellValue instanceof InputTestElement) {
            inputs.get(row).copyOf((InputTestElement) cellValue);
          }
        } else if (col == 1) {
          if (cellValue instanceof String) {
            this.inputs.get(row).setInput((String) cellValue);
          }
        }
      }
    }

    private void switchRows(int row1, int row2) {
      InputTestElement temp = inputs.get(row1);
      inputs.set(row1, inputs.get(row2));
      inputs.set(row2, temp);
    }
  }

}
