package com.blazemeter.jmeter.rte.sampler.gui;

import com.blazemeter.jmeter.rte.core.Protocol;
import com.blazemeter.jmeter.rte.core.SSLType;
import com.blazemeter.jmeter.rte.core.TerminalType;
import com.blazemeter.jmeter.rte.sampler.RTESampler;
import java.awt.BorderLayout;
import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.config.gui.AbstractConfigGui;
import org.apache.jmeter.testelement.TestElement;

public class RTEConfigGui extends AbstractConfigGui {

  private static final long serialVersionUID = 8495980373764997386L;
  private RTEConfigPanel rteConfigPanelConfigPanel;

  public RTEConfigGui() {
    super();
    init();
    initFields();

    setLayout(new BorderLayout(0, 5));
    setBorder(makeBorder());

    add(makeTitlePanel(), BorderLayout.NORTH);
    add(rteConfigPanelConfigPanel, BorderLayout.CENTER);
  }

  @Override
  public String getStaticLabel() {
    return "RTE Config";
  }

  @Override
  public String getLabelResource() {
    throw new IllegalStateException("This shouldn't be called"); //$NON-NLS-1$
  }

  @Override
  public void configure(TestElement element) {
    super.configure(element);
    if (element instanceof ConfigTestElement) {
      ConfigTestElement configTestElement = (ConfigTestElement) element;
      rteConfigPanelConfigPanel
          .setServer(configTestElement.getPropertyAsString(RTESampler.CONFIG_SERVER));
      rteConfigPanelConfigPanel
          .setPort(configTestElement.getPropertyAsString(RTESampler.CONFIG_PORT));
      rteConfigPanelConfigPanel.setProtocol(
          Protocol.valueOf(configTestElement.getPropertyAsString(RTESampler.CONFIG_PROTOCOL)));
      rteConfigPanelConfigPanel.setTerminal(TerminalType
          .valueOf(configTestElement.getPropertyAsString(RTESampler.CONFIG_TERMINAL_TYPE)));
      rteConfigPanelConfigPanel
          .setUser(configTestElement.getPropertyAsString(RTESampler.CONFIG_USER));
      rteConfigPanelConfigPanel
          .setPass(configTestElement.getPropertyAsString(RTESampler.CONFIG_PASS));
      rteConfigPanelConfigPanel.setSSLType(
          SSLType.valueOf(configTestElement.getPropertyAsString(RTESampler.CONFIG_SSL_TYPE)));
      rteConfigPanelConfigPanel
          .setTimeout(configTestElement.getPropertyAsString(RTESampler.CONFIG_TIMEOUT));

    }
  }

  @Override
  public TestElement createTestElement() {
    ConfigTestElement config = new ConfigTestElement();
    config.setName(this.getName());
    config.setProperty(TestElement.GUI_CLASS, this.getClass().getName());
    config.setProperty(TestElement.TEST_CLASS, config.getClass().getName());
    modifyTestElement(config);
    return config;
  }

  @Override
  public void modifyTestElement(TestElement te) {
    configureTestElement(te);
    if (te instanceof ConfigTestElement) {
      ConfigTestElement configTestElement = (ConfigTestElement) te;
      configTestElement
          .setProperty(RTESampler.CONFIG_SERVER, rteConfigPanelConfigPanel.getServer());
      configTestElement.setProperty(RTESampler.CONFIG_PORT, rteConfigPanelConfigPanel.getPort());
      configTestElement.setProperty(RTESampler.CONFIG_USER, rteConfigPanelConfigPanel.getUser());
      configTestElement.setProperty(RTESampler.CONFIG_PASS, rteConfigPanelConfigPanel.getPass());
      configTestElement
          .setProperty(RTESampler.CONFIG_PROTOCOL, rteConfigPanelConfigPanel.getProtocol().name());
      configTestElement
          .setProperty(RTESampler.CONFIG_SSL_TYPE, rteConfigPanelConfigPanel.getSSLType().name());
      configTestElement.setProperty(RTESampler.CONFIG_TERMINAL_TYPE,
          rteConfigPanelConfigPanel.getTerminal().name());
      configTestElement
          .setProperty(RTESampler.CONFIG_TIMEOUT, rteConfigPanelConfigPanel.getTimeout());

    }
  }

  @Override
  public void clearGui() {
    super.clearGui();
    initFields();
  }

  private void init() {
    rteConfigPanelConfigPanel = new RTEConfigPanel();
  }

  private void initFields() {
    rteConfigPanelConfigPanel.initFields();
  }
}
