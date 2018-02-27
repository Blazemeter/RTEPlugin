package com.blazemeter.jmeter.rte.sampler.gui;

import com.blazemeter.jmeter.rte.sampler.CoordInputRowGUI;
import com.blazemeter.jmeter.rte.sampler.Inputs;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Rectangle;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Iterator;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.ListSelectionModel;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.gui.util.HeaderAsPropertyRenderer;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.gui.GuiUtils;
import org.apache.jorphan.gui.ObjectTableModel;
import org.apache.jorphan.reflect.Functor;

public class CoordInputPanel extends JPanel implements ActionListener {

  private static final long serialVersionUID = -6184904133375045201L;
  private static final String ADD = "add";
  private static final String ADD_FROM_CLIPBOARD = "addFromClipboard";
  private static final String DELETE = "delete";
  private static final String UP = "up";
  private static final String DOWN = "down";
  private static final String CLIPBOARD_LINE_DELIMITERS = "\n";
  private static final String CLIPBOARD_ARG_DELIMITERS = "\t";

  private JLabel tableLabel;
  private transient ObjectTableModel tableModel;
  private transient JTable table;
  private JButton add;
  private JButton delete;
  private JButton up;
  private JButton down;
  private JButton addFromClipboard;

  public CoordInputPanel() {
    this("Payload");
  }

  public CoordInputPanel(String label) {
    this.tableLabel = new JLabel(label);
    init();
  }

  private void init() {
    JPanel p = this;

    p.setLayout(new BorderLayout());

    p.add(makeLabelPanel(), BorderLayout.NORTH);
    p.add(makeMainPanel(), BorderLayout.CENTER);

    p.add(Box.createVerticalStrut(70), BorderLayout.WEST);
    p.add(makeButtonPanel(), BorderLayout.SOUTH);

    table.revalidate();
  }

  private Component makeMainPanel() {
    initializeTableModel();
    table = new JTable(tableModel);
    table.getTableHeader().setDefaultRenderer(new HeaderAsPropertyRenderer());
    table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    JMeterUtils.applyHiDPI(table);
    JScrollPane pane = new JScrollPane(table);
    pane.setPreferredSize(pane.getMinimumSize());
    return pane;
  }

  private void initializeTableModel() {
    if (tableModel == null) {
      tableModel = new ObjectTableModel(new String[]{"Value", "Column", "Row"},
          CoordInputRowGUI.class,
          new Functor[]{new Functor("getInput"), new Functor("getColumn"), new Functor("getRow")},
          new Functor[]{new Functor("setInput"), new Functor("setColumn"), new Functor("setRow")},
          new Class[]{String.class, String.class, String.class});
    }
  }

  private Component makeLabelPanel() {
    JPanel labelPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
    labelPanel.add(tableLabel);
    return labelPanel;
  }

