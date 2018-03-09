package com.blazemeter.jmeter.rte.sampler.gui;

import com.blazemeter.jmeter.rte.core.Protocol;
import com.blazemeter.jmeter.rte.core.SSLType;
import com.blazemeter.jmeter.rte.core.TerminalType;
import com.blazemeter.jmeter.rte.sampler.RTESampler;
import java.awt.GridLayout;
import java.util.Arrays;
import java.util.HashMap;
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

  private JPanel connectionPanel = new JPanel();
  private JPanel sslPanel = new JPanel();
  private ButtonGroup sslTypeGroup = new ButtonGroup();
  private Map<SSLType, JRadioButton> sslType = new HashMap<>();
  private JLabel serverLabel = new JLabel();
  private JTextField server = new JTextField();
  private JLabel portLabel = new JLabel();
  private JTextField port = new JTextField();
  private JLabel protocolLabel = new JLabel();
  private JComboBox<Protocol> protocolComboBox = new JComboBox<>(Protocol.values());

  private DefaultComboBoxModel<TerminalType> modelTN5250 = new DefaultComboBoxModel<>(
      TerminalType.findByProtocol(Protocol.TN5250));
  private DefaultComboBoxModel<TerminalType> modelTN3270 = new DefaultComboBoxModel<>(
      TerminalType.findByProtocol(Protocol.TN3270));

  private JLabel terminalTypeLabel = new JLabel();
  private JComboBox<TerminalType> terminalTypeComboBox = new JComboBox<>(modelTN5250);

  private JPanel timeoutPanel = new JPanel();
  private JLabel connectTimeoutLabel = new JLabel();
  private JTextField connectionTimeout = new JTextField();

  public RTEConfigPanel() {
    initComponents();
  }

  private void initComponents() {

    connectionPanel.setBorder(BorderFactory.createTitledBorder("Connection"));

    protocolComboBox.addItemListener(e -> {
      Protocol protocolEnum = (Protocol) e.getItem();
      if (protocolEnum.equals(Protocol.TN5250)) {
        terminalTypeComboBox.setModel(modelTN5250);
      } else if (protocolEnum.equals(Protocol.TN3270)) {
        terminalTypeComboBox.setModel(modelTN3270);
      }
      validate();
      repaint();
    });

    serverLabel.setText("Server: ");
    portLabel.setText("Port: ");
    protocolLabel.setText("Protocol: ");
    terminalTypeLabel.setText("Terminal Type:");

    sslPanel.setBorder(BorderFactory.createTitledBorder("SSL Type"));
    sslPanel.setLayout(new GridLayout(1, 4));

    Arrays.stream(SSLType.values()).forEach(s -> {
      JRadioButton r = new JRadioButton(s.toString());
      r.setActionCommand(s.toString());
      sslPanel.add(r);
      sslType.put(s, r);
      sslTypeGroup.add(r);
    });

    GroupLayout connectionPanelLayout = new GroupLayout(connectionPanel);
    connectionPanel.setLayout(connectionPanelLayout);
    connectionPanelLayout.setHorizontalGroup(connectionPanelLayout
        .createParallelGroup(Alignment.LEADING)
        .addGroup(connectionPanelLayout.createSequentialGroup()
            .addGroup(connectionPanelLayout.createParallelGroup(Alignment.LEADING)
                .addGroup(connectionPanelLayout.createSequentialGroup().addComponent(serverLabel)
                    .addPreferredGap(ComponentPlacement.RELATED)
                    .addComponent(server, GroupLayout.PREFERRED_SIZE, 500,
                        GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(ComponentPlacement.UNRELATED)
                    .addComponent(portLabel)
                    .addPreferredGap(ComponentPlacement.RELATED)
                    .addComponent(port, GroupLayout.PREFERRED_SIZE, 150,
                        GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(ComponentPlacement.UNRELATED))
                .addGroup(connectionPanelLayout.createSequentialGroup().addComponent(protocolLabel)
                    .addPreferredGap(ComponentPlacement.RELATED)
                    .addComponent(protocolComboBox, 0, 1, Short.MAX_VALUE)
                    .addPreferredGap(ComponentPlacement.UNRELATED))
                .addGroup(
                    connectionPanelLayout.createSequentialGroup().addComponent(terminalTypeLabel)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(terminalTypeComboBox, 0, 1, Short.MAX_VALUE)
                        .addPreferredGap(ComponentPlacement.UNRELATED))
                .addComponent(sslPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE,
                    Short.MAX_VALUE))));

    connectionPanelLayout.setVerticalGroup(connectionPanelLayout
        .createParallelGroup(Alignment.LEADING)
        .addGroup(connectionPanelLayout.createSequentialGroup().addContainerGap()
            .addGroup(connectionPanelLayout.createParallelGroup(Alignment.BASELINE)
                .addComponent(serverLabel)
                .addComponent(server, GroupLayout.PREFERRED_SIZE,
                    GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addComponent(portLabel).addComponent(port, GroupLayout.PREFERRED_SIZE,
                    GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(ComponentPlacement.RELATED)
            .addGroup(connectionPanelLayout.createParallelGroup(Alignment.BASELINE)
                .addComponent(protocolLabel).addComponent(protocolComboBox,
                    GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
                    GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(ComponentPlacement.RELATED)
            .addGroup(connectionPanelLayout.createParallelGroup(Alignment.BASELINE)
                .addComponent(terminalTypeLabel).addComponent(terminalTypeComboBox,
                    GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
                    GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(ComponentPlacement.RELATED).addComponent(sslPanel)
            .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));

    timeoutPanel
        .setBorder(BorderFactory.createTitledBorder(JMeterUtils.getResString("timeout_title")));

    connectTimeoutLabel.setText(JMeterUtils.getResString("web_server_timeout_connect"));

    GroupLayout timeoutPanelLayout = new GroupLayout(timeoutPanel);
    timeoutPanel.setLayout(timeoutPanelLayout);
    timeoutPanelLayout.setHorizontalGroup(timeoutPanelLayout
        .createParallelGroup(Alignment.LEADING)
        .addGroup(timeoutPanelLayout.createSequentialGroup().addContainerGap()
            .addComponent(connectTimeoutLabel)
            .addPreferredGap(ComponentPlacement.RELATED)
            .addComponent(connectionTimeout, GroupLayout.PREFERRED_SIZE, 150,
                GroupLayout.PREFERRED_SIZE)
            .addContainerGap()));
    timeoutPanelLayout.setVerticalGroup(timeoutPanelLayout
        .createParallelGroup(Alignment.LEADING)
        .addGroup(timeoutPanelLayout.createSequentialGroup().addContainerGap()
            .addGroup(timeoutPanelLayout.createParallelGroup(Alignment.BASELINE)
                .addComponent(connectTimeoutLabel).addComponent(connectionTimeout,
                    GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
                    GroupLayout.PREFERRED_SIZE))
            .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));

    GroupLayout layout = new GroupLayout(this);
    this.setLayout(layout);
    layout.setHorizontalGroup(layout.createParallelGroup(Alignment.LEADING)
        .addGroup(layout.createSequentialGroup().addContainerGap()
            .addGroup(layout.createParallelGroup(Alignment.LEADING)
                .addComponent(connectionPanel, GroupLayout.DEFAULT_SIZE,
                    GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(timeoutPanel, GroupLayout.DEFAULT_SIZE,
                    GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addContainerGap()));
    layout.setVerticalGroup(layout.createParallelGroup(Alignment.LEADING)
        .addGroup(layout.createSequentialGroup().addContainerGap()
            .addComponent(connectionPanel, GroupLayout.PREFERRED_SIZE,
                GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(ComponentPlacement.RELATED)
            .addComponent(timeoutPanel, GroupLayout.PREFERRED_SIZE,
                GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
            .addContainerGap()));
  }

  public void initFields() {
    connectionTimeout.setText("");
    server.setText("");
    port.setText("");
    protocolComboBox.setSelectedItem(RTESampler.DEFAULT_PROTOCOL);
    terminalTypeComboBox.setSelectedItem(RTESampler.DEFAULT_TERMINAL_TYPE);
    sslType.get(RTESampler.DEFAULT_SSLTYPE).setSelected(true);
  }

  public String getServer() {
    return server.getText();
  }

  public void setServer(String serverAddressParam) {
    server.setText(serverAddressParam);
  }

  public String getPort() {
    return port.getText();
  }

  public void setPort(String portParam) {
    port.setText(portParam);
  }

  public SSLType getSSLType() {
    String sslType = sslTypeGroup.getSelection().getActionCommand();
    return SSLType.valueOf(sslType);
  }

  public void setSSLType(SSLType ssl) {
    if (sslType.containsKey(ssl)) {
      sslType.get(ssl).setSelected(true);
    } else {
      sslType.get(RTESampler.DEFAULT_SSLTYPE).setSelected(true);
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
