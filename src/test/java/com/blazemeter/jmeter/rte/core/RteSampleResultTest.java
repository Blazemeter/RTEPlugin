package com.blazemeter.jmeter.rte.core;

import com.blazemeter.jmeter.rte.core.ssl.SSLType;
import com.blazemeter.jmeter.rte.sampler.Action;
import org.junit.Test;

import java.awt.*;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;


public class RteSampleResultTest {

    private static final Position CURSOR_POSITION = new Position(1, 1);

    @Test
    public void shouldGetConnectionInfoAndActionWhenGetRequestHeaders(){
        RteSampleResult rteSampleResult = buildBasicRTESampleResult();
        rteSampleResult.setInputInhibitedRequest(true);

        String expectedHeaders = "Server: Test Server\n" +
                "Port: 2123\n" +
                "Protocol: TN5250\n" +
                "Terminal-type: IBM-3179-2: 24x80\n" +
                "Security: NONE\n" +
                "Action: CONNECT\n"+
                "Input-inhibited: true\n";

        assertEquals(expectedHeaders, rteSampleResult.getRequestHeaders());
    }

    private RteSampleResult buildBasicRTESampleResult(){
        RteSampleResult rteSampleResult = new RteSampleResult();
        rteSampleResult.setAction(Action.CONNECT);
        rteSampleResult.setProtocol(Protocol.TN5250);
        rteSampleResult.setTerminalType(new TerminalType("IBM-3179-2", new Dimension(80, 24)));
        rteSampleResult.setServer("Test Server");
        rteSampleResult.setPort(2123);
        rteSampleResult.setSslType(SSLType.NONE);

        rteSampleResult.setCursorPosition(CURSOR_POSITION);

        return rteSampleResult;
    }

    @Test
    public void shouldGetNoInputInhibitedHeaderWhenGetRequestHeadersWithNotSetInputInhibitedRequest(){

        RteSampleResult rteSampleResult = buildBasicRTESampleResult();

        rteSampleResult.setSoundedAlarm(true);
        rteSampleResult.setCursorPosition(CURSOR_POSITION);
        rteSampleResult.setScreen(new Screen(new Dimension(10,10)));

        String expectedHeaders = "Server: Test Server\n" +
                "Port: 2123\n" +
                "Protocol: TN5250\n" +
                "Terminal-type: IBM-3179-2: 24x80\n" +
                "Security: NONE\n" +
                "Action: CONNECT\n";

        assertEquals(expectedHeaders, rteSampleResult.getRequestHeaders());
    }

    @Test
    public void shouldGetEmptyStringWhenGetSamplerDataWithNoAttentionKey(){
        RteSampleResult rteSampleResult = buildBasicRTESampleResult();

        assertEquals("", rteSampleResult.getSamplerData());
    }

    @Test
    public void shouldGetTerminalStatusHeadersWhenGetResponseHeadersWithNoDisconnectAction(){
        RteSampleResult rteSampleResult = buildBasicRTESampleResult();
        rteSampleResult.setSoundedAlarm(true);
        rteSampleResult.setInputInhibitedResponse(true);

        String expectedResponseHeaders = "Input-inhibited: true\n" +
                "Cursor-position: 1,1\n" +
                "Sound-Alarm: true";

        assertEquals(expectedResponseHeaders, rteSampleResult.getResponseHeaders());
    }

    @Test
    public void shouldGetEmptyStringWhenGetResponseHeadersWithDisconnectAction(){
        RteSampleResult rteSampleResult = buildBasicRTESampleResult();
        rteSampleResult.setSoundedAlarm(true);

        rteSampleResult.setAction(Action.DISCONNECT);
        String expectedResponseHeader = "";

        assertEquals(expectedResponseHeader, rteSampleResult.getResponseHeaders());
    }

    @Test
    public void shouldGetNoSoundAlarmHeaderWhenGetResponseHeadersAndNoSoundAlarm(){
        RteSampleResult rteSampleResult = buildBasicRTESampleResult();
        rteSampleResult.setInputInhibitedResponse(true);
        String expectedResponseHeader = "Input-inhibited: true\n" +
                "Cursor-position: 1,1";

        assertEquals(expectedResponseHeader, rteSampleResult.getResponseHeaders());
    }

    @Test
    public void shouldGetScreenTextWhenGetResponseData(){
        Screen screen = new Screen(new Dimension(30, 1));
        screen.addSegment(1,1, "Testing screen text");

        RteSampleResult rteSampleResult = new RteSampleResult();
        rteSampleResult.setScreen(screen);
        String expectedScreenText = "Testing screen text           \n";

        assertEquals(expectedScreenText, rteSampleResult.getResponseDataAsString());
    }

    @Test
    public void shouldGetEmptyStringWhenGetResponseDataWithoutScreen(){
        RteSampleResult rteSampleResult = buildBasicRTESampleResult();

        assertEquals("", rteSampleResult.getResponseDataAsString());
    }

    @Test
    public void shouldGetAttentionKeysAndInputsWhenGetSamplerDataWithAttentionKey(){
        List<Input> customInputs = Collections
                .singletonList(new CoordInput(new Position(3, 2), "input"));

        RteSampleResult rteSampleResult = buildBasicRTESampleResult();
        rteSampleResult.setInputs(customInputs);
        rteSampleResult.setAttentionKey(AttentionKey.ENTER);
        rteSampleResult.setAction(Action.CONNECT);

        String expectedSamplerData = "AttentionKey: ENTER\n" +
                "Inputs:\n" +
                "3,2,input\n";

        assertEquals(expectedSamplerData, rteSampleResult.getSamplerData());
    }
}