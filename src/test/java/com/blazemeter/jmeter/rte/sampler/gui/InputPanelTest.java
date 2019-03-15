package com.blazemeter.jmeter.rte.sampler.gui;

import static org.assertj.swing.fixture.Containers.showInFrame;
import static org.assertj.swing.timing.Pause.pause;
import static org.assertj.swing.timing.Timeout.timeout;
import static org.junit.Assert.assertFalse;

import com.blazemeter.jmeter.rte.core.CoordInput;
import com.blazemeter.jmeter.rte.core.Input;
import com.blazemeter.jmeter.rte.core.LabelInput;
import com.blazemeter.jmeter.rte.core.Position;
import com.blazemeter.jmeter.rte.sampler.InputTestElement;
import com.blazemeter.jmeter.rte.sampler.Inputs;
import com.blazemeter.jmeter.rte.sampler.gui.InputPanel.FieldPanel;

import java.awt.*;
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

import kg.apc.emulators.TestJMeterUtils;
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

public class InputPanelTest {

  private static final long CHANGE_TIMEOUT_MILLIS = 10000;
  private static final LabelInput USER_LABEL_INPUT = new LabelInput("User", "TESTUSR");
  private static final CoordInput PASS_COORD_INPUT = new CoordInput(new Position(1, 2), "TESTPSW");
  private FrameFixture frame;
  private InputPanel panel;
  private JTableFixture inputTable;

  @BeforeClass
  public static void setupClass() {
    TestJMeterUtils.createJmeterEnv();
  }

  @Before
  public void setup() {
    panel = new InputPanel();
    frame = showInFrame(panel);
    frame.resizeHeightTo(200);
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
    clickAddLabel();
    setInputLabelRow(0, USER_LABEL_INPUT);
    assertInputs(USER_LABEL_INPUT);
  }

  private void clickAddLabel() {
    frame.button("addLabelInputButton").click();
  }

  private void setInputLabelRow(int row, LabelInput input) {
    setInputRow(row, input, (panel, i) -> setTextField(panel, "fieldLabel", i.getLabel()));
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
    clickAddCoord();
    CoordInput input = new CoordInput(new Position(2, 3), "TESTUSR");
    setInputCoordRow(0, input);
    assertInputs(input);
  }

  private void clickAddCoord() {
    frame.button("addCoordInputButton").click();
  }

  private void setInputCoordRow(int row, CoordInput input) {
    setInputRow(row, input, (panel, i) -> {
      Position position = i.getPosition();
      setTextField(panel, "fieldRow", String.valueOf(position.getRow()));
      setTextField(panel, "fieldColumn", String.valueOf(position.getColumn()));
    });
  }

  @Test
  public void shouldGetCoordAndLabelInputsWhenClickAddCoordAndAddLabel() {
    clickAddLabel();
    setInputLabelRow(0, USER_LABEL_INPUT);
    clickAddCoord();
    CoordInput passInput = new CoordInput(new Position(1, 2), "TESTPSW");
    setInputCoordRow(1, passInput);
    clickAddCoord();
    CoordInput nameInput = new CoordInput(new Position(4, 2), "TESTNAME");
    setInputCoordRow(2, nameInput);
    clickAddLabel();
    LabelInput emailInput = new LabelInput("eMail", "test@example.com");
    setInputLabelRow(3, emailInput);
    assertInputs(USER_LABEL_INPUT, passInput, nameInput, emailInput);

  }

  @Test
  public void shouldNotMoveFirstRowWhenClickUp() {
    clickAddLabel();
    setInputLabelRow(0, USER_LABEL_INPUT);
    clickAddCoord();
    setInputCoordRow(1, PASS_COORD_INPUT);
    inputTable.selectCells(TableCell.row(0).column(0));
    clickUpButton();
    assertInputs(USER_LABEL_INPUT, PASS_COORD_INPUT);
  }

  private void clickUpButton() {
    frame.button("upButton").click();
  }

  @Test
  public void shouldMoveUpLastRowWhenClickUp() {
    clickAddLabel();
    setInputLabelRow(0, USER_LABEL_INPUT);
    clickAddCoord();
    setInputCoordRow(1, PASS_COORD_INPUT);
    inputTable.selectCells(TableCell.row(1).column(0));
    clickUpButton();
    assertInputs(PASS_COORD_INPUT, USER_LABEL_INPUT);
  }

