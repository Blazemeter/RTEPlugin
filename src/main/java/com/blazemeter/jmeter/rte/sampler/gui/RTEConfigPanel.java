package com.blazemeter.jmeter.rte.sampler.gui;

import com.blazemeter.jmeter.rte.core.Protocol;
import com.blazemeter.jmeter.rte.core.TerminalType;
import com.blazemeter.jmeter.rte.core.ssl.SSLType;
import com.blazemeter.jmeter.rte.sampler.RTESampler;
import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import org.apache.jmeter.util.JMeterUtils;

public class RTEConfigPanel extends JPanel {

  private static final long serialVersionUID = -3671411083800369578L;

  private static final DefaultComboBoxModel<TerminalType> TN5250_TERMINAL_TYPES =
      buildTerminalTypesComboBoxModel(Protocol.TN5250);
  private static final DefaultComboBoxModel<TerminalType> TN3270_TERMINAL_TYPES =
      buildTerminalTypesComboBoxModel(Protocol.TN3270);
  private static final DefaultComboBoxModel<TerminalType> VT420_TERMINAL_TYPES = 
      buildTerminalTypesComboBoxModel(Protocol.VT420);

  private ButtonGroup sslTypeGroup = new ButtonGroup();
  private Map<SSLType, JRadioButton> sslTypeRadios = new EnumMap<>(SSLType.class);

  private JTextField serverField = SwingUtils.createComponent("serverField", new JTextField());
  private JTextField portField = SwingUtils.createComponent("portField", new JTextField());
  private JComboBox<Protocol> protocolComboBox;
  private JComboBox<TerminalType> terminalTypeComboBox = SwingUtils
      .createComponent("terminalTypeComboBox", new JComboBox<>(TN5250_TERMINAL_TYPES));
  private JTextField connectionTimeout = SwingUtils
      .createComponent("connectionTimeout", new JTextField());

