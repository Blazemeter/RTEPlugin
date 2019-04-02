package com.blazemeter.jmeter.rte.recorder.gui;

import static org.apache.tika.parser.ner.NamedEntityParser.LOG;

import com.blazemeter.jmeter.rte.core.Protocol;
import com.blazemeter.jmeter.rte.core.TerminalType;
import com.blazemeter.jmeter.rte.core.ssl.SSLType;
import com.blazemeter.jmeter.rte.sampler.gui.RTEConfigPanel;
import com.blazemeter.jmeter.rte.sampler.gui.SwingUtils;

import java.awt.Component;
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
  private final RTEConfigPanel configPanel;
  private JPanel panel = SwingUtils.createComponent("statePanel", new JPanel());

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
    
    buildButton("start", "/arrow-right-3.png", ADD_ACTION_START, true);
    buildButton("stop", "/process-stop-4.png", ADD_ACTION_STOP, false);
    buildButton("restart", "/edit-redo-7.png", ADD_ACTION_RESTART, false);
    return panel;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    String action = e.getActionCommand();
    switch (action) {
      case ADD_ACTION_START:
        //RTERecorder recorder = new RTERecorder();
        //recorder.placeSampler(new RTESampler());
        LOG.debug("WhenStartIsPressed");
        updateButtonsIfRunning(true);
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

  private void buildButton(String resourceString, String imageName,
                           String actionCommand, boolean enabled) {
    String iconSize = JMeterUtils.getPropDefault(JMeterToolBar.TOOLBAR_ICON_SIZE,
        JMeterToolBar.DEFAULT_TOOLBAR_ICON_SIZE);
    JButton button = new JButton(JMeterUtils.getResString(resourceString));
    ImageIcon image = JMeterUtils.getImage("toolbar/" + iconSize + imageName);
    button.setIcon(image);
    button.addActionListener(this);
    button.setActionCommand(actionCommand);
    button.setEnabled(enabled);
    panel.add(resourceString, button);
    panel.add(Box.createHorizontalStrut(10));
  }
  
  private void updateButtonsIfRunning(boolean running) {
    //TODO
    if (running) {
      getButton(1).setEnabled(false);
      getButton(2).setEnabled(true);
      getButton(3).setEnabled(false);
    } else {
      getButton(2).setEnabled(false);
      getButton(3).setEnabled(true);
      getButton(1).setEnabled(true);
    }
  
  }

  private Component getButton(int index) {
    Component[] components = configPanel.getComponents();
    switch (index) {
      case 1:
        return  components[index];
      case 2:
        return  components[index];
      case 3:
        return  components[index];
      default:
        return null;
    }
  }
}
