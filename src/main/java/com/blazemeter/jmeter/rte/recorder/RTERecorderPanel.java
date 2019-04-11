package com.blazemeter.jmeter.rte.recorder;

import static org.apache.tika.parser.ner.NamedEntityParser.LOG;

import com.blazemeter.jmeter.rte.core.Protocol;
import com.blazemeter.jmeter.rte.core.TerminalType;
import com.blazemeter.jmeter.rte.core.ssl.SSLType;
import com.blazemeter.jmeter.rte.sampler.gui.RTEConfigPanel;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import org.apache.jmeter.gui.util.JMeterToolBar;
import org.apache.jmeter.util.JMeterUtils;

public class RTERecorderPanel extends JPanel implements ActionListener {

  private static final String ADD_ACTION_START = "addActionStart";
  private static final String ADD_ACTION_STOP = "addActionStop";
  private static final String ADD_ACTION_RESTART = "addActionRestart";
  private final RTERecorderGui rteRecorderGui;
  private final RTEConfigPanel configPanel;
  private JButton startButton;
  private JButton stopButton;
  private JButton restartButton;

  public RTERecorderPanel(RTERecorderGui rteRecorderGui) {
    this.rteRecorderGui = rteRecorderGui;

    GroupLayout layout = new GroupLayout(this);
    layout.setAutoCreateGaps(true);
    this.setLayout(layout);

    JPanel statePanel = buildStatePanel();
    configPanel = new RTEConfigPanel();

    layout.setHorizontalGroup(layout.createParallelGroup()
        .addComponent(statePanel)
        .addComponent(configPanel)
    );
    layout.setVerticalGroup(layout.createSequentialGroup()
        .addComponent(statePanel, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE,
            GroupLayout.PREFERRED_SIZE)
        .addComponent(configPanel)
    );

  }

  private JPanel buildStatePanel() {
    JPanel panel = new JPanel();
    panel.setBorder(BorderFactory.createTitledBorder("State"));
    GroupLayout layout = new GroupLayout(panel);
    panel.setLayout(layout);
    panel.setLayout(new FlowLayout(FlowLayout.CENTER));

    startButton = buildButton("start", "/arrow-right-3.png", ADD_ACTION_START);
    panel.add(startButton);
    panel.add(Box.createHorizontalStrut(10));
    stopButton = buildButton("stop", "/process-stop-4.png", ADD_ACTION_STOP);
    panel.add(stopButton);
    panel.add(Box.createHorizontalStrut(10));
    restartButton = buildButton("restart", "/edit-redo-7.png", ADD_ACTION_RESTART);
    panel.add(restartButton);
    updateButtonsIfRunning(false);
    return panel;
  }

  private JButton buildButton(String resourceString, String imageName,
      String actionCommand) {
    String iconSize = JMeterUtils.getPropDefault(JMeterToolBar.TOOLBAR_ICON_SIZE,
        JMeterToolBar.DEFAULT_TOOLBAR_ICON_SIZE);
    JButton button = new JButton(JMeterUtils.getResString(resourceString));
    ImageIcon image = JMeterUtils.getImage("toolbar/" + iconSize + imageName);
    button.setIcon(image);
    button.addActionListener(this);
    button.setActionCommand(actionCommand);
    return button;
  }

  private void updateButtonsIfRunning(boolean running) {
    startButton.setEnabled(!running);
    stopButton.setEnabled(running);
    restartButton.setEnabled(running);
  }

  public String getServer() {
    return configPanel.getServer();
  }

  public void setServer(String server) {
    configPanel.setServer(server);
  }

  public String getPort() {
    return configPanel.getPort();
  }

  public void setPort(String port) {
    configPanel.setPort(port);
  }

  public Protocol getProtocol() {
    return configPanel.getProtocol();
  }

  public void setProtocol(Protocol protocol) {
    configPanel.setProtocol(protocol);
  }

  public TerminalType getTerminalType() {
    return configPanel.getTerminalType();
  }

  public void setTerminalType(TerminalType terminalType) {
    configPanel.setTerminalType(terminalType);
  }

  public SSLType getSSLType() {
    return configPanel.getSSLType();
  }

  public void setSSLType(SSLType sslType) {
    configPanel.setSSLType(sslType);
  }

  public String getConnectionTimeout() {
    return configPanel.getConnectionTimeout();
  }

  public void setConnectionTimeout(String connectionTimeout) {
    configPanel.setConnectionTimeout(connectionTimeout);
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    String action = e.getActionCommand();
    try {
      switch (action) {
        case ADD_ACTION_START:
          LOG.debug("WhenStartIsPressed");
          rteRecorderGui.startRecording();
          updateButtonsIfRunning(true);
          break;
        case ADD_ACTION_STOP:
          LOG.debug("WhenStopIsPressed");
          rteRecorderGui.stopRecording();
          updateButtonsIfRunning(false);
          break;
        case ADD_ACTION_RESTART:
          LOG.debug("WhenRestartIsPressed");
          rteRecorderGui.stopRecording();
          rteRecorderGui.startRecording();
          updateButtonsIfRunning(true);
          break;
        default:
          throw new UnsupportedOperationException(action);
      }
    } catch (Exception ex) {
      LOG.error("Problem performing requested action {}", action, ex);
      JMeterUtils.reportErrorToUser(ex.getMessage());
    }
  }

}
