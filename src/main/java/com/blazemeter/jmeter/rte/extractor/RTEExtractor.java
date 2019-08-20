package com.blazemeter.jmeter.rte.extractor;

import com.blazemeter.jmeter.rte.core.Position;
import com.blazemeter.jmeter.rte.core.RteSampleResultBuilder;
import com.blazemeter.jmeter.rte.core.TerminalType;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;
import org.apache.jmeter.processor.PostProcessor;
import org.apache.jmeter.testelement.AbstractScopedTestElement;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jmeter.util.JMeterUtils;

public class RTEExtractor extends AbstractScopedTestElement implements PostProcessor {

  private static final String ROW_PROPERTY = "RTEExtractor.row";
  private static final String COLUMN_PROPERTY = "RTEExtractor.column";
  private static final String OFFSET_PROPERTY = "RTEExtractor.offset";
  private static final String VARIABLE_PREFIX_PROPERTY = "RTEExtractor.variablePrefix";
  private static final String POSITION_TYPE_PROPERTY = "RTEExtractor.positionType";
  private static final PositionType DEFAULT_POSITION_TYPE = PositionType.CURSOR_POSITION;

  private JMeterContext context;

  public RTEExtractor() {
    
  }

  @Override
  public void process() {
    context = getThreadContext();
    JMeterVariables vars = context.getVariables();
    Position position = extractPosition(context.getPreviousResult().getResponseHeaders());
    vars.put(getVariablePrefix() + "_COLUMN", String.valueOf(position.getColumn()));
    vars.put(getVariablePrefix() + "_ROW", String.valueOf(position.getRow()));
  }

  private Position extractPosition(String responseHeaders) {
    if (getPositionType() == PositionType.CURSOR_POSITION) {
      return extractCursorPosition(responseHeaders);
    } else {
      return getFieldPosition(responseHeaders);
    }
  }

  private Position getFieldPosition(String responseHeaders) {
    Position effectivePosition = null;
    int startFieldsPosition =
        responseHeaders.indexOf(RteSampleResultBuilder.FIELDS_POSITION_HEADER)
            + RteSampleResultBuilder.FIELDS_POSITION_HEADER.length();
    int lastFieldsPosition = responseHeaders
        .indexOf(RteSampleResultBuilder.HEADERS_SEPARATOR, startFieldsPosition);

    String fieldsPositionAsText = responseHeaders
        .substring(startFieldsPosition, lastFieldsPosition);

    List<Position> fieldPositions = new ArrayList<>();
    for (String field : fieldsPositionAsText
        .split(RteSampleResultBuilder.FIELD_POSITION_SEPARATOR)) {
      fieldPositions.add(Position.fromString(field));
    }

    try {
      if (isGivenFieldPositionValid(responseHeaders)) {
        effectivePosition = fieldPositions
            .get(fieldPositions
                .indexOf(new Position(Integer.parseInt(getRow()), Integer.parseInt(getColumn())))
                + Integer.parseInt(getOffset()));
      } else {
        JMeterUtils.reportErrorToUser(
            "Inserted values for row and column in extractor\n"
                + "do not match with the screen size.");
      }

    } catch (IndexOutOfBoundsException e) {
      JMeterUtils.reportErrorToUser(
          "Number of fields in the screen was " + fieldPositions.size()
              + "Therefore is not possible to skip "
              + getOffset() + " fields");
    }
    return effectivePosition;
  }

  private Position extractCursorPosition(String responseHeaders) {
    Position effectivePosition;
    int cursorPositionStart = responseHeaders.indexOf(RteSampleResultBuilder.CURSOR_POSITION_HEADER)
        + RteSampleResultBuilder.CURSOR_POSITION_HEADER.length();
    String cursorPositionAsText = responseHeaders
        .substring(cursorPositionStart,
            responseHeaders.indexOf(RteSampleResultBuilder.HEADERS_SEPARATOR));
    effectivePosition = Position.fromString(cursorPositionAsText);
    return effectivePosition;
  }

  private boolean isGivenFieldPositionValid(String responseHeaders) {
    Dimension screenSize = extractScreenDimensions(responseHeaders);
    return (getRowAsInt() <= screenSize.height && getRowAsInt() >= 1) && (
        getColumnAsInt() <= screenSize.width
            && getColumnAsInt() >= 1);
  }

  private Dimension extractScreenDimensions(String responseHeaders) {
    int terminalTypeIndex =
        responseHeaders.indexOf(RteSampleResultBuilder.HEADERS_TERMINAL_TYPE)
            + RteSampleResultBuilder.HEADERS_TERMINAL_TYPE.length();
    String screenSizeAsText = responseHeaders
        .substring(responseHeaders.indexOf(": ", terminalTypeIndex) + 2,
            responseHeaders.indexOf(RteSampleResultBuilder.HEADERS_SEPARATOR, terminalTypeIndex));
    int rowSize = Integer.parseInt(screenSizeAsText.substring(0, screenSizeAsText.indexOf(
        TerminalType.SCREEN_SIZE_SEPARATOR)));
    int columnSize = Integer.parseInt(screenSizeAsText
        .substring(screenSizeAsText.indexOf(TerminalType.SCREEN_SIZE_SEPARATOR) + 1,
            screenSizeAsText.length() - 1));
    return new Dimension(columnSize, rowSize);
  }

  private int getRowAsInt() {
    return Integer.parseInt(getRow());
  }

  private int getColumnAsInt() {
    return Integer.parseInt(getColumn());
  }

  public String getRow() {
    return getPropertyAsString(ROW_PROPERTY);
  }

  public void setRow(String row) {
    setProperty(ROW_PROPERTY, row);
  }

  public String getColumn() {
    return getPropertyAsString(COLUMN_PROPERTY);
  }

  public void setColumn(String column) {
    setProperty(COLUMN_PROPERTY, column);
  }

  public String getOffset() {
    return getPropertyAsString(OFFSET_PROPERTY);
  }

  public void setOffset(String offset) {
    setProperty(OFFSET_PROPERTY, offset);
  }

  public String getVariablePrefix() {
    return getPropertyAsString(VARIABLE_PREFIX_PROPERTY);
  }

  public void setVariablePrefix(String prefix) {
    setProperty(VARIABLE_PREFIX_PROPERTY, prefix);
  }

  public PositionType getPositionType() {
    return PositionType
        .valueOf(getPropertyAsString(POSITION_TYPE_PROPERTY, DEFAULT_POSITION_TYPE.name()));
  }

  public void setPositionType(PositionType positionType) {
    setProperty(POSITION_TYPE_PROPERTY, positionType.name());
  }
}
