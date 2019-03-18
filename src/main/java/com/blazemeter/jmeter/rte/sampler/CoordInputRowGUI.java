package com.blazemeter.jmeter.rte.sampler;

import com.blazemeter.jmeter.rte.core.CoordInput;
import com.blazemeter.jmeter.rte.core.Input;
import com.blazemeter.jmeter.rte.core.Position;
import org.apache.jmeter.testelement.property.StringProperty;

public class CoordInputRowGUI extends InputTestElement {

  private static final String COLUMN = "CoordInputRowGUI.column";
  private static final String ROW = "CoordInputRowGUI.row";

  private static final long serialVersionUID = 4525234536003480135L;

  public CoordInputRowGUI() {
  }

  public CoordInputRowGUI(String row, String column, String input) {
    super(input);
    setRow(row);
    setColumn(column);
  }

  public String getRow() {
    return getPropertyAsString(ROW, "1");
  }

  public void setRow(String row) {
    setProperty(new StringProperty(ROW, row));
  }

  public String getColumn() {
    return getPropertyAsString(COLUMN, "1");
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
    if (source instanceof CoordInputRowGUI) {
      CoordInputRowGUI sourceCoords = (CoordInputRowGUI) source;
      setRow(sourceCoords.getRow());
      setColumn(sourceCoords.getColumn());
      setInput(sourceCoords.getInput());
    }
  }

}
