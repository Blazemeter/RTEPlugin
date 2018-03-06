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
      RTESampler sampler = (RTESampler) element;
      rteSamplerPanel.setTypingStyle(sampler.getTypingStyle());
      rteSamplerPanel.setDisconnect(sampler.getDisconnect());
      rteSamplerPanel.setWaitCursor(sampler.getWaitCursor());
      rteSamplerPanel.setWaitSilent(sampler.getWaitSilent());
      rteSamplerPanel.setWaitSync(sampler.getWaitSync());
      rteSamplerPanel.setWaitText(sampler.getWaitText());
      rteSamplerPanel.setWaitTimeoutSync(sampler.getWaitTimeoutSync());
      rteSamplerPanel.setWaitTimeoutCursor(sampler.getWaitTimeoutCursor());
      rteSamplerPanel.setWaitTimeoutSilent(sampler.getWaitTimeoutSilent());
      rteSamplerPanel.setWaitForSilent(sampler.getWaitForSilent());
      rteSamplerPanel.setWaitTimeoutText(sampler.getWaitTimeoutText());
      rteSamplerPanel.setCoordXWait(sampler.getCoordXToWait());
      rteSamplerPanel.setCoordYWait(sampler.getCoordYToWait());
      rteSamplerPanel.setTextWait(sampler.getTextToWait());
      rteSamplerPanel.setAction(sampler.getAction());
      Inputs payload = sampler.getInputs();
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
      RTESampler sampler = (RTESampler) te;
      sampler.setTypingStyle(rteSamplerPanel.getTypingStyle());
      sampler.setDisconnect(rteSamplerPanel.getDisconnect());
      sampler.setWaitCursor(rteSamplerPanel.getWaitCursor());
      sampler.setWaitSilent(rteSamplerPanel.getWaitSilent());
      sampler.setWaitSync(rteSamplerPanel.getWaitSync());
      sampler.setWaitText(rteSamplerPanel.getWaitText());
      sampler.setWaitTimeoutSync(rteSamplerPanel.getWaitTimeoutSync());
      sampler.setWaitTimeoutCursor(rteSamplerPanel.getWaitTimeoutCursor());
      sampler.setWaitTimeoutSilent(rteSamplerPanel.getWaitTimeoutSilent());
      sampler.setWaitForSilent(rteSamplerPanel.getWaitForSilent());
      sampler.setWaitTimeoutText(rteSamplerPanel.getWaitTimeoutText());
      sampler.setCoordXToWait(rteSamplerPanel.getCoordXWait());
      sampler.setCoordYToWait(rteSamplerPanel.getCoordYWait());
      sampler.setTextToWait(rteSamplerPanel.getTextWait());
      sampler.setAction(rteSamplerPanel.getAction());

      CoordInputPanel payload = rteSamplerPanel.getPayload();
      if (payload != null) {
        sampler.setPayload((Inputs) payload.createTestElement());
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
