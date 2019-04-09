package com.blazemeter.jmeter.rte.recorder.gui;

import com.blazemeter.jmeter.rte.recorder.RTERecorder;
import com.helger.commons.annotation.VisibleForTesting;

import java.awt.BorderLayout;

import org.apache.jmeter.config.gui.AbstractConfigGui;
import org.apache.jmeter.gui.UnsharedComponent;
import org.apache.jmeter.testelement.TestElement;

public class RTERecorderGui extends AbstractConfigGui implements UnsharedComponent {
  private RTERecorderPanel recordingPanel;
  
  public RTERecorderGui() {
    recordingPanel = new RTERecorderPanel();
    setLayout(new BorderLayout(0, 5));
    setBorder(makeBorder());
    add(makeTitlePanel(), BorderLayout.NORTH);
    add(recordingPanel, BorderLayout.CENTER);
     
  }
  
  @VisibleForTesting
  protected RTERecorderGui(RTERecorderPanel panel) {
    recordingPanel = panel;
  }
  
  @Override
  public String getStaticLabel() {
    return "bzm - RTE Recorder";
  }
  
  @Override
  public String getLabelResource() {
    throw new IllegalStateException("This shouldn't be called"); 
  }
  
  public TestElement createTestElement() {
    //TODO Create RteRecorder
    RTERecorder config = new RTERecorder();
    configureTestElement(config);
    return config;
  }
  
  public void modifyTestElement(TestElement te) {
    configureTestElement(te);
    //TODO change with RteRecorder
    if (te instanceof RTERecorder) {
      RTERecorder recorder = (RTERecorder) te;
      recorder.setServer(recordingPanel.getServer());
      recorder.setPort(recordingPanel.getPort());
      recorder.setProtocol(recordingPanel.getProtocol());
      recorder.setSSLType(recordingPanel.getSSLType());
      recorder.setTerminalType(recordingPanel.getTerminalType());
      recorder.setConnectionTimeout(recordingPanel.getConnectionTimeout());
    }
  }
  
  @Override
  public void configure(TestElement element) {
    super.configure(element);
    //TODO change with RteRecorder
    if (element instanceof RTERecorder) {
      RTERecorder configTestElement = (RTERecorder) element;
      recordingPanel.setRecorder(configTestElement);
    }
  }
  
}
