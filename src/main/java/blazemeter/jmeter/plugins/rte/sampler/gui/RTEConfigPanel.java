
package blazemeter.jmeter.plugins.rte.sampler.gui;

import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import org.apache.jmeter.util.JMeterUtils;

import blazemeter.jmeter.plugins.rte.sampler.RTESampler;

public class RTEConfigPanel extends javax.swing.JPanel {

	private static final long serialVersionUID = -3671411083800369578L;
	private JPanel connectionPanel = new JPanel();
	private JLabel sslTypeLabel = new JLabel();
	ButtonGroup group = new ButtonGroup();
	private JRadioButton noneRadio = new JRadioButton(RTESampler.SSLTYPE_NONE, true);
	private JRadioButton sslv2Radio = new JRadioButton(RTESampler.SSLTYPE_SSLV2);
	private JRadioButton sslv3Radio = new JRadioButton(RTESampler.SSLTYPE_SSLV3);
	private JRadioButton tlsRadio = new JRadioButton(RTESampler.SSLTYPE_TLS);
	private JLabel serverLabel = new JLabel();
	private JTextField server = new JTextField();
	private JLabel portLabel = new JLabel();
	private JTextField port = new JTextField();
	private JLabel userLabel = new JLabel();
	private JTextField user = new JTextField();
	private JLabel passLabel = new JLabel();
	private JTextField pass = new JTextField();
	private JLabel protocolLabel = new JLabel();
	private JComboBox protocolComboBox = new JComboBox();

	private JPanel timeoutPanel = new JPanel();
	private JLabel connectTimeoutLabel = new JLabel();
	private JTextField connectionTimeout = new JTextField();

	public RTEConfigPanel() {
		initComponents();
	}

	private void initComponents() {

		group.add(noneRadio);
		group.add(sslv2Radio);
		group.add(sslv3Radio);
		group.add(tlsRadio);

		connectionPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Connection"));

		serverLabel.setText("Server: ");
		portLabel.setText("Port: ");
		userLabel.setText("User: ");
		passLabel.setText("Password: ");
		protocolLabel.setText("Protocol: ");
		sslTypeLabel.setText("SSL Type: ");
		protocolComboBox.setModel(new javax.swing.DefaultComboBoxModel(
				new String[] { RTESampler.PROTOCOL_TN5250, RTESampler.PROTOCOL_TN3270 }));

		javax.swing.GroupLayout connectionPanelLayout = new javax.swing.GroupLayout(connectionPanel);
		connectionPanel.setLayout(connectionPanelLayout);
		connectionPanelLayout.setHorizontalGroup(connectionPanelLayout
				.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(connectionPanelLayout.createSequentialGroup()
						.addGroup(connectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
								.addGroup(connectionPanelLayout.createSequentialGroup().addComponent(serverLabel)
										.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addComponent(server, javax.swing.GroupLayout.PREFERRED_SIZE, 500,
												javax.swing.GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
										.addComponent(portLabel)
										.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addComponent(port, javax.swing.GroupLayout.PREFERRED_SIZE, 150,
												javax.swing.GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED))
								.addGroup(connectionPanelLayout.createSequentialGroup().addComponent(userLabel)
										.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addComponent(user, javax.swing.GroupLayout.PREFERRED_SIZE, 200,
												javax.swing.GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
										.addComponent(passLabel)
										.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addComponent(pass, javax.swing.GroupLayout.PREFERRED_SIZE, 200,
												javax.swing.GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED))
								.addGroup(connectionPanelLayout.createSequentialGroup().addComponent(protocolLabel)
										.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addComponent(protocolComboBox, 0, 1, Short.MAX_VALUE)
										.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED))
								.addGroup(connectionPanelLayout.createSequentialGroup().addComponent(sslTypeLabel)
										.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED))
								.addGroup(connectionPanelLayout.createSequentialGroup().addComponent(noneRadio)
										.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addComponent(tlsRadio)
										.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addComponent(sslv2Radio)
										.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addComponent(sslv3Radio)
										.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)))));

