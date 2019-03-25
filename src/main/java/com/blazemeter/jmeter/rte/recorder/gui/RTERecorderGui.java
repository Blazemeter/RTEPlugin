package com.blazemeter.jmeter.rte.recorder.gui;

import com.blazemeter.jmeter.rte.core.Protocol;
import com.blazemeter.jmeter.rte.core.ssl.SSLType;
import com.blazemeter.jmeter.rte.sampler.RTESampler;
import com.helger.commons.annotation.VisibleForTesting;

import java.awt.BorderLayout;

import org.apache.jmeter.config.ConfigTestElement;
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
    return "bzm - Recorder";
  }
  
  @Override
  public String getLabelResource() {
    throw new IllegalStateException("This shouldn't be called"); 
  }
  
  public TestElement createTestElement() {
    ConfigTestElement config = new ConfigTestElement();
    configureTestElement(config);
    return config;
  }
  
  public void modifyTestElement(TestElement te) {
    configureTestElement(te);
    if (te instanceof ConfigTestElement) {
      ConfigTestElement configTestElement = (ConfigTestElement) te;
      configTestElement
          .setProperty(RTESampler.CONFIG_SERVER, recordingPanel.getServer());
      configTestElement
          .setProperty(RTESampler.CONFIG_PROTOCOL, recordingPanel.getProtocol().name());
      configTestElement
          .setProperty(RTESampler.CONFIG_SSL_TYPE, recordingPanel.getSSLType().name());
      configTestElement.setProperty(RTESampler.CONFIG_TERMINAL_TYPE,
          recordingPanel.getTerminalType().getId());
      configTestElement.setProperty(RTESampler.CONFIG_CONNECTION_TIMEOUT,
          recordingPanel.getConnectionTimeout());

    }
  }
  
  @Override
  public void configure(TestElement element) {
    super.configure(element);
    if (element instanceof ConfigTestElement) {
      ConfigTestElement configTestElement = (ConfigTestElement) element;
      recordingPanel
          .setServer(configTestElement.getPropertyAsString(RTESampler.CONFIG_SERVER));
      recordingPanel.setPort(
          configTestElement.getPropertyAsString(RTESampler.CONFIG_PORT,
              String.valueOf(RTESampler.DEFAULT_PORT)));
      Protocol protocol = Protocol
          .valueOf(configTestElement.getPropertyAsString(RTESampler.CONFIG_PROTOCOL,
              RTESampler.DEFAULT_PROTOCOL.name()));
      recordingPanel.setProtocol(protocol);
      recordingPanel.setTerminalType(protocol.createProtocolClient().getTerminalTypeById(
          configTestElement.getPropertyAsString(RTESampler.CONFIG_TERMINAL_TYPE,
              RTESampler.DEFAULT_TERMINAL_TYPE.getId())));
      recordingPanel.setSSLType(
          SSLType.valueOf(configTestElement
              .getPropertyAsString(RTESampler.CONFIG_SSL_TYPE, RTESampler.DEFAULT_SSLTYPE.name())));
      recordingPanel
          .setConnectionTimeout(
              configTestElement.getPropertyAsString(RTESampler.CONFIG_CONNECTION_TIMEOUT,
                  String.valueOf(RTESampler.DEFAULT_CONNECTION_TIMEOUT_MILLIS)));
    }
  }

}
