package com.blazemeter.jmeter.rte.sampler;

import com.blazemeter.jmeter.rte.core.Input;
import com.blazemeter.jmeter.rte.core.LabelInput;
import org.apache.jmeter.testelement.property.StringProperty;

public class LabelInputRowGUI extends InputRowGUI {

  private static final String LABEL = "LabelInputRowGUI.column";

  public LabelInputRowGUI() {
  }

  public LabelInputRowGUI(String label, String input) {
    super(input);
    setLabel(label);
  }

  public String getLabel() {
    return getPropertyAsString(LABEL, "");
  }

  public void setLabel(String label) {
    setProperty(new StringProperty(LABEL, label));
  }

  public Input toInput() {
    return new LabelInput(getLabel(), getInput());
  }

}
