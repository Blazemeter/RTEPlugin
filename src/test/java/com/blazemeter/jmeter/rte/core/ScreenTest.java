package com.blazemeter.jmeter.rte.core;

import static org.assertj.core.api.Assertions.assertThat;

import com.blazemeter.jmeter.rte.core.Screen.Segment;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import java.awt.Dimension;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.xmlunit.assertj.XmlAssert;

public class ScreenTest {

  private final int SCREEN_WIDTH = 5;
  private final int SCREEN_HEIGHT = 2;
  private final String WHITESPACES_FILLED_ROW = StringUtils.repeat(' ', (SCREEN_WIDTH));

  private Screen buildScreen() {
    return new Screen(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));
  }

  @Test
  public void shouldSplitRowWhenGetTextWithSegmentWithLengthBiggerThanScreenWidth() {
    Screen screen = buildScreen();
    screen.addSegment(0, WHITESPACES_FILLED_ROW + WHITESPACES_FILLED_ROW);
    assertThat(screen.getText())
        .isEqualTo(WHITESPACES_FILLED_ROW + "\n" + WHITESPACES_FILLED_ROW + "\n");
  }

  @Test
  public void shouldSplitRowWhenGetTextWithFinalSegmentPositionGreaterThanScreenWidth() {
    Screen screen = buildScreen();
    int offset = 3;
    screen.addSegment(0, StringUtils.repeat(' ', offset));
    screen.addSegment(offset, WHITESPACES_FILLED_ROW);
    screen.addSegment(offset, StringUtils.repeat(' ', SCREEN_WIDTH - offset));
    assertThat(screen.getText())
        .isEqualTo(WHITESPACES_FILLED_ROW + "\n" + WHITESPACES_FILLED_ROW + "\n");
  }

  @Test
  public void shouldGetScreenTextWithAddedFieldsAndSegmentsWhenGetText() {
    Screen screen = new Screen(new Dimension(SCREEN_WIDTH * 3, SCREEN_HEIGHT));
    String segmentText = "Name: ";
    screen.addSegment(0, segmentText);
    String fieldText = "TESTUSR";
    screen.addField(segmentText.length(), fieldText);
    String lastSegmentText = StringUtils
        .repeat(' ', SCREEN_WIDTH * 3 - (segmentText.length() + fieldText.length()));
    screen.addSegment(segmentText.length() + fieldText.length(), lastSegmentText);
    assertThat(screen.getText()).isEqualTo(segmentText + fieldText + lastSegmentText + "\n");
  }

  @Test
  public void shouldGetScreenTextWithInvisibleCharactersAsSpacesWhenGetText() {
    Screen screen = buildScreen();
    screen.addSegment(0, "T\u0000est");
    assertThat(screen.getText()).isEqualTo("T est\n");
  }

  @Test
  public void shouldGetAddedFieldsAndSegmentsWhenGetSegments() {
    Screen screen = new Screen(new Dimension(SCREEN_WIDTH * 2, SCREEN_HEIGHT));
    screen.addSegment(0, "S1: ");
    screen.addField(4, "F1");
    screen.addSegment(SCREEN_WIDTH * 2, "S2: ");
    screen.addField(SCREEN_WIDTH * 2 + 4, "F2");

    List<Segment> expectedSegments = new ArrayList<>();
    expectedSegments.add(new Screen.Segment(1, 1, "S1: ", false));
    expectedSegments.add(new Screen.Segment(1, 5, "F1", true));
    expectedSegments.add(new Screen.Segment(2, 1, "S2: ", false));
    expectedSegments.add(new Screen.Segment(2, 5, "F2", true));

    assertThat(screen.getSegments()).isEqualTo(expectedSegments);
  }

  @Test
  public void shouldGetScreenWithInvisibleCharsAsSpacesWhenWithInvisibleCharsAsSpaces() {
    Screen screen = buildScreen();
    screen.addSegment(0, "T\u0000est");

    Screen expectedScreen = buildScreen();
    expectedScreen.addSegment(0, "T est");
    assertThat(screen.withInvisibleCharsToSpaces()).isEqualTo(expectedScreen);
  }

  @Test
  public void shouldGetScreenWithTwoRowsWhenValueOfWithOneEnter() {
    assertThat(Screen.valueOf("Row1\nRow2").getText()).isEqualTo("Row1\nRow2\n");
  }

  @Test(expected = ArithmeticException.class)
  public void shouldThrowArithmeticExceptionWhenValueOfStringWithoutEnter() {
    Screen.valueOf("Row1");
  }

  @Test
  public void shouldGetScreenWithFieldsWhenFromHtml() throws Exception {
    assertThat(Screen.fromHtml(getHtmlTestScreenHtml())).isEqualTo(buildHtmlTestScreen());
  }

  private String getHtmlTestScreenHtml() throws IOException {
    return getFileContents("test-screen.html");
  }

  private String getFileContents(String fileName) throws IOException {
    return Resources.toString(getClass().getResource(fileName), Charsets.UTF_8);
  }

  private Screen buildHtmlTestScreen() {
    return buildHtmlTestScreenForUser("USR ");
  }

  private Screen buildHtmlTestScreenForUser(String user) {
    Screen expectedScreen = new Screen(new Dimension(10, 2));
    String initialSegmentText = "  Welcome User: ";
    expectedScreen.addSegment(0, initialSegmentText);
    expectedScreen.addField(initialSegmentText.length(), "USR ");
    return expectedScreen;
  }

  @Test
  public void shouldGetScreenHtmlWhenGetHtml() throws Exception {
    XmlAssert.assertThat(buildHtmlTestScreen().getHtml())
        .and(getHtmlTestScreenHtml())
        .areIdentical();
  }

  @Test
  public void shouldGetScreenHtmlWithInvisibleCharsAsSpacesWhenGetHtmlWithScreenWithInvisibleChars()
      throws Exception {
    XmlAssert.assertThat(buildHtmlTestScreenForUser("USR\u0000").getHtml())
        .and(getHtmlTestScreenHtml())
        .areIdentical();
  }

}
