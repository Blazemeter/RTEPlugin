package com.blazemeter.jmeter.rte.sampler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.blazemeter.jmeter.rte.core.Action;
import com.blazemeter.jmeter.rte.core.Position;
import com.blazemeter.jmeter.rte.core.Protocol;
import com.blazemeter.jmeter.rte.core.RteProtocolClient;
import com.blazemeter.jmeter.rte.core.SSLType;
import com.blazemeter.jmeter.rte.core.TerminalType;
import com.blazemeter.jmeter.rte.core.wait.Area;
import com.blazemeter.jmeter.rte.core.wait.SilentWaitCondition;
import com.blazemeter.jmeter.rte.core.wait.SyncWaitCondition;
import com.blazemeter.jmeter.rte.core.wait.TextWaitCondition;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;
import java.util.concurrent.TimeoutException;
import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.util.JMeterUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class RTESamplerTest {

  private static final int CUSTOM_TIMEOUT_MILLIS = 3000;
  private static final int CUSTOM_STABLE_TIMEOUT_MILLIS = 500;

  @Mock
  private RteProtocolClient rteProtocolClientMock;
  private RTESampler rteSampler;
  private ConfigTestElement configTestElement = new ConfigTestElement();

  @Before
  public void setup() {
    rteSampler = new RTESampler(p -> rteProtocolClientMock);
    when(rteProtocolClientMock.getScreen()).thenReturn("Test screen");
    createDefaultRTEConfig();
    rteSampler.addTestElement(configTestElement);
    rteSampler.setPayload(createInputs());
  }

  private void createDefaultRTEConfig() {
    createRTEConfig("server", 23, RTESampler.DEFAULT_TERMINAL_TYPE, RTESampler.DEFAULT_PROTOCOL,
        "user", "pass", RTESampler.DEFAULT_SSLTYPE, "0");
  }

  private void createRTEConfig(String server, int port, TerminalType terminalType,
      Protocol protocol,
      String user, String pass, SSLType sslType, String connectionTimeout) {
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

  private void assertSampleResult(SampleResult result, SampleResult expected) {
    assertThat(result)
        .isEqualToComparingOnlyGivenFields(expected, "sampleLabel", "dataType", "responseCode",
            "responseMessage", "responseData", "successful");
  }

  @Test
  public void shouldGetErrorSamplerResultWhenSendThrowIllegalArgumentException() throws Exception {
    IllegalArgumentException e = new IllegalArgumentException();
    assertSampleResultWhenThrowSendException(e);
  }

  private void assertSampleResultWhenThrowSendException(Exception e) throws Exception {
    doThrow(e).when(rteProtocolClientMock)
        .send(any(), any(), any());
    SampleResult result = rteSampler.sample(null);
    SampleResult expected = createExpectedErrorResult(e);

    assertSampleResult(result, expected);
  }

  @Test
  public void shouldGetErrorSamplerResultWhenSendThrowInterruptedException() throws Exception {
    InterruptedException e = new InterruptedException();
    assertSampleResultWhenThrowSendException(e);

  }

  @Test
  public void shouldGetSuccessfulSamplerResultWhenSend() {
    String response = "Response";
    when(rteProtocolClientMock.getScreen()).thenReturn(response);
    SampleResult result = rteSampler.sample(null);
    SampleResult expected = createExpectedSuccessfulResult(response);
    assertSampleResult(result, expected);
  }

  private SampleResult createExpectedSuccessfulResult(String responseData) {
    SampleResult expected = new SampleResult();
    expected.setSampleLabel(rteSampler.getName());
    expected.setDataType(SampleResult.TEXT);
    expected.setResponseData(responseData, "utf-8");
    expected.setSuccessful(true);
    return expected;
  }

  @Test
  public void shouldSendSyncWaitConditionToEmulatorWhenSyncWaitEnabled() throws Exception {
    rteSampler.sample(null);
    verify(rteProtocolClientMock)
        .send(any(), eq(Action.ENTER), eq(Collections.singletonList(
            new SyncWaitCondition(RTESampler.DEFAULT_WAIT_SYNC_TIMEOUT_MILLIS,
                RTESampler.DEFAULT_STABLE_TIMEOUT_MILLIS))));
  }

  @Test
  public void shouldSendSyncWaitConditionWithCustomValuesToEmulatorWhenSyncWaitEnabled()
      throws Exception {
    rteSampler.setWaitSyncTimeout(String.valueOf(CUSTOM_TIMEOUT_MILLIS));
    rteSampler.setStableTimeout(CUSTOM_STABLE_TIMEOUT_MILLIS);
    rteSampler.sample(null);
    verify(rteProtocolClientMock)
        .send(any(), eq(Action.ENTER), eq(Collections.singletonList(
            new SyncWaitCondition(CUSTOM_TIMEOUT_MILLIS, CUSTOM_STABLE_TIMEOUT_MILLIS))));
  }

  @Test
  public void shouldNotSendWaitersToEmulatorWhenNoneAreEnabled() throws Exception {
    rteSampler.setWaitSync(false);
    rteSampler.sample(null);
    verify(rteProtocolClientMock)
        .send(any(), eq(Action.ENTER), eq(Collections.emptyList()));
  }

  @Test
  public void shouldSendSilentWaitConditionToEmulatorWhenSilentWaitEnabled() throws Exception {
    rteSampler.setWaitSync(false);
    rteSampler.setWaitSilent(true);
    rteSampler.sample(null);
    verify(rteProtocolClientMock)
        .send(any(), eq(Action.ENTER), eq(Collections.singletonList(
            new SilentWaitCondition(RTESampler.DEFAULT_WAIT_SILENT_TIMEOUT_MILLIS,
                RTESampler.DEFAULT_WAIT_SILENT_TIME_MILLIS))));
  }

  @Test
  public void shouldSendSilentConditionWithCustomValuesToEmulatorWhenSilentWaitEnabled()
      throws Exception {
    rteSampler.setWaitSync(false);
    rteSampler.setWaitSilent(true);
    rteSampler.setWaitSilentTimeout(String.valueOf(CUSTOM_TIMEOUT_MILLIS));
    rteSampler.setWaitSilentTime(String.valueOf(CUSTOM_STABLE_TIMEOUT_MILLIS));
    rteSampler.sample(null);
    verify(rteProtocolClientMock)
        .send(any(), eq(Action.ENTER), eq(Collections.singletonList(
            new SilentWaitCondition(CUSTOM_TIMEOUT_MILLIS, CUSTOM_STABLE_TIMEOUT_MILLIS))));
  }

  @Test
  public void shouldSendTextWaitConditionWhenWaitTextEnabled() throws Exception {
    rteSampler.setWaitSync(false);
    rteSampler.setWaitText(true);
    String regex = "test";
    rteSampler.setWaitTextRegex(regex);
    rteSampler.sample(null);
    verify(rteProtocolClientMock)
        .send(any(), eq(Action.ENTER), eq(Collections
            .singletonList(new TextWaitCondition(
                JMeterUtils.getPattern(regex),
                JMeterUtils.getMatcher(),
                Area.fromTopLeftBottomRight(1, 1, Position.UNSPECIFIED_INDEX,
                    Position.UNSPECIFIED_INDEX),
                RTESampler.DEFAULT_WAIT_TEXT_TIMEOUT_MILLIS,
                RTESampler.DEFAULT_STABLE_TIMEOUT_MILLIS))));
  }

  @Test
  public void shouldSendTextWaitConditionWithCustomValuesWhenWaitTextEnabled() throws Exception {
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
        .send(any(), eq(Action.ENTER), eq(Collections
            .singletonList(new TextWaitCondition(
                JMeterUtils.getPattern(regex),
                JMeterUtils.getMatcher(),
                Area.fromTopLeftBottomRight(areaTop, areaLeft, areaBottom, areaRight),
                CUSTOM_TIMEOUT_MILLIS,
                CUSTOM_STABLE_TIMEOUT_MILLIS))));
  }

}
