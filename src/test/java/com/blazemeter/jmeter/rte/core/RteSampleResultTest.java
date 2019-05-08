package com.blazemeter.jmeter.rte.core;

import com.blazemeter.jmeter.rte.sampler.Action;
import com.blazemeter.jmeter.rte.sampler.CoordInputRowGUI;
import com.blazemeter.jmeter.rte.sampler.Inputs;
import com.blazemeter.jmeter.rte.sampler.RTESampler;
import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.samplers.SampleResult;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.awt.*;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;

public class RteSampleResultTest {
    /*
    private static final String TEST_SCREEN = "Test screen\n";
    private static final Position CURSOR_POSITION = new Position(1, 1);


    @Mock
    private RteProtocolClient rteProtocolClientMock;
    private RTESampler rteSampler;
    private ConfigTestElement configTestElement = new ConfigTestElement();

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
    */

    @Test
    public void shouldGetConnectionInfoAndActionWhenGetRequestHeaders(){
        RteSampleResult rteSampleResult = new RteSampleResult();

        rteSampleResult.setAction(Action.CONNECT);
        rteSampleResult.setServer("new Server");
        rteSampleResult.setPort(2323);

        String requestHeaders = rteSampleResult.getRequestHeaders();

        boolean containsAction = requestHeaders.contains("Action: CONNECT");
        boolean containsServer = requestHeaders.contains("Server: new Server");
        boolean containsPort   = requestHeaders.contains("Port: 2323");

        assertThat(containsAction && containsServer && containsPort);
    }

    @Test
    public void shouldGetNoInputInhibitedHeaderWhenGetRequestHeadersWithNotSetInputInhibitedRequest(){
        RteSampleResult rteSampleResult = new RteSampleResult();

        String responseHeaders = rteSampleResult.getResponseHeaders();

        assertThat(responseHeaders.contains("Input-inhibited: false"));
    }

    @Test
    public void shouldGetEmptyStringWhenGetSamplerDataWithNoAttentionKey(){
        RteSampleResult rteSampleResult = new RteSampleResult();

        String samplerData = rteSampleResult.getSamplerData();

        assertThat(samplerData.equals(""));
    }

    @Test
    public void shouldGetTerminalStatusHeadersWhenGetResponseHeadersWithNoDisconnectAction(){
        RteSampleResult rteSampleResult = new RteSampleResult();

        rteSampleResult.setAction(Action.CONNECT);
        rteSampleResult.setSoundedAlarm(true);
        rteSampleResult.setCursorPosition(new Position(1,1));

        String responseHeaders = rteSampleResult.getResponseHeaders();

        boolean hasCursorPosition = (responseHeaders.contains("Cursor-position: 1,1"));
        boolean hasAlarm          = (responseHeaders.contains("Sound-Alarm: true"));
        boolean hasInputInhibited = (responseHeaders.contains("Input-inhibited: false"));

        assertThat(hasAlarm && hasCursorPosition && hasInputInhibited);
    }

    @Test
    public void shouldGetEmptyStringWhenGetResponseHeadersWithDisconnectAction(){
        RteSampleResult rteSampleResult = new RteSampleResult();

        rteSampleResult.setAction(Action.DISCONNECT);

        String responseHeaders = rteSampleResult.getResponseHeaders();

        assertThat("".equals(responseHeaders));
    }

    @Test
    public void shouldGetNoSoundAlarmHeaderWhenGetResponseHeadersAndNoSoundAlarm(){
        RteSampleResult rteSampleResult = new RteSampleResult();

        String responseHeaders = rteSampleResult.getResponseHeaders();

        assertThat(!responseHeaders.contains("Sound-Alarm: true"));
    }

    @Test
    public void shouldGetScreenTextWhenGetResponseData(){
        RteSampleResult rteSampleResult = new RteSampleResult();


        Dimension size = new Dimension(30, 1);
        Screen screen = new Screen(size);

        screen.addSegment(1,1,"This new screen");

        rteSampleResult.setScreen(screen);

        String responseDataAsString = rteSampleResult.getResponseDataAsString();

        assertThat("This new screen".equals(responseDataAsString));
    }

    @Test
    public void shouldGetEmptyStringWhenGetResponseDataWithoutScreen(){
        RteSampleResult rteSampleResult = new RteSampleResult();

        String responseDataAsString = rteSampleResult.getResponseDataAsString();

        assertThat("".equals(responseDataAsString));
    }


    @Test
    public void shouldGetAttentionKeysAndInputsWhenGetSamplerDataWithAttentionKey(){

        List<Input> customInputs = Collections
                .singletonList(new CoordInput(new Position(3, 2), "input"));

        RteSampleResult rteSampleResult = new RteSampleResult();
        rteSampleResult.setInputs(customInputs);
        rteSampleResult.setAttentionKey(AttentionKey.ENTER);
        rteSampleResult.setAction(Action.CONNECT);

        String samplerData = rteSampleResult.getSamplerData();

        boolean hasAttentionKeys = (samplerData.equals("AttentionKey: ENTER"));
        boolean hasInputs = (samplerData.contains("3,2,input"));

        assertThat(hasAttentionKeys);
        assertThat(hasInputs);
    }

}
