package com.blazemeter.jmeter.rte.recorder.gui;

import static org.apache.tika.parser.ner.NamedEntityParser.LOG;

import com.blazemeter.jmeter.rte.core.Protocol;
import com.blazemeter.jmeter.rte.core.TerminalType;
import com.blazemeter.jmeter.rte.core.ssl.SSLType;
import com.blazemeter.jmeter.rte.recorder.RTERecorder;
import com.blazemeter.jmeter.rte.sampler.RTESampler;
import com.blazemeter.jmeter.rte.sampler.gui.RTEConfigPanel;
import com.blazemeter.jmeter.rte.sampler.gui.SwingUtils;

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
import org.apache.jmeter.samplers.SampleEvent;
import org.apache.jmeter.util.JMeterUtils;

public class RTERecorderPanel extends JPanel implements ActionListener {
  
  private static final String ADD_ACTION_START = "addActionStart";
  private static final String ADD_ACTION_STOP = "addActionStop";
  private static final String ADD_ACTION_RESTART = "addActionRestart";
  private final RTEConfigPanel configPanel;
  private JPanel panel = SwingUtils.createComponent("statePanel", new JPanel());
  private JButton startButton;
  private JButton stopButton;
  private JButton restartButton;

  public RTERecorderPanel() {
    
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
        .addComponent(statePanel)
        .addComponent(configPanel)
    );
    
  }

  private JPanel buildStatePanel() {

    panel.setBorder(BorderFactory.createTitledBorder("State"));
    GroupLayout layout = new GroupLayout(panel);
    panel.setLayout(layout);
    panel.setLayout(new FlowLayout(FlowLayout.CENTER));
    
    startButton = buildButton("start", "/arrow-right-3.png", ADD_ACTION_START, true);
    panel.add(startButton);
    panel.add(Box.createHorizontalStrut(10));
    stopButton = buildButton("stop", "/process-stop-4.png", ADD_ACTION_STOP, false);
    panel.add(stopButton);
    panel.add(Box.createHorizontalStrut(10));
    restartButton = buildButton("restart", "/edit-redo-7.png", ADD_ACTION_RESTART, false);
    panel.add(restartButton);
    return panel;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    String action = e.getActionCommand();
    switch (action) {
      case ADD_ACTION_START:
        LOG.debug("WhenStartIsPressed");
        updateButtonsIfRunning(true);
        startActionButton();
        break;
      case ADD_ACTION_STOP:
        LOG.debug("WhenStopIsPressed");
        updateButtonsIfRunning(false);
        break;
      case ADD_ACTION_RESTART:
        LOG.debug("WhenRestartIsPressed");
        updateButtonsIfRunning(true);
        break;
      default:
        throw new UnsupportedOperationException(action);
    }
  }

  public String getPort() {
    return configPanel.getPort();
  }

  public Protocol getProtocol() {
    return configPanel.getProtocol();
  }

  public SSLType getSSLType() {
    return configPanel.getSSLType();
  }

  public TerminalType getTerminalType() {
    return configPanel.getTerminalType();
  }

  public String getConnectionTimeout() {
    return configPanel.getConnectionTimeout();
  }

  public String getServer() {
    return configPanel.getServer();
  }

  public void setServer(String serParam) {
    configPanel.setServer(serParam);
  }

  public void setPort(String portParam) {
    configPanel.setPort(portParam);
  }

  public void setProtocol(Protocol protocol) {
    configPanel.setProtocol(protocol);
  }

  public void setTerminalType(TerminalType terminalTypeById) {
    configPanel.setTerminalType(terminalTypeById);
  }

  public void setSSLType(SSLType sslValue) {
    configPanel.setSSLType(sslValue);
  }

  public void setConnectionTimeout(String timeOut) {
    configPanel.setConnectionTimeout(timeOut);
  }

  private JButton buildButton(String resourceString, String imageName,
                              String actionCommand, boolean enabled) {
    String iconSize = JMeterUtils.getPropDefault(JMeterToolBar.TOOLBAR_ICON_SIZE,
        JMeterToolBar.DEFAULT_TOOLBAR_ICON_SIZE);
    JButton button = new JButton(JMeterUtils.getResString(resourceString));
    ImageIcon image = JMeterUtils.getImage("toolbar/" + iconSize + imageName);
    button.setIcon(image);
    button.addActionListener(this);
    button.setActionCommand(actionCommand);
    button.setEnabled(enabled);
    return button;
  }

  private void updateButtonsIfRunning(boolean running) {
    startButton.setEnabled(!running);
    stopButton.setEnabled(running);
    restartButton.setEnabled(running);
  }

  private void startActionButton() {
    RTESampler rteSampler = new RTESampler();
    RTERecorder rteRecorder = new RTERecorder();
    rteRecorder.configureSampler(rteSampler);
    rteRecorder.placeSampler(rteSampler);
    rteRecorder.notifySampleListeners(new SampleEvent(
            rteRecorder.sampleResult(rteSampler, configPanel),
            "WorkBench"));
  }

}
