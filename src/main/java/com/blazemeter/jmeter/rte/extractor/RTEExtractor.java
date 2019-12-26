package com.blazemeter.jmeter.rte.extractor;

import com.blazemeter.jmeter.rte.core.Position;
import com.blazemeter.jmeter.rte.core.RteSampleResultBuilder;
import com.blazemeter.jmeter.rte.core.TerminalType;
import com.helger.commons.annotation.VisibleForTesting;
import java.awt.Dimension;
import java.util.Arrays;
import java.util.List;
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
  private static final int UNSPECIFIED_COORDS = 1;
  private static final int UNSPECIFIED_OFFSET = 0;
  private JMeterContext context;

  public RTEExtractor() {

  }

  @Override
  public void process() {
    LOG.info("RTE-Extractor {}: processing result", getProperty(TestElement.NAME));
    JMeterContext context = this.context != null ? this.context : getThreadContext();
    JMeterVariables vars = context.getVariables();
    String variablePrefix = getVariablePrefix();
    Position position = extractPosition(context.getPreviousResult().getResponseHeaders(),
        context.getPreviousResult().getRequestHeaders());
    if (position != null && !variablePrefix.isEmpty()) {
      vars.put(variablePrefix + "_COLUMN", String.valueOf(position.getColumn()));
      vars.put(variablePrefix + "_ROW", String.valueOf(position.getRow()));
    } else if (variablePrefix.isEmpty()) {
      LOG.error("The variable name in extractor is essential for later usage");
    }
  }

  private Position extractPosition(String responseHeaders, String requestHeaders) {
    if (getPositionType() == PositionType.CURSOR_POSITION) {
      return extractCursorPosition(responseHeaders);
    } else {
      return extractFieldPosition(responseHeaders, requestHeaders);
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

  private Position extractFieldPosition(String responseHeaders, String requestHeaders) {

    if (!isGivenFieldPositionValid(requestHeaders)) {
      LOG.error(
          "Inserted values for row and column {} in "
              + "extractor do not match with the screen size {}.",
          getBasePosition(), getScreenDimensions(requestHeaders));
      return null;
    }

    if (getOffsetAsInt() == 0) {
      return getBasePosition();
    }

    String fieldsPositionAsText = extractHeaderValue(
        RteSampleResultBuilder.FIELDS_POSITION_HEADER, responseHeaders);

    if (fieldsPositionAsText.isEmpty()) {
      LOG.error("No fields found in screen");
      return null;
    }

    List<PositionRange> fieldPositionRanges = Arrays
        .stream(fieldsPositionAsText.split(RteSampleResultBuilder.FIELD_POSITION_SEPARATOR))
        .map(PositionRange::fromStrings)
        .collect(Collectors.toList());

    return findField(getBasePosition(), fieldPositionRanges, getOffsetAsInt());

  }

  private int getOffsetAsInt() {
    return getOffset().isEmpty() ? UNSPECIFIED_OFFSET : Integer.parseInt(getOffset());
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

  private Position findField(Position basePosition, List<PositionRange> fieldPositionRanges,
      int offset) {
    int basePositionIndex = findBasePositionIndex(basePosition, fieldPositionRanges, offset);
    try {
      return fieldPositionRanges.get(basePositionIndex + offset).getStart();
    } catch (IndexOutOfBoundsException e) {
      LOG.error(
          "Couldn't find a field from {} with offset {} in screen fields {}",
          basePositionIndex, getOffsetAsInt(),
          fieldPositionRanges.stream().map(PositionRange::toString));
      return null;
    }
  }

  private int findBasePositionIndex(Position basePosition, List<PositionRange> fieldPositionRanges,
      int offset) {
    int index = 0;
    for (PositionRange fieldRange : fieldPositionRanges) {
      if (fieldRange.contains(basePosition)) {
        return index;
      } else if (basePosition.compare(fieldRange.getStart()) < 0) {
        return offset > 0 ? index - 1 : index;
      }
      index++;
    }
    return offset > 0 ? index - 1 : index;
  }

  private String extractTerminalType(String requestHeaders) {
    return extractHeaderValue(RteSampleResultBuilder.HEADERS_TERMINAL_TYPE, requestHeaders);
  }

  private int getRowAsInt() {
    return getRow().isEmpty() ? UNSPECIFIED_COORDS : Integer.parseInt(getRow());
  }

  private int getColumnAsInt() {
    return getColumn().isEmpty() ? UNSPECIFIED_COORDS : Integer.parseInt(getColumn());
  }

  public String getRow() {
    return getPropertyAsString(ROW_PROPERTY);
  }

  public void setRow(String row) {
    setProperty(ROW_PROPERTY, row, "1");
  }

  public String getColumn() {
    return getPropertyAsString(COLUMN_PROPERTY);
  }

  public void setColumn(String column) {
    setProperty(COLUMN_PROPERTY, column, "1");
  }

  public String getOffset() {
    return getPropertyAsString(OFFSET_PROPERTY);
  }

  public void setOffset(String offset) {
    setProperty(OFFSET_PROPERTY, offset, "0");
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
    this.context = context;
  }
}
