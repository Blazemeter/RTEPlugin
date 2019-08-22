package com.blazemeter.jmeter.rte.extractor;

import com.blazemeter.jmeter.rte.core.Position;
import com.blazemeter.jmeter.rte.core.RteSampleResultBuilder;
import com.blazemeter.jmeter.rte.core.TerminalType;
import com.blazemeter.jmeter.rte.core.exceptions.InvalidFieldPositionException;
import com.helger.commons.annotation.VisibleForTesting;
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

  private boolean testing;
  private JMeterContext context;

  public RTEExtractor() {

  }

  @Override
  public void process() {
    context = testing ? context : getThreadContext();
    JMeterVariables vars = context.getVariables();
    Position position = extractPosition(context.getPreviousResult().getResponseHeaders());
    String variablePrefix = validateVariablePrefix(getVariablePrefix());
    vars.put(variablePrefix + "_COLUMN", String.valueOf(position.getColumn()));
    vars.put(variablePrefix + "_ROW", String.valueOf(position.getRow()));
  }

  private String validateVariablePrefix(String variablePrefix) {
    if (variablePrefix.isEmpty()) {
      JMeterUtils.reportErrorToUser("The variable name in extractor is essential for later usage");
    }
    return variablePrefix;
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
      if (isGivenFieldPositionValid(context.getPreviousResult().getRequestHeaders())) {

        int positionIndex = fieldPositions
            .indexOf(new Position(getRowAsInt(), getColumnAsInt()));
        if (positionIndex != -1) {
          effectivePosition = fieldPositions
              .get(positionIndex + Integer.parseInt(getOffset()));
        } else {
          String cause = "Inserted values for row/column in extractor\n"
              + "do not match with any field in current screen";
          JMeterUtils.reportErrorToUser(cause);

          throw new InvalidFieldPositionException(new Position(getRowAsInt(), getColumnAsInt()),
              new Throwable(cause));
        }
      } else {
        JMeterUtils.reportErrorToUser(
            "Inserted values for row and column in extractor\n"
                + "do not match with the screen size.");
      }

    } catch (IndexOutOfBoundsException e) {
      JMeterUtils.reportErrorToUser(
          "Number of fields in the screen was " + fieldPositions.size()
              + "\nTherefore is not possible to skip "
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
            responseHeaders.indexOf(RteSampleResultBuilder.HEADERS_SEPARATOR, cursorPositionStart));
    effectivePosition = Position.fromString(cursorPositionAsText);
    return effectivePosition;
  }

  private boolean isGivenFieldPositionValid(String requestHeaders) {
    Dimension screenSize = getScreenDimensions(requestHeaders);
    return (getRowAsInt() <= screenSize.height && getRowAsInt() >= 1) && (
        getColumnAsInt() <= screenSize.width
            && getColumnAsInt() >= 1);
  }

  private Dimension getScreenDimensions(String requestHeaders) {
    return TerminalType.fromString(extractScreenSize(requestHeaders));
  }

  private String extractScreenSize(String requestHeaders) {
    int terminalTypeIndex =
        requestHeaders.indexOf(RteSampleResultBuilder.HEADERS_TERMINAL_TYPE)
            + RteSampleResultBuilder.HEADERS_TERMINAL_TYPE.length();
    int endOfScreenDimension = requestHeaders
        .indexOf(RteSampleResultBuilder.HEADERS_SEPARATOR, terminalTypeIndex);
    String terminalTypeHeader = requestHeaders.substring(terminalTypeIndex, endOfScreenDimension);
    int lastNonBlankPosition = terminalTypeHeader.length() - 1;
    while (lastNonBlankPosition >= 0 && (terminalTypeHeader.charAt(lastNonBlankPosition) != ' ')) {
      lastNonBlankPosition--;
    }
    return terminalTypeHeader.substring(lastNonBlankPosition + 1);
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

  @VisibleForTesting
  public void setContext(JMeterContext context) {
    testing = true;
    this.context = context;
  }
}
