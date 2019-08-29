package com.blazemeter.jmeter.rte.extractor;

import static org.assertj.core.api.Assertions.assertThat;

import com.blazemeter.jmeter.rte.JMeterTestUtils;
import com.blazemeter.jmeter.rte.core.Protocol;
import com.blazemeter.jmeter.rte.core.RteSampleResultBuilder;
import com.blazemeter.jmeter.rte.core.TerminalType;
import com.blazemeter.jmeter.rte.core.ssl.SSLType;
import com.blazemeter.jmeter.rte.sampler.Action;
import java.awt.Dimension;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
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
import org.junit.runners.Parameterized;

public class RTEExtractorTest {

  public static final String POSITION_VAR_ROW = "positionVar_ROW";
  public static final String POSITION_VAR_COLUMN = "positionVar_COLUMN";
  public static final String VARIABLE_PREFIX = "positionVar";
  public static final String FOURTEEN_LITERAL = "14";
  public static final String TWENTY_FIVE_LITERAL = "25";
  public static final String ONE_LITERAL = "1";
  private static final String RESPONSE_HEADERS = "Input-inhibited: true\n" +
      "Cursor-position: (1,1)" + '\n' +
      "Field-positions: [(2,25)-(2,30)], [(4,25)-(4,30)], [(6,25)-(6,30)]\n";
  @Rule
  public JUnitSoftAssertions softly = new JUnitSoftAssertions();
  private JMeterContext context;
  private RTEExtractor rteExtractor;
  private JMeterVariables vars;

  @BeforeClass
  public static void setupClass() {
    JMeterTestUtils.setupJmeterEnv();
    JMeterContext context = JMeterContextService.getContext();
    context.setPreviousResult(getCustomizedResult());
  }

