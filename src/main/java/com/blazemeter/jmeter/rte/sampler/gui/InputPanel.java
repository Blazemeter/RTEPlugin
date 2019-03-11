package com.blazemeter.jmeter.rte.sampler.gui;

import com.blazemeter.jmeter.rte.sampler.CoordInputRowGUI;
import com.blazemeter.jmeter.rte.sampler.InputRowGUI;
import com.blazemeter.jmeter.rte.sampler.Inputs;
import com.blazemeter.jmeter.rte.sampler.LabelInputRowGUI;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Rectangle;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JViewport;
import javax.swing.LayoutStyle.ComponentPlacement;
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
  private static final String ADD_FROM_CLIPBOARD_ACTION = "addFromClipboard";
  private static final String DELETE_ACTION = "delete";
  private static final String UP_ACTION = "up";
  private static final String DOWN_ACTION = "down";
  private static final String CLIPBOARD_LINE_DELIMITERS = "\n";
  private static final String CLIPBOARD_ARG_DELIMITERS = "\t";

  private transient InputTableModel tableModel;
  private transient JTable table;
  private JButton addButtonLabel;
  private JButton addButtonPosition;
  private JButton addFromClipboardButton;
  private JButton deleteButton;
  private JButton upButton;
  private JButton downButton;

  public InputPanel() {
    setLayout(new BorderLayout());
    add(makeMainPanel(), BorderLayout.CENTER);
    add(makeButtonPanel(), BorderLayout.SOUTH);
    table.revalidate();
  }

  private static int getNumberOfVisibleRows(JTable table) {
    Rectangle vr = table.getVisibleRect();
    int first = table.rowAtPoint(vr.getLocation());
    vr.translate(0, vr.height);
    return table.rowAtPoint(vr.getLocation()) - first;
  }

  private Component makeMainPanel() {
    initializeTableModel();
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
    table.setRowHeight(20);
    JMeterUtils.applyHiDPI(table);
    JScrollPane pane = new JScrollPane(table);
    pane.setPreferredSize(pane.getMinimumSize());
    return pane;
  }

  private void initializeTableModel() {
    if (tableModel == null) {
      tableModel = new InputTableModel(new Object[]{"Field", "Value"});
    }
  }

  private JPanel makeButtonPanel() {

    addButtonLabel = SwingUtils
        .createComponent("addButtonLabel", new JButton("Add Input by Label"));
    addButtonLabel.setActionCommand(ADD_ACTION_LABEL);
    addButtonLabel.setEnabled(true);

    addButtonPosition = SwingUtils
        .createComponent("addButtonPosition", new JButton("Add Input by Position"));
    addButtonPosition.setActionCommand(ADD_ACTION_POSITION);
    addButtonPosition.setEnabled(true);

    addFromClipboardButton = SwingUtils.createComponent("addFromClipboardButton",
        new JButton(JMeterUtils.getResString("add_from_clipboard")));
    addFromClipboardButton.setActionCommand(ADD_FROM_CLIPBOARD_ACTION);
    addFromClipboardButton.setEnabled(true);

    deleteButton = SwingUtils
        .createComponent("deleteButton", new JButton(JMeterUtils.getResString("delete")));
    deleteButton.setActionCommand(DELETE_ACTION);

    upButton = SwingUtils.createComponent("upButton", new JButton(JMeterUtils.getResString("up")));
    upButton.setActionCommand(UP_ACTION);

    downButton = SwingUtils
        .createComponent("downButton", new JButton(JMeterUtils.getResString("down")));
    downButton.setActionCommand(DOWN_ACTION);

    updateEnabledButtons();

    JPanel buttonPanel = SwingUtils.createComponent("buttonPanel", new JPanel());
    buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));

    addButtonLabel.addActionListener(this);
    addButtonPosition.addActionListener(this);
    addFromClipboardButton.addActionListener(this);
    deleteButton.addActionListener(this);
    upButton.addActionListener(this);
    downButton.addActionListener(this);
    buttonPanel.add(addButtonLabel);
    buttonPanel.add(addButtonPosition);
    buttonPanel.add(addFromClipboardButton);
    buttonPanel.add(deleteButton);
    buttonPanel.add(upButton);
    buttonPanel.add(downButton);
    return buttonPanel;
  }

  private void updateEnabledButtons() {
    int rowCount = tableModel.getRowCount();
    deleteButton.setEnabled(isEnabled() && rowCount != 0);
    upButton.setEnabled(isEnabled() && rowCount > 1);
    downButton.setEnabled(isEnabled() && rowCount > 1);
  }

  public TestElement createTestElement() {
    Inputs inputs = new Inputs();
    modifyTestElement(inputs);
    return inputs;
  }

  private void modifyTestElement(TestElement element) {
    GuiUtils.stopTableEditing(table);
    if (element instanceof Inputs) {
      Inputs inputs = (Inputs) element;
      inputs.clear();
      @SuppressWarnings("unchecked")
      Iterator<InputRowGUI> modelData = tableModel.iterator();
      while (modelData.hasNext()) {
        InputRowGUI input = modelData.next();
        if (!StringUtils.isEmpty(input.getInput())) {
          inputs.addInput(input);
        }
      }
    }
  }

  public void configure(TestElement el) {
    if (el instanceof Inputs) {
      tableModel.clearData();
      for (JMeterProperty jMeterProperty : (Inputs) el) {
        InputRowGUI input = (InputRowGUI) jMeterProperty.getObjectValue();
        tableModel.addRow(input);
      }
    }
    updateEnabledButtons();
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    String action = e.getActionCommand();
    switch (action) {
      case ADD_ACTION_LABEL:
        addArgument(ADD_ACTION_LABEL);
        break;
      case ADD_ACTION_POSITION:
        addArgument(ADD_ACTION_POSITION);
        break;
      case ADD_FROM_CLIPBOARD_ACTION:
        addFromClipboard();
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
      if (clipboardContent == null) {
        return;
      }
      String[] clipboardLines = clipboardContent.split(CLIPBOARD_LINE_DELIMITERS);
      for (String clipboardLine : clipboardLines) {
        String[] clipboardCols = clipboardLine.split(CLIPBOARD_ARG_DELIMITERS);
        if (clipboardCols.length > 0) {
          InputRowGUI input = buildArgumentFromClipboard(clipboardCols);
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

  private InputRowGUI buildArgumentFromClipboard(String[] clipboardCols) {
    
    if (clipboardCols.length >= 3) {
      CoordInputRowGUI argument = new CoordInputRowGUI();
      argument.setRow(clipboardCols[0]);
      argument.setColumn(clipboardCols[1]);
      argument.setInput(clipboardCols[2]);
      return argument;
    }
    
    if (clipboardCols.length == 2) {
      LabelInputRowGUI labelArgument = new LabelInputRowGUI();
      labelArgument.setLabel(clipboardCols[0]);
      labelArgument.setInput(clipboardCols[1]);
      return labelArgument;
    }
    
    if (clipboardCols.length < 1) {
      CoordInputRowGUI defaultArgument = new CoordInputRowGUI();
      defaultArgument.setRow("1");
      defaultArgument.setColumn("2");
      defaultArgument.setInput(clipboardCols[0]);
      return defaultArgument;
    }
    return null;
  }

  private int parseCoordIndex(String val) {
    try {
      return Integer.valueOf(val);
    } catch (NumberFormatException e) {
      LOG.warn("Invalid value ({}) for coordinate index.", val);
      return 1;
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
    addButtonLabel.setEnabled(enabled);
    addButtonPosition.setEnabled(enabled);
    addFromClipboardButton.setEnabled(enabled);
    updateEnabledButtons();
  }

  private class InputTableModel extends DefaultTableModel {

    private transient ArrayList<InputRowGUI> inputs;

    private InputTableModel(Object[] header) {
      super(header, 0);
      inputs = new ArrayList<>();
    }

    public void clearData() {
      int size = this.getRowCount();
      this.inputs.clear();
      super.fireTableRowsDeleted(0, size);
    }
    
    private void deleteRow(int row) {
      LOG.debug("Removing row value: " + row);
      this.inputs.remove(row);
      fireTableRowsDeleted(row, row);
    }
    
    private void addRow(InputRowGUI value) {
      LOG.debug("Adding row value: " + value);
      inputs.add(value);
      int insertedRowIndex = inputs.size() - 1;
      super.fireTableRowsInserted(insertedRowIndex, insertedRowIndex);
    }
    
    public Iterator<InputRowGUI> iterator() {
      return this.inputs.iterator();
    }

    public int getRowCount() {
      return this.inputs == null ? 0 : this.inputs.size();
    }

    public Object getValueAt(int row, int col) {
      LOG.debug("Getting row value");
      if (col == 0) {
        return this.inputs.get(row);
      } else if (col == 1) {
        return this.inputs.get(row).getInput();
      } else {
        return "";
      }
    }

    public void setValueAt(Object cellValue, int row, int col) {
      if (row < this.inputs.size()) {
        if (col == 0) {
          if (cellValue instanceof CoordInputRowGUI) {
            CoordInputRowGUI coordInputRowGUI = (CoordInputRowGUI) cellValue;
            ((CoordInputRowGUI) this.inputs.get(row)).setColumn(coordInputRowGUI.getColumn());
            ((CoordInputRowGUI) this.inputs.get(row)).setRow(coordInputRowGUI.getRow());
            this.inputs.get(row).setInput(coordInputRowGUI.getInput());
          } else if (cellValue instanceof LabelInputRowGUI) {
            LabelInputRowGUI labelInputRowGUI = (LabelInputRowGUI) cellValue;
            ((LabelInputRowGUI) this.inputs.get(row)).setLabel(labelInputRowGUI.getLabel());
            this.inputs.get(row).setInput(labelInputRowGUI.getInput());
          }
        } else if (col == 1) {
          if (cellValue instanceof String) {
            this.inputs.get(row).setInput(((String) cellValue));
          }
        }
      }
    }

    public void switchRows(int row1, int row2) {
      InputRowGUI temp = inputs.get(row1);
      inputs.set(row1, inputs.get(row2));
      inputs.set(row2, temp);
    }
  
  }

  private static class FieldPanel extends JPanel {

    private final GroupLayout layout;
    private JLabel label = new JLabel();
    private JTextField rowField = SwingUtils.createComponent("rowField", new JTextField());
    private JTextField columnField = SwingUtils.createComponent("columField", new JTextField());
    private JTextField labelField = SwingUtils.createComponent("labelField", new JTextField());

    private FieldPanel() {
      layout = new GroupLayout(this);
      setLayout(layout);
    }

    private void updateFromField(InputRowGUI value) {
      this.removeAll();
      if (value instanceof CoordInputRowGUI) {
        buildPanel((CoordInputRowGUI) value);
      } else if (value instanceof LabelInputRowGUI) {
        buildPanel((LabelInputRowGUI) value);
      }
    }

    private void updateField(InputRowGUI value) {
      if (value instanceof CoordInputRowGUI) {
        CoordInputRowGUI input = (CoordInputRowGUI) value;
        input.setRow(rowField.getText());
        input.setColumn(columnField.getText());
      }
      if (value instanceof LabelInputRowGUI) {
        LabelInputRowGUI input = (LabelInputRowGUI) value;
        input.setLabel(labelField.getText());
      }
    }

    private void buildPanel(CoordInputRowGUI coordInputRowGUI) {
      label.setText("Position (Row Column)");
      rowField.setText(coordInputRowGUI.getRow());
      columnField.setText(coordInputRowGUI.getColumn());
      layout.setHorizontalGroup(layout.createSequentialGroup()
          .addComponent(label)
          .addPreferredGap(ComponentPlacement.RELATED)
          .addComponent(rowField, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE,
              Short.MAX_VALUE)
          .addPreferredGap(ComponentPlacement.RELATED)
          .addComponent(columnField, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE,
              Short.MAX_VALUE));
      layout.setVerticalGroup(layout.createParallelGroup(Alignment.LEADING)
          .addComponent(label, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
              Short.MAX_VALUE)
          .addComponent(rowField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
              Short.MAX_VALUE)
          .addComponent(columnField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
              Short.MAX_VALUE));
    }

    private void buildPanel(LabelInputRowGUI labelInputRowGUI) {
      label.setText("Label");
      labelField.setText(labelInputRowGUI.getLabel());
      layout.setHorizontalGroup(layout.createSequentialGroup()
          .addComponent(label, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE,
              GroupLayout.DEFAULT_SIZE)
          .addPreferredGap(ComponentPlacement.RELATED)
          .addComponent(labelField, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE,
              Short.MAX_VALUE)
          .addPreferredGap(ComponentPlacement.UNRELATED));
      layout.setVerticalGroup(layout.createParallelGroup(Alignment.LEADING)
          .addComponent(label, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
              Short.MAX_VALUE)
          .addComponent(labelField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
              Short.MAX_VALUE));
    }

  }

  private static class FieldRenderer implements TableCellRenderer {

    private final FieldPanel fieldPanel = new FieldPanel();

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
        boolean hasFocus, int row, int column) {
      fieldPanel.updateFromField((InputRowGUI) value);
      return fieldPanel;
    }

  }

  private static class FieldEditor extends AbstractCellEditor implements TableCellEditor {

    private final FieldPanel fieldPanel = new FieldPanel();
    private InputRowGUI value;

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected,
        int row, int column) {
      this.value = (InputRowGUI) value;
      fieldPanel.updateFromField(this.value);
      return fieldPanel;
    }

    @Override
    public Object getCellEditorValue() {
      fieldPanel.updateField(value);
      return value;
    }

  }

}
