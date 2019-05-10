package com.blazemeter.jmeter.rte.core;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import static org.junit.Assert.*;
import java.awt.*;
import java.util.List;


public class ScreenTest {

    private final int SCREEN_WIDTH  = 5;
    private final int SCREEN_HEIGHT = 2;

    public Screen buildScreen(){
        Screen screen  = new Screen(new Dimension(SCREEN_WIDTH,SCREEN_HEIGHT));

        return screen;
    }

    @Test
    public void shouldSplitRowWhenAddSegmentWithLengthBiggerThanScreenWidth() {

        Screen screen = buildScreen();
        screen.addSegment(1, 1, StringUtils.repeat(' ', (SCREEN_WIDTH * 2)));

        String expectedScreen = StringUtils.repeat(' ', (SCREEN_WIDTH)) + "\n"+StringUtils.repeat(' ', (SCREEN_WIDTH))+"\n";

        assertEquals(expectedScreen, screen.toString());
    }

    @Test
    public void shouldSplitRowWhenFinalSegmentPositionGreaterThanScreenWidth(){
        Screen screen  = buildScreen();

        screen.addSegment(1, 4, StringUtils.repeat(' ', SCREEN_WIDTH));

        String expectedScreen = StringUtils.repeat(' ', (SCREEN_WIDTH)) + "\n"+StringUtils.repeat(' ', (SCREEN_WIDTH))+"\n";

        assertEquals(expectedScreen,screen.toString());
    }

    @Test
    public void shouldGetCompleteRowWhenToStringWithAddSegmentShorterThanScreenWidth(){
        Screen screen  = buildScreen();

        screen.addSegment(1, 1, "Test");

        String expectedScreenText = "Test \n";

        assertEquals(expectedScreenText, screen.toString());
    }

    @Test
    public void shouldGetCompleteRowWhenAddSegmentWithEmptyString(){
        Screen screen  = buildScreen();

        screen.addSegment(1, 1, "");

        String expectedScreenText = StringUtils.repeat(' ', SCREEN_WIDTH)+"\n";

        assertEquals(expectedScreenText, screen.toString());
    }

    @Test
    public void shouldGetAddedFieldsAndSegmentsWhenGetSegments(){
        Screen screen  = new Screen(new Dimension(SCREEN_WIDTH*2,SCREEN_HEIGHT));

        String firstSegment  = "S1: ";
        String secondSegment = "S2: ";

        String firstField  = "F1";
        String secondField = "F2";

        screen.addSegment(1, 1, "S1: ");
        screen.addField(1,5,"F1");

        screen.addSegment(2, 1, "S2: ");
        screen.addField(2,5,"F2");

        List<Screen.Segment> segments = screen.getSegments();

        assertEquals(firstSegment, segments.get(0).getText());
        assertEquals(firstField, segments.get(1).getText());
        assertEquals(secondSegment, segments.get(2).getText());
        assertEquals(secondField, segments.get(3).getText());

        assertEquals("Segment", segments.get(0).getClass().getSimpleName());
        assertEquals("Field", segments.get(1).getClass().getSimpleName());
        assertEquals("Segment", segments.get(2).getClass().getSimpleName());
        assertEquals("Field", segments.get(3).getClass().getSimpleName());
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
        Screen screen  = new Screen(new Dimension(SCREEN_WIDTH ,SCREEN_HEIGHT));
        screen.addSegment(1,1, "Row1");

        assertEquals(screen.toString(), screen.valueOf("Row1"));
    }
}
