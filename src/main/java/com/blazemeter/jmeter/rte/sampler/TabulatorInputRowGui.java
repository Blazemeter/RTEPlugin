package com.blazemeter.jmeter.rte.sampler;

import com.blazemeter.jmeter.rte.core.Input;
import com.blazemeter.jmeter.rte.core.NavigationInput;
import org.apache.jmeter.testelement.property.StringProperty;

/*
This class has been deprecated since the addition of NavigationInput which is a more complex 
structure that can store, what we used to call TabulatorInput and also the new arrows navigation.
This class is currently needed for the proper deserialization of older TestPlans.
Once all the older TestPlans migrate, this class will be eliminated.
*/

@Deprecated
public class TabulatorInputRowGui extends InputTestElement {

  private static final String OFFSET_COLUMN = "TabulatorInputOffsetGUI.column";

  public String getOffset() {
    String val = getPropertyAsString(OFFSET_COLUMN, "1");
    return val == null ? getPropertyAsString("TabulatorInputOffsetGUI.column") : val;
  }

  public void setOffset(String offset) {
    setProperty(new StringProperty(OFFSET_COLUMN, offset));
  }

  @Override
  public Input toInput() {
    return new NavigationInput(Integer.valueOf(getOffset()), NavigationType.TAB, getInput());
  }

  @Override
  public void copyOf(InputTestElement cellValue) {
    if (cellValue instanceof TabulatorInputRowGui) {
      TabulatorInputRowGui source = (TabulatorInputRowGui) cellValue;
      setOffset(source.getOffset());
    }
  }
}