  private static SampleResult getCustomizedResult() {
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

  @Before
  public void setup() {
    context = JMeterContextService.getContext();
    rteExtractor = new RTEExtractor();
    rteExtractor.setContext(context);
    context.setVariables(new JMeterVariables());
    vars = context.getVariables();
  }

  @Test
  public void shouldExtractCursorPositionWhenCursorPositionSelected() {
    setUpExtractorForCursorPosition();
    rteExtractor.process();
    softly.assertThat(context.getVariables().get(POSITION_VAR_ROW)).isEqualTo(ONE_LITERAL);
    softly.assertThat(context.getVariables().get(POSITION_VAR_COLUMN)).isEqualTo(ONE_LITERAL);
  }

  private void setUpExtractorForCursorPosition() {
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


  @Test()
  public void shouldAlertUserWhenNextFieldCursorWithCoordsBiggerThanScreenDimension() {
    setUpExtractorForNextFieldPosition(ONE_LITERAL, "100", "30", VARIABLE_PREFIX);
    rteExtractor.process();
    assertUnchangedJMeterVariables();
  }

  private void assertUnchangedJMeterVariables() {
    assertThat(context.getVariables()).isEqualTo(vars);
  }

  @Test
  public void shouldAlertUserWhenOffsetValueBiggerThanPossibleFieldsToSkip() {
    setUpExtractorForNextFieldPosition("4", ONE_LITERAL, FOURTEEN_LITERAL, VARIABLE_PREFIX);
    rteExtractor.process();
    assertUnchangedJMeterVariables();
  }


  @Test
  public void shouldAlertUserWhenNoPrefixVariableNameSet() {
    setUpExtractorForNextFieldPosition(ONE_LITERAL, ONE_LITERAL, FOURTEEN_LITERAL, "");
    rteExtractor.process();
    assertUnchangedJMeterVariables();
  }
  
  @Test
  public void shouldGetTheGivenPositionWhenOffsetZero() {
    setUpExtractorForNextFieldPosition("0", ONE_LITERAL, FOURTEEN_LITERAL, VARIABLE_PREFIX);
    rteExtractor.process();
    softly.assertThat(context.getVariables().get(POSITION_VAR_ROW)).isEqualTo(ONE_LITERAL);
    softly.assertThat(context.getVariables().get(POSITION_VAR_COLUMN)).isEqualTo(FOURTEEN_LITERAL);
  }

  @Test
  public void shouldGetNextFieldWhenOffsetOneAndPositionRightBeforeField() {
    setUpExtractorForNextFieldPosition(ONE_LITERAL, "2", "24");
    rteExtractor.process();
    softly.assertThat(context.getVariables().get(POSITION_VAR_ROW)).isEqualTo("2");
    softly.assertThat(context.getVariables().get(POSITION_VAR_COLUMN)).isEqualTo(
        TWENTY_FIVE_LITERAL);
  }

  @Test
  public void shouldGetNextFieldWhenOffsetOneAndPositionIsInField() {
    setUpExtractorForNextFieldPosition(ONE_LITERAL, "2", TWENTY_FIVE_LITERAL);
    rteExtractor.process();
    softly.assertThat(context.getVariables().get(POSITION_VAR_ROW)).isEqualTo("4");
    softly.assertThat(context.getVariables().get(POSITION_VAR_COLUMN))
        .isEqualTo(TWENTY_FIVE_LITERAL);
  }

  @Test
  public void shouldGetCurrentPositionField() {
    setUpExtractorForNextFieldPosition(ONE_LITERAL, "2", "26");
    rteExtractor.process();
    softly.assertThat(context.getVariables().get(POSITION_VAR_ROW)).isEqualTo("2");
    softly.assertThat(context.getVariables().get(POSITION_VAR_COLUMN))
        .isEqualTo(TWENTY_FIVE_LITERAL);
  }

  @Test
  public void shouldGetFieldSkippingOne() {
    setUpExtractorForNextFieldPosition("2", "2", "24");
    rteExtractor.process();
    softly.assertThat(context.getVariables().get(POSITION_VAR_ROW)).isEqualTo("4");
    softly.assertThat(context.getVariables().get(POSITION_VAR_COLUMN))
        .isEqualTo(TWENTY_FIVE_LITERAL);
  }

  @Test
  public void shouldNotifyUserWhenNoForwardFieldsToSkip() {
    setUpExtractorForNextFieldPosition("2", "4", TWENTY_FIVE_LITERAL);
    rteExtractor.process();
    assertUnchangedJMeterVariables();
  }

  @Test
  public void shouldNotifyUserWhenNoBackwardFieldsToSkip() {
    setUpExtractorForNextFieldPosition("-1", "2", "24");
    rteExtractor.process();
    assertUnchangedJMeterVariables();
  }

  @Test
  public void shouldNotifyUserWhenGivenPositionIsInTheEndOfFieldWhileBackwards() {
    setUpExtractorForNextFieldPosition("-1", "2", "30");
    rteExtractor.process();
    assertUnchangedJMeterVariables();
  }

  @Test
  public void shouldGetFieldWhenOffsetIsMinusOne() {
    setUpExtractorForNextFieldPosition("-1", "2", "31");
    rteExtractor.process();
    softly.assertThat(context.getVariables().get(POSITION_VAR_ROW)).isEqualTo("2");
    softly.assertThat(context.getVariables().get(POSITION_VAR_COLUMN))
        .isEqualTo(TWENTY_FIVE_LITERAL);
  }

  @Test
  public void shouldNotifyUserOfSkippingInvalidTimesWhenNoFieldsBackwardsWhileOffsetNegativeTwo() {
    setUpExtractorForNextFieldPosition("-2", "2", "31");
    rteExtractor.process();
    assertUnchangedJMeterVariables();
  }

  @Test
  public void shouldNotifyUserOfSkippingInvalidTimesWhenPositionIsEndOfFieldAndOffsetNegative() {
    setUpExtractorForNextFieldPosition("-2", "2", "30");
    rteExtractor.process();
    assertUnchangedJMeterVariables();
  }

  @Test
  public void shouldGetFieldWhenSkippingBackwardsOnceFromValidPosition() {
    setUpExtractorForNextFieldPosition("-2", "4", "31");
    rteExtractor.process();
    softly.assertThat(context.getVariables().get(POSITION_VAR_ROW)).isEqualTo("2");
    softly.assertThat(context.getVariables().get(POSITION_VAR_COLUMN))
        .isEqualTo(TWENTY_FIVE_LITERAL);
  }
}