  @Test
  public void shouldMoveUpSelectedRowsWhenClickUp() {
    clickAddLabel();
    setInputLabelRow(0, USER_LABEL_INPUT);
    clickAddCoord();
    setInputCoordRow(1, PASS_COORD_INPUT);
    clickAddCoord();
    CoordInput nameInput = new CoordInput(new Position(4, 2), "TESTNAME");
    setInputCoordRow(2, nameInput);
    clickAddLabel();
    LabelInput emailInput = new LabelInput("eMail", "test@example.com");
    setInputLabelRow(3, emailInput);
    inputTable.selectCells(TableCell.row(3).column(0), TableCell.row(1).column(0));
    clickUpButton();
    assertInputs(PASS_COORD_INPUT, USER_LABEL_INPUT, emailInput, nameInput);
  }

  @Test
  public void shouldMoveDownFirstRowWhenClickDown() {
    clickAddLabel();
    setInputLabelRow(0, USER_LABEL_INPUT);
    clickAddCoord();
    setInputCoordRow(1, PASS_COORD_INPUT);
    inputTable.selectCells(TableCell.row(0).column(0));
    clickDownButton();
    assertInputs(PASS_COORD_INPUT, USER_LABEL_INPUT);
  }

  private void clickDownButton() {
    frame.button("downButton").click();
  }

  @Test
  public void shouldNotMoveLastRowWhenClickDown() {
    clickAddLabel();
    setInputLabelRow(0, USER_LABEL_INPUT);
    clickAddCoord();
    setInputCoordRow(1, PASS_COORD_INPUT);
    inputTable.selectCells(TableCell.row(1).column(0));
    clickDownButton();
    assertInputs(USER_LABEL_INPUT, PASS_COORD_INPUT);
  }

  @Test
  public void shouldMoveDownSelectedRowsWhenClickDown() {
    clickAddLabel();
    setInputLabelRow(0, USER_LABEL_INPUT);
    clickAddCoord();
    setInputCoordRow(1, PASS_COORD_INPUT);
    clickAddCoord();
    CoordInput nameInput = new CoordInput(new Position(4, 2), "TESTNAME");
    setInputCoordRow(2, nameInput);
    clickAddLabel();
    LabelInput emailInput = new LabelInput("eMail", "test@example.com");
    setInputLabelRow(3, emailInput);
    inputTable.selectCells(TableCell.row(0).column(0), TableCell.row(2).column(0));
    clickDownButton();
    assertInputs(PASS_COORD_INPUT, USER_LABEL_INPUT, emailInput, nameInput);
  }

  @Test
  public void shouldDeleteSelectedRowsWhenClickDelete() {
    clickAddLabel();
    setInputLabelRow(0, USER_LABEL_INPUT);
    clickAddCoord();
    setInputCoordRow(1, PASS_COORD_INPUT);
    clickAddCoord();
    CoordInput nameInput = new CoordInput(new Position(4, 2), "TESTNAME");
    setInputCoordRow(2, nameInput);
    inputTable.selectCells(TableCell.row(0).column(0), TableCell.row(2).column(0));
    clickDeleteButton();
    assertInputs(PASS_COORD_INPUT);
  }

  private void clickDeleteButton() {
    frame.button("deleteButton").click();
  }

  @Test
  public void shouldNotChangeInputsWhenCopyFromEmptyClipboard() {
    clickAddLabel();
    setInputLabelRow(0,USER_LABEL_INPUT);
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
    clickAddLabel();
    setInputLabelRow(0, USER_LABEL_INPUT);
    waitButtonEnabled("deleteButton", true);
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
    clickAddLabel();
    setInputLabelRow(0, USER_LABEL_INPUT);
    clickDeleteButton();
    waitButtonEnabled("deleteButton",false);
  }

  @Test
  public void shouldDisableUpButtonWhenOneInputIsLeft() {
    clickAddLabel();
    setInputLabelRow(0, USER_LABEL_INPUT);
    clickAddCoord();
    setInputCoordRow(1, PASS_COORD_INPUT);
    clickDeleteButton();
    waitButtonEnabled("upButton",false);
  }

  @Test
  public void checkEnableUpButtonWithTwoInputs() {
    clickAddLabel();
    setInputLabelRow(0, USER_LABEL_INPUT);
    clickAddCoord();
    setInputCoordRow(1, PASS_COORD_INPUT);
    waitButtonEnabled("upButton", true);
  }

  @Test
  public void shouldEnableDownButtonWhenAddTwoInputs() {
    clickAddLabel();
    setInputLabelRow(0, USER_LABEL_INPUT);
    clickAddCoord();
    setInputCoordRow(1, PASS_COORD_INPUT);
    waitButtonEnabled("downButton",true);
  }

  @Test
  public void shouldDisableDownButtonWhenOneInputIsLeft() {
    clickAddLabel();
    setInputLabelRow(0, USER_LABEL_INPUT);
    clickAddCoord();
    setInputCoordRow(1, PASS_COORD_INPUT);
    clickDeleteButton();
    waitButtonEnabled("downButton",false);
  }

}
