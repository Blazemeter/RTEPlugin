package com.blazemeter.jmeter.rte.extractor;

import com.blazemeter.jmeter.rte.sampler.gui.BlazemeterLabsLogo;
import java.awt.BorderLayout;
import org.apache.jmeter.gui.JMeterGUIComponent;
import org.apache.jmeter.processor.gui.AbstractPostProcessorGui;
import org.apache.jmeter.testelement.TestElement;

public class RTEExtractorGui extends AbstractPostProcessorGui implements JMeterGUIComponent {

  private RTEExtractorPanel extractorPanel;

  public RTEExtractorGui() {
    extractorPanel = new RTEExtractorPanel();
    setLayout(new BorderLayout(0, 5));
    setBorder(makeBorder());
    add(makeTitlePanel(), BorderLayout.NORTH);
    add(extractorPanel, BorderLayout.CENTER);
    add(new BlazemeterLabsLogo(), BorderLayout.AFTER_LAST_LINE);
  }

  @Override
  public String getStaticLabel() {
    return "bzm - RTE Extractor";
  }

  @Override
  public String getLabelResource() {
    return null;
  }

  @Override
  public TestElement createTestElement() {
    RTEExtractor rteExtractor = new RTEExtractor();
    configure(rteExtractor);
    return rteExtractor;
  }

  @Override
  public void modifyTestElement(TestElement testElement) {

  }
}
