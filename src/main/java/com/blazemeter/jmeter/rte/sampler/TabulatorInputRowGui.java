package com.blazemeter.jmeter.rte.sampler;

import com.blazemeter.jmeter.rte.core.Input;
import com.blazemeter.jmeter.rte.core.TabulatorInput;
import org.apache.jmeter.testelement.property.StringProperty;

public class TabulatorInputRowGui extends InputTestElement {

  private static final String OFFSET_COLUMN = "TabulatorInputOffsetGUI.column";

  //provided for proper serialization
  public TabulatorInputRowGui() {

  }

  public String getOffset() {
    String val = getPropertyAsString(OFFSET_COLUMN, "1");
    return val == null ? getPropertyAsString("TabulatorInputOffsetGUI.column") : val;
  }

  public void setOffset(String offset) {
    setProperty(new StringProperty(OFFSET_COLUMN, offset));
  }

  @Override
  public Input toInput() {
    return new TabulatorInput(Integer.valueOf(getOffset()), getInput());
  }

  @Override
  public void copyOf(InputTestElement cellValue) {
    if (cellValue instanceof TabulatorInputRowGui) {
      TabulatorInputRowGui source = (TabulatorInputRowGui) cellValue;
      setOffset(source.getOffset());
    }
  }
}
