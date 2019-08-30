package com.blazemeter.jmeter.rte.extractor;

import com.blazemeter.jmeter.rte.JMeterTestUtils;
import com.blazemeter.jmeter.rte.core.Protocol;
import com.blazemeter.jmeter.rte.core.RteSampleResultBuilder;
import com.blazemeter.jmeter.rte.core.TerminalType;
import com.blazemeter.jmeter.rte.core.ssl.SSLType;
import com.blazemeter.jmeter.rte.sampler.Action;
import java.awt.Dimension;
import java.util.Arrays;
import java.util.Collection;
import org.apache.commons.lang3.StringUtils;
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
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class RTEExtractorTest {

  public static final String POSITION_VAR_ROW = "positionVar_ROW";
  public static final String POSITION_VAR_COLUMN = "positionVar_COLUMN";
  public static final String VARIABLE_PREFIX = "positionVar";
  private static final String RESPONSE_HEADERS = "Input-inhibited: true\n" +
      "Cursor-position: (1,1)" + '\n' +
      "Field-positions: [(2,25)-(2,30)], [(4,25)-(4,30)], [(6,25)-(6,30)]\n";
  @Rule
  public JUnitSoftAssertions softly = new JUnitSoftAssertions();
  @Parameter()
  public String iRow;
  @Parameter(1)
  public String iColumn;
  @Parameter(2)
  public String offset;
  @Parameter(3)
  public String eRow;
  @Parameter(4)
  public String eColumn;
  @Parameter(5)
  public String variablePrefix;

  private JMeterContext context;
  private RTEExtractor rteExtractor;

  @BeforeClass
  public static void setupClass() {
    JMeterTestUtils.setupJmeterEnv();
    JMeterContext context = JMeterContextService.getContext();
    context.setPreviousResult(getCustomizedResult());
  }

  private static SampleResult getCustomizedResult() {
    TerminalType terminalType = new TerminalType("IBM-3179-2", new Dimension(80, 24));
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

  @Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(new Object[][]{
        {"100", "200", "1", null, null, VARIABLE_PREFIX},
        {"2", "25", "10", null, null, VARIABLE_PREFIX},
        {"2", "25", "0", "2", "25", VARIABLE_PREFIX},
        {"2", "24", "1", "2", "25", VARIABLE_PREFIX},
        {"2", "25", "1", "4", "25", VARIABLE_PREFIX},
        {"2", "26", "1", "4", "25", VARIABLE_PREFIX},
        {"2", "24", "2", "4", "25", VARIABLE_PREFIX},
        {"2", "25", "2", "6", "25", VARIABLE_PREFIX},
        {"4", "25", "2", null, null, VARIABLE_PREFIX},
        {"2", "24", "-1", null, null, VARIABLE_PREFIX},
        {"2", "30", "-1", null, null, VARIABLE_PREFIX},
        {"2", "31", "-1", "2", "25", VARIABLE_PREFIX},
        {"2", "31", "-2", null, null, VARIABLE_PREFIX},
        {"4", "30", "-2", null, null, VARIABLE_PREFIX},
        {"4", "31", "-2", "2", "25", VARIABLE_PREFIX},
        {"2", "25", "0", null, null, StringUtils.EMPTY},
    });
  }

  @Before
  public void setup() {
    context = JMeterContextService.getContext();
    rteExtractor = new RTEExtractor();
    rteExtractor.setContext(context);
    context.setVariables(new JMeterVariables());
  }

  @Test
  public void shouldExtractCursorPositionWhenCursorPositionSelected() {
    setUpExtractorForCursorPosition();
    rteExtractor.process();
    softly.assertThat(context.getVariables().get(POSITION_VAR_ROW)).isEqualTo("1");
    softly.assertThat(context.getVariables().get(POSITION_VAR_COLUMN)).isEqualTo("1");
  }

  private void setUpExtractorForCursorPosition() {
    rteExtractor.setPositionType(PositionType.CURSOR_POSITION);
    rteExtractor.setVariablePrefix(VARIABLE_PREFIX);
  }

  @Test
  public void whenNextFieldPositionCases() {
    setUpExtractorForNextFieldPosition(offset, iRow, iColumn, variablePrefix);
    rteExtractor.process();
    softly.assertThat(context.getVariables().get(POSITION_VAR_ROW)).isEqualTo(eRow);
    softly.assertThat(context.getVariables().get(POSITION_VAR_COLUMN)).isEqualTo(eColumn);
  }

  private void setUpExtractorForNextFieldPosition(String offset, String row, String column,
      String prefix) {
    rteExtractor.setPositionType(PositionType.NEXT_FIELD_POSITION);
    rteExtractor.setVariablePrefix(prefix);
    rteExtractor.setOffset(offset);
    rteExtractor.setRow(row);
    rteExtractor.setColumn(column);
  }
}
