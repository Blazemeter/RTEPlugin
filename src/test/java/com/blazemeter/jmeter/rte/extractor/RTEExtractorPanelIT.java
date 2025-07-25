package com.blazemeter.jmeter.rte.extractor;

import static org.assertj.swing.fixture.Containers.showInFrame;

import com.blazemeter.jmeter.rte.SwingTestRunner;
import org.assertj.core.api.JUnitSoftAssertions;
import org.assertj.swing.fixture.FrameFixture;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(SwingTestRunner.class)
public class RTEExtractorPanelIT {

  public static final String NEXT_FIELD_POSITION = "nextFieldPosition";
  public static final String CURSOR_POSITION = "cursorPosition";
  @Rule
  public JUnitSoftAssertions softly = new JUnitSoftAssertions();
  private FrameFixture frame;
  private RTEExtractorPanel panel;

  @Before
  public void setup() {
    panel = new RTEExtractorPanel();
    frame = showInFrame(panel);
  }

  @After
  public void tearDown() {
    frame.cleanUp();
  }

  @Test
  public void shouldSelectOneRadioButtonAndDeselectTheOtherOne() {
    clickRadioButton(NEXT_FIELD_POSITION);
    frame.radioButton(CURSOR_POSITION).requireNotSelected();
  }

  private void clickRadioButton(String name) {
    frame.radioButton(name).click();
  }

  @Test
  public void shouldDisableNextFieldPanelWhenCursorPosition() {
    clickRadioButton(CURSOR_POSITION);
    frame.panel("fieldPanel").requireDisabled();
  }

  @Test
  public void shouldGetConfiguredPropertiesWhenFieldsAreSet() {
    clickRadioButton(NEXT_FIELD_POSITION);
    setProperties();
    softly.assertThat(panel.getVariablePrefix()).as("Variable Prefix Name").isEqualTo("position");
    softly.assertThat(panel.getRow()).as("Field Row").isEqualTo("1");
    softly.assertThat(panel.getColumn()).as("Field Column").isEqualTo("2");
    softly.assertThat(panel.getOffset()).as("Field Position Offset").isEqualTo("1");
  }

  private void setProperties() {
    setTextField("variablePrefix", "position");
    setTextField("fieldRow", "1");
    setTextField("fieldColumn", "2");
    setTextField("fieldOffset", "1");
  }

  private void setTextField(String fieldName, String fieldValue) {
    frame.textBox(fieldName).setText(fieldValue);
  }


}
