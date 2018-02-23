package com.blazemeter.jmeter.rte.sampler;

import java.io.Serializable;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.testelement.property.StringProperty;

public class CoordInput extends AbstractTestElement implements Serializable {

  public static final String INPUT = "CoordInput.input";
  public static final String COLUMN = "CoordInput.column";
  public static final String ROW = "CoordInput.row";

  private static final long serialVersionUID = 4525234536003480135L;

  public CoordInput() {
  }

  public CoordInput(String input, String column, String row) {
    if (input != null) {
      setProperty(new StringProperty(INPUT, input));
    }
    if (column != null) {
      setProperty(new StringProperty(COLUMN, column));
    }
    if (row != null) {
      setProperty(new StringProperty(ROW, row));
    }
  }

  public String getInput() {
    return getPropertyAsString(INPUT);
  }

  public void setInput(String input) {
    setProperty(new StringProperty(INPUT, input));
  }

  public String getRow() {
    return getPropertyAsString(ROW);
  }

  public void setRow(String row) {
    setProperty(new StringProperty(ROW, row));
  }

  public String getColumn() {
    return getPropertyAsString(COLUMN);
  }

  public void setColumn(String column) {
    setProperty(new StringProperty(COLUMN, column));
  }

}
