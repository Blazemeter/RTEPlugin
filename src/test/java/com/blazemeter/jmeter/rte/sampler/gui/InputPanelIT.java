package com.blazemeter.jmeter.rte.sampler.gui;

import static org.assertj.swing.fixture.Containers.showInFrame;
import static org.assertj.swing.timing.Pause.pause;
import static org.assertj.swing.timing.Timeout.timeout;

import com.blazemeter.jmeter.rte.JMeterTestUtils;
import com.blazemeter.jmeter.rte.core.CoordInput;
import com.blazemeter.jmeter.rte.core.Input;
import com.blazemeter.jmeter.rte.core.LabelInput;
import com.blazemeter.jmeter.rte.core.Position;
import com.blazemeter.jmeter.rte.sampler.InputTestElement;
import com.blazemeter.jmeter.rte.sampler.Inputs;
import com.blazemeter.jmeter.rte.sampler.gui.InputPanel.FieldPanel;
import java.awt.Component;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import javax.swing.JTable;
import javax.swing.text.JTextComponent;
import org.assertj.swing.core.Robot;
import org.assertj.swing.data.TableCell;
import org.assertj.swing.driver.AbstractJTableCellWriter;
import org.assertj.swing.driver.JTableTextComponentEditorCellWriter;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.fixture.JPanelFixture;
import org.assertj.swing.fixture.JTableCellFixture;
import org.assertj.swing.fixture.JTableFixture;
import org.assertj.swing.fixture.JTextComponentFixture;
import org.assertj.swing.timing.Condition;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class InputPanelIT {

  private static final long CHANGE_TIMEOUT_MILLIS = 10000;
  private static final LabelInput USER_LABEL_INPUT = new LabelInput("User", "TESTUSR");
  private static final CoordInput PASS_COORD_INPUT = new CoordInput(new Position(1, 2), "TESTPSW");
  private static final CoordInput NAME_INPUT = new CoordInput(new Position(4, 2), "TESTNAME");
  private static final LabelInput EMAIL_INPUT = new LabelInput("eMail", "test@example.com");
  private static final String DELETE_BUTTON = "deleteButton";
  private static final String UP_BUTTON = "upButton";
  private static final String DOWN_BUTTON = "downButton";

  private FrameFixture frame;
  private InputPanel panel;
  private JTableFixture inputTable;

  @BeforeClass
  public static void setupClass() {
    JMeterTestUtils.setupJmeterEnv();
  }

  @Before
  public void setup() {
    panel = new InputPanel();
    frame = showInFrame(panel);
    inputTable = frame.table("table");
    inputTable.replaceCellWriter(new FieldWriter(frame.robot()));
  }

  private static class FieldWriter extends AbstractJTableCellWriter {

    private final JTableTextComponentEditorCellWriter textComponentWriter;

    private FieldWriter(Robot robot) {
      super(robot);
      textComponentWriter = new JTableTextComponentEditorCellWriter(robot);
    }

    @Override
    public void startCellEditing(JTable table, int row, int column) {
      Component editor = editorForCell(table, row, column);
      if (editor instanceof JTextComponent) {
        textComponentWriter.startCellEditing(table, row, column);
      } else {
        Point cellLocation = cellLocation(table, row, column, location());
        robot.click(table, cellLocation);
        waitForEditorActivation(table, row, column, FieldPanel.class);
      }
    }

    @Override
    public void enterValue(JTable table, int row, int column, String value) {
    }

  }

  @After
  public void tearDown() {
    frame.cleanUp();
  }

  @Test
  public void shouldAddLabelInputWhenClickAddLabel() {
    addFieldByLabel(0, USER_LABEL_INPUT);
    assertInputs(USER_LABEL_INPUT);
  }

  private void addFieldByLabel(int row, LabelInput input) {
    frame.button("addLabelInputButton").click();
    awaitAddedRow(row);
    setInputRow(row, input, (panel, i) -> setTextField(panel, "fieldLabel", i.getLabel()));
  }

  private void awaitAddedRow(int row) {
    pause(new Condition("row " + row + " added") {

      @Override
      public boolean test() {
        return inputTable.rowCount() > row;
      }

    }, CHANGE_TIMEOUT_MILLIS);
  }

  private <T extends Input> void setInputRow(int row, T input,
      BiConsumer<JPanelFixture, T> fieldSetter) {
    JTableCellFixture fieldCell = inputTable
        .cell(TableCell.row(row).column(0));
    fieldCell.startEditing();
    JPanelFixture fieldPanel = new JPanelFixture(frame.robot(), (FieldPanel) fieldCell.editor());
    fieldSetter.accept(fieldPanel, input);
    fieldCell.stopEditing();
    JTableCellFixture inputCell = inputTable.cell(TableCell.row(row).column(1));
    // Using this approach instead of inputCell.enterText() because is way faster
    inputCell.startEditing();
    ((JTextComponent) inputCell.editor()).setText(input.getInput());
    inputCell.stopEditing();
  }

  private void setTextField(JPanelFixture fieldPanel, String name, String text) {
    JTextComponentFixture textField = fieldPanel.textBox(name);
    frame.robot().click(textField.target());
    textField.setText(text);
  }

  private void assertInputs(Input... inputs) {
    List<Input> inputsList = Arrays.asList(inputs);
    pause(new Condition("inputs to be " + buildInputsString(inputsList)) {

      private List<Input> lastInputs;

      @Override
      public boolean test() {
        lastInputs = getInputs();
        return lastInputs.equals(inputsList);
      }

      @Override
      protected String descriptionAddendum() {
        return " but last seen inputs were " + buildInputsString(lastInputs);
      }

      private List<Input> getInputs() {
        Inputs testElem = (Inputs) panel.createTestElement();
        return StreamSupport.stream(testElem.getInputs().spliterator(), false)
            .map(p -> ((InputTestElement) p.getObjectValue()).toInput())
            .collect(Collectors.toList());
      }
    }, CHANGE_TIMEOUT_MILLIS);
  }

  private String buildInputsString(List<Input> inputsList) {
    return inputsList.stream()
        .map(i -> "\"" + i + "\"")
        .collect(Collectors.joining(",", "[", "]"));
  }

  @Test
  public void shouldAddCoordInputWhenClickAddCoord() {
    addFieldByCoord(0, PASS_COORD_INPUT);
    assertInputs(PASS_COORD_INPUT);
  }

  private void addFieldByCoord(int row, CoordInput input) {
    frame.button("addCoordInputButton").click();
    awaitAddedRow(row);
    setInputRow(row, input, (panel, i) -> {
      Position position = i.getPosition();
      setTextField(panel, "fieldRow", String.valueOf(position.getRow()));
      setTextField(panel, "fieldColumn", String.valueOf(position.getColumn()));
    });
  }

  @Test
  public void shouldGetCoordAndLabelInputsWhenClickAddCoordAndAddLabel() {
    int row = 0;
    addFieldByLabel(row++, USER_LABEL_INPUT);
    addFieldByCoord(row++, PASS_COORD_INPUT);
    addFieldByCoord(row++, NAME_INPUT);
    addFieldByLabel(row, EMAIL_INPUT);
    assertInputs(USER_LABEL_INPUT, PASS_COORD_INPUT, NAME_INPUT, EMAIL_INPUT);

  }

  @Test
  public void shouldNotMoveFirstRowWhenClickUp() {
    int row = 0;
    addFieldByLabel(row++, USER_LABEL_INPUT);
    addFieldByCoord(row, PASS_COORD_INPUT);
    inputTable.selectRows(0);
    clickUpButton();
    assertInputs(USER_LABEL_INPUT, PASS_COORD_INPUT);
  }

  private void clickUpButton() {
    frame.button(UP_BUTTON).click();
  }

  @Test
  public void shouldMoveUpLastRowWhenClickUp() {
    int row = 0;
    addFieldByLabel(row++, USER_LABEL_INPUT);
    addFieldByCoord(row, PASS_COORD_INPUT);
    inputTable.selectRows(row);
    clickUpButton();
    assertInputs(PASS_COORD_INPUT, USER_LABEL_INPUT);
  }

  @Test
  public void shouldMoveUpSelectedRowsWhenClickUp() {
    int row = 0;
    addFieldByLabel(row++, USER_LABEL_INPUT);
    addFieldByCoord(row++, PASS_COORD_INPUT);
    addFieldByCoord(row++, NAME_INPUT);
    addFieldByLabel(row, EMAIL_INPUT);
    inputTable.selectRows(3, 1);
    clickUpButton();
    assertInputs(PASS_COORD_INPUT, USER_LABEL_INPUT, EMAIL_INPUT, NAME_INPUT);
  }

  @Test
  public void shouldMoveDownFirstRowWhenClickDown() {
    int row = 0;
    addFieldByLabel(row++, USER_LABEL_INPUT);
    addFieldByCoord(row, PASS_COORD_INPUT);
    inputTable.selectRows(0);
    clickDownButton();
    assertInputs(PASS_COORD_INPUT, USER_LABEL_INPUT);
  }

  private void clickDownButton() {
    frame.button(DOWN_BUTTON).click();
  }

  @Test
  public void shouldNotMoveLastRowWhenClickDown() {
    int row = 0;
    addFieldByLabel(row++, USER_LABEL_INPUT);
    addFieldByCoord(row, PASS_COORD_INPUT);
    inputTable.selectRows(row);
    clickDownButton();
    assertInputs(USER_LABEL_INPUT, PASS_COORD_INPUT);
  }

  @Test
  public void shouldMoveDownSelectedRowsWhenClickDown() {
    int row = 0;
    addFieldByLabel(row++, USER_LABEL_INPUT);
    addFieldByCoord(row++, PASS_COORD_INPUT);
    addFieldByCoord(row++, NAME_INPUT);
    addFieldByLabel(row, EMAIL_INPUT);
    inputTable.selectRows(0, 2);
    clickDownButton();
    assertInputs(PASS_COORD_INPUT, USER_LABEL_INPUT, EMAIL_INPUT, NAME_INPUT);
  }

  @Test
  public void shouldDeleteSelectedRowsWhenClickDelete() {
    int row = 0;
    addFieldByLabel(row++, USER_LABEL_INPUT);
    addFieldByCoord(row++, PASS_COORD_INPUT);
    addFieldByCoord(row, NAME_INPUT);
    inputTable.selectRows(0, 2);
    clickDeleteButton();
    assertInputs(PASS_COORD_INPUT);
  }

  private void clickDeleteButton() {
    frame.button(DELETE_BUTTON).click();
  }

  @Test
  public void shouldNotChangeInputsWhenCopyFromEmptyClipboard() {
    addFieldByLabel(0, USER_LABEL_INPUT);
    setTextToClipboard(null);
    clickAddFromClipboard();
    assertInputs(USER_LABEL_INPUT);
  }

  private void setTextToClipboard(String s) {
    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
    Transferable transferable = new StringSelection(s);
    clipboard.setContents(transferable, null);
  }

  private void clickAddFromClipboard() {
    frame.button("addFromClipboardButton").click();
  }

  @Test
  public void shouldAddInputsWhenCopyFromClipboard() {
    setTextToClipboard("TEST\nUser\tTESTUSR\n1\t2\tTESTPSW\n1\t4\tTESTNAME\tNAME");
    clickAddFromClipboard();
    CoordInput test = new CoordInput(new Position(1, 1), "TEST");
    CoordInput name = new CoordInput(new Position(1, 4), "TESTNAME");
    assertInputs(test, USER_LABEL_INPUT, PASS_COORD_INPUT, name);
  }

  @Test
  public void shouldEnableDeleteButtonWhenAddOneInput() {
    addFieldByLabel(0, USER_LABEL_INPUT);
    waitButtonEnabled(DELETE_BUTTON, true);
  }

  private void waitButtonEnabled(String buttonName, boolean enable) {
    pause(new Condition("button " + buttonName + " to be " + (enable ? "enabled" : "disabled")) {
      @Override
      public boolean test() {
        return frame.button(buttonName).isEnabled() == enable;
      }
    }, timeout(CHANGE_TIMEOUT_MILLIS));
  }

  @Test
  public void shouldDisableDeleteButtonWhenDeleteUniqueInput() {
    addFieldByLabel(0, USER_LABEL_INPUT);
    clickDeleteButton();
    waitButtonEnabled(DELETE_BUTTON, false);
  }

  @Test
  public void shouldDisableUpButtonWhenOneInputIsLeft() {
    int row = 0;
    addFieldByLabel(row++, USER_LABEL_INPUT);
    addFieldByCoord(row, PASS_COORD_INPUT);
    clickDeleteButton();
    waitButtonEnabled(UP_BUTTON, false);
  }

  @Test
  public void checkEnableUpButtonWithTwoInputs() {
    int row = 0;
    addFieldByLabel(row++, USER_LABEL_INPUT);
    addFieldByCoord(row, PASS_COORD_INPUT);
    waitButtonEnabled(UP_BUTTON, true);
  }

  @Test
  public void shouldEnableDownButtonWhenAddTwoInputs() {
    int row = 0;
    addFieldByLabel(row++, USER_LABEL_INPUT);
    addFieldByCoord(row, PASS_COORD_INPUT);
    waitButtonEnabled(DOWN_BUTTON, true);
  }

  @Test
  public void shouldDisableDownButtonWhenOneInputIsLeft() {
    int row = 0;
    addFieldByLabel(row++, USER_LABEL_INPUT);
    addFieldByCoord(row, PASS_COORD_INPUT);
    clickDeleteButton();
    waitButtonEnabled(DOWN_BUTTON, false);
  }
  
}
