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
import com.blazemeter.jmeter.rte.core.RequestListener;
import com.blazemeter.jmeter.rte.core.RteIOException;
import com.blazemeter.jmeter.rte.core.RteProtocolClient;
import com.blazemeter.jmeter.rte.core.TerminalType;
import com.blazemeter.jmeter.rte.core.ssl.SSLData;
import com.blazemeter.jmeter.rte.core.ssl.SSLType;
import com.blazemeter.jmeter.rte.core.wait.Area;
import com.blazemeter.jmeter.rte.core.wait.CursorWaitCondition;
import com.blazemeter.jmeter.rte.core.wait.SilentWaitCondition;
import com.blazemeter.jmeter.rte.core.wait.SyncWaitCondition;
import com.blazemeter.jmeter.rte.core.wait.TextWaitCondition;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.TimeoutException;
import kg.apc.emulators.TestJMeterUtils;
import org.apache.commons.io.FileUtils;
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
  private static final String TEST_SCREEN = "Test screen";
  private static final String CUSTOM_SSL_KEY_STORE = "/apache-jmeter4.0/ssl/cert.keystore";
  private static final String CUSTOM_SSL_KEY_STORE_PASSWORD = "pwd123";
  private static final String BASE_REQUEST_HEADERS_FORMAT = "Server: server\n"
      + "Port: 23\n"
      + "Protocol: TN5250\n"
      + "Terminal-type: IBM-3179-2: 24x80 color display\n"
      + "Security: %s\n";
  private static final String REQUEST_HEADERS_FORMAT = BASE_REQUEST_HEADERS_FORMAT
      + "Input-inhibited: true\n";
  private static final String REQUEST_BODY = "Action: ENTER\n"
      + "Inputs (Row,Column,Value):\n"
      + "1,1,input\n";

  @Mock
  private RteProtocolClient rteProtocolClientMock;
  @Mock
  private RequestListener requestListenerMock;
  private RTESampler rteSampler;
  private ConfigTestElement configTestElement = new ConfigTestElement();

  @BeforeClass
  public static void setupClass() {
    TestJMeterUtils.createJmeterEnv();
  }

  @AfterClass
  public static void tearDownClass() throws IOException {
    FileUtils.deleteDirectory(new File(TestJMeterUtils.getTempDir()));
  }

  @SuppressWarnings("unchecked")
  @Before
  public void setup() {
    rteSampler = new RTESampler(p -> rteProtocolClientMock);
    when(rteProtocolClientMock.isInputInhibited()).thenReturn(true, false);
    when(rteProtocolClientMock.getScreen()).thenReturn(TEST_SCREEN);
    when(rteProtocolClientMock.buildRequestListener()).thenReturn(requestListenerMock);
    when(requestListenerMock.getEndTime()).thenReturn((long) 100);
    when(requestListenerMock.getLatency()).thenReturn((long) 100);
    when(rteProtocolClientMock.getCursorPosition()).thenReturn(new Position(1, 1));
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
    doThrow(e).when(rteProtocolClientMock)
        .connect(any(), anyInt(), any(), any(), anyLong(), anyLong());
    assertSampleResult(rteSampler.sample(null), createExpectedErrorResult(e,
        BASE_REQUEST_HEADERS_FORMAT));
  }

  @Test
  public void shouldGetErrorSamplerResultWhenGetClientThrowInterruptedException() throws Exception {
    InterruptedException e = new InterruptedException();
    doThrow(e).when(rteProtocolClientMock)
        .connect(any(), anyInt(), any(), any(), anyLong(), anyLong());
    assertSampleResult(rteSampler.sample(null), createExpectedErrorResult(e,
        BASE_REQUEST_HEADERS_FORMAT));
  }

  private SampleResult createExpectedErrorResult(Exception e, String requestHeadersFormat) {
    SampleResult expected = new SampleResult();
    expected.setSampleLabel(rteSampler.getName());
    StringWriter sw = new StringWriter();
    e.printStackTrace(new PrintWriter(sw));
    expected.setRequestHeaders(String.format(requestHeadersFormat, SSLType.NONE));
    expected.setSamplerData(REQUEST_BODY);
    expected.setSuccessful(false);
    expected.setResponseCode(e.getClass().getName());
    expected.setResponseMessage(e.getMessage());
    expected.setDataType(SampleResult.TEXT);
    expected.setResponseData(sw.toString(), SampleResult.DEFAULT_HTTP_ENCODING);
    return expected;
  }

  private void assertSampleResult(SampleResult result, SampleResult expected) {
    assertThat(result)
        .isEqualToComparingOnlyGivenFields(expected, "sampleLabel", "requestHeaders", "samplerData",
            "successful", "responseCode", "responseMessage", "responseHeaders", "dataType",
            "responseData");
  }

  @Test
  public void shouldGetErrorSamplerResultWhenSendThrowIllegalArgumentException() throws Exception {
    IllegalArgumentException e = new IllegalArgumentException();
    doThrow(e).when(rteProtocolClientMock)
        .send(any(), any());
    assertSampleResult(rteSampler.sample(null),
        createExpectedErrorResult(e, REQUEST_HEADERS_FORMAT));
  }

  @Test
  public void shouldGetErrorSamplerResultWhenAwaitThrowsException() throws Exception {
    TimeoutException e = new TimeoutException();
    doThrow(e).
        when(rteProtocolClientMock).await(any());
    assertSampleResult(rteSampler.sample(null),
        createExpectedErrorResult(e, REQUEST_HEADERS_FORMAT));
  }

  @Test
  public void shouldGetSuccessfulSamplerResultWhenSend() {
    SampleResult result = rteSampler.sample(null);
    SampleResult expected = createExpectedSuccessfulResult(
        String.format(REQUEST_HEADERS_FORMAT, SSLType.NONE), REQUEST_BODY);
    assertSampleResult(result, expected);
  }

  private SampleResult createExpectedSuccessfulResult(String requestHeaders, String samplerData) {
    SampleResult expected = new SampleResult();
    expected.setSampleLabel(rteSampler.getName());
    expected.setRequestHeaders(requestHeaders);
    expected.setSamplerData(samplerData);
    expected.setSuccessful(true);
    expected.setResponseHeaders("Input-inhibited: false\n"
        + "Cursor-position: 1,1");
    expected.setDataType(SampleResult.TEXT);
    expected.setResponseData(TEST_SCREEN, "utf-8");
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
  public void shouldGetJustConnectResultWhenSampleWithJustConnect() {
    rteSampler.setJustConnect(true);
    assertSampleResult(rteSampler.sample(null),
        createExpectedSuccessfulResult(
            String.format(BASE_REQUEST_HEADERS_FORMAT, SSLType.NONE)
                + "Just-connect: true\n"
                + "Input-inhibited: true\n", ""));
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

  private SampleResult createExpectedErrorResultDisconnect(Exception e) {
    SampleResult expected = new SampleResult();
    expected.setSampleLabel(rteSampler.getName());
    expected.setRequestHeaders(String.format(REQUEST_HEADERS_FORMAT, SSLType.NONE));
    expected.setSamplerData(REQUEST_BODY);
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
  public void shouldAwaitSyncWaiterWhenSyncWaitEnabled() throws Exception {
    rteSampler.sample(null);
    verify(rteProtocolClientMock)
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
  public void shouldNotAwaitWhenNoWaitersAreEnabled() throws Exception {
    rteSampler.setWaitSync(false);
    rteSampler.sample(null);
    verify(rteProtocolClientMock, never())
        .await(any());
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
            new CursorWaitCondition(new Position(1, 1), CUSTOM_TIMEOUT_MILLIS,
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
            new CursorWaitCondition(new Position(1, 1), CUSTOM_TIMEOUT_MILLIS - 1,
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
        .await(Collections.singletonList(new CursorWaitCondition(new Position(1, 1),
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
  public void shouldConnectUsingSSLDataCustomValuesToEmulatorWhenKeyStorePropertiesEnabled()
      throws Exception {
    rteSampler.setSslType(SSLType.TLS);
    rteSampler.setKeyStore(CUSTOM_SSL_KEY_STORE);
    rteSampler.setKeyStorePassword(CUSTOM_SSL_KEY_STORE_PASSWORD);
    rteSampler.sample(null);
    verify(rteProtocolClientMock)
        .connect(any(), anyInt(),
            eq(new SSLData(SSLType.TLS, CUSTOM_SSL_KEY_STORE_PASSWORD, CUSTOM_SSL_KEY_STORE)),
            any(), anyLong(), anyLong());
  }

  @Test
  public void shouldGetCustomSslHeaderWhenUsingCustomSsl() {
    rteSampler.setSslType(SSLType.TLS);
    rteSampler.setKeyStore(CUSTOM_SSL_KEY_STORE);
    rteSampler.setKeyStorePassword(CUSTOM_SSL_KEY_STORE_PASSWORD);
    assertSampleResult(rteSampler.sample(null), createExpectedSuccessfulResult(
        String.format(REQUEST_HEADERS_FORMAT, SSLType.TLS), REQUEST_BODY));
  }

}
