package com.blazemeter.jmeter.rte.sampler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.blazemeter.jmeter.rte.JMeterTestUtils;
import com.blazemeter.jmeter.rte.core.AttentionKey;
import com.blazemeter.jmeter.rte.core.CoordInput;
import com.blazemeter.jmeter.rte.core.Input;
import com.blazemeter.jmeter.rte.core.Position;
import com.blazemeter.jmeter.rte.core.Protocol;
import com.blazemeter.jmeter.rte.core.RteIOException;
import com.blazemeter.jmeter.rte.core.RteProtocolClient;
import com.blazemeter.jmeter.rte.core.RteSampleResult;
import com.blazemeter.jmeter.rte.core.Screen;
import com.blazemeter.jmeter.rte.core.TerminalType;
import com.blazemeter.jmeter.rte.core.ssl.SSLType;
import com.blazemeter.jmeter.rte.core.wait.Area;
import com.blazemeter.jmeter.rte.core.wait.CursorWaitCondition;
import com.blazemeter.jmeter.rte.core.wait.SilentWaitCondition;
import com.blazemeter.jmeter.rte.core.wait.SyncWaitCondition;
import com.blazemeter.jmeter.rte.core.wait.TextWaitCondition;
import java.awt.Dimension;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeoutException;
import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.util.JMeterUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class RTESamplerTest {

  private static final long CUSTOM_TIMEOUT_MILLIS = 3000;
  private static final long CUSTOM_STABLE_TIMEOUT_MILLIS = 500;
  private static final String TEST_SCREEN = "Test screen\n";
  private static final List<Input> INPUTS = Collections
      .singletonList(new CoordInput(new Position(1, 1), "input"));
  private static final Position CURSOR_POSITION = new Position(1, 1);

  @Mock
  private RteProtocolClient rteProtocolClientMock;
  private RTESampler rteSampler;
  private ConfigTestElement configTestElement = new ConfigTestElement();

  @BeforeClass
  public static void setupClass() {
    JMeterTestUtils.setupJmeterEnv();
  }

  @AfterClass
  public static void tearDownClass() {
    /*RESampler in some cases throw an InterruptedException and set true the interrupted flag when
    this is tested is needed reset this flag otherwise the following I/O operations will fail.*/
    Thread.interrupted();
  }

  @Before
  public void setup() {
    rteSampler = new RTESampler(p -> rteProtocolClientMock);
    when(rteProtocolClientMock.isInputInhibited()).thenReturn(true, false);
    when(rteProtocolClientMock.getScreen()).thenReturn(Screen.valueOf(TEST_SCREEN));

    when(rteProtocolClientMock.resetAlarm()).thenReturn(false);
    when(rteProtocolClientMock.getCursorPosition()).thenReturn(Optional.of(CURSOR_POSITION));
    buildDefaultRTEConfig();
    rteSampler.addTestElement(configTestElement);
    rteSampler.setPayload(buildInputs());
    rteSampler.getThreadContext().getVariables().incIteration();
  }

  private void buildDefaultRTEConfig() {
    configTestElement.setProperty(RTESampler.CONFIG_SERVER, "server");
    configTestElement.setProperty(RTESampler.CONFIG_PORT, 23);
    configTestElement
        .setProperty(RTESampler.CONFIG_TERMINAL_TYPE, RTESampler.DEFAULT_TERMINAL_TYPE.getId());
    configTestElement.setProperty(RTESampler.CONFIG_PROTOCOL, RTESampler.DEFAULT_PROTOCOL.name());
    configTestElement.setProperty(RTESampler.CONFIG_SSL_TYPE, RTESampler.DEFAULT_SSLTYPE.name());
    configTestElement.setProperty(RTESampler.CONFIG_CONNECTION_TIMEOUT, "0");
  }

  private Inputs buildInputs() {
    Inputs ret = new Inputs();
    ret.addInput(new CoordInputRowGUI("1", "1", "input"));
    return ret;
  }

  @After
  public void teardown() {
    rteSampler.threadFinished();
    rteSampler.setStableTimeout(null);
  }

  @Test
  public void shouldGetErrorSamplerResultWhenGetClientThrowTimeoutException() throws Exception {
    TimeoutException e = new TimeoutException();
    doThrow(e).when(rteProtocolClientMock)
        .connect(any(), anyInt(), any(), any(), anyLong());
    assertSampleResult(rteSampler.sample(null), buildExpectedConnectTimeoutErrorResult(e));
  }

  private RteSampleResult buildExpectedConnectTimeoutErrorResult(Exception e) {
    RteSampleResult expected = buildBaseSampleResult();
    expected.setSslType(SSLType.NONE);
    expected.setAction(Action.SEND_INPUT);
    expected.setSuccessful(false);
    expected.setResponseCode(e.getClass().getName());
    expected.setResponseMessage(e.getMessage());
    expected.setDataType(SampleResult.TEXT);
    return expected;
  }

  private RteSampleResult buildBaseSampleResult() {
    RteSampleResult expected = new RteSampleResult();
    expected.setSampleLabel(rteSampler.getName());
    expected.setServer("server");
    expected.setPort(23);
    expected.setProtocol(Protocol.TN5250);
    expected.setTerminalType(new TerminalType("IBM-3179-2", new Dimension(80, 24)));
    return expected;
  }

  private void assertSampleResult(SampleResult result, SampleResult expected) {
    assertThat(result)
        .isEqualToComparingOnlyGivenFields(expected, "sampleLabel", "requestHeaders", "samplerData",
            "successful", "responseCode", "responseMessage", "responseHeaders", "dataType",
            "responseDataAsString");
  }

  @Test
  public void shouldGetErrorSamplerResultWhenGetClientThrowInterruptedException() throws Exception {
    InterruptedException e = new InterruptedException();
    doThrow(e).when(rteProtocolClientMock)
        .connect(any(), anyInt(), any(), any(), anyLong());
    assertSampleResult(rteSampler.sample(null), buildExpectedErrorResult(e));
  }

  private RteSampleResult buildExpectedErrorResult(Exception e) {
    RteSampleResult expected = buildBaseSampleResult();
    expected.setSslType(SSLType.NONE);
    expected.setAction(Action.SEND_INPUT);
    expected.setSuccessful(false);
    expected.setResponseCode(e.getClass().getName());
    expected.setResponseMessage(e.getMessage());
    expected.setDataType(SampleResult.TEXT);
    StringWriter sw = new StringWriter();
    e.printStackTrace(new PrintWriter(sw));
    expected.setResponseData(sw.toString(), SampleResult.DEFAULT_HTTP_ENCODING);
    return expected;
  }

  @Test
  public void shouldGetErrorSamplerResultWhenSendThrowIllegalArgumentException() throws Exception {
    IllegalArgumentException e = new IllegalArgumentException();
    doThrow(e).when(rteProtocolClientMock)
        .send(any(), any());
    RteSampleResult expected = buildExpectedErrorResult(e);
    expected.setInputInhibitedRequest(true);
    expected.setInputs(INPUTS);
    expected.setAttentionKey(AttentionKey.ENTER);
    SampleResult result = rteSampler.sample(null);
    // stack traces won't match since they are obtained from different parts of code, so we just truncate them
    truncateExceptionStacktrace(result);
    truncateExceptionStacktrace(expected);
    assertSampleResult(result, expected);
  }

  private void truncateExceptionStacktrace(SampleResult result) {
    String response = result.getResponseDataAsString();
    result.setResponseData(response.substring(0, response.indexOf('\n')), "UTF-8");
  }

  @Test
  public void shouldGetErrorSamplerResultWhenSendAwaitThrowsException() throws Exception {
    TimeoutException e = new TimeoutException();
    // we use a custom timeout to differentiate it from connection wait
    rteSampler.setWaitSyncTimeout(String.valueOf(CUSTOM_TIMEOUT_MILLIS));
    doThrow(e).
        when(rteProtocolClientMock).await(Collections
        .singletonList(new SyncWaitCondition(CUSTOM_TIMEOUT_MILLIS,
            RTESampler.DEFAULT_STABLE_TIMEOUT_MILLIS)));
    when(rteProtocolClientMock.getScreen()).thenReturn(Screen.valueOf(TEST_SCREEN));
    assertSampleResult(rteSampler.sample(null), buildExpectedTimeoutErrorResult(e));
  }

  private RteSampleResult buildExpectedTimeoutErrorResult(Exception e) {
    RteSampleResult expected = buildExpectedConnectTimeoutErrorResult(e);
    expected.setInputInhibitedRequest(true);
    expected.setInputs(INPUTS);
    expected.setAttentionKey(AttentionKey.ENTER);
    expected.setScreen(Screen.valueOf(TEST_SCREEN));
    return expected;
  }

  @Test
  public void shouldGetSuccessfulSamplerResultWhenSend() {
    SampleResult result = rteSampler.sample(null);
    SampleResult expected = buildExpectedSuccessfulSendInputResult(SSLType.NONE);
    assertSampleResult(result, expected);
  }

  private RteSampleResult buildExpectedSuccessfulSendInputResult(SSLType sslType) {
    RteSampleResult expected = buildBaseSampleResult();
    expected.setSslType(sslType);
    expected.setAction(Action.SEND_INPUT);
    expected.setAttentionKey(AttentionKey.ENTER);
    expected.setInputs(INPUTS);
    expected.setInputInhibitedRequest(true);
    expected.setSuccessful(true);
    expected.setCursorPosition(CURSOR_POSITION);
    expected.setScreen(Screen.valueOf(TEST_SCREEN));
    expected.setInputInhibitedResponse(false);
    return expected;
  }

  @Test
  public void shouldGetSuccessfulSamplerWithResultAlarmHeaderResultWhenClientGetAlarmSignal() {
    when(rteProtocolClientMock.isAlarmOn()).thenReturn(true);
    RteSampleResult expected = buildExpectedSuccessfulSendInputResult(SSLType.NONE);
    expected.setSoundedAlarm(true);
    assertSampleResult(rteSampler.sample(null), expected);
  }

  @Test
  public void shouldSendDefaultAttentionKeyToEmulatorWhenSampleWithoutSpecifyingAttentionKey()
      throws Exception {
    rteSampler.sample(null);
    verify(rteProtocolClientMock)
        .send(any(), eq(AttentionKey.ENTER));
  }

  @Test
  public void shouldSendCustomAttentionKeyToEmulatorWhenSampleWithCustomAttentionKey()
      throws Exception {
    rteSampler.setAttentionKey(AttentionKey.F1);
    rteSampler.sample(null);
    verify(rteProtocolClientMock)
        .send(any(), eq(AttentionKey.F1));
  }

  @Test
  public void shouldNotSendInputToEmulatorWhenSampleWithConnectAction() throws Exception {
    rteSampler.setAction(Action.CONNECT);
    rteSampler.sample(null);
    verify(rteProtocolClientMock, never())
        .send(any(), any());
  }

  @Test
  public void shouldGetConnectActionResultWhenSampleWithConnectAction() {
    rteSampler.setAction(Action.CONNECT);
    assertSampleResult(rteSampler.sample(null), buildExpectedSuccessfulConnectResult());
  }

  private RteSampleResult buildExpectedSuccessfulConnectResult() {
    RteSampleResult expected = buildBaseSampleResult();
    expected.setSslType(SSLType.NONE);
    expected.setAction(Action.CONNECT);
    expected.setSuccessful(true);
    expected.setCursorPosition(CURSOR_POSITION);
    expected.setScreen(Screen.valueOf(TEST_SCREEN));
    expected.setInputInhibitedResponse(true);
    return expected;
  }

  @Test
  public void shouldDisconnectEmulatorWhenSampleWithDisconnectAction() throws Exception {
    connectClient();
    rteSampler.setAction(Action.DISCONNECT);
    rteSampler.sample(null);
    verify(rteProtocolClientMock).disconnect();
  }

  @Test
  public void shouldDisconnectEmulatorWhenIterationStart() throws Exception {
    rteSampler.sample(null);
    rteSampler.iterationStart(null);
    verify(rteProtocolClientMock).disconnect();
  }

  @Test
  public void shouldNotDisconnectEmulatorWhenIterationStartAndReuseConnectionsEnabled()
      throws Exception {
    rteSampler.setReuseConnections(true);
    try {
      rteSampler.sample(null);
      rteSampler.iterationStart(null);
      verify(rteProtocolClientMock, never()).disconnect();
    } finally {
      rteSampler.setReuseConnections(false);
    }
  }

  private void connectClient() {
    RTESampler sampler = new RTESampler(p -> rteProtocolClientMock);
    sampler.addTestElement(configTestElement);
    sampler.setPayload(buildInputs());
    sampler.setAction(Action.CONNECT);
    sampler.sample(null);
  }

  @Test
  public void shouldNotDisconnectEmulatorWhenIterationStartAndIterationHasNotChanged()
      throws Exception {
    rteSampler.sample(null);
    rteSampler.iterationStart(null);
    reset(rteProtocolClientMock);
    rteSampler.sample(null);
    rteSampler.iterationStart(null);
    verify(rteProtocolClientMock, never()).disconnect();
  }

  @Test
  public void shouldGetDisconnectActionResultWhenSampleWithDisconnectAction() {
    connectClient();
    rteSampler.setAction(Action.DISCONNECT);
    assertSampleResult(rteSampler.sample(null), buildExpectedDisconnectSuccessfulResult());
  }

  private RteSampleResult buildExpectedDisconnectSuccessfulResult() {
    RteSampleResult expected = buildBaseSampleResult();
    expected.setSslType(SSLType.NONE);
    expected.setAction(Action.DISCONNECT);
    expected.setSuccessful(true);
    return expected;
  }

  @Test
  public void shouldGetDisconnectActionResultWhenSampleWithDisconnectActionAndNoExistingConnection() {
    rteSampler.setAction(Action.DISCONNECT);
    assertSampleResult(rteSampler.sample(null), buildExpectedDisconnectSuccessfulResult());
  }

  @Test
  public void shouldGetErrorSamplerResultWhenDisconnectThrowRteIOException() throws Exception {
    connectClient();
    rteSampler.setAction(Action.DISCONNECT);
    RteIOException e = new RteIOException(null);
    doThrow(e)
        .when(rteProtocolClientMock).disconnect();
    SampleResult result = rteSampler.sample(null);
    SampleResult expected = buildExpectedErrorResultDisconnect(e);
    assertSampleResult(result, expected);
  }

  private RteSampleResult buildExpectedErrorResultDisconnect(Exception e) {
    RteSampleResult expected = buildBaseSampleResult();
    expected.setSslType(SSLType.NONE);
    expected.setAction(Action.DISCONNECT);
    expected.setSuccessful(false);
    expected.setResponseCode(e.getClass().getName());
    expected.setResponseMessage(e.getMessage());
    expected.setDataType(SampleResult.TEXT);
    StringWriter sw = new StringWriter();
    e.printStackTrace(new PrintWriter(sw));
    expected.setResponseData(sw.toString(), SampleResult.DEFAULT_HTTP_ENCODING);
    return expected;
  }

  @Test
  public void shouldAwaitSyncWaiterWhenSendInputWithSyncWaitEnabled() throws Exception {
    rteSampler.sample(null);
    // we wait for 2 events since both connection wait and send input wait use same parameters
    verify(rteProtocolClientMock, times(2))
        .await(Collections.singletonList(
            new SyncWaitCondition(RTESampler.DEFAULT_WAIT_SYNC_TIMEOUT_MILLIS,
                RTESampler.DEFAULT_STABLE_TIMEOUT_MILLIS)));
  }

  @Test
  public void shouldBuildSyncWaiterWithCustomWhenSyncWaitEnabled() throws Exception {
    rteSampler.setWaitSyncTimeout(String.valueOf(CUSTOM_TIMEOUT_MILLIS));
    rteSampler.setStableTimeout(CUSTOM_STABLE_TIMEOUT_MILLIS);
    rteSampler.sample(null);
    verify(rteProtocolClientMock)
        .await(Collections.singletonList(
            new SyncWaitCondition(CUSTOM_TIMEOUT_MILLIS, CUSTOM_STABLE_TIMEOUT_MILLIS)));
  }

  @Test
  public void shouldAwaitOnlyConnectionSyncWhenNoWaitersAreEnabled() throws Exception {
    rteSampler.setWaitSync(false);
    rteSampler.sample(null);
    verify(rteProtocolClientMock).await(any());
  }

  @Test
  public void shouldAwaitWithDefaultOrderConditionsWhenSampleAndWaitersHaveSameTimeout()
      throws Exception {
    rteSampler.setWaitSyncTimeout(String.valueOf(CUSTOM_TIMEOUT_MILLIS));
    rteSampler.setWaitCursor(true);
    rteSampler.setWaitCursorTimeout(String.valueOf(CUSTOM_TIMEOUT_MILLIS));
    rteSampler.sample(null);
    verify(rteProtocolClientMock)
        .await(Arrays.asList(
            new SyncWaitCondition(CUSTOM_TIMEOUT_MILLIS, RTESampler.DEFAULT_STABLE_TIMEOUT_MILLIS),
            new CursorWaitCondition(CURSOR_POSITION, CUSTOM_TIMEOUT_MILLIS,
                RTESampler.DEFAULT_STABLE_TIMEOUT_MILLIS)));
  }

  @Test
  public void shouldAwaitWithConditionsSortedByTimeoutToOptimizeWaitingTimeOnTimeouts()
      throws Exception {
    rteSampler.setWaitSyncTimeout(String.valueOf(CUSTOM_TIMEOUT_MILLIS));
    rteSampler.setWaitCursor(true);
    rteSampler.setWaitCursorTimeout(String.valueOf(CUSTOM_TIMEOUT_MILLIS - 1));
    rteSampler.sample(null);
    verify(rteProtocolClientMock)
        .await(Arrays.asList(
            new CursorWaitCondition(CURSOR_POSITION, CUSTOM_TIMEOUT_MILLIS - 1,
                RTESampler.DEFAULT_STABLE_TIMEOUT_MILLIS),
            new SyncWaitCondition(CUSTOM_TIMEOUT_MILLIS,
                RTESampler.DEFAULT_STABLE_TIMEOUT_MILLIS)));
  }

  @Test
  public void shouldAwaitSilentWhenSilentWaitEnabled() throws Exception {
    rteSampler.setWaitSync(false);
    rteSampler.setWaitSilent(true);
    rteSampler.sample(null);
    verify(rteProtocolClientMock)
        .await(Collections.singletonList(
            new SilentWaitCondition(RTESampler.DEFAULT_WAIT_SILENT_TIMEOUT_MILLIS,
                RTESampler.DEFAULT_WAIT_SILENT_TIME_MILLIS)));
  }

  @Test
  public void shouldAwaitSilentWithCustomValuesWhenSilentWaitEnabled() throws Exception {
    rteSampler.setWaitSync(false);
    rteSampler.setWaitSilent(true);
    rteSampler.setWaitSilentTimeout(String.valueOf(CUSTOM_TIMEOUT_MILLIS));
    rteSampler.setWaitSilentTime(String.valueOf(CUSTOM_STABLE_TIMEOUT_MILLIS));
    rteSampler.sample(null);
    verify(rteProtocolClientMock)
        .await(Collections.singletonList(
            new SilentWaitCondition(CUSTOM_TIMEOUT_MILLIS, CUSTOM_STABLE_TIMEOUT_MILLIS)));
  }

  @Test
  public void shouldAwaitCursorWhenCursorWaitEnabled() throws Exception {
    rteSampler.setWaitSync(false);
    rteSampler.setWaitCursor(true);
    rteSampler.sample(null);
    verify(rteProtocolClientMock)
        .await(Collections.singletonList(new CursorWaitCondition(CURSOR_POSITION,
            RTESampler.DEFAULT_WAIT_CURSOR_TIMEOUT_MILLIS,
            RTESampler.DEFAULT_STABLE_TIMEOUT_MILLIS)));
  }

  @Test
  public void shouldAwaitCursorWithCustomValuesWhenCursorWaitEnabled() throws Exception {
    rteSampler.setWaitSync(false);
    rteSampler.setWaitCursor(true);
    int customRow = 5;
    rteSampler.setWaitCursorRow(String.valueOf(customRow));
    int customColumn = 7;
    rteSampler.setWaitCursorColumn(String.valueOf(customColumn));
    rteSampler.setWaitCursorTimeout(String.valueOf(CUSTOM_TIMEOUT_MILLIS));
    rteSampler.setStableTimeout(CUSTOM_STABLE_TIMEOUT_MILLIS);
    rteSampler.sample(null);
    verify(rteProtocolClientMock)
        .await(Collections.singletonList(
            new CursorWaitCondition(new Position(customRow, customColumn), CUSTOM_TIMEOUT_MILLIS,
                CUSTOM_STABLE_TIMEOUT_MILLIS)));
  }

  @Test
  public void shouldAwaitTextWhenWaitTextEnabled() throws Exception {
    rteSampler.setWaitSync(false);
    rteSampler.setWaitText(true);
    String regex = "test";
    rteSampler.setWaitTextRegex(regex);
    rteSampler.sample(null);
    verify(rteProtocolClientMock)
        .await(Collections.singletonList(new TextWaitCondition(
            JMeterUtils.getPattern(regex),
            JMeterUtils.getMatcher(),
            Area.fromTopLeftBottomRight(1, 1, Position.UNSPECIFIED_INDEX,
                Position.UNSPECIFIED_INDEX),
            RTESampler.DEFAULT_WAIT_TEXT_TIMEOUT_MILLIS,
            RTESampler.DEFAULT_STABLE_TIMEOUT_MILLIS)));
  }

  @Test
  public void shouldAwaitTextWithCustomValuesWhenWaitTextEnabled() throws Exception {
    rteSampler.setWaitSync(false);
    rteSampler.setWaitText(true);
    String regex = "test";
    rteSampler.setWaitTextRegex(regex);
    int areaTop = 2;
    rteSampler.setWaitTextAreaTop(String.valueOf(areaTop));
    int areaLeft = 3;
    rteSampler.setWaitTextAreaLeft(String.valueOf(areaLeft));
    int areaBottom = 4;
    rteSampler.setWaitTextAreaBottom(String.valueOf(areaBottom));
    int areaRight = 5;
    rteSampler.setWaitTextAreaRight(String.valueOf(areaRight));
    rteSampler.setWaitTextTimeout(String.valueOf(CUSTOM_TIMEOUT_MILLIS));
    rteSampler.setStableTimeout(CUSTOM_STABLE_TIMEOUT_MILLIS);
    rteSampler.sample(null);
    verify(rteProtocolClientMock)
        .await(Collections
            .singletonList(new TextWaitCondition(
                JMeterUtils.getPattern(regex),
                JMeterUtils.getMatcher(),
                Area.fromTopLeftBottomRight(areaTop, areaLeft, areaBottom, areaRight),
                CUSTOM_TIMEOUT_MILLIS,
                CUSTOM_STABLE_TIMEOUT_MILLIS)));
  }

  @Test
  public void shouldConnectUsingCustomSSLTypeValueToEmulatorWhenKeyStorePropertiesEnabled()
      throws Exception {
    rteSampler.setSslType(SSLType.TLS);
    rteSampler.sample(null);
    verify(rteProtocolClientMock)
        .connect(any(), anyInt(), eq(SSLType.TLS), any(), anyLong());
  }

  @Test
  public void shouldGetCustomSslHeaderWhenUsingCustomSsl() {
    rteSampler.setSslType(SSLType.TLS);
    assertSampleResult(rteSampler.sample(null),
        buildExpectedSuccessfulSendInputResult(SSLType.TLS));
  }

}
