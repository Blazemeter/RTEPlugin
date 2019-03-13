package com.blazemeter.jmeter.rte.sampler;

import com.blazemeter.jmeter.rte.core.CoordInput;
import com.blazemeter.jmeter.rte.core.Input;
import com.blazemeter.jmeter.rte.core.Position;
import org.apache.jmeter.testelement.property.StringProperty;

public class CoordInputTestElement extends InputTestElement {

  private static final String COLUMN = "CoordInput.column";
  private static final String ROW = "CoordInput.row";

  private static final long serialVersionUID = 4525234536003480135L;

  public CoordInputTestElement() {
  }

  public CoordInputTestElement(String row, String column, String input) {
    super(input);
    setRow(row);
    setColumn(column);
  }

  public String getRow() {
    /*
    We keep support for old property name to be backwards compatible with .jmx of previous plugin
    versions
     */
    String val = getPropertyAsString(ROW, null);
    return val == null ? getPropertyAsString("CoordInputRowGUI.row", "1") : val;
  }

  public void setRow(String row) {
    setProperty(new StringProperty(ROW, row));
  }

  public String getColumn() {
    /*
    We keep support for old property name to be backwards compatible with .jmx of previous plugin
    versions
     */
    String val = getPropertyAsString(COLUMN, null);
    return val == null ? getPropertyAsString("CoordInputRowGUI.column", "1") : val;
  }

  public void setColumn(String column) {
    setProperty(new StringProperty(COLUMN, column));
  }

  public Input toInput() {
    return new CoordInput(new Position(Integer.parseInt(getRow()), Integer.parseInt(getColumn())),
        getInput());
  }

  @Override
  public void copyOf(InputTestElement source) {
    if (source instanceof CoordInputTestElement) {
      CoordInputTestElement sourceCoords = (CoordInputTestElement) source;
      setRow(sourceCoords.getRow());
      setColumn(sourceCoords.getColumn());
      setInput(sourceCoords.getInput());
    }
  }

}
