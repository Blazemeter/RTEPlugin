package com.blazemeter.jmeter.rte.extractor;

import com.blazemeter.jmeter.rte.core.Position;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PositionRange {

  public static final int DEFAULT_WIDTH = 80;
  private static final Pattern END_TO_END_FIELD_POSITION_PATTERN = Pattern
      .compile("^\\[\\((\\d+),(\\d+)\\)-\\((\\d+),(\\d+)\\)]$");
  //serialization and deserialization like from string and so the like;
  private Position start;
  private Position end;

  public PositionRange(Position start, Position end) {
    this.start = start;
    this.end = end;
  }

  public static PositionRange fromStrings(String position) {

    Matcher m = END_TO_END_FIELD_POSITION_PATTERN.matcher(position);
    if (m.matches()) {
      return new PositionRange(
          new Position(Integer.parseInt(m.group(1)), Integer.parseInt(m.group(2))),
          new Position(Integer.parseInt(m.group(3)), Integer.parseInt(m.group(4))));

    } else {
      throw new IllegalArgumentException(
          "The text '" + position + "' does not match position field format");
    }
  }

  public Position getStart() {
    return start;
  }

  public boolean contains(Position position) {
    int givenLinealPos = Position.getLinealPosition(position);
    int startLinealPos = Position.getLinealPosition(start);
    int endLinealPos = Position.getLinealPosition(end);
    return startLinealPos < givenLinealPos && endLinealPos > givenLinealPos;
  }

  @Override
  public String toString() {
    return "[" + start + "-" + end + "]";
  }
}
