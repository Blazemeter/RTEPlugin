package com.blazemeter.jmeter.rte.core;

import com.blazemeter.jmeter.rte.sampler.Action;
import com.blazemeter.jmeter.rte.sampler.CoordInputRowGUI;
import com.blazemeter.jmeter.rte.sampler.Inputs;
import com.blazemeter.jmeter.rte.sampler.RTESampler;
import org.apache.jmeter.config.ConfigTestElement;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.awt.*;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

public class RteSampleResultTest {

    @Test
    public void shouldGetConnectionInfoAndActionWhenGetRequestHeaders(){
        RteSampleResult rteSampleResult = new RteSampleResult();
        rteSampleResult.setAction(Action.CONNECT);
        rteSampleResult.setServer("Test Server");
        rteSampleResult.setPort(2123);

        String expectedText = "Server: Test Server\n" +
                "Port: 2123\n" +
                "Protocol: null\n" +
                "Terminal-type: null\n" +
                "Security: null\n" +
                "Action: CONNECT\n";

        assertEquals(expectedText, rteSampleResult.getRequestHeaders());
    }

    @Test
    public void shouldGetNoInputInhibitedHeaderWhenGetRequestHeadersWithNotSetInputInhibitedRequest(){
        RteSampleResult rteSampleResult = new RteSampleResult();
        String expectedHeaders = "Server: null\n" +
                "Port: 0\n" +
                "Protocol: null\n" +
                "Terminal-type: null\n" +
                "Security: null\n" +
                "Action: null\n";

        assertEquals(expectedHeaders, rteSampleResult.getRequestHeaders());
    }

    @Test
    public void shouldGetEmptyStringWhenGetSamplerDataWithNoAttentionKey(){
        RteSampleResult rteSampleResult = new RteSampleResult();
        String expectedSamplerData = "";

        assertEquals(expectedSamplerData, rteSampleResult.getSamplerData());
    }

    @Test
    public void shouldGetTerminalStatusHeadersWhenGetResponseHeadersWithNoDisconnectAction(){
        RteSampleResult rteSampleResult = new RteSampleResult();
        rteSampleResult.setAction(Action.CONNECT);
        rteSampleResult.setSoundedAlarm(true);
        rteSampleResult.setCursorPosition(new Position(1,1));

        String expectedResponseHeaders = "Input-inhibited: false\n" +
                "Cursor-position: 1,1\n" +
                "Sound-Alarm: true";

        assertEquals(expectedResponseHeaders, rteSampleResult.getResponseHeaders());
    }

    @Test
    public void shouldGetEmptyStringWhenGetResponseHeadersWithDisconnectAction(){
        RteSampleResult rteSampleResult = new RteSampleResult();
        rteSampleResult.setAction(Action.DISCONNECT);
        String expectedResponseHeader = "";

        assertEquals(expectedResponseHeader, rteSampleResult.getResponseHeaders());
    }

    @Test
    public void shouldGetNoSoundAlarmHeaderWhenGetResponseHeadersAndNoSoundAlarm(){
        RteSampleResult rteSampleResult = new RteSampleResult();
        String expectedResponseHeader = "Input-inhibited: false\n" +
                "Cursor-position: ";

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
        RteSampleResult rteSampleResult = new RteSampleResult();
        String expectedResponseData = "";

        assertEquals(expectedResponseData, rteSampleResult.getResponseDataAsString());
    }

    @Test
    public void shouldGetAttentionKeysAndInputsWhenGetSamplerDataWithAttentionKey(){
        List<Input> customInputs = Collections
                .singletonList(new CoordInput(new Position(3, 2), "input"));

        RteSampleResult rteSampleResult = new RteSampleResult();
        rteSampleResult.setInputs(customInputs);
        rteSampleResult.setAttentionKey(AttentionKey.ENTER);
        rteSampleResult.setAction(Action.CONNECT);

        String expectedSamplerData = "AttentionKey: ENTER\n" +
                "Inputs:\n" +
                "3,2,input\n";

        assertEquals(expectedSamplerData, rteSampleResult.getSamplerData());
    }

}
