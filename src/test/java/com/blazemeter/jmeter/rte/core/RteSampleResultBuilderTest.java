package com.blazemeter.jmeter.rte.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.blazemeter.jmeter.rte.core.ssl.SSLType;
import com.blazemeter.jmeter.rte.sampler.Action;
import java.awt.Dimension;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class RteSampleResultBuilderTest {

  private static final Position CURSOR_POSITION = new Position(1, 1);
  private static final String EXPECTED_HEADERS_RESPONSE = "Input-inhibited: true\n" +
      "Cursor-position: (1,1)" + '\n';
  private static final String EXPECTED_HEADERS = "Server: Test Server\n" +
      "Port: 2123\n" +
      "Protocol: TN5250\n" +
      "Terminal-type: IBM-3179-2: 24x80\n" +
      "Security: NONE\n" +
      "Action: CONNECT\n";
  private static final String SCREEN_TEXT = "Testing screen text";
  private static final Screen SCREEN = buildScreen();

  private static Screen buildScreen() {
    Screen screen = new Screen(new Dimension(30, 1));
    screen.addSegment(0, SCREEN_TEXT);
    return screen;
  }

  private static final List<Input> CUSTOM_INPUTS = Collections
      .singletonList(new CoordInput(new Position(3, 2), "input"));

  @Mock
  private RteProtocolClient client;

  @Before
  public void setUp() {
    when(client.getScreen()).thenReturn(SCREEN);
    when(client.isAlarmOn()).thenReturn(true);
    when(client.isInputInhibited()).thenReturn(true);
    when(client.getCursorPosition()).thenReturn(Optional.of(CURSOR_POSITION));
  }

  @Test
  public void shouldGetConnectionInfoAndActionWhenGetRequestHeaders() {
    RteSampleResultBuilder resultBuilder = buildBasicResultBuilder()
        .withInputInhibitedRequest(true);

    String expectedHeaders = EXPECTED_HEADERS +
        "Input-inhibited: true\n";
    assertThat(resultBuilder.build().getRequestHeaders()).isEqualTo(expectedHeaders);
  }

  private RteSampleResultBuilder buildBasicResultBuilder() {
    return new RteSampleResultBuilder()
        .withAction(Action.CONNECT)
        .withProtocol(Protocol.TN5250)
        .withTerminalType(new TerminalType("IBM-3179-2", new Dimension(80, 24)))
        .withServer("Test Server")
        .withPort(2123)
        .withSslType(SSLType.NONE);
  }

  @Test
  public void shouldGetNoInputInhibitedHeaderWhenGetRequestHeadersWithNotSetInputInhibitedRequest() {
    RteSampleResultBuilder resultBuilder = buildBasicResultBuilder();
    assertThat(resultBuilder.build().getRequestHeaders()).isEqualTo(EXPECTED_HEADERS);
  }

  @Test
  public void shouldGetEmptyStringWhenGetSamplerDataWithNoAttentionKey() {
    RteSampleResultBuilder resultBuilder = buildBasicResultBuilder();
    assertThat(resultBuilder.build().getSamplerData()).isEqualTo("");
  }

  @Test
  public void shouldGetAttentionKeysAndInputsWhenGetSamplerDataWithAttentionKey() {
    RteSampleResultBuilder resultBuilder = buildBasicResultBuilder()
        .withInputs(CUSTOM_INPUTS)
        .withAttentionKey(AttentionKey.ENTER);
    String expectedSamplerData = "AttentionKey: ENTER\n" +
        "Inputs:\n" +
        "3,2,input\n";
    assertThat(resultBuilder.build().getSamplerData()).isEqualTo(expectedSamplerData);
  }

  @Test
  public void shouldGetTerminalStatusHeadersWhenGetResponseHeadersWithSuccessResponse() {
    RteSampleResultBuilder resultBuilder = buildBasicResultBuilder()
        .withSuccessResponse(client);
    String expectedResponseHeaders = EXPECTED_HEADERS_RESPONSE + "\n" +
        "Sound-Alarm: true" + "\n";
    assertThat(resultBuilder.build().getResponseHeaders()).isEqualTo(expectedResponseHeaders);
  }

  @Test
  public void shouldGetEmptyStringWhenGetResponseHeadersWithSuccessDisconnectAction() {
    RteSampleResultBuilder resultBuilder = buildBasicResultBuilder()
        .withAction(Action.DISCONNECT)
        .withSuccessResponse(null);
    assertThat(resultBuilder.build().getResponseHeaders()).isEqualTo("");
  }

  @Test
  public void shouldGetNoSoundAlarmHeaderWhenGetResponseHeadersAndNoSoundAlarm() {
    when(client.isAlarmOn()).thenReturn(false);
    RteSampleResultBuilder resultBuilder = buildBasicResultBuilder()
        .withSuccessResponse(client);
    assertThat(resultBuilder.build().getResponseHeaders()).isEqualTo(EXPECTED_HEADERS_RESPONSE);
  }

  @Test
  public void shouldGetScreenTextWhenGetResponseData() {
    RteSampleResultBuilder resultBuilder = buildBasicResultBuilder()
        .withSuccessResponse(client);
    assertThat(resultBuilder.build().getResponseDataAsString())
        .isEqualTo(StringUtils.rightPad(SCREEN_TEXT, SCREEN.getSize().width) + "\n");
  }

  @Test
  public void shouldGetEmptyStringWhenGetResponseDataWithoutScreen() {
    RteSampleResultBuilder resultBuilder = buildBasicResultBuilder()
        .withSuccessResponse(null);
    assertThat(resultBuilder.build().getResponseDataAsString()).isEqualTo("");
  }

}
