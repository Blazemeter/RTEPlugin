package com.blazemeter.jmeter.rte.sampler;

import com.blazemeter.jmeter.rte.core.Input;
import java.io.Serializable;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.testelement.property.StringProperty;

public abstract class InputTestElement extends AbstractTestElement implements Serializable {

  private static final String INPUT = "Input.input";

  public InputTestElement() {
  }

  public InputTestElement(String input) {
    setInput(input);
  }

  public String getInput() {
    /*
    We keep support for old property name to be backwards compatible with .jmx of previous plugin
    versions
     */
    String val = getPropertyAsString(INPUT, null);
    return val == null ? getPropertyAsString("CoordInputRowGUI.input") : val;
  }

  public void setInput(String input) {
    setProperty(new StringProperty(INPUT, input));
  }

  public abstract Input toInput();

  public abstract void copyOf(InputTestElement cellValue);

}
