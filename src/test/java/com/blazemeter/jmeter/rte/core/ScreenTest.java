package com.blazemeter.jmeter.rte.core;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import static org.junit.Assert.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;


public class ScreenTest {

    private final int SCREEN_WIDTH  = 5;
    private final int SCREEN_HEIGHT = 2;
    private final String WHITESPACES_FILLED_ROW = StringUtils.repeat(' ', (SCREEN_WIDTH));

    private Screen buildScreen(){
        return new Screen(new Dimension(SCREEN_WIDTH,SCREEN_HEIGHT));
    }

    @Test
    public void shouldSplitRowWhenAddSegmentWithLengthBiggerThanScreenWidth() {
        Screen screen = buildScreen();
        screen.addSegment(1, 1, WHITESPACES_FILLED_ROW + WHITESPACES_FILLED_ROW);
        String expectedScreen = WHITESPACES_FILLED_ROW + "\n"+WHITESPACES_FILLED_ROW+"\n";
        assertEquals(expectedScreen, screen.toString());
    }

    @Test
    public void shouldSplitRowWhenFinalSegmentPositionGreaterThanScreenWidth(){
        Screen screen  = buildScreen();
        screen.addSegment(1, 4, WHITESPACES_FILLED_ROW);
        String expectedScreen = WHITESPACES_FILLED_ROW +"\n"+WHITESPACES_FILLED_ROW+"\n";
        assertEquals(expectedScreen,screen.toString());
    }

    @Test
    public void shouldGetCompleteRowWhenToStringWithAddSegmentShorterThanScreenWidth(){
        Screen screen  = buildScreen();
        screen.addSegment(1, 1, "Test");
        assertEquals("Test \n", screen.toString());
    }

    @Test
    public void shouldGetCompleteRowWhenAddSegmentWithEmptyString(){
        Screen screen  = buildScreen();
        screen.addSegment(1, 1, "");
        String expectedScreenText = WHITESPACES_FILLED_ROW+"\n";
        assertEquals(expectedScreenText, screen.toString());
    }

    @Test
    public void shouldGetAddedFieldsAndSegmentsWhenGetSegments(){
        Screen screen  = new Screen(new Dimension(SCREEN_WIDTH*2,SCREEN_HEIGHT));

        screen.addSegment(1, 1, "S1: ");
        screen.addField(1,5,"F1");
        screen.addSegment(2, 1, "S2: ");
        screen.addField(2,5,"F2");

        List<Screen.Segment> expectedSegments = new ArrayList<>();
        expectedSegments.add(new Screen.Segment(1,1,"S1: ", false));
        expectedSegments.add(new Screen.Segment(1,5,"F1", true));
        expectedSegments.add(new Screen.Segment(2,1,"S2: ", false));
        expectedSegments.add(new Screen.Segment(2,5,"F2", true));

        assertEquals(expectedSegments, screen.getSegments());
    }

    @Test
    public void shouldGetScreenTextWithAddedFieldsAndSegmentsWhenToString(){
        Screen screen  = new Screen(new Dimension(SCREEN_WIDTH * 3 ,SCREEN_HEIGHT));
        screen.addSegment(1,1, "Name: ");
        screen.addField(1, 7, "TESTUSR");
        String expectedString = "Name: TESTUSR  \n";

        assertEquals(expectedString, screen.toString());
    }

    @Test
    public void shouldGetScreenWithTwoRowsWhenValueOfWithStringWithOneEnter(){
        Screen screen  = new Screen(new Dimension(SCREEN_WIDTH * 2 ,SCREEN_HEIGHT));
        screen.addSegment(1,1, "Row1\nRow2");
        String expectedScreen = "Row1\nRow2 \n";

        assertEquals(expectedScreen,screen.toString());
    }

    @Test(expected = ArithmeticException.class)
    public void shouldThrowArithmeticExceptionWhenValueOfStringWithoutEnter(){
        Screen.valueOf("Row1");
    }
}
