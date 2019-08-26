package com.blazemeter.jmeter.rte.extractor;

import com.blazemeter.jmeter.rte.core.Position;
import com.blazemeter.jmeter.rte.core.RteSampleResultBuilder;
import com.blazemeter.jmeter.rte.core.TerminalType;
import com.helger.commons.annotation.VisibleForTesting;
import java.awt.Dimension;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.jmeter.processor.PostProcessor;
import org.apache.jmeter.testelement.AbstractScopedTestElement;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jmeter.util.JMeterUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RTEExtractor extends AbstractScopedTestElement implements PostProcessor {

  private static final Logger LOG = LoggerFactory.getLogger(RTEExtractor.class);

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
    context = context != null ? context : getThreadContext();
    JMeterVariables vars = context.getVariables();
    Position position = extractPosition(context.getPreviousResult().getResponseHeaders());
    String variablePrefix = validateVariablePrefix(getVariablePrefix());
    vars.put(variablePrefix + "_COLUMN", String.valueOf(position.getColumn()));
    vars.put(variablePrefix + "_ROW", String.valueOf(position.getRow()));
  }

  private String validateVariablePrefix(String variablePrefix) {
    if (variablePrefix.isEmpty()) {
      LOG.warn("The variable name in extractor is essential for later usage");
    }
    return variablePrefix;
  }

  private Position extractPosition(String responseHeaders) {
    if (getPositionType() == PositionType.CURSOR_POSITION) {
      return extractCursorPosition(responseHeaders);
    } else {
      return extractFieldPosition(responseHeaders);
    }
  }

  private Position extractFieldPosition(String responseHeaders) {
    Position effectivePosition = null;
    int startFieldsPosition =
        responseHeaders.indexOf(RteSampleResultBuilder.FIELDS_POSITION_HEADER)
            + RteSampleResultBuilder.FIELDS_POSITION_HEADER.length();
    int lastFieldsPosition = responseHeaders
        .indexOf(RteSampleResultBuilder.HEADERS_SEPARATOR, startFieldsPosition);

    String fieldsPositionAsText = responseHeaders
        .substring(startFieldsPosition, lastFieldsPosition);

    List<Position> fieldPositions = Arrays
        .stream(fieldsPositionAsText.split(RteSampleResultBuilder.FIELD_POSITION_SEPARATOR))
        .map(Position::fromString)
        .collect(Collectors.toList());

    String requestHeaders = context.getPreviousResult().getRequestHeaders();
    if (isGivenFieldPositionValid(requestHeaders)) {
      Position givenPosition = new Position(getRowAsInt(), getColumnAsInt());

      effectivePosition = getNewPosition(givenPosition, fieldPositions);

    } else {
      JMeterUtils.reportErrorToUser(
          "Inserted values for row and column in extractor\n"
              + "do not match with the screen size.");

    }

    return effectivePosition;
  }

  private Position getNewPosition(Position givenPosition,
      List<Position> fieldPositions) {
    int givenLinearPosition = buildLinealPosition(givenPosition);
    List<Integer> fieldsLinearPosition = fieldPositions.stream()
        .map(this::buildLinealPosition)
        .collect(Collectors.toList());
    int offset = Integer.parseInt(getOffset());

    if (offset > 0) {
      int nextField = getForwardLinearPositionField(fieldsLinearPosition, givenLinearPosition);
      try {
        return fieldPositions.get(fieldsLinearPosition.indexOf(nextField) + offset);
      } catch (ArrayIndexOutOfBoundsException e) {
        e.printStackTrace();
        LOG.warn("Number of fields in the screen was " + fieldPositions.size()
            + "\nTherefore is not possible to skip "
            + getOffset() + " fields");
      }
    } else if (offset < 0) {

      int nextField = getBackwardLinearPositionField(fieldsLinearPosition, givenLinearPosition);

      try {
        return fieldPositions.get(fieldsLinearPosition.indexOf(nextField) + offset);
      } catch (ArrayIndexOutOfBoundsException e) {
        e.printStackTrace();
        LOG.warn("Number of fields in the screen was " + fieldPositions.size()
            + "\nTherefore is not possible to go backwards "
            + Math.abs(Integer.parseInt(getOffset())) + " fields");
      }
    } else {
      int nextField = getForwardLinearPositionField(fieldsLinearPosition, givenLinearPosition);
      return fieldPositions.get(fieldsLinearPosition.indexOf(nextField) + offset);
    }
    return givenPosition;

  }

  private int getBackwardLinearPositionField(List<Integer> fieldsLinearPosition,
      int givenLinearPosition) {
    fieldsLinearPosition.sort(Collections.reverseOrder());
    for (Integer field : fieldsLinearPosition) {
      if (field == givenLinearPosition) {
        return field;
      } else if (field < givenLinearPosition) {
        return field;
      }
    }
    LOG.warn("There is not fields position in the right close of the given position");
    return -1;
  }

  private int getForwardLinearPositionField(List<Integer> fieldsLinearPosition,
      int givenLinearPosition) {
    for (Integer field : fieldsLinearPosition) {
      if (field == givenLinearPosition) {
        return field;
      } else if (field > givenLinearPosition) {
        return field;
      }
    }
    LOG.warn("There is not fields position in the right close of the given position");
    return -1;
  }

  private Position extractCursorPosition(String responseHeaders) {
    Position effectivePosition;
    int cursorPositionStart =
        responseHeaders.indexOf(RteSampleResultBuilder.CURSOR_POSITION_HEADER)
            + RteSampleResultBuilder.CURSOR_POSITION_HEADER.length();
    String cursorPositionAsText = responseHeaders
        .substring(cursorPositionStart,
            responseHeaders
                .indexOf(RteSampleResultBuilder.HEADERS_SEPARATOR, cursorPositionStart));
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
    return TerminalType.fromString(extractTerminalType(requestHeaders)).getScreenSize();
  }

  private String extractTerminalType(String requestHeaders) {
    int terminalTypeIndex =
        requestHeaders.indexOf(RteSampleResultBuilder.HEADERS_TERMINAL_TYPE)
            + RteSampleResultBuilder.HEADERS_TERMINAL_TYPE.length();
    int endOfScreenDimension = requestHeaders
        .indexOf(RteSampleResultBuilder.HEADERS_SEPARATOR, terminalTypeIndex);
    return requestHeaders.substring(terminalTypeIndex, endOfScreenDimension);
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

  private int buildLinealPosition(Position position) {
    Dimension size = getScreenDimensions(context.getPreviousResult().getRequestHeaders());
    return size.width * (position.getRow() - 1) + position.getColumn() - 1;
  }

  @VisibleForTesting
  public void setContext(JMeterContext context) {
    this.context = context;
  }
}
