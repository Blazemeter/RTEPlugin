package com.blazemeter.jmeter.rte.extractor;

import static org.assertj.core.api.Assertions.extractProperty;
import static org.assertj.swing.assertions.Assertions.assertThat;

import com.blazemeter.jmeter.rte.core.CoordInput;
import com.blazemeter.jmeter.rte.core.Input;
import com.blazemeter.jmeter.rte.core.Position;
import com.blazemeter.jmeter.rte.core.Protocol;
import com.blazemeter.jmeter.rte.core.RteSampleResultBuilder;
import com.blazemeter.jmeter.rte.core.Screen;
import com.blazemeter.jmeter.rte.core.TerminalType;
import com.blazemeter.jmeter.rte.core.exceptions.InvalidFieldPositionException;
import com.blazemeter.jmeter.rte.core.ssl.SSLType;
import com.blazemeter.jmeter.rte.recorder.emulator.Xtn5250TerminalEmulatorIT;
import com.blazemeter.jmeter.rte.sampler.Action;
import java.awt.Dimension;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Collections;
import java.util.List;
import kg.apc.emulators.TestJMeterUtils;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class RTEExtractorTest {

  public static final String POSITION_VAR_ROW = "positionVar_ROW";
  public static final String POSITION_VAR_COLUMN = "positionVar_COLUMN";
  public static final String VARIABLE_PREFIX = "positionVar";
  public static final String USER_MESSAGE_ERROR =
      "Inserted values for row and column in extractor\n"
          + "do not match with the screen size.\n";
  private static Position CURSOR_POSITION = new Position(10, 2);
  @Rule
  public JUnitSoftAssertions softly = new JUnitSoftAssertions();
  private JMeterVariables vars;
  private JMeterContext context;
  private RTEExtractor rteExtractor;
  private Screen screen;
  private ByteArrayOutputStream outputStream;

  @BeforeClass
  public static void setupClass() {
  }

  @Before
  public void setup() {
    vars = new JMeterVariables();
    screen = Xtn5250TerminalEmulatorIT.buildLoginScreenWithUserNameAndPasswordFields();
    rteExtractor = new RTEExtractor();
    TestJMeterUtils.createJmeterEnv();
    configureEnvironment();
    rteExtractor.setContext(context);
    outputStream = new ByteArrayOutputStream();
  }

  public void configureEnvironment() {
    context = JMeterContextService.getContext();
    context.setPreviousResult(getCustomizedResult());
  }

  private SampleResult getCustomizedResult() {
    RteSampleResultBuilder ret = new RteSampleResultBuilder(CURSOR_POSITION, screen)
        .withLabel("bzm-Connect")
        .withServer("localhost")
        .withPort(2526)
        .withProtocol(Protocol.TN3270)
        .withTerminalType(new TerminalType("IBM-3179-2", new Dimension(24, 80)))
        .withSslType(SSLType.NONE)
        .withAction(Action.SEND_INPUT)
        .withConnectEndNow()
        .withLatencyEndNow();

    return ret.build();
  }

  @Test
  public void shouldVerifyJmeterVariablesWhenCursorPositionSelected() {
    setUpExtractorForCursorPosition();
    rteExtractor.process();
    softly.assertThat(context.getVariables().get(POSITION_VAR_ROW)).isEqualTo("10");
    softly.assertThat(context.getVariables().get(POSITION_VAR_COLUMN)).isEqualTo("2");
  }

  public void setUpExtractorForCursorPosition() {
    rteExtractor.setPositionType(PositionType.CURSOR_POSITION);
    rteExtractor.setVariablePrefix(VARIABLE_PREFIX);
  }

  @Test
  public void shouldVerifyJmeterVariablesWhenNextFieldSelected() {
    setUpExtractorForNextFieldPosition("1", "1", "14", VARIABLE_PREFIX);
    rteExtractor.process();
    softly.assertThat(context.getVariables().get(POSITION_VAR_ROW)).isEqualTo("2");
    softly.assertThat(context.getVariables().get(POSITION_VAR_COLUMN)).isEqualTo("18");
  }

  private void setUpExtractorForNextFieldPosition(String offset, String row, String column, String prefix) {
    rteExtractor.setPositionType(PositionType.NEXT_FIELD_POSITION);
    rteExtractor.setVariablePrefix(prefix);
    rteExtractor.setOffset(offset);
    rteExtractor.setRow(row);
    rteExtractor.setColumn(column);
  }

  @Test
  public void shouldAlertUserWhenNextFieldCursorWithCoordsBiggerThanScreenDimension() {
    setUpExtractorForNextFieldPosition("1", "100", "30", VARIABLE_PREFIX);
    setOutputListener();
    try {
      rteExtractor.process();
      // This exception is not needed to be cached
      // in the extractor, 'couse before it, user
      // will receive an alert informing
    } catch (NullPointerException e) {
      assertThat(outputStream.toString()).isEqualTo(USER_MESSAGE_ERROR);
    }
  }

  private void setOutputListener() {
    PrintStream ps = new PrintStream(outputStream);
    System.setOut(ps);
  }

  @Test
  public void shouldAlertUserWhenOffsetValueBiggerThanPossibleFieldsToSkip() {
    setUpExtractorForNextFieldPosition("4", "1", "14", VARIABLE_PREFIX);
    setOutputListener();
    try {
      rteExtractor.process();
    } catch (NullPointerException e) {
      assertThat(outputStream.toString().trim()).isEqualTo("Number of fields in the screen was 2"
          + "\nTherefore is not possible to skip 4 fields");
    }
  }
  
  @Test(expected = InvalidFieldPositionException.class)
  public void shouldAlertUserWhenNextFieldPositionWithCoordsDifferentThanFieldCoords() {
    setUpExtractorForNextFieldPosition("1", "21", "3", VARIABLE_PREFIX);
    setOutputListener();
    try {
      rteExtractor.process();
    } catch (NullPointerException e) {
      assertThat(outputStream.toString().trim()).isEqualTo("Inserted values for row/column in extractor\n"
          + "do not match with any field in current screen");
    }
    
  }
  @Test
  public void shouldAlertUserWhenNoPrefixVariableNameSet() {
    setUpExtractorForNextFieldPosition("1", "1", "14", "");
    setOutputListener();
    rteExtractor.process();
    assertThat(outputStream.toString().trim()).isEqualTo("The variable name in extractor is essential for later usage");
  }
}
