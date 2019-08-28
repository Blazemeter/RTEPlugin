package com.blazemeter.jmeter.rte.extractor;

import com.blazemeter.jmeter.rte.core.Protocol;
import com.blazemeter.jmeter.rte.core.RteSampleResultBuilder;
import com.blazemeter.jmeter.rte.core.TerminalType;
import com.blazemeter.jmeter.rte.core.ssl.SSLType;
import com.blazemeter.jmeter.rte.sampler.Action;
import java.awt.Dimension;
import kg.apc.emulators.TestJMeterUtils;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
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
  private static final String RESPONSE_HEADERS = "Input-inhibited: true\n" +
      "Cursor-position: (1,1)" + '\n' +
      "Field-positions: [(2,25)-(2,30)], [(4,25)-(4,30)], [(6,25)-(6,30)]\n";

  @Rule
  public JUnitSoftAssertions softly = new JUnitSoftAssertions();
  private JMeterContext context;
  private RTEExtractor rteExtractor;

  @BeforeClass
  public static void setupClass() {
  }

  @Before
  public void setup() {
    rteExtractor = new RTEExtractor();
    TestJMeterUtils.createJmeterEnv();
    configureEnvironment();
    rteExtractor.setContext(context);
  }

  public void configureEnvironment() {
    context = JMeterContextService.getContext();
    context.setPreviousResult(getCustomizedResult());
  }

  private SampleResult getCustomizedResult() {
    TerminalType terminalType = new TerminalType("IBM-3179-2", new Dimension(24, 80));
    RteSampleResultBuilder ret = new RteSampleResultBuilder(null, null,
        RESPONSE_HEADERS, terminalType)
        .withLabel("bzm-Connect")
        .withServer("localhost")
        .withPort(2526)
        .withProtocol(Protocol.TN3270)
        .withTerminalType(terminalType)
        .withSslType(SSLType.NONE)
        .withAction(Action.SEND_INPUT)
        .withConnectEndNow()
        .withLatencyEndNow();

    return ret.build();
  }

  @Test
  public void shouldExtractCursorPositionWhenCursorPositionSelected() {
    setUpExtractorForCursorPosition();
    rteExtractor.process();
    softly.assertThat(context.getVariables().get(POSITION_VAR_ROW)).isEqualTo("1");
    softly.assertThat(context.getVariables().get(POSITION_VAR_COLUMN)).isEqualTo("1");
  }

  public void setUpExtractorForCursorPosition() {
    rteExtractor.setPositionType(PositionType.CURSOR_POSITION);
    rteExtractor.setVariablePrefix(VARIABLE_PREFIX);
  }

  private void setUpExtractorForNextFieldPosition(String offset, String row, String column,
      String prefix) {
    rteExtractor.setPositionType(PositionType.NEXT_FIELD_POSITION);
    rteExtractor.setVariablePrefix(prefix);
    rteExtractor.setOffset(offset);
    rteExtractor.setRow(row);
    rteExtractor.setColumn(column);
  }

  private void setUpExtractorForNextFieldPosition(String offset, String row, String column) {
    setUpExtractorForNextFieldPosition(offset, row, column, VARIABLE_PREFIX);
  }

  @Test(expected = NullPointerException.class)
  public void shouldAlertUserWhenNextFieldCursorWithCoordsBiggerThanScreenDimension() {
    setUpExtractorForNextFieldPosition("1", "100", "30", VARIABLE_PREFIX);
    rteExtractor.process();
    //Tried to assert on LOGs to be more precise
  }


  @Test
  public void shouldAlertUserWhenOffsetValueBiggerThanPossibleFieldsToSkip() {
    setUpExtractorForNextFieldPosition("4", "1", "14", VARIABLE_PREFIX);
    rteExtractor.process();
    //shouldThrowIndexOutOfBoundsException
  }


  @Test
  public void shouldAlertUserWhenNoPrefixVariableNameSet() {
    setUpExtractorForNextFieldPosition("1", "1", "14", "");
    rteExtractor.process();
    //shouldAssertTheError
  }

  @Test
  public void shouldGetTheGivenPositionWhenOffsetZero() {
    setUpExtractorForNextFieldPosition("0", "1", "14", VARIABLE_PREFIX);
    rteExtractor.process();
    softly.assertThat(context.getVariables().get(POSITION_VAR_ROW)).isEqualTo("1");
    softly.assertThat(context.getVariables().get(POSITION_VAR_COLUMN)).isEqualTo("14");
  }

  @Test
  public void shouldGetNextFieldWhenOffsetOneAndPositionRightBeforeField() {
    setUpExtractorForNextFieldPosition("1", "2", "24");
    rteExtractor.process();
    softly.assertThat(context.getVariables().get(POSITION_VAR_ROW)).isEqualTo("2");
    softly.assertThat(context.getVariables().get(POSITION_VAR_COLUMN)).isEqualTo("25");
  }

  @Test
  public void shouldGetNextFieldWhenOffsetOneAndPositionIsInField() {
    setUpExtractorForNextFieldPosition("1", "2", "25");
    rteExtractor.process();
    softly.assertThat(context.getVariables().get(POSITION_VAR_ROW)).isEqualTo("4");
    softly.assertThat(context.getVariables().get(POSITION_VAR_COLUMN)).isEqualTo("25");
  }

  @Test
  public void shouldGetCurrentPositionField() {
    setUpExtractorForNextFieldPosition("1", "2", "26");
    rteExtractor.process();
    softly.assertThat(context.getVariables().get(POSITION_VAR_ROW)).isEqualTo("2");
    softly.assertThat(context.getVariables().get(POSITION_VAR_COLUMN)).isEqualTo("25");
  }

  @Test
  public void shouldGetFieldSkippingOne() {
    setUpExtractorForNextFieldPosition("2", "2", "24");
    rteExtractor.process();
    softly.assertThat(context.getVariables().get(POSITION_VAR_ROW)).isEqualTo("4");
    softly.assertThat(context.getVariables().get(POSITION_VAR_COLUMN)).isEqualTo("25");
  }

  @Test
  public void shouldNotifyUserWhenNoForwardFieldsToSkip() {
    setUpExtractorForNextFieldPosition("2", "4", "25");
    rteExtractor.process();
    //should assert on IndexOutOfBoundsException
  }

  @Test
  public void shouldNotifyUserWhenNoBackwardFieldsToSkip() {
    setUpExtractorForNextFieldPosition("-1", "2", "24");
    rteExtractor.process();
    //>>> KEEP THIS TEST IN MIND
  }

  @Test
  public void shouldNotifyUserWhenGivenPositionIsInTheEndOfFieldWhileBackwards() {
    setUpExtractorForNextFieldPosition("-1", "2", "30");
    rteExtractor.process();
    //There are no fields position in the left side of the given position
  }

  @Test
  public void shouldGetFieldWhenOffsetIsMinusOne() {
    setUpExtractorForNextFieldPosition("-1", "2", "31");
    rteExtractor.process();
    softly.assertThat(context.getVariables().get(POSITION_VAR_ROW)).isEqualTo("2");
    softly.assertThat(context.getVariables().get(POSITION_VAR_COLUMN)).isEqualTo("25");
  }

  @Test
  public void shouldNotifyUserOfSkippingInvalidTimesWhenNoFieldsBackwardsWhileOffsetNegativeTwo() {
    setUpExtractorForNextFieldPosition("-2", "2", "31");
    rteExtractor.process();
  }

  @Test
  public void shouldNotifyUserOfSkippingInvalidTimesWhenPositionIsEndOfFieldAndOffsetNegative() {
    setUpExtractorForNextFieldPosition("-2", "2", "30");
    rteExtractor.process();
  }

  @Test
  public void shouldGetFieldWhenSkippingBackwardsOnceFromValidPosition() {
    setUpExtractorForNextFieldPosition("-2", "4", "31");
    rteExtractor.process();
    softly.assertThat(context.getVariables().get(POSITION_VAR_ROW)).isEqualTo("2");
    softly.assertThat(context.getVariables().get(POSITION_VAR_COLUMN)).isEqualTo("25");
  }
}
