package com.blazemeter.jmeter.rte.sampler;

import static com.blazemeter.jmeter.rte.SampleResultAssertions.assertSampleResult;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
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
import com.blazemeter.jmeter.rte.core.RteProtocolClient;
import com.blazemeter.jmeter.rte.core.RteSampleResultBuilder;
import com.blazemeter.jmeter.rte.core.Screen;
import com.blazemeter.jmeter.rte.core.TerminalType;
import com.blazemeter.jmeter.rte.core.exceptions.RteIOException;
import com.blazemeter.jmeter.rte.core.ssl.SSLType;
import com.blazemeter.jmeter.rte.core.wait.Area;
import com.blazemeter.jmeter.rte.core.wait.CursorWaitCondition;
import com.blazemeter.jmeter.rte.core.wait.SilentWaitCondition;
import com.blazemeter.jmeter.rte.core.wait.SyncWaitCondition;
import com.blazemeter.jmeter.rte.core.wait.TextWaitCondition;
import java.awt.Dimension;
import java.nio.charset.StandardCharsets;
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
  private RteProtocolClient client;
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
    rteSampler = new RTESampler(p -> client);
    when(client.isInputInhibited()).thenReturn(Optional.of(true));
    when(client.getScreen()).thenReturn(Screen.valueOf(TEST_SCREEN));
    when(client.resetAlarm()).thenReturn(false);
    when(client.getCursorPosition()).thenReturn(Optional.of(CURSOR_POSITION));
    buildDefaultRTEConfig();
    rteSampler.addTestElement(configTestElement);
    rteSampler.setPayload(buildInputs());
    rteSampler.getThreadContext().getVariables().incIteration();
  }

  private void buildDefaultRTEConfig() {
    configTestElement.setProperty(RTESampler.CONFIG_SERVER, "server");
    configTestElement.setProperty(RTESampler.CONFIG_DEVNAME, "devName");
    configTestElement.setProperty(RTESampler.CONFIG_PORT, 23);
    configTestElement
        .setProperty(RTESampler.CONFIG_TERMINAL_TYPE, RTESampler.DEFAULT_TERMINAL_TYPE.getId());
    configTestElement.setProperty(RTESampler.CONFIG_PROTOCOL, RTESampler.DEFAULT_PROTOCOL.name());
    configTestElement.setProperty(RTESampler.CONFIG_SSL_TYPE, RTESampler.DEFAULT_SSL_TYPE.name());
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
    doThrow(e).when(client)
        .connect(any(), anyInt(), any(), any(), anyLong());
    assertSampleResult(rteSampler.sample(null), buildConnectTimeoutResultBuilder(e, null).build());
  }

  private RteSampleResultBuilder buildConnectTimeoutResultBuilder(Exception e,
      RteProtocolClient client) {
    return buildBaseSampleResultBuilder()
        .withSslType(SSLType.NONE)
        .withAction(Action.SEND_INPUT)
        .withTimeoutFailure(e, client);
  }

  private RteSampleResultBuilder buildBaseSampleResultBuilder() {
    return new RteSampleResultBuilder()
        .withLabel(rteSampler.getName())
        .withServer("server")
        .withPort(23)
        .withProtocol(Protocol.TN5250)
        .withTerminalType(new TerminalType("IBM-3179-2", new Dimension(80, 24)));
  }

  @Test
  public void shouldGetErrorSamplerResultWhenGetClientThrowInterruptedException() throws Exception {
    InterruptedException e = new InterruptedException();
    doThrow(e).when(client)
        .connect(any(), anyInt(), any(), any(), anyLong());
    assertSampleResult(rteSampler.sample(null), buildErrorResultBuilder(e).build());
  }

  private RteSampleResultBuilder buildErrorResultBuilder(Exception e) {
    return buildBaseSampleResultBuilder()
        .withSslType(SSLType.NONE)
        .withAction(Action.SEND_INPUT)
        .withFailure(e);
  }

  @Test
  public void shouldGetErrorSamplerResultWhenSendThrowIllegalArgumentException() throws Exception {
    IllegalArgumentException e = new IllegalArgumentException();
    doThrow(e).when(client)
        .send(INPUTS, AttentionKey.ENTER, RTESampler.DEFAULT_CONNECTION_TIMEOUT_MILLIS);
    SampleResult expected = buildErrorResultBuilder(e)
        .withInputInhibitedRequest(true)
        .withInputs(INPUTS)
        .withAttentionKey(AttentionKey.ENTER)
        .build();
    SampleResult result = rteSampler.sample(null);
    // stack traces won't match since they are obtained from different parts of code, so we just 
    // truncate them
    truncateExceptionStacktrace(result);
    truncateExceptionStacktrace(expected);
    assertSampleResult(result, expected);
  }

  private void truncateExceptionStacktrace(SampleResult result) {
    String response = result.getResponseDataAsString();
    result.setResponseData(response.substring(0, response.indexOf('\n')),
        StandardCharsets.UTF_8.name());
  }

  @Test
  public void shouldGetErrorSampleResultWhenSendAwaitThrowsException() throws Exception {
    TimeoutException e = new TimeoutException();
    // we use a custom timeout to differentiate it from connection wait
    rteSampler.setWaitSyncTimeout(String.valueOf(CUSTOM_TIMEOUT_MILLIS));
    doThrow(e).
        when(client).await(Collections
        .singletonList(new SyncWaitCondition(CUSTOM_TIMEOUT_MILLIS,
            RTESampler.DEFAULT_STABLE_TIMEOUT_MILLIS)));
    assertSampleResult(rteSampler.sample(null), buildTimeoutErrorResult(e));
  }

  private SampleResult buildTimeoutErrorResult(Exception e) {
    return buildConnectTimeoutResultBuilder(e, client)
        .withInputInhibitedRequest(true)
        .withInputs(INPUTS)
        .withAttentionKey(AttentionKey.ENTER)
        .build();
  }

  @Test
  public void shouldGetSuccessfulSamplerResultWhenSend() {
    assertSampleResult(rteSampler.sample(null), buildSuccessfulSendInputResult(SSLType.NONE));
  }

  private SampleResult buildSuccessfulSendInputResult(SSLType sslType) {
    return buildBaseSampleResultBuilder()
        .withSslType(sslType)
        .withAction(Action.SEND_INPUT)
        .withAttentionKey(AttentionKey.ENTER)
        .withInputs(INPUTS)
        .withInputInhibitedRequest(true)
        .withSuccessResponse(client)
        .build();
  }

  @Test
  public void shouldGetSuccessfulSamplerWithResultAlarmHeaderResultWhenClientGetAlarmSignal() {
    when(client.isAlarmOn()).thenReturn(true);
    assertSampleResult(rteSampler.sample(null), buildSuccessfulSendInputResult(SSLType.NONE));
  }

  @Test
  public void shouldSendDefaultAttentionKeyToEmulatorWhenSampleWithoutSpecifyingAttentionKey()
      throws Exception {
    rteSampler.sample(null);
    verify(client)
        .send(anyList(), eq(AttentionKey.ENTER), anyLong());
  }

  @Test
  public void shouldSendCustomAttentionKeyToEmulatorWhenSampleWithCustomAttentionKey()
      throws Exception {
    rteSampler.setAttentionKey(AttentionKey.F1);
    rteSampler.sample(null);
    verify(client)
        .send(anyList(), eq(AttentionKey.F1), anyLong());
  }

  @Test
  public void shouldNotSendInputToEmulatorWhenSampleWithConnectAction() throws Exception {
    rteSampler.setAction(Action.CONNECT);
    rteSampler.sample(null);
    verify(client, never())
        .send(anyList(), any(AttentionKey.class), anyLong());
  }

  @Test
  public void shouldGetConnectActionResultWhenSampleWithConnectAction() {
    rteSampler.setAction(Action.CONNECT);
    assertSampleResult(rteSampler.sample(null), buildSuccessfulConnectResult());
  }

  private SampleResult buildSuccessfulConnectResult() {
    return buildBaseSampleResultBuilder()
        .withSslType(SSLType.NONE)
        .withAction(Action.CONNECT)
        .withSuccessResponse(client)
        .build();
  }

  @Test
  public void shouldDisconnectEmulatorWhenSampleWithDisconnectAction() throws Exception {
    connectClient();
    rteSampler.setAction(Action.DISCONNECT);
    rteSampler.sample(null);
    verify(client).disconnect();
  }

  @Test
  public void shouldDisconnectEmulatorWhenIterationStart() throws Exception {
    rteSampler.sample(null);
    rteSampler.iterationStart(null);
    verify(client).disconnect();
  }

  @Test
  public void shouldNotDisconnectEmulatorWhenIterationStartAndReuseConnectionsEnabled()
      throws Exception {
    rteSampler.setReuseConnections(true);
    try {
      rteSampler.sample(null);
      rteSampler.iterationStart(null);
      verify(client, never()).disconnect();
    } finally {
      rteSampler.setReuseConnections(false);
    }
  }

  private void connectClient() {
    RTESampler sampler = new RTESampler(p -> client);
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
    reset(client);
    rteSampler.sample(null);
    rteSampler.iterationStart(null);
    verify(client, never()).disconnect();
  }

  @Test
  public void shouldGetDisconnectActionResultWhenSampleWithDisconnectAction() {
    connectClient();
    rteSampler.setAction(Action.DISCONNECT);
    assertSampleResult(rteSampler.sample(null), buildDisconnectSuccessfulResult());
  }

  private SampleResult buildDisconnectSuccessfulResult() {
    return buildBaseSampleResultBuilder()
        .withSslType(SSLType.NONE)
        .withAction(Action.DISCONNECT)
        .withSuccessResponse(null)
        .build();
  }

  @Test
  public void shouldGetDisconnectActionResultWhenSampleWithDisconnectActionAndNoExistingConnection() {
    rteSampler.setAction(Action.DISCONNECT);
    assertSampleResult(rteSampler.sample(null), buildDisconnectSuccessfulResult());
  }

  @Test
  public void shouldGetErrorSamplerResultWhenDisconnectThrowRteIOException() throws Exception {
    connectClient();
    rteSampler.setAction(Action.DISCONNECT);
    RteIOException e = new RteIOException(null, "localhost");
    doThrow(e)
        .when(client).disconnect();
    assertSampleResult(rteSampler.sample(null), buildDisconnectErrorResult(e));
  }

  private SampleResult buildDisconnectErrorResult(Exception e) {
    return buildBaseSampleResultBuilder()
        .withSslType(SSLType.NONE)
        .withAction(Action.DISCONNECT)
        .withFailure(e)
        .build();
  }

  @Test
  public void shouldAwaitSyncWaiterWhenSendInputWithSyncWaitEnabled() throws Exception {
    rteSampler.sample(null);
    // we wait for 2 events since both connection wait and send input wait use same parameters
    verify(client, times(2))
        .await(Collections.singletonList(
            new SyncWaitCondition(RTESampler.DEFAULT_WAIT_SYNC_TIMEOUT_MILLIS,
                RTESampler.DEFAULT_STABLE_TIMEOUT_MILLIS)));
  }

  @Test
  public void shouldBuildSyncWaiterWithCustomWhenSyncWaitEnabled() throws Exception {
    rteSampler.setWaitSyncTimeout(String.valueOf(CUSTOM_TIMEOUT_MILLIS));
    rteSampler.setStableTimeout(CUSTOM_STABLE_TIMEOUT_MILLIS);
    rteSampler.sample(null);
    verify(client)
        .await(Collections.singletonList(
            new SyncWaitCondition(CUSTOM_TIMEOUT_MILLIS, CUSTOM_STABLE_TIMEOUT_MILLIS)));
  }

  @Test
  public void shouldAwaitOnlyConnectionSyncWhenNoWaitersAreEnabled() throws Exception {
    rteSampler.setWaitSync(false);
    rteSampler.sample(null);
    verify(client).await(any());
  }

  @Test
  public void shouldAwaitWithDefaultOrderConditionsWhenSampleAndWaitersHaveSameTimeout()
      throws Exception {
    rteSampler.setWaitSyncTimeout(String.valueOf(CUSTOM_TIMEOUT_MILLIS));
    rteSampler.setWaitCursor(true);
    rteSampler.setWaitCursorTimeout(String.valueOf(CUSTOM_TIMEOUT_MILLIS));
    rteSampler.sample(null);
    verify(client)
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
    verify(client)
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
    verify(client)
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
    verify(client)
        .await(Collections.singletonList(
            new SilentWaitCondition(CUSTOM_TIMEOUT_MILLIS, CUSTOM_STABLE_TIMEOUT_MILLIS)));
  }

  @Test
  public void shouldAwaitCursorWhenCursorWaitEnabled() throws Exception {
    rteSampler.setWaitSync(false);
    rteSampler.setWaitCursor(true);
    rteSampler.sample(null);
    verify(client)
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
    verify(client)
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
    verify(client)
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
    verify(client)
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
    verify(client)
        .connect(any(), anyInt(), eq(SSLType.TLS), any(), anyLong());
  }

  @Test
  public void shouldGetCustomSslHeaderWhenUsingCustomSsl() {
    rteSampler.setSslType(SSLType.TLS);
    assertSampleResult(rteSampler.sample(null), buildSuccessfulSendInputResult(SSLType.TLS));
  }

  @Test
  public void shouldSendInputsToProtocolClientWhenSampleAfterSetInputs() throws RteIOException {
    rteSampler.setAttentionKey(AttentionKey.ENTER);
    rteSampler.setInputs(INPUTS);
    rteSampler.sample(null);
    verify(client).send(INPUTS, AttentionKey.ENTER, RTESampler.DEFAULT_CONNECTION_TIMEOUT_MILLIS);
  }

}
