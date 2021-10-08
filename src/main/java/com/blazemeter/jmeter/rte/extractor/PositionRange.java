package com.blazemeter.jmeter.rte.extractor;

import com.blazemeter.jmeter.rte.core.Position;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PositionRange {

  private static final Pattern RANGE_POSITION_PATTERN = Pattern
      .compile("^\\[\\((\\d+),(\\d+)\\)-\\((\\d+),(\\d+)\\)]$");
  private Position start;
  private Position end;

  public PositionRange(Position start, Position end) {
    this.start = start;
    this.end = end;
  }

  public static PositionRange fromStrings(String positionRange) {

    Matcher m = RANGE_POSITION_PATTERN.matcher(positionRange);
    if (m.matches()) {
      return new PositionRange(
          new Position(Integer.parseInt(m.group(1)), Integer.parseInt(m.group(2))),
          new Position(Integer.parseInt(m.group(3)), Integer.parseInt(m.group(4))));

    } else {
      throw new IllegalArgumentException(
          "The text '" + positionRange + "' does not match positionRange field format");
    }
  }

  public Position getStart() {
    return start;
  }

  public Position getEnd() {
    return end;
  }

  public boolean contains(Position position) {
    return position.compare(start) >= 0 && position.compare(end) <= 0;
  }

  public int getStartLinealPosition(int width) {
    return width * (start.getRow() - 1) + start.getColumn() - 1;
  }

  @Override
  public String toString() {
    return "[" + start + "-" + end + "]";
  }
}