		connectionPanelLayout.setVerticalGroup(connectionPanelLayout
				.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(connectionPanelLayout.createSequentialGroup().addContainerGap()
						.addGroup(connectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
								.addComponent(serverLabel)
								.addComponent(server, javax.swing.GroupLayout.PREFERRED_SIZE,
										javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
								.addComponent(portLabel).addComponent(port, javax.swing.GroupLayout.PREFERRED_SIZE,
										javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addGroup(connectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
								.addComponent(userLabel)
								.addComponent(user, javax.swing.GroupLayout.PREFERRED_SIZE,
										javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
								.addComponent(passLabel).addComponent(pass, javax.swing.GroupLayout.PREFERRED_SIZE,
										javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addGroup(connectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
								.addComponent(protocolLabel).addComponent(protocolComboBox,
										javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE,
										javax.swing.GroupLayout.PREFERRED_SIZE))
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(sslTypeLabel)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addGroup(connectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
								.addComponent(userLabel).addGap(8, 8, 8).addComponent(noneRadio).addComponent(tlsRadio)
								.addComponent(sslv2Radio).addComponent(sslv3Radio))
						.addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));

		timeoutPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(JMeterUtils.getResString("timeout_title")));

		connectTimeoutLabel.setText(JMeterUtils.getResString("web_server_timeout_connect"));

		javax.swing.GroupLayout timeoutPanelLayout = new javax.swing.GroupLayout(timeoutPanel);
		timeoutPanel.setLayout(timeoutPanelLayout);
		timeoutPanelLayout.setHorizontalGroup(timeoutPanelLayout
				.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(timeoutPanelLayout.createSequentialGroup().addContainerGap().addComponent(connectTimeoutLabel)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(connectionTimeout, javax.swing.GroupLayout.PREFERRED_SIZE, 150,
								javax.swing.GroupLayout.PREFERRED_SIZE)
						.addContainerGap()));
		timeoutPanelLayout.setVerticalGroup(timeoutPanelLayout
				.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(timeoutPanelLayout.createSequentialGroup().addContainerGap()
						.addGroup(timeoutPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
								.addComponent(connectTimeoutLabel).addComponent(connectionTimeout,
										javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE,
										javax.swing.GroupLayout.PREFERRED_SIZE))
						.addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));

		javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
		this.setLayout(layout);
		layout.setHorizontalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(layout.createSequentialGroup().addContainerGap()
						.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
								.addComponent(connectionPanel, javax.swing.GroupLayout.DEFAULT_SIZE,
										javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(timeoutPanel, javax.swing.GroupLayout.DEFAULT_SIZE,
										javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
						.addContainerGap()));
		layout.setVerticalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(layout.createSequentialGroup().addContainerGap()
						.addComponent(connectionPanel, javax.swing.GroupLayout.PREFERRED_SIZE,
								javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addComponent(timeoutPanel, javax.swing.GroupLayout.PREFERRED_SIZE,
								javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
						.addContainerGap()));
	}

	public void initFields() {
		connectionTimeout.setText("");
		server.setText("");
		port.setText("");
		pass.setText("");
		user.setText("");
		protocolComboBox.setSelectedItem(RTESampler.PROTOCOL_TN5250);
		noneRadio.setSelected(true);
	}

	public void setServer(String serverAddressParam) {
		server.setText(serverAddressParam);
	}

	public String getServer() {
		return server.getText();
	}

	public void setPort(String serverPortParam) {
		port.setText(serverPortParam);
	}

	public String getPort() {
		return port.getText();
	}

	public void setPass(String passParam) {
		pass.setText(passParam);
	}

	public String getPass() {
		return pass.getText();
	}

	public void setUser(String userParam) {
		user.setText(userParam);
	}

	public String getUser() {
		return user.getText();
	}

	public void setSSLType(String sslType) {
		if (sslType.equals(RTESampler.SSLTYPE_NONE)) {
			noneRadio.setSelected(true);
		} else if (sslType.equals(RTESampler.SSLTYPE_SSLV2)) {
			sslv2Radio.setSelected(true);
		} else if (sslType.equals(RTESampler.SSLTYPE_SSLV3)) {
			sslv3Radio.setSelected(true);
		} else if (sslType.equals(RTESampler.SSLTYPE_TLS)) {
			tlsRadio.setSelected(true);
		}
	}

	public String getSSLType() {
		if (noneRadio.isSelected()) {
			return RTESampler.SSLTYPE_NONE;
		} else if (sslv2Radio.isSelected()) {
			return RTESampler.SSLTYPE_SSLV2;
		} else if (sslv3Radio.isSelected()) {
			return RTESampler.SSLTYPE_SSLV3;
		} else if (tlsRadio.isSelected()) {
			return RTESampler.SSLTYPE_TLS;
		} else {
			return RTESampler.SSLTYPE_NONE;
		}

	}

	public void setProtocol(String protocol) {
		protocolComboBox.setSelectedItem(protocol);
	}

	public String getProtocol() {
		return (String) protocolComboBox.getSelectedItem();
	}

	public void setTimeout(String timeout) {
		connectionTimeout.setText(timeout);
	}

	public String getTimeout() {
		return connectionTimeout.getText();
	}

}