  private JPanel makeButtonPanel() {

    add = new JButton(JMeterUtils.getResString("add"));
    add.setActionCommand(ADD);
    add.setEnabled(true);

    addFromClipboard = new JButton(JMeterUtils.getResString("add_from_clipboard"));
    addFromClipboard.setActionCommand("addFromClipboard");
    addFromClipboard.setEnabled(true);

    delete = new JButton(JMeterUtils.getResString("delete"));
    delete.setActionCommand(DELETE);

    up = new JButton(JMeterUtils.getResString("up"));
    up.setActionCommand(UP);

    down = new JButton(JMeterUtils.getResString("down"));
    down.setActionCommand(DOWN);

    checkButtonsStatus();

    JPanel buttonPanel = new JPanel();
    buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));

    add.addActionListener(this);
    addFromClipboard.addActionListener(this);
    delete.addActionListener(this);
    up.addActionListener(this);
    down.addActionListener(this);
    buttonPanel.add(add);
    buttonPanel.add(addFromClipboard);
    buttonPanel.add(delete);
    buttonPanel.add(up);
    buttonPanel.add(down);
    return buttonPanel;
  }

  protected void checkButtonsStatus() {
    if (tableModel.getRowCount() == 0) {
      delete.setEnabled(false);
    } else {
      delete.setEnabled(true);
    }
    if (tableModel.getRowCount() > 1) {
      up.setEnabled(true);
      down.setEnabled(true);
    } else {
      up.setEnabled(false);
      down.setEnabled(false);
    }
  }

  public TestElement createTestElement() {
    Inputs inputs = new Inputs();
    modifyTestElement(inputs);
    return inputs;
  }

  public void modifyTestElement(TestElement element) {
    GuiUtils.stopTableEditing(table);
    if (element instanceof Inputs) {
      Inputs inputs = (Inputs) element;
      inputs.clear();
      @SuppressWarnings("unchecked")
      Iterator<CoordInputRowGUI> modelData = (Iterator<CoordInputRowGUI>) tableModel.iterator();
      while (modelData.hasNext()) {
        CoordInputRowGUI input = modelData.next();
        if (StringUtils.isEmpty(input.getInput()) && StringUtils.isEmpty(input.getColumn())
            && StringUtils.isEmpty(input.getRow())) {
          continue;
        }
        inputs.addCoordInput(input);
      }
    }
  }

  public void configure(TestElement el) {
    if (el instanceof Inputs) {
      tableModel.clearData();
      for (JMeterProperty jMeterProperty : (Inputs) el) {
        CoordInputRowGUI input = (CoordInputRowGUI) jMeterProperty.getObjectValue();
        tableModel.addRow(input);
      }
    }
    checkButtonsStatus();
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    String action = e.getActionCommand();
    switch (action) {
      case DELETE:
        deleteArgument();
        break;
      case ADD:
        addArgument();
        break;
      case ADD_FROM_CLIPBOARD:
        addFromClipboard();
        break;
      case UP:
        moveUp();
        break;
      case DOWN:
        moveDown();
        break;
      default:
        throw new UnsupportedOperationException(action);
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
        tableModel.moveRow(rowSelected, rowSelected + 1, rowSelected + 1);
      }
      for (int rowSelected : rowsSelected) {
        table.addRowSelectionInterval(rowSelected + 1, rowSelected + 1);
      }

      scrollToRowIfNotVisible(rowsSelected[0] + 1);
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

  private static int getNumberOfVisibleRows(JTable table) {
    Rectangle vr = table.getVisibleRect();
    int first = table.rowAtPoint(vr.getLocation());
    vr.translate(0, vr.height);
    return table.rowAtPoint(vr.getLocation()) - first;
  }

  private void moveUp() {
    // get the selected rows before stopping editing
    // or the selected rows will be unselected
    int[] rowsSelected = table.getSelectedRows();
    GuiUtils.stopTableEditing(table);

    if (rowsSelected.length > 0 && rowsSelected[0] > 0) {
      table.clearSelection();
      for (int rowSelected : rowsSelected) {
        tableModel.moveRow(rowSelected, rowSelected + 1, rowSelected - 1);
      }

      for (int rowSelected : rowsSelected) {
        table.addRowSelectionInterval(rowSelected - 1, rowSelected - 1);
      }

      scrollToRowIfNotVisible(rowsSelected[0] - 1);
    }
  }

  private void deleteArgument() {
    GuiUtils.cancelEditing(table);

    int[] rowsSelected = table.getSelectedRows();
    int anchorSelection = table.getSelectionModel().getAnchorSelectionIndex();
    table.clearSelection();
    if (rowsSelected.length > 0) {
      for (int i = rowsSelected.length - 1; i >= 0; i--) {
        tableModel.removeRow(rowsSelected[i]);
      }

      // Table still contains one or more rows, so highlight (select)
      // the appropriate one.
      if (tableModel.getRowCount() > 0) {
        if (anchorSelection >= tableModel.getRowCount()) {
          anchorSelection = tableModel.getRowCount() - 1;
        }
        table.setRowSelectionInterval(anchorSelection, anchorSelection);
      }

      checkButtonsStatus();
    }
  }

  private void addArgument() {
    // If a table cell is being edited, we should accept the current value
    // and stop the editing before adding a new row.
    GuiUtils.stopTableEditing(table);

    tableModel.addRow(makeNewArgument());

    checkButtonsStatus();

    // Highlight (select) and scroll to the appropriate row.
    int rowToSelect = tableModel.getRowCount() - 1;
    table.setRowSelectionInterval(rowToSelect, rowToSelect);
    table.scrollRectToVisible(table.getCellRect(rowToSelect, 0, true));
  }

  private void addFromClipboard(String lineDelimiter, String argDelimiter) {
    GuiUtils.stopTableEditing(table);
    int rowCount = table.getRowCount();
    try {
      String clipboardContent = GuiUtils.getPastedText();
      if (clipboardContent == null) {
        return;
      }
      String[] clipboardLines = clipboardContent.split(lineDelimiter);
      for (String clipboardLine : clipboardLines) {
        String[] clipboardCols = clipboardLine.split(argDelimiter);
        if (clipboardCols.length > 0) {
          CoordInputRowGUI input = createArgumentFromClipboard(clipboardCols);
          tableModel.addRow(input);
        }
      }
      if (table.getRowCount() > rowCount) {
        checkButtonsStatus();

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

  protected void addFromClipboard() {
    addFromClipboard(CLIPBOARD_LINE_DELIMITERS, CLIPBOARD_ARG_DELIMITERS);
  }

  protected CoordInputRowGUI createArgumentFromClipboard(String[] clipboardCols) {
    CoordInputRowGUI argument = makeNewArgument();
    argument.setInput(clipboardCols[0]);
    if (clipboardCols.length > 1) {
      argument.setColumn(clipboardCols[1]);
      if (clipboardCols.length > 2) {
        argument.setRow(clipboardCols[2]);
      }
    }
    return argument;
  }

  private CoordInputRowGUI makeNewArgument() {
    return new CoordInputRowGUI();
  }

  public void clear() {
    GuiUtils.stopTableEditing(table);
    tableModel.clearData();
  }
}