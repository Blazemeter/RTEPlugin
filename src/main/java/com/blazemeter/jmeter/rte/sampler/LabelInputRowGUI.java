package com.blazemeter.jmeter.rte.sampler;

import com.blazemeter.jmeter.rte.core.Input;
import com.blazemeter.jmeter.rte.core.LabelInput;
import org.apache.jmeter.testelement.property.StringProperty;

public class LabelInputRowGUI extends InputTestElement {

  private static final String LABEL = "LabelInputRowGUI.label";

  public LabelInputRowGUI() {
  }

  public String getLabel() {
    /*
    We keep support for initial incorrect property name to avoid compatibility issues. We should
    remove this once the new version gets used by all users (and the probability of someone using
    the incorrect property is low)
     */
    String val = getPropertyAsString(LABEL, null);
    return val == null ? getPropertyAsString("LabelInputRowGUI.column") : val;
  }

  public void setLabel(String label) {
    setProperty(new StringProperty(LABEL, label));
  }

  @Override
  public Input toInput() {
    return new LabelInput(getLabel(), getInput());
  }

  @Override
  public void copyOf(InputTestElement source) {
    if (source instanceof LabelInputRowGUI) {
      LabelInputRowGUI sourceCoords = (LabelInputRowGUI) source;
      setLabel(sourceCoords.getLabel());
      setInput(sourceCoords.getInput());
    }
  }

}
