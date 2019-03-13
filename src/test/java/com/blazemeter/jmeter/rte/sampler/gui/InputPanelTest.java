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
import com.blazemeter.jmeter.rte.sampler.InputsTestElement;
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
  private static final LabelInput USER_LABEL_INPUT = new LabelInput("User","TESTUSR");
  private static final CoordInput PASS_COORD_INPUT = new CoordInput(new Position(1,2),"TESTPSW");
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
        InputsTestElement testElem = (InputsTestElement) panel.createTestElement();
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
  public void shouldAddMultipleLabelInputsWhenMultipleClickAddLabel() {
    clickAddLabel();
    setInputLabelRow(0, USER_LABEL_INPUT);
    clickAddLabel();
    LabelInput passInput = new LabelInput("Password", "TESTPSW");
    setInputLabelRow(1, passInput);
    assertInputs(USER_LABEL_INPUT, passInput);
  }

  @Test
  public void shouldAddMixedInputs(){
    clickAddLabel();
    setInputLabelRow(0, USER_LABEL_INPUT);
    clickAddCoord();
    CoordInput passInput = new CoordInput(new Position(1,2),"TESTPSW");
    setInputCoordRow(1,passInput);
    clickAddCoord();
    CoordInput nameInput = new CoordInput(new Position(4,2),"TESTNAME");
    setInputCoordRow(2,nameInput);
    clickAddLabel();
    LabelInput emailInput = new LabelInput("eMail","test@example.com");
    setInputLabelRow(3,emailInput);
    assertInputs(USER_LABEL_INPUT, passInput, nameInput, emailInput);

  }
  
  @Test
  public void shouldMoveUpFirstRow(){
    clickAddLabel();
    LabelInput userInput = new LabelInput("User","TESTUSR");
    setInputLabelRow(0,userInput);
    clickAddCoord();
    CoordInput passInput = new CoordInput(new Position(1,2),"TESTPSW");
    setInputCoordRow(1,passInput);
    JTableCellFixture getCell = inputTable
        .cell(TableCell.row(0).column(0));
    getCell.select();
    frame.button("upButton").click();
    assertInputs(userInput, passInput);
  }
  
  @Test
  public void shouldMoveUpLastRow(){
    clickAddLabel();
    LabelInput userInput = new LabelInput("User","TESTUSR");
    setInputLabelRow(0,userInput);
    clickAddCoord();
    setInputCoordRow(1, PASS_COORD_INPUT);
    JTableCellFixture getCell = inputTable
        .cell(TableCell.row(1).column(0));
    getCell.select();
    frame.button("upButton").click();
    assertInputs(PASS_COORD_INPUT, userInput);
  }
  
  @Test
  public void shouldMoveDownMultipleIntercalatedRows(){
    clickAddLabel();
    setInputLabelRow(0, USER_LABEL_INPUT);
    clickAddCoord();
    setInputCoordRow(1, PASS_COORD_INPUT);
    clickAddCoord();
    CoordInput nameInput = new CoordInput(new Position(4,2),"TESTNAME");
    setInputCoordRow(2,nameInput);
    clickAddLabel();
    LabelInput emailInput = new LabelInput("eMail","test@example.com");
    setInputLabelRow(3,emailInput);
    inputTable.selectCells(TableCell.row(0).column(0), TableCell.row(2).column(0));
    frame.button("downButton").click();
    assertInputs(PASS_COORD_INPUT, USER_LABEL_INPUT,emailInput,nameInput);
  }
  
  @Test
  public void shouldMoveUpTwice(){
    clickAddLabel();
    setInputLabelRow(0, USER_LABEL_INPUT);
    clickAddCoord();
    setInputCoordRow(1, PASS_COORD_INPUT);
    clickAddCoord();
    CoordInput nameInput = new CoordInput(new Position(4,2),"TESTNAME");
    setInputCoordRow(2,nameInput);
    inputTable.selectCells(TableCell.row(2).column(0));
    frame.button("upButton").click();
    frame.button("upButton").click();
    assertInputs(nameInput, USER_LABEL_INPUT, PASS_COORD_INPUT);
  }
  @Test
  public void shouldMoveDownFirstRow(){
    clickAddLabel();
    setInputLabelRow(0, USER_LABEL_INPUT);
    clickAddCoord();
    setInputCoordRow(1, PASS_COORD_INPUT);
    inputTable.selectCells(TableCell.row(0).column(0));
    frame.button("downButton").click();
    assertInputs(PASS_COORD_INPUT, USER_LABEL_INPUT);
  }
  @Test
  public void shouldMoveDownLastRow(){
    clickAddLabel();
    setInputLabelRow(0, USER_LABEL_INPUT);
    clickAddCoord();
    setInputCoordRow(1, PASS_COORD_INPUT);
    inputTable.selectCells(TableCell.row(1).column(0));
    frame.button("downButton").click();
    assertInputs(USER_LABEL_INPUT, PASS_COORD_INPUT);
  }
  @Test
  public void shouldMoveDownTwice(){
    clickAddLabel();
    setInputLabelRow(0, USER_LABEL_INPUT);
    clickAddCoord();
    setInputCoordRow(1, PASS_COORD_INPUT);
    clickAddCoord();
    CoordInput nameInput = new CoordInput(new Position(4,2),"TESTNAME");
    setInputCoordRow(2,nameInput);
    inputTable.selectCells(TableCell.row(0).column(0));
    frame.button("downButton").click();
    frame.button("downButton").click();
    assertInputs(PASS_COORD_INPUT, nameInput, USER_LABEL_INPUT);
  }
  @Test
  public void shouldDeleteOneRow(){
    clickAddLabel();
    setInputLabelRow(0, USER_LABEL_INPUT);
    inputTable.selectCell(TableCell.row(0).column(0));
    frame.button("deleteButton").click();
    assertInputs();
  }
  
  @Test
  public void shouldDeleteFewRow(){
    clickAddLabel();
    setInputLabelRow(0, USER_LABEL_INPUT);
    clickAddCoord();
    setInputCoordRow(1, PASS_COORD_INPUT);
    inputTable.selectCells(TableCell.row(0).column(0), TableCell.row(1).column(0));
    frame.button("deleteButton").click();
    assertInputs();
  }
  
  @Test
  public void shouldDeleteIntercalatedRows(){
    clickAddLabel();
    setInputLabelRow(0, USER_LABEL_INPUT);
    clickAddCoord();
    setInputCoordRow(1, PASS_COORD_INPUT);
    clickAddCoord();
    CoordInput nameInput = new CoordInput(new Position(4,2),"TESTNAME");
    setInputCoordRow(2,nameInput);
    inputTable.selectCells(TableCell.row(0).column(0), TableCell.row(2).column(0));
    frame.button("deleteButton").click();
    assertInputs(PASS_COORD_INPUT);
  }
  @Test
  public void shouldCopyFromEmptyClipboard(){
  setTextToClipboard("");
  clickAddFromClipboard();
  assertInputs();
  }
  private void setTextToClipboard(String s) {
    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
    Transferable transferable = new StringSelection(s);
    clipboard.setContents(transferable, null);
  }
  
  private void clickAddFromClipboard(){
    frame.button("addFromClipboardButton").click();
  }
  
  @Test
  public void shouldCopyWithCordInClipboard(){
    setTextToClipboard("1\t2\tTESTUSR");
    clickAddFromClipboard();
    CoordInput testInput = new CoordInput(new Position(1,2),"TESTUSR");
    assertInputs(testInput);
  }
  
  @Test
  public void shouldCopyWithLabelInClipboard(){
  setTextToClipboard("USERID\ttestusr");
  clickAddFromClipboard();
  LabelInput userInput = new LabelInput("USERID","testusr");
  assertInputs(userInput);

  }
  @Test
  public void shouldCopyWithMixedClipboard(){
  setTextToClipboard("User\tTESTUSR\n1\t2\tTESTPSW");
  clickAddFromClipboard();
  assertInputs(USER_LABEL_INPUT, PASS_COORD_INPUT);
  }
  @Test
  public void shouldCopyWithOneColumnInClipboard(){
  setTextToClipboard("USERID");
  clickAddFromClipboard();
  CoordInput userInput = new CoordInput(new Position(1,1),"USERID");
  assertInputs(userInput);
  }
  @Test
  public void shouldCopyWithMoreThanThreeColumns(){
  setTextToClipboard("1\t2\tUSERID\t5\t4");
  clickAddFromClipboard();
  CoordInput userInput = new CoordInput(new Position(1,2),"USERID");
  assertInputs(userInput);
  }
  @Test
  public void shouldCopyWithParameterRefInPos(){

  }
  
  @Test
  public void checkDisableButtonsOnEmptyLayout() {
    assertFalse(frame.button("deleteButton").isEnabled());
    assertFalse(frame.button("upButton").isEnabled());
    assertFalse(frame.button("downButton").isEnabled());
  }
    
  @Test
  public void checkEnableDeleteButtonOnOneInput(){
    clickAddLabel();
    setInputLabelRow(0, USER_LABEL_INPUT);
   boolean enable=false;
   pause(new Condition("Button is delete " + (enable ? "enabled" : "disabled")) {
     @Override
     public boolean test() {
       if (frame.button("deleteButton").isEnabled()){
         return true;
       }
       return false;
     }
     
   },timeout(CHANGE_TIMEOUT_MILLIS) );
  }
  @Test
  public void checkDisableDeleteButtonWithoutInput(){
assertFalse(frame.button("deleteButton").isEnabled());
  }
  @Test
  public void checkDisableUpButtonWithOneInput(){
    clickAddLabel();
    setInputLabelRow(0, USER_LABEL_INPUT);
    boolean enable=false;
    pause(new Condition("Button up is " + (enable ? "enabled" : "disabled")) {
      @Override
      public boolean test() {
        if (!frame.button("upButton").isEnabled()){
          return true;
        }
        return false;
      }


    },timeout(CHANGE_TIMEOUT_MILLIS) );
  }
  @Test
  public void checkEnableUpButtonWithTwoInputs(){
    clickAddLabel();
    setInputLabelRow(0, USER_LABEL_INPUT);
    clickAddCoord();
    setInputCoordRow(1, PASS_COORD_INPUT);
    boolean enable=false;
    pause(new Condition("Button up is " + (enable ? "enabled" : "disabled")) {
      @Override
      public boolean test() {
        if (frame.button("upButton").isEnabled()){
          return true;
        }
        return false;
      }


    },timeout(CHANGE_TIMEOUT_MILLIS) );
  }
  @Test
  public void checkDisableDownButtonWithOneInput(){
    clickAddLabel();
    setInputLabelRow(0, USER_LABEL_INPUT);
    boolean enable=false;
    pause(new Condition("Button down is " + (enable ? "enabled" : "disabled")) {
      @Override
      public boolean test() {
        if (!frame.button("downButton").isEnabled()){
          return true;
        }
        return false;
      }


    },timeout(CHANGE_TIMEOUT_MILLIS) );
  }
  @Test
  public void checkEnableDownButtonWithTwoInputs(){
    clickAddLabel();
    setInputLabelRow(0, USER_LABEL_INPUT);
    clickAddCoord();
    setInputCoordRow(1, PASS_COORD_INPUT);
    boolean enable=false;
    pause(new Condition("Button down is " + (enable ? "enabled" : "disabled")) {
      @Override
      public boolean test() {
        if (frame.button("downButton").isEnabled()){
          return true;
        }
        return false;
      }


    },timeout(CHANGE_TIMEOUT_MILLIS) );
  }
  
}
