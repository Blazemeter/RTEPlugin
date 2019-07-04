package com.blazemeter.jmeter.rte.core;

import static org.assertj.core.api.Assertions.assertThat;

import com.blazemeter.jmeter.rte.core.ssl.SSLType;
import com.blazemeter.jmeter.rte.sampler.Action;
import java.awt.Dimension;
import java.util.Collections;
import java.util.List;
import org.junit.Test;

public class RteSampleResultTest {

  private static final Position CURSOR_POSITION = new Position(1, 1);
  private static final String EXPECTED_HEADERS_RESPONSE = "Input-inhibited: true\n" +
      "Cursor-position: 1,1";
  private static final String EXPECTED_HEADERS = "Server: Test Server\n" +
      "Port: 2123\n" +
      "Protocol: TN5250\n" +
      "Terminal-type: IBM-3179-2: 24x80\n" +
      "Security: NONE\n" +
      "Action: CONNECT\n";

  private static List<Input> CUSTOM_INPUTS = Collections
      .singletonList(new CoordInput(new Position(3, 2), "input"));

  @Test
  public void shouldGetConnectionInfoAndActionWhenGetRequestHeaders() {
    RteSampleResult rteSampleResult = buildBasicRTESampleResult();
    rteSampleResult.setInputInhibitedRequest(true);

    String expectedHeaders = EXPECTED_HEADERS +
        "Input-inhibited: true\n";
    assertThat(rteSampleResult.getRequestHeaders()).isEqualTo(expectedHeaders);
  }

  private RteSampleResult buildBasicRTESampleResult() {
    RteSampleResult rteSampleResult = new RteSampleResult();
    rteSampleResult.setAction(Action.CONNECT);
    rteSampleResult.setProtocol(Protocol.TN5250);
    rteSampleResult.setTerminalType(new TerminalType("IBM-3179-2", new Dimension(80, 24)));
    rteSampleResult.setServer("Test Server");
    rteSampleResult.setPort(2123);
    rteSampleResult.setSslType(SSLType.NONE);
    rteSampleResult.setSoundedAlarm(true);
    rteSampleResult.setCursorPosition(CURSOR_POSITION);
    return rteSampleResult;
  }

  @Test
  public void shouldGetNoInputInhibitedHeaderWhenGetRequestHeadersWithNotSetInputInhibitedRequest() {
    RteSampleResult rteSampleResult = buildBasicRTESampleResult();
    rteSampleResult.setScreen(new Screen(new Dimension(10, 10)));
    assertThat(rteSampleResult.getRequestHeaders()).isEqualTo(EXPECTED_HEADERS);
  }

  @Test
  public void shouldGetEmptyStringWhenGetSamplerDataWithNoAttentionKey() {
    RteSampleResult rteSampleResult = buildBasicRTESampleResult();
    assertThat(rteSampleResult.getSamplerData()).isEqualTo("");
  }

  @Test
  public void shouldGetAttentionKeysAndInputsWhenGetSamplerDataWithAttentionKey() {
    RteSampleResult rteSampleResult = buildBasicRTESampleResult();
    rteSampleResult.setInputs(CUSTOM_INPUTS);
    rteSampleResult.setAttentionKey(AttentionKey.ENTER);

    String expectedSamplerData = "AttentionKey: ENTER\n" +
        "Inputs:\n" +
        "3,2,input\n";
    assertThat(rteSampleResult.getSamplerData()).isEqualTo(expectedSamplerData);
  }

  @Test
  public void shouldGetTerminalStatusHeadersWhenGetResponseHeadersWithNoDisconnectAction() {
    RteSampleResult rteSampleResult = buildBasicRTESampleResult();
    rteSampleResult.setInputInhibitedResponse(true);

    String expectedResponseHeaders = EXPECTED_HEADERS_RESPONSE + "\n" +
        "Sound-Alarm: true";
    assertThat(rteSampleResult.getResponseHeaders()).isEqualTo(expectedResponseHeaders);
  }

  @Test
  public void shouldGetEmptyStringWhenGetResponseHeadersWithDisconnectAction() {
    RteSampleResult rteSampleResult = buildBasicRTESampleResult();
    rteSampleResult.setAction(Action.DISCONNECT);
    assertThat(rteSampleResult.getResponseHeaders()).isEqualTo("");
  }

  @Test
  public void shouldGetNoSoundAlarmHeaderWhenGetResponseHeadersAndNoSoundAlarm() {
    RteSampleResult rteSampleResult = buildBasicRTESampleResult();
    rteSampleResult.setSoundedAlarm(false);
    rteSampleResult.setInputInhibitedResponse(true);

    assertThat(rteSampleResult.getResponseHeaders()).isEqualTo(EXPECTED_HEADERS_RESPONSE);
  }

  @Test
  public void shouldGetScreenTextWhenGetResponseData() {
    Screen screen = new Screen(new Dimension(30, 1));
    String screenText = "Testing screen text";
    screen.addSegment(0, screenText);

    RteSampleResult rteSampleResult = new RteSampleResult();
    rteSampleResult.setScreen(screen);

    assertThat(rteSampleResult.getResponseDataAsString()).isEqualTo(screenText);
  }

  @Test
  public void shouldGetEmptyStringWhenGetResponseDataWithoutScreen() {
    RteSampleResult rteSampleResult = buildBasicRTESampleResult();
    assertThat(rteSampleResult.getResponseDataAsString()).isEqualTo("");
  }

}
