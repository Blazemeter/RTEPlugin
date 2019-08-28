package com.blazemeter.jmeter.rte.extractor;

import com.blazemeter.jmeter.rte.core.Position;
import com.blazemeter.jmeter.rte.core.RteSampleResultBuilder;
import com.blazemeter.jmeter.rte.core.TerminalType;
import com.helger.commons.annotation.VisibleForTesting;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.jmeter.processor.PostProcessor;
import org.apache.jmeter.testelement.AbstractScopedTestElement;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterVariables;
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
  private static final Pattern END_TO_END_FIELD_POSITION_PATTERN = Pattern
      .compile("^\\[\\((\\d+),(\\d+)\\)-\\((\\d+),(\\d+)\\)]$");
  private JMeterContext context;

  public RTEExtractor() {

  }

  @Override
  public void process() {
    LOG.info("RTE-Extractor {}: processing result", getProperty(TestElement.NAME));
    context = context != null ? context : getThreadContext();
    JMeterVariables vars = context.getVariables();
    String variablePrefix = validateVariablePrefix(getVariablePrefix());
    Position position = extractPosition(context.getPreviousResult().getResponseHeaders());
    if (position != null && !variablePrefix.isEmpty()) {
      vars.put(variablePrefix + "_COLUMN", String.valueOf(position.getColumn()));
      vars.put(variablePrefix + "_ROW", String.valueOf(position.getRow()));
    }
  }

  private String validateVariablePrefix(String variablePrefix) {
    if (variablePrefix.isEmpty()) {
      LOG.error("The variable name in extractor is essential for later usage");
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

  private Position extractCursorPosition(String responseHeaders) {
    return Position.fromString(
        extractHeaderValue(RteSampleResultBuilder.CURSOR_POSITION_HEADER, responseHeaders));
  }

  private String extractHeaderValue(String headerName, String responseHeaders) {
    int startPosition = responseHeaders.indexOf(headerName) + headerName.length();
    int endPosition = responseHeaders
        .indexOf(RteSampleResultBuilder.HEADERS_SEPARATOR, startPosition);
    return responseHeaders.substring(startPosition, endPosition);
  }

  private Position extractFieldPosition(String responseHeaders) {

    if (isGivenFieldPositionValid(context.getPreviousResult().getRequestHeaders())) {

      String fieldsPositionAsText = extractHeaderValue(
          RteSampleResultBuilder.FIELDS_POSITION_HEADER, responseHeaders);

      List<String> fieldPositions = Arrays
          .stream(fieldsPositionAsText.split(RteSampleResultBuilder.FIELD_POSITION_SEPARATOR))
          .collect(Collectors.toList());

      Map<Position, Position> completeFieldPosition = getCompleteFieldMapPositions(fieldPositions);
      Position givenPosition = new Position(getRowAsInt(), getColumnAsInt());

      return getNewPosition(givenPosition, completeFieldPosition);

    } else {
      LOG.error("Inserted values for row and column in extractor\n"
          + "do not match with the screen size.");
      return null;
    }
  }

  private boolean isGivenFieldPositionValid(String requestHeaders) {
    return getBasePosition().isInside(getScreenDimensions(requestHeaders));
  }

  private Position getBasePosition() {
    return new Position(getRowAsInt(), getColumnAsInt());
  }

  private Dimension getScreenDimensions(String requestHeaders) {
    return TerminalType.fromString(extractTerminalType(requestHeaders)).getScreenSize();
  }

  private Map<Position, Position> getCompleteFieldMapPositions(List<String> fieldPositions) {
    Map<Position, Position> fieldPositionsMap = new LinkedHashMap<>();
    for (String segments : fieldPositions) {
      Matcher m = END_TO_END_FIELD_POSITION_PATTERN.matcher(segments);
      if (m.matches()) {
        fieldPositionsMap
            .put(new Position(Integer.parseInt(m.group(1)), Integer.parseInt(m.group(2))),
                new Position(Integer.parseInt(m.group(3)), Integer.parseInt(m.group(4))));
      } else {
        throw new IllegalArgumentException(
            "The text '" + segments + "' does not position field format");
      }
    }
    return fieldPositionsMap;
  }

  private Position getNewPosition(Position givenPosition,
      Map<Position, Position> fieldPositions) {
    int givenLinearPosition = buildLinealPosition(givenPosition);
    int offset = Integer.parseInt(getOffset());
    List<Position> keys = new ArrayList<>(fieldPositions.keySet());

    if (offset > 0) {
      Position nextField = getForwardPositionField(fieldPositions, givenLinearPosition);
      try {
        return keys.get(keys.indexOf(nextField) + offset - 1);
      } catch (IndexOutOfBoundsException e) {
        LOG.error("Number of fields in the screen was " + fieldPositions.size()
            + "\nTherefore is not possible to skip "
            + getOffset() + " fields");
        return null;
      }
    } else if (offset < 0) {

      Position position = getBackwardLinearPositionField(fieldPositions, givenLinearPosition);

      try {
        return keys.get(keys.indexOf(position) + offset + 1);
      } catch (IndexOutOfBoundsException e) {
        LOG.error("Number of fields in the screen was " + fieldPositions.size()
            + "\nTherefore is not possible to go backwards "
            + Math.abs(Integer.parseInt(getOffset())) + " fields");
        return null;
      }
    }

    return givenPosition;

  }

  private Position getBackwardLinearPositionField(Map<Position, Position> fieldsPositionsMap,
      int givenLinearPosition) {
    List<Position> reverseKeysOrder = new ArrayList<>(fieldsPositionsMap.keySet());
    Collections.reverse(reverseKeysOrder);
    for (Position key : reverseKeysOrder) {
      int linealStartFieldPosition = buildLinealPosition(key);
      int linealEndFieldPosition = buildLinealPosition(fieldsPositionsMap.get(key));

      if (linealStartFieldPosition == givenLinearPosition) {
        return key;
      } else if (givenLinearPosition < linealEndFieldPosition
          && givenLinearPosition > linealStartFieldPosition) {
        return reverseKeysOrder.get(reverseKeysOrder.indexOf(key) - 1);
      } else if (givenLinearPosition > linealEndFieldPosition) {
        return key;
      }
    }
    LOG.error("There are no fields position in the left side of the given position");
    return null;
  }

  private Position getForwardPositionField(Map<Position, Position> fieldsPositionMap,
      int givenLinearPosition) {
    Iterator<Map.Entry<Position, Position>> it = fieldsPositionMap.entrySet().iterator();
    List<Position> keys = new ArrayList<>(fieldsPositionMap.keySet());
    while (it.hasNext()) {
      Map.Entry<Position, Position> pairPos = it.next();
      int linealStartFieldPosition = buildLinealPosition(pairPos.getKey());
      int linealEndFieldPosition = buildLinealPosition(pairPos.getValue());

      if (linealStartFieldPosition == givenLinearPosition) {
        return keys.get(keys.indexOf(pairPos.getKey()) + 1);
      } else if (givenLinearPosition > linealStartFieldPosition
          && givenLinearPosition < linealEndFieldPosition) {
        LOG.warn("Given position in Extractor is in the middle of a field");
        return pairPos.getKey();

      } else if (givenLinearPosition < linealStartFieldPosition) {
        return pairPos.getKey();
      }
    }
    LOG.warn("There are not fields position in the right close of the given position");
    return null;
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
