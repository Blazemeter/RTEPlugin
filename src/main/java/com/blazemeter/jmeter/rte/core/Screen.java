package com.blazemeter.jmeter.rte.core;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;

public class Screen {

  private List<Segment> segments = new ArrayList<>();
  private Dimension size;

  // Provided for proper deserialization of sample results
  public Screen() {
  }

  public Screen(Dimension size) {
    this.size = size;
  }

  public static Screen valueOf(String screen) {
    int width = screen.indexOf('\n');
    int height = screen.length() / (width + 1);
    Screen ret = new Screen(new Dimension(width, height));
    int row = 0;
    for (String part : screen.split("\n")) {
      row++;
      ret.addSegment(row, 0, part);
    }
    return ret;
  }

  public List<Segment> getSegments() {
    return segments;
  }

  public void addSegment(int row, int column, String text) {
    segments.add(new Segment(row, column, text));
  }

  public void addField(int row, int column, String text) {
    segments.add(new Field(row, column, text));
  }

  @Override
  public String toString() {
    int currentLine = 1;
    StringBuilder screen = new StringBuilder();
    StringBuilder line = new StringBuilder();
    for (Segment segment : segments) {
      if (segment.row > currentLine) {
        if (line.length() > 0 && line.length() < size.width) {
          completeLine(line, screen);
          currentLine++;
          line = new StringBuilder();
        }
        while (segment.row > currentLine) {
          screen.append(StringUtils.repeat(' ', size.width));
          screen.append('\n');
          currentLine++;
        }
      }
      if (segment.column > line.length() + 1) {
        line.append(StringUtils.repeat(' ', segment.column - (line.length() + 1)));
      }
      String segmentText = segment.text;
      while (segmentText.length() >= (size.width - line.length())) {
        String lineSegmentText = segmentText.substring(0, size.width - line.length());
        line.append(lineSegmentText);
        line.append('\n');
        screen.append(line);
        currentLine++;
        line = new StringBuilder();
        segmentText = segmentText.substring(lineSegmentText.length());
      }
      if (!segmentText.isEmpty()) {
        line.append(segmentText);
      }
    }
    if (line.length() > 0 && line.length() < size.width) {
      completeLine(line, screen);
      currentLine++;
    }
    while (currentLine < size.height) {
      screen.append(StringUtils.repeat(' ', size.width));
      screen.append('\n');
      currentLine++;
    }
    // in tn5250 and potentially other protocols, the screen contains non visible characters which
    // are used as markers of no data or additional info. We replace them with spaces for better
    // visualization in text representation.
    return screen.toString().replace('\u0000', ' ');
  }

  private void completeLine(StringBuilder line, StringBuilder screen) {
    line.append(StringUtils.repeat(' ', size.width - line.length()));
    line.append('\n');
    screen.append(line);
  }

  public static class Segment {

    private int row;
    private int column;
    private String text;

    public Segment(int row, int column, String text) {
      this.text = text;
      this.row = row;
      this.column = column;
    }

    public int getRow() {
      return row;
    }

    public int getColumn() {
      return column;
    }

    public String getText() {
      return text;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }

      Segment segment = (Segment) o;
      return row == segment.row &&
              column == segment.column &&
              text.equals(segment.text);
    }

    @Override
    public int hashCode() {
      return Objects.hash(row, column, text);
    }

    @Override
    public String toString() {
      return "Segment{" +
              "row=" + row +
              ", column=" + column +
              ", text='" + text + '\'' +
              '}';
    }

  }

  public static class Field extends Segment {

    public Field(int row, int column, String text) {
      super(row, column, text);
    }
    
    @Override
    public String toString() {
      return "Field{" +
              "row=" + super.row +
              ", column=" + super.column +
              ", text='" + super.text + '\'' +
              '}';
    }
  }

}
