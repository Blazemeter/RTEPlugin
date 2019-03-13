package com.blazemeter.jmeter.rte.sampler;

import java.io.Serializable;
import java.util.ArrayList;
import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.testelement.property.CollectionProperty;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.testelement.property.PropertyIterator;
import org.apache.jmeter.testelement.property.TestElementProperty;

public class InputsTestElement extends ConfigTestElement implements Serializable,
    Iterable<JMeterProperty> {

  public static final String INPUTS_PROPERTY = "Inputs.inputs";
  private static final long serialVersionUID = 5810149938611069868L;

  public InputsTestElement() {
    setProperty(new CollectionProperty(INPUTS_PROPERTY, new ArrayList<>()));
  }

  public CollectionProperty getInputs() {
    return (CollectionProperty) getProperty(INPUTS_PROPERTY);
  }

  @Override
  public void clear() {
    super.clear();
    setProperty(new CollectionProperty(INPUTS_PROPERTY, new ArrayList<InputTestElement>()));
  }

  public void addInput(InputTestElement input) {
    TestElementProperty newInput = new TestElementProperty(input.getName(), input);
    if (isRunningVersion()) {
      this.setTemporary(newInput);
    }
    getInputs().addItem(newInput);
  }

  @Override
  public PropertyIterator iterator() {
    return getInputs().iterator();
  }

}
