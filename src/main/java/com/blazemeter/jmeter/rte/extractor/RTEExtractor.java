package com.blazemeter.jmeter.rte.extractor;

import com.blazemeter.jmeter.rte.core.Position;
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
    setJMeterVariables(vars);
  }

  private void setJMeterVariables(JMeterVariables vars) {
    String columnVariablePrefix = getVariablePrefix() + "_COLUMN";
    String rowVariablePrefix = getVariablePrefix() + "_ROW";
    Position position = extractPosition(context.getPreviousResult().getResponseHeaders());
    vars.put(columnVariablePrefix, String.valueOf(position.getColumn()));
    vars.put(rowVariablePrefix, String.valueOf(position.getRow()));
  }

  private Position extractPosition(String responseHeaders) {
    Position effectivePosition = null;
    if (getPositionType() == PositionType.CURSOR_POSITION) {

      String cursorDelimiter = "Cursor-position: ";
      String cursorPositionAsText = responseHeaders
          .substring(responseHeaders.indexOf(cursorDelimiter) + cursorDelimiter.length(),
              responseHeaders.indexOf(')') + 1);
      effectivePosition = Position.getPositionFromString(cursorPositionAsText);
    } else {
      String fieldDelimiter = "Field-positions: ";

      String fieldsPositionAsText = responseHeaders
          .substring(responseHeaders.indexOf(fieldDelimiter) + fieldDelimiter.length());

      List<Position> fieldPositions = new ArrayList<>();
      for (String field : fieldsPositionAsText.split(", ")) {
        fieldPositions.add(Position.getPositionFromString(field));
      }

      try {
        if (isGivenFieldPositionValid()) {
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

    }
    return effectivePosition;
  }

  private boolean isGivenFieldPositionValid() {
    String requestHeaders = context.getPreviousResult().getRequestHeaders();
    String screenSizeDelimiter = "Terminal-type: ";
    int terminalTypeIndex =
        requestHeaders.indexOf(screenSizeDelimiter) + screenSizeDelimiter.length();
    String screenSizeAsText = requestHeaders
        .substring(requestHeaders.indexOf(": ", terminalTypeIndex) + 2,
            requestHeaders.indexOf('\n', terminalTypeIndex));
    int rowSize = Integer.parseInt(screenSizeAsText.substring(0, screenSizeAsText.indexOf('x')));
    int columnSize = Integer.parseInt(screenSizeAsText
        .substring(screenSizeAsText.indexOf('x') + 1, screenSizeAsText.length() - 1));
    return (getRowAsInt() <= rowSize && getRowAsInt() >= 1) && (getColumnAsInt() <= columnSize
        && getColumnAsInt() >= 1);
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
