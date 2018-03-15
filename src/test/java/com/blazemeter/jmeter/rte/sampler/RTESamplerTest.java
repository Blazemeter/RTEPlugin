package com.blazemeter.jmeter.rte.sampler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.blazemeter.jmeter.rte.core.Action;
import com.blazemeter.jmeter.rte.core.Position;
import com.blazemeter.jmeter.rte.core.Protocol;
import com.blazemeter.jmeter.rte.core.RteIOException;
import com.blazemeter.jmeter.rte.core.RteProtocolClient;
import com.blazemeter.jmeter.rte.core.TerminalType;
import com.blazemeter.jmeter.rte.core.ssl.SSLData;
import com.blazemeter.jmeter.rte.core.ssl.SSLType;
import com.blazemeter.jmeter.rte.core.wait.Area;
import com.blazemeter.jmeter.rte.core.wait.ConditionWaiter;
import com.blazemeter.jmeter.rte.core.wait.CursorWaitCondition;
import com.blazemeter.jmeter.rte.core.wait.SilentWaitCondition;
import com.blazemeter.jmeter.rte.core.wait.SyncWaitCondition;
import com.blazemeter.jmeter.rte.core.wait.TextWaitCondition;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeoutException;
import kg.apc.emulators.TestJMeterUtils;
import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.util.JMeterUtils;
import org.junit.After;
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
  private static final String CUSTOM_SSL_KEY_STORE = "/apache-jmeter4.0/ssl/cert.keystore";
  private static final String CUSTOM_SSL_KEY_STORE_PASSWORD = "pwd123";

  @Mock
  private RteProtocolClient rteProtocolClientMock;
  @Mock
  private ConditionWaiter waiter1, waiter2;
  private RTESampler rteSampler;
  private ConfigTestElement configTestElement = new ConfigTestElement();

  @BeforeClass
  public static void setupClass() {
    TestJMeterUtils.createJmeterEnv();
  }

  @SuppressWarnings("unchecked")
  @Before
  public void setup() {
    rteSampler = new RTESampler(p -> rteProtocolClientMock);
    when(rteProtocolClientMock.getScreen()).thenReturn("Test screen");
    when(rteProtocolClientMock.buildConditionWaiters(any()))
        .thenReturn((List) Arrays.asList(waiter1, waiter2));
    createDefaultRTEConfig();
    rteSampler.addTestElement(configTestElement);
    rteSampler.setPayload(createInputs());
  }

  private void createDefaultRTEConfig() {
    createRTEConfig("server", 23, RTESampler.DEFAULT_TERMINAL_TYPE, RTESampler.DEFAULT_PROTOCOL,
        RTESampler.DEFAULT_SSLTYPE, "0");
  }

  private void createRTEConfig(String server, int port, TerminalType terminalType,
      Protocol protocol, SSLType sslType, String connectionTimeout) {
    configTestElement.setProperty(RTESampler.CONFIG_SERVER, server);
    configTestElement.setProperty(RTESampler.CONFIG_PORT, port);
    configTestElement
        .setProperty(RTESampler.CONFIG_TERMINAL_TYPE, terminalType.name());
    configTestElement.setProperty(RTESampler.CONFIG_PROTOCOL, protocol.name());
    configTestElement.setProperty(RTESampler.CONFIG_SSL_TYPE, sslType.name());
    configTestElement.setProperty(RTESampler.CONFIG_CONNECTION_TIMEOUT, connectionTimeout);
  }

  private Inputs createInputs() {
    Inputs ret = new Inputs();
    ret.addCoordInput(new CoordInputRowGUI(1, 1, "input"));
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
    assertSampleResultWhenThrowConnectException(e);
  }

  @Test
  public void shouldGetErrorSamplerResultWhenGetClientThrowInterruptedException() throws Exception {
    InterruptedException e = new InterruptedException();
    assertSampleResultWhenThrowConnectException(e);
  }

  private void assertSampleResultWhenThrowConnectException(Exception e) throws Exception {
    doThrow(e).when(rteProtocolClientMock)
        .connect(any(), anyInt(), any(), any(), anyLong(), anyLong());
    SampleResult result = rteSampler.sample(null);
    SampleResult expected = createExpectedErrorResult(e);

    assertSampleResult(result, expected);
  }

  private SampleResult createExpectedErrorResult(Exception e) {
    SampleResult expected = new SampleResult();
    expected.setSampleLabel(rteSampler.getName());
    StringWriter sw = new StringWriter();
    e.printStackTrace(new PrintWriter(sw));
    expected.setDataType(SampleResult.TEXT);
    expected.setResponseCode(e.getClass().getName());
    expected.setResponseMessage(e.getMessage());
    expected.setResponseData(sw.toString(), SampleResult.DEFAULT_HTTP_ENCODING);
    expected.setSuccessful(false);
    return expected;
  }

  private SampleResult createExpectedErrorResultDisconnect(Exception e) {
    SampleResult expected = new SampleResult();
    expected.setSampleLabel(rteSampler.getName());
    StringWriter sw = new StringWriter();
    e.printStackTrace(new PrintWriter(sw));
    expected.setDataType(SampleResult.TEXT);
    expected.setResponseCode(e.getClass().getName());
    expected.setResponseMessage(e.getMessage());
    expected.setRequestHeaders("Server: server\n"
        + "Port: 23\n"
        + "Protocol: TN5250\n"
        + "Terminal type: IBM-3179-2: 24x80 color display\n"
        + "Inhibited: false\n"
        + "\n"
        + "Action: ENTER\n"
        + "Inputs:\n"
        + "\n"
        + "Row 1 Column 1 = input\n");
    expected.setResponseData(sw.toString(), SampleResult.DEFAULT_HTTP_ENCODING);
    expected.setSuccessful(false);
    return expected;
  }

  private void assertSampleResult(SampleResult result, SampleResult expected) {
    assertThat(result)
        .isEqualToComparingOnlyGivenFields(expected, "sampleLabel", "dataType", "requestHeaders" ,"responseHeaders", "responseData", "successful", "responseCode", "responseMessage");
  }

  @Test
  public void shouldGetErrorSamplerResultWhenSendThrowIllegalArgumentException() throws Exception {
    IllegalArgumentException e = new IllegalArgumentException();
    doThrow(e).when(rteProtocolClientMock)
        .send(any(), any());
    assertSampleResult(rteSampler.sample(null), createExpectedErrorResult(e));
  }

  @Test
  public void shouldGetErrorSamplerResultWhenWaitThrowsException() throws Exception {
    TimeoutException e = new TimeoutException();
    doThrow(e).
        when(waiter1).await();
    assertSampleResult(rteSampler.sample(null), createExpectedErrorResult(e));
  }

  @Test
  public void shouldGetSuccessfulSamplerResultWhenSend() {
    String response = "Response";
    when(rteProtocolClientMock.getScreen()).thenReturn(response);
    when(rteProtocolClientMock.isInputInhibited()).thenReturn(true);
    when(rteProtocolClientMock.getCursorPosition()).thenReturn(new Position (1,1));
    SampleResult result = rteSampler.sample(null);
    SampleResult expected = createExpectedSuccessfulResult(response);
    assertSampleResult(result, expected);
  }

  private SampleResult createExpectedSuccessfulResult(String responseData) {
    SampleResult expected = new SampleResult();
    expected.setSampleLabel(rteSampler.getName());
    expected.setDataType(SampleResult.TEXT);
    expected.setResponseData(responseData, "utf-8");
    expected.setResponseHeaders("Inhibited: true\n"
        + "Cursor position: row 1 column 1");
    expected.setRequestHeaders("Server: server\n"
        + "Port: 23\n"
        + "Protocol: TN5250\n"
        + "Terminal type: IBM-3179-2: 24x80 color display\n"
        + "Inhibited: true\n"
        + "\n"
        + "Action: ENTER\n"
        + "Inputs:\n"
        + "\n"
        + "Row 1 Column 1 = input\n");
    expected.setSuccessful(true);
    return expected;
  }

  @Test
  public void shouldSendDefaultActionToEmulatorWhenSampleWithoutSpecifyingAction()
      throws Exception {
    rteSampler.sample(null);
    verify(rteProtocolClientMock)
        .send(any(), eq(Action.ENTER));
  }

  @Test
  public void shouldSendCustomActionToEmulatorWhenSampleWithCustomAction() throws Exception {
    rteSampler.setAction(Action.F1);
    rteSampler.sample(null);
    verify(rteProtocolClientMock)
        .send(any(), eq(Action.F1));
  }

  @Test
  public void shouldNotSendInputToEmulatorWhenSampleWithJustConnect() throws Exception {
    rteSampler.setJustConnect(true);
    rteSampler.sample(null);
    verify(rteProtocolClientMock, never())
        .send(any(), any());
  }

  @Test
  public void shouldDisconnectEmulatorWhenSampleWithDisconnect() throws Exception {
    rteSampler.setDisconnect(true);
    rteSampler.sample(null);
    verify(rteProtocolClientMock).disconnect();
  }

  @Test
  public void shouldGetErrorSamplerResultWhenDisconnectThrowRteIOException() throws Exception {
    rteSampler.setDisconnect(true);
    RteIOException e = new RteIOException(null);
    doThrow(e)
        .when(rteProtocolClientMock).disconnect();
    SampleResult result = rteSampler.sample(null);
    SampleResult expected = createExpectedErrorResultDisconnect(e);
    assertSampleResult(result, expected);
  }

  @Test
  public void shouldBuildSyncWaiterWhenSyncWaitEnabled() {
    rteSampler.sample(null);
    verify(rteProtocolClientMock)
        .buildConditionWaiters(Collections.singletonList(
            new SyncWaitCondition(RTESampler.DEFAULT_WAIT_SYNC_TIMEOUT_MILLIS,
                RTESampler.DEFAULT_STABLE_TIMEOUT_MILLIS)));
  }

  @Test
  public void shouldBuildSyncWaiterWithCustomWhenSyncWaitEnabled() {
    rteSampler.setWaitSyncTimeout(String.valueOf(CUSTOM_TIMEOUT_MILLIS));
    rteSampler.setStableTimeout(CUSTOM_STABLE_TIMEOUT_MILLIS);
    rteSampler.sample(null);
    verify(rteProtocolClientMock)
        .buildConditionWaiters(Collections.singletonList(
            new SyncWaitCondition(CUSTOM_TIMEOUT_MILLIS, CUSTOM_STABLE_TIMEOUT_MILLIS)));
  }

  @Test
  public void shouldBuildNoWaitersWhenNoneAreEnabled() {
    rteSampler.setWaitSync(false);
    rteSampler.sample(null);
    verify(rteProtocolClientMock)
        .buildConditionWaiters(Collections.emptyList());
  }

  @Test
  public void shouldWaitAndStopOnBuiltWaitersWhenSample() throws Exception {
    rteSampler.sample(null);
    verify(waiter1).await();
    verify(waiter2).await();
    verify(waiter1).stop();
    verify(waiter2).stop();
  }

  @Test
  public void shouldStopOnBuiltWaitersWhenSampleAndSomeWaiterWaitFails() throws Exception {
    doThrow(new TimeoutException())
        .when(waiter1).await();
    rteSampler.sample(null);
    verify(waiter1).stop();
    verify(waiter2).stop();
  }

  @Test
  public void shouldBuildSilentWaiterWhenSilentWaitEnabled() {
    rteSampler.setWaitSync(false);
    rteSampler.setWaitSilent(true);
    rteSampler.sample(null);
    verify(rteProtocolClientMock)
        .buildConditionWaiters(Collections.singletonList(
            new SilentWaitCondition(RTESampler.DEFAULT_WAIT_SILENT_TIMEOUT_MILLIS,
                RTESampler.DEFAULT_WAIT_SILENT_TIME_MILLIS)));
  }

  @Test
  public void shouldBuildSilentWaiterWithCustomValuesWhenSilentWaitEnabled() {
    rteSampler.setWaitSync(false);
    rteSampler.setWaitSilent(true);
    rteSampler.setWaitSilentTimeout(String.valueOf(CUSTOM_TIMEOUT_MILLIS));
    rteSampler.setWaitSilentTime(String.valueOf(CUSTOM_STABLE_TIMEOUT_MILLIS));
    rteSampler.sample(null);
    verify(rteProtocolClientMock)
        .buildConditionWaiters(Collections.singletonList(
            new SilentWaitCondition(CUSTOM_TIMEOUT_MILLIS, CUSTOM_STABLE_TIMEOUT_MILLIS)));
  }

  @Test
  public void shouldBuildCursorWaiterWhenCursorWaitEnabled() {
    rteSampler.setWaitSync(false);
    rteSampler.setWaitCursor(true);
    rteSampler.sample(null);
    verify(rteProtocolClientMock)
        .buildConditionWaiters(Collections.singletonList(
            new CursorWaitCondition(new Position(1, 1),
                RTESampler.DEFAULT_WAIT_CURSOR_TIMEOUT_MILLIS,
                RTESampler.DEFAULT_STABLE_TIMEOUT_MILLIS)));
  }

  @Test
  public void shouldBuildCursorWaiterWithCustomValuesWhenCursorWaitEnabled() {
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
        .buildConditionWaiters(Collections.singletonList(
            new CursorWaitCondition(new Position(customRow, customColumn), CUSTOM_TIMEOUT_MILLIS,
                CUSTOM_STABLE_TIMEOUT_MILLIS)));
  }

  @Test
  public void shouldBuildTextWaiterWhenWaitTextEnabled() {
    rteSampler.setWaitSync(false);
    rteSampler.setWaitText(true);
    String regex = "test";
    rteSampler.setWaitTextRegex(regex);
    rteSampler.sample(null);
    verify(rteProtocolClientMock)
        .buildConditionWaiters(Collections
            .singletonList(new TextWaitCondition(
                JMeterUtils.getPattern(regex),
                JMeterUtils.getMatcher(),
                Area.fromTopLeftBottomRight(1, 1, Position.UNSPECIFIED_INDEX,
                    Position.UNSPECIFIED_INDEX),
                RTESampler.DEFAULT_WAIT_TEXT_TIMEOUT_MILLIS,
                RTESampler.DEFAULT_STABLE_TIMEOUT_MILLIS)));
  }

  @Test
  public void shouldBuildTextWaiterWithCustomValuesWhenWaitTextEnabled() {
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
        .buildConditionWaiters(Collections
            .singletonList(new TextWaitCondition(
                JMeterUtils.getPattern(regex),
                JMeterUtils.getMatcher(),
                Area.fromTopLeftBottomRight(areaTop, areaLeft, areaBottom, areaRight),
                CUSTOM_TIMEOUT_MILLIS,
                CUSTOM_STABLE_TIMEOUT_MILLIS)));
  }

  @Test
  public void shouldConnectUsingSSLDataCustomValuesToEmulatorWhenKeyStorePropertiesEnabled()
      throws Exception {
    rteSampler.setKeyStore(CUSTOM_SSL_KEY_STORE);
    rteSampler.setKeyStorePassword(CUSTOM_SSL_KEY_STORE_PASSWORD);
    rteSampler.sample(null);
    verify(rteProtocolClientMock)
        .connect(any(), anyInt(),
            new SSLData(any(), CUSTOM_SSL_KEY_STORE_PASSWORD, CUSTOM_SSL_KEY_STORE),
            any(), anyLong(), anyLong());
  }

}