  public RTEConfigPanel() {
    GroupLayout layout = new GroupLayout(this);
    layout.setAutoCreateGaps(true);
    this.setLayout(layout);

    JPanel connectionPanel = buildConnectionPanel();

    layout.setHorizontalGroup(layout.createParallelGroup(Alignment.LEADING)
        .addComponent(connectionPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE,
            Short.MAX_VALUE));
    layout.setVerticalGroup(layout.createSequentialGroup()
        .addComponent(connectionPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
            GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(ComponentPlacement.RELATED));
  }

  private JPanel buildConnectionPanel() {
    JPanel panel = SwingUtils.createComponent("connectionPanel", new JPanel());
    panel.setBorder(BorderFactory.createTitledBorder("Connection"));
    GroupLayout layout = new GroupLayout(panel);
    layout.setAutoCreateContainerGaps(true);
    panel.setLayout(layout);

    JLabel serverLabel = SwingUtils.createComponent("serverLabel", new JLabel("Server: "));
    JLabel portLabel = SwingUtils.createComponent("portLabel", new JLabel("Port: "));
    JLabel protocolLabel = SwingUtils.createComponent("protocolLabel", new JLabel("Protocol: "));
    protocolComboBox = buildProtocolComboBox();
    JLabel terminalTypeLabel = SwingUtils
        .createComponent("terminalTypeLabel", new JLabel("Terminal Type:"));
    JPanel sslPanel = buildSslPanel();
    JPanel timeOutPanel = buildTimeoutPanel();
    layout.setHorizontalGroup(layout.createParallelGroup(Alignment.LEADING)
        .addGroup(layout.createSequentialGroup()
            .addComponent(serverLabel)
            .addPreferredGap(ComponentPlacement.RELATED)
            .addComponent(serverField, GroupLayout.PREFERRED_SIZE, 500, GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(ComponentPlacement.UNRELATED)
            .addComponent(portLabel)
            .addPreferredGap(ComponentPlacement.RELATED)
            .addComponent(portField, GroupLayout.PREFERRED_SIZE, 150, GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(ComponentPlacement.UNRELATED))
        .addGroup(layout.createSequentialGroup()
            .addComponent(protocolLabel)
            .addPreferredGap(ComponentPlacement.RELATED)
            .addComponent(protocolComboBox, 0, 1, Short.MAX_VALUE)
            .addPreferredGap(ComponentPlacement.UNRELATED))
        .addGroup(
            layout.createSequentialGroup()
                .addComponent(terminalTypeLabel)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(terminalTypeComboBox, 0, 1, Short.MAX_VALUE)
                .addPreferredGap(ComponentPlacement.UNRELATED))
        .addGroup(
                layout.createSequentialGroup()
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(connectionTimeout, GroupLayout.PREFERRED_SIZE, 150,
                GroupLayout.PREFERRED_SIZE))
        .addComponent(sslPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
            GroupLayout.PREFERRED_SIZE)
        .addGroup(
            layout.createSequentialGroup()
            .addComponent(timeOutPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
                          GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(ComponentPlacement.RELATED)));
    layout.setVerticalGroup(layout.createSequentialGroup()
        .addGroup(layout.createParallelGroup(Alignment.BASELINE)
            .addComponent(serverLabel)
            .addComponent(serverField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
                GroupLayout.PREFERRED_SIZE)
            .addComponent(portLabel)
            .addComponent(portField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
                GroupLayout.PREFERRED_SIZE))
        .addPreferredGap(ComponentPlacement.RELATED)
        .addGroup(layout.createParallelGroup(Alignment.BASELINE)
            .addComponent(protocolLabel)
            .addComponent(protocolComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
                GroupLayout.PREFERRED_SIZE))
        .addPreferredGap(ComponentPlacement.RELATED)
        .addGroup(layout.createParallelGroup(Alignment.BASELINE)
            .addComponent(terminalTypeLabel)
            .addComponent(terminalTypeComboBox, GroupLayout.PREFERRED_SIZE,
                GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
        .addGroup(layout.createParallelGroup(Alignment.BASELINE)
            .addComponent(connectionTimeout, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
                    GroupLayout.PREFERRED_SIZE))
        .addPreferredGap(ComponentPlacement.RELATED)
        .addComponent(sslPanel)
            .addPreferredGap(ComponentPlacement.RELATED)
            .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                  .addComponent(timeOutPanel)
            ));
    return panel;
  }

  private JComboBox<Protocol> buildProtocolComboBox() {
    JComboBox<Protocol> comboBox = SwingUtils
        .createComponent("protocolComboBox", new JComboBox<>(Protocol.values()));
    comboBox.addItemListener(e -> {
      if (e.getStateChange() != ItemEvent.SELECTED) {
        return;
      }
      Protocol protocolEnum = (Protocol) e.getItem();
      if (protocolEnum.equals(Protocol.TN5250)) {
        terminalTypeComboBox.setModel(TN5250_TERMINAL_TYPES);
      } else if (protocolEnum.equals(Protocol.TN3270)) {
        terminalTypeComboBox.setModel(TN3270_TERMINAL_TYPES);
      } else if (protocolEnum.equals(Protocol.VT420)) {
        terminalTypeComboBox.setModel(VT420_TERMINAL_TYPES);
      }
      validate();
      repaint();
    });
    return comboBox;
  }

  private static DefaultComboBoxModel<TerminalType> buildTerminalTypesComboBoxModel(
      Protocol protocol) {
    return new DefaultComboBoxModel<>(
        protocol.createProtocolClient().getSupportedTerminalTypes()
            .toArray(new TerminalType[0]));
  }

  private JPanel buildSslPanel() {
    JPanel panel = SwingUtils.createComponent("sslPanel", new JPanel());
    panel.setBorder(BorderFactory.createTitledBorder("SSL Type"));
    panel.setLayout(new GridLayout(1, 3));

    Arrays.stream(SSLType.values()).forEach(s -> {
      JRadioButton r = SwingUtils.createComponent(s.toString(), new JRadioButton(s.toString()));
      r.setActionCommand(s.name());
      panel.add(r);
      sslTypeRadios.put(s, r);
      sslTypeGroup.add(r);
    });

    return panel;
  }

  private JPanel buildTimeoutPanel() {
    JPanel panel = SwingUtils.createComponent("timeoutPanel", new JPanel());
    panel
        .setBorder(BorderFactory.createTitledBorder(JMeterUtils.getResString("timeout_title")));
    GroupLayout layout = new GroupLayout(panel);
    layout.setAutoCreateContainerGaps(true);
    panel.setLayout(layout);

    JLabel connectTimeoutLabel = SwingUtils.createComponent("connectTimeoutLabel",
        new JLabel(JMeterUtils.getResString("web_server_timeout_connect")));

    layout.setHorizontalGroup(layout.createSequentialGroup()
        .addComponent(connectTimeoutLabel)
        .addPreferredGap(ComponentPlacement.RELATED)
        .addComponent(connectionTimeout, GroupLayout.PREFERRED_SIZE, 150,
            GroupLayout.PREFERRED_SIZE));
    layout.setVerticalGroup(layout.createParallelGroup(Alignment.BASELINE)
        .addComponent(connectTimeoutLabel)
        .addComponent(connectionTimeout, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
            GroupLayout.PREFERRED_SIZE));
    return panel;
  }

  public String getServer() {
    return serverField.getText();
  }

  public void setServer(String serverAddressParam) {
    serverField.setText(serverAddressParam);
  }

  public String getPort() {
    return portField.getText();
  }

  public void setPort(String portParam) {
    portField.setText(portParam);
  }

  public SSLType getSSLType() {
    String sslType = sslTypeGroup.getSelection().getActionCommand();
    return SSLType.valueOf(sslType);
  }

  public void setSSLType(SSLType ssl) {
    if (sslTypeRadios.containsKey(ssl)) {
      sslTypeRadios.get(ssl).setSelected(true);
    } else {
      sslTypeRadios.get(RTESampler.DEFAULT_SSL_TYPE).setSelected(true);
    }
  }

  public Protocol getProtocol() {
    return (Protocol) protocolComboBox.getSelectedItem();
  }

  public void setProtocol(Protocol protocol) {
    protocolComboBox.setSelectedItem(protocol);
  }

  public TerminalType getTerminalType() {
    return (TerminalType) terminalTypeComboBox.getSelectedItem();
  }

  public void setTerminalType(TerminalType terminal) {
    terminalTypeComboBox.setSelectedItem(terminal);
  }

  public String getConnectionTimeout() {
    return connectionTimeout.getText();
  }

  public void setConnectionTimeout(String timeout) {
    connectionTimeout.setText(timeout);
  }

}
