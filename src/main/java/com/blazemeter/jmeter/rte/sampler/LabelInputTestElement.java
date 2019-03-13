package com.blazemeter.jmeter.rte.sampler;

import com.blazemeter.jmeter.rte.core.Input;
import com.blazemeter.jmeter.rte.core.LabelInput;
import com.helger.commons.annotation.VisibleForTesting;
import org.apache.jmeter.testelement.property.StringProperty;

public class LabelInputTestElement extends InputTestElement {

  private static final String LABEL = "LabelInput.column";

  public LabelInputTestElement() {
  }

  @VisibleForTesting
  public LabelInputTestElement(String label, String input) {
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

  @Override
  public void copyOf(InputTestElement source) {
    if (source instanceof LabelInputTestElement) {
      LabelInputTestElement sourceCoords = (LabelInputTestElement) source;
      setLabel(sourceCoords.getLabel());
      setInput(sourceCoords.getInput());
    }
  }

}
