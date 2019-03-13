package com.blazemeter.jmeter.rte.sampler.gui;

import static org.assertj.swing.fixture.Containers.showInFrame;
import static org.assertj.swing.timing.Pause.pause;

import com.blazemeter.jmeter.rte.core.CoordInput;
import com.blazemeter.jmeter.rte.core.Input;
import com.blazemeter.jmeter.rte.core.LabelInput;
import com.blazemeter.jmeter.rte.core.Position;
import com.blazemeter.jmeter.rte.sampler.InputTestElement;
import com.blazemeter.jmeter.rte.sampler.InputsTestElement;
import com.blazemeter.jmeter.rte.sampler.gui.InputPanel.FieldPanel;
import java.awt.Component;
import java.awt.Point;
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
    LabelInput input = new LabelInput("User", "TESTUSR");
    setInputLabelRow(0, input);
    assertInputs(input);
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
    LabelInput userInput = new LabelInput("User", "TESTUSR");
    setInputLabelRow(0, userInput);
    clickAddLabel();
    LabelInput passInput = new LabelInput("Password", "TESTPSW");
    setInputLabelRow(1, passInput);
    assertInputs(userInput, passInput);
  }

}
