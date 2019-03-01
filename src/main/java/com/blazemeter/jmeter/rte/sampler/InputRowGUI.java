package com.blazemeter.jmeter.rte.sampler;

import com.blazemeter.jmeter.rte.core.Input;
import java.io.Serializable;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.testelement.property.StringProperty;

public abstract class InputRowGUI extends AbstractTestElement implements Serializable {

  private static final String INPUT = "CoordInputRowGUI.input";

  public InputRowGUI() {
  }

  public InputRowGUI(String input) {
    setInput(input);
  }

  public String getInput() {
    return getPropertyAsString(INPUT);
  }

  public void setInput(String input) {
    setProperty(new StringProperty(INPUT, input));
  }

  public abstract Input toInput();

}
