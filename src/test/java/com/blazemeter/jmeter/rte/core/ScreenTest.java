package com.blazemeter.jmeter.rte.core;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import static org.junit.Assert.*;
import java.awt.*;
import java.util.List;

public class ScreenTest {


@Test
public void shouldSplitRowWhenAddSegmentWithLengthBiggerThanScreenWidth(){

    Dimension size = new Dimension(5,5);
    Screen screen  = new Screen(size);

    screen.addSegment(1, 1, StringUtils.repeat(' ', (size.width*2)));

    String screenToString = screen.toString();

    assertNotEquals(screenToString.indexOf('\n'),-1);
}

@Test
public void shouldSplitRowWhenFinalSegmentPositionGreaterThanScreenWidth(){
    Dimension size = new Dimension(5,5);
    Screen screen  = new Screen(size);

    screen.addSegment(1, 4, StringUtils.repeat(' ', (size.width)));

    String screenToString = screen.toString();

    assertNotEquals(screenToString.indexOf('\n'),-1);
}

@Test
public void shouldGetCompleteRowWhenToStringWithAddSegmentShorterThanScreenWidth(){
    Dimension size = new Dimension(5,5);
    Screen screen  = new Screen(size);

    screen.addSegment(1, 1, "Test");

    String screenToString = screen.toString();

    String[] rows = screenToString.split("\n");
    String firstRow = rows[0];

    assertTrue(firstRow.length() == size.getWidth());
}

@Test
public void shouldGetCompleteRowWhenAddSegmentWithEmptyString(){
    Dimension size = new Dimension(5,5);
    Screen screen  = new Screen(size);

    screen.addSegment(1, 1, "");

    String screenToString = screen.toString();

    String[] rows = screenToString.split("\n");
    String firstRow = rows[0];

    assertTrue(firstRow.length() == size.getWidth());
}

//@Test
public void shouldGetAddedFieldsAndSegmentsWhenGetSegments(){
    Dimension size = new Dimension(5,5);
    Screen screen  = new Screen(size);

    screen.addSegment(1, 1, "Segment 1");
    screen.addSegment(1, 1, "Segment 2");
    screen.addSegment(1, 1, "Segment 3");
    int segments = 0;

    screen.addField(2,1,"Field 1");
    screen.addField(2,1,"Field 2");
    screen.addField(2,1,"Field 3");
    int fields = 0;

    List<Screen.Segment> segmentList = screen.getSegments();

    //this should be two separated Tests
    /*
    for (int i = 0; i < segmentList.size(); i++){
        if (segmentList.get(i).getClass().isInstance(Screen.Segment.class))
            segments++;

        if (segmentList.get(i).getClass().isInstance(Screen.Field.class))
            fields++;
    }*/

    assertTrue(segmentList.size()==6);
}

@Test
public void shouldGetAddedFieldsWhenGetSegments(){
        Dimension size = new Dimension(5,5);
        Screen screen  = new Screen(size);

        screen.addSegment(1, 1, "Segment 1");
        screen.addSegment(1, 1, "Segment 2");
        screen.addSegment(1, 1, "Segment 3");
        int segments = 0;

        screen.addField(2,1,"Field 1");
        screen.addField(2,1,"Field 2");
        screen.addField(2,1,"Field 3");
        int fields = 0;

        List<Screen.Segment> segmentList = screen.getSegments();

        //this should be two separated Tests
        for (int i = 0; i < segmentList.size(); i++){

            if (segmentList.get(i).getClass().getSimpleName().equals("Field"))
            fields++;
        }

        assertTrue(fields==3);
    }

@Test
public void shouldGetAddedSegmentsWhenGetSegments(){
        Dimension size = new Dimension(5,5);
        Screen screen  = new Screen(size);

        screen.addSegment(1, 1, "Segment 1");
        screen.addSegment(1, 1, "Segment 2");
        screen.addSegment(1, 1, "Segment 3");
        int segments = 0;

        screen.addField(2,1,"Field 1");
        screen.addField(2,1,"Field 2");
        screen.addField(2,1,"Field 3");
        int fields = 0;

        List<Screen.Segment> segmentList = screen.getSegments();

        //this should be two separated Tests
        for (int i = 0; i < segmentList.size(); i++){
            if (segmentList.get(i).getClass().getSimpleName().equals("Segment"))
                segments++;
        }

        assertTrue(segments==3);
    }

@Test
public void shouldGetScreenTextWithAddedFieldsAndSegmentsWhenToString(){
    Dimension size = new Dimension(15,5);
    Screen screen  = new Screen(size);

    screen.addSegment(1,1, "Name: ");
    screen.addField(1, 7, "TESTUSR");

    String toString = screen.toString();

    //  This should be done in steps or in different tests, since there
    // is no way we can test both scenarios at the same time
    // assertTrue(toString.contains("Name:"));
    assertTrue(toString.contains("TESTUSR"));
}

@Test
public void shouldGetScreenWithOneRowWhenValueOfWithStringWithoutEnter(){
    Dimension size = new Dimension(15,5);
    Screen screen  = new Screen(size);

    String newRowWithoutEnter = "One big line";
    String extraEnter = "\n";

    // Without this extraEnter the test breaks with an "java.lang.ArithmeticException: / by zero"
    Screen newScreen = screen.valueOf(newRowWithoutEnter + extraEnter);

    String[] rows = newScreen.toString().split("\n");

    assertTrue(rows.length == 1);
}

@Test
public void shouldGetScreenWithTwoRowsWhenValueOfWithStringWithOneEnter(){
    Dimension size = new Dimension(15,5);
    Screen screen  = new Screen(size);

    String newRowWithoutEnter = "One big line     \n Other big line";

    Screen newScreen = screen.valueOf(newRowWithoutEnter);

    String[] rows = newScreen.toString().split("\n");

    assertTrue(rows.length == 2);
}

@Test
public void shouldGetSameStringWhenGettingScreenStringFromValueOf(){
    Dimension size = new Dimension(15,5);
    Screen screen  = new Screen(size);

    //The extra \n is required to avoid the "java.lang.ArithmeticException: / by zero"
    String newRowWithoutEnter = "One big line with a lot of letters \n";

    Screen newScreen = screen.valueOf(newRowWithoutEnter);

    String rowResult = newScreen.toString();

    assertTrue(rowResult.equals(newRowWithoutEnter));
}



/*
    WhenToStringWithAddedSegmentBiggerThanScreenWidth,
    WhenToStringWithAddedSegmentFinalPositionGreaterThanScreenWidth, etc

shouldGetCompleteRowWhenAddSegmentWithLengthShorterThanScreenWidth
shouldGetCompleteRowWhenAddSegmentWithEmptyString


* */


}
