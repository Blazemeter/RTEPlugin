package com.blazemeter.jmeter.rte.sampler.gui;

import com.blazemeter.jmeter.rte.sampler.Inputs;
import com.blazemeter.jmeter.rte.sampler.RTESampler;
import java.awt.BorderLayout;
import org.apache.jmeter.samplers.gui.AbstractSamplerGui;
import org.apache.jmeter.testelement.TestElement;

public class RTESamplerGui extends AbstractSamplerGui {

  private static final long serialVersionUID = 4024916662489960067L;
  private RTESamplerPanel rteSamplerPanel;

  public RTESamplerGui() {
    super();
    init();
    initFields();

    setLayout(new BorderLayout(0, 5));
    setBorder(makeBorder());

    add(makeTitlePanel(), BorderLayout.NORTH);
    add(rteSamplerPanel, BorderLayout.CENTER);
  }

  @Override
  public String getStaticLabel() {
    return "RTE Sampler";
  }

  @Override
  public String getLabelResource() {
    throw new IllegalStateException("This shouldn't be called"); //$NON-NLS-1$
  }

  @Override
  public void configure(TestElement element) {
    super.configure(element);
    if (element instanceof RTESampler) {
      RTESampler rteSamplerTestElement = (RTESampler) element;
      rteSamplerPanel.setTypingStyle(rteSamplerTestElement.getTypingStyle());
      rteSamplerPanel.setDisconnect(rteSamplerTestElement.getDisconnect());
      rteSamplerPanel.setWaitCursor(rteSamplerTestElement.getWaitCursor());
      rteSamplerPanel.setWaitSilent(rteSamplerTestElement.getWaitSilent());
      rteSamplerPanel.setWaitSync(rteSamplerTestElement.getWaitSync());
      rteSamplerPanel.setWaitText(rteSamplerTestElement.getWaitText());
      rteSamplerPanel.setWaitTimeoutSync(rteSamplerTestElement.getWaitTimeoutSync());
      rteSamplerPanel.setWaitTimeoutCursor(rteSamplerTestElement.getWaitTimeoutCursor());
      rteSamplerPanel.setWaitTimeoutSilent(rteSamplerTestElement.getWaitTimeoutSilent());
      rteSamplerPanel.setWaitForSilent(rteSamplerTestElement.getWaitForSilent());
      rteSamplerPanel.setWaitTimeoutText(rteSamplerTestElement.getWaitTimeoutText());
      rteSamplerPanel.setCoordXWait(rteSamplerTestElement.getCoordXToWait());
      rteSamplerPanel.setCoordYWait(rteSamplerTestElement.getCoordYToWait());
      rteSamplerPanel.setTextWait(rteSamplerTestElement.getTextToWait());
      rteSamplerPanel.setTrigger(rteSamplerTestElement.getTrigger());
      Inputs payload = rteSamplerTestElement.getPayload();
      if (payload != null) {
        rteSamplerPanel.getPayload().configure(payload);
      }
    }
  }

  @Override
  public TestElement createTestElement() {
    RTESampler preproc = new RTESampler();
    configureTestElement(preproc);
    return preproc;
  }

  @Override
  public void modifyTestElement(TestElement te) {
    configureTestElement(te);
    if (te instanceof RTESampler) {
      RTESampler rteSamplerTestElement = (RTESampler) te;
      rteSamplerTestElement.setTypingStyle(rteSamplerPanel.getTypingStyle());
      rteSamplerTestElement.setDisconnect(rteSamplerPanel.getDisconnect());
      rteSamplerTestElement.setWaitCursor(rteSamplerPanel.getWaitCursor());
      rteSamplerTestElement.setWaitSilent(rteSamplerPanel.getWaitSilent());
      rteSamplerTestElement.setWaitSync(rteSamplerPanel.getWaitSync());
      rteSamplerTestElement.setWaitText(rteSamplerPanel.getWaitText());
      rteSamplerTestElement.setWaitTimeoutSync(rteSamplerPanel.getWaitTimeoutSync());
      rteSamplerTestElement.setWaitTimeoutCursor(rteSamplerPanel.getWaitTimeoutCursor());
      rteSamplerTestElement.setWaitTimeoutSilent(rteSamplerPanel.getWaitTimeoutSilent());
      rteSamplerTestElement.setWaitForSilent(rteSamplerPanel.getWaitForSilent());
      rteSamplerTestElement.setWaitTimeoutText(rteSamplerPanel.getWaitTimeoutText());
      rteSamplerTestElement.setCoordXToWait(rteSamplerPanel.getCoordXWait());
      rteSamplerTestElement.setCoordYToWait(rteSamplerPanel.getCoordYWait());
      rteSamplerTestElement.setTextToWait(rteSamplerPanel.getTextWait());
      rteSamplerTestElement.setTrigger(rteSamplerPanel.getTrigger());

      CoordInputPanel payload = rteSamplerPanel.getPayload();
      if (payload != null) {
        rteSamplerTestElement.setPayload((Inputs) payload.createTestElement());
      }
    }
  }

  @Override
  public void clearGui() {
    super.clearGui();
    initFields();
  }

  private void init() {
    rteSamplerPanel = new RTESamplerPanel();
  }

  private void initFields() {
    rteSamplerPanel.initFields();
  }
}
