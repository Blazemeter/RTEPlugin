
package blazemeter.jmeter.plugins.rte.sampler.gui;

import java.awt.GridLayout;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.BorderFactory;
import org.apache.jmeter.util.JMeterUtils;

import blazemeter.jmeter.plugins.rte.sampler.Protocol;
import blazemeter.jmeter.plugins.rte.sampler.RTESampler;
import blazemeter.jmeter.plugins.rte.sampler.SSLType;
import blazemeter.jmeter.plugins.rte.sampler.TerminalType;

public class RTEConfigPanel extends JPanel {

	private static final long serialVersionUID = -3671411083800369578L;
	private JPanel connectionPanel = new JPanel();
	private JPanel sslPanel = new JPanel();
	ButtonGroup sslTypeGroup = new ButtonGroup();
	private Map<SSLType,JRadioButton> sslType = new HashMap<>();
	private JLabel serverLabel = new JLabel();
	private JTextField server = new JTextField();
	private JLabel portLabel = new JLabel();
	private JTextField port = new JTextField();
	private JLabel userLabel = new JLabel();
	private JTextField user = new JTextField();
	private JLabel passLabel = new JLabel();
	private JTextField pass = new JTextField();
	private JLabel protocolLabel = new JLabel();
	private JComboBox<Protocol> protocolComboBox = new JComboBox<>(Protocol.values());
	
	private DefaultComboBoxModel<TerminalType> modelTN5250 = new DefaultComboBoxModel<TerminalType>(TerminalType.findByProtocol(Protocol.TN5250));
	private DefaultComboBoxModel<TerminalType> modelTN3270 = new DefaultComboBoxModel<TerminalType>(TerminalType.findByProtocol(Protocol.TN3270));
	
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
		userLabel.setText("User: ");
		passLabel.setText("Password: ");
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
								.addGroup(connectionPanelLayout.createSequentialGroup().addComponent(userLabel)
										.addPreferredGap(ComponentPlacement.RELATED)
										.addComponent(user, GroupLayout.PREFERRED_SIZE, 200,
												GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(ComponentPlacement.UNRELATED)
										.addComponent(passLabel)
										.addPreferredGap(ComponentPlacement.RELATED)
										.addComponent(pass, GroupLayout.PREFERRED_SIZE, 200,
												GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(ComponentPlacement.UNRELATED))
								.addGroup(connectionPanelLayout.createSequentialGroup().addComponent(protocolLabel)
										.addPreferredGap(ComponentPlacement.RELATED)
										.addComponent(protocolComboBox, 0, 1, Short.MAX_VALUE)
										.addPreferredGap(ComponentPlacement.UNRELATED))
								.addGroup(connectionPanelLayout.createSequentialGroup().addComponent(terminalTypeLabel)
										.addPreferredGap(ComponentPlacement.RELATED)
										.addComponent(terminalTypeComboBox, 0, 1, Short.MAX_VALUE)
										.addPreferredGap(ComponentPlacement.UNRELATED))
								.addComponent(sslPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))));

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
								.addComponent(userLabel)
								.addComponent(user, GroupLayout.PREFERRED_SIZE,
										GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
								.addComponent(passLabel).addComponent(pass, GroupLayout.PREFERRED_SIZE,
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

		timeoutPanel.setBorder(BorderFactory.createTitledBorder(JMeterUtils.getResString("timeout_title")));

		connectTimeoutLabel.setText(JMeterUtils.getResString("web_server_timeout_connect"));

		GroupLayout timeoutPanelLayout = new GroupLayout(timeoutPanel);
		timeoutPanel.setLayout(timeoutPanelLayout);
		timeoutPanelLayout.setHorizontalGroup(timeoutPanelLayout
				.createParallelGroup(Alignment.LEADING)
				.addGroup(timeoutPanelLayout.createSequentialGroup().addContainerGap().addComponent(connectTimeoutLabel)
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
		pass.setText("");
		user.setText("");
		protocolComboBox.setSelectedItem(RTESampler.DEFAULT_PROTOCOL);
		terminalTypeComboBox.setSelectedItem(RTESampler.DEFAULT_TERMINAL_TYPE);
		sslType.get(RTESampler.DEFAULT_SSLTYPE).setSelected(true);
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

	public void setSSLType(SSLType ssl) {
		if (sslType.containsKey(ssl))
			sslType.get(ssl).setSelected(true);
		else
			sslType.get(RTESampler.DEFAULT_TRIGGER).setSelected(true);
	}

	public SSLType getSSLType() {
		String sslType = sslTypeGroup.getSelection().getActionCommand();
		return SSLType.valueOf(sslType);
	}
	
	public void setProtocol(Protocol protocol) {
		protocolComboBox.setSelectedItem(protocol);
	}

	public Protocol getProtocol() {
		return (Protocol) protocolComboBox.getSelectedItem();
	}
	
	public void setTerminal(TerminalType terminal) {
		terminalTypeComboBox.setSelectedItem(terminal);
	}

	public TerminalType getTerminal() {
		return (TerminalType) terminalTypeComboBox.getSelectedItem();
	}

	public void setTimeout(String timeout) {
		connectionTimeout.setText(timeout);
	}

	public String getTimeout() {
		return connectionTimeout.getText();
	}

}
