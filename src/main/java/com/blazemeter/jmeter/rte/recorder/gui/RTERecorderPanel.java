package com.blazemeter.jmeter.rte.recorder.gui;
import com.blazemeter.jmeter.rte.core.Protocol;
import com.blazemeter.jmeter.rte.core.TerminalType;
import com.blazemeter.jmeter.rte.core.ssl.SSLType;
import com.blazemeter.jmeter.rte.sampler.gui.RTEConfigPanel;
import com.blazemeter.jmeter.rte.sampler.gui.SwingUtils;
import org.apache.jmeter.gui.util.JMeterToolBar;
import org.apache.jmeter.util.JMeterUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import static org.apache.tika.parser.ner.NamedEntityParser.LOG;


public class RTERecorderPanel extends JPanel implements ActionListener {
  
  private static final String ADD_ACTION_START = "addActionStart";
  private static final String ADD_ACTION_STOP = "addActionStop";
  private static final String ADD_ACTION_RESTART = "addActionRestart";
  private JButton start;
  private JButton stop;
  private JButton restart;
  private final RTEConfigPanel configPanel;

  public RTERecorderPanel(){
    
    GroupLayout layout= new GroupLayout(this);
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

    JPanel panel = SwingUtils.createComponent("statePanel", new JPanel());
    panel.setBorder(BorderFactory.createTitledBorder("State"));
    GroupLayout layout = new GroupLayout(panel);
    panel.setLayout(layout);
    panel.setLayout(new FlowLayout(FlowLayout.CENTER));

    String iconSize = JMeterUtils.getPropDefault(JMeterToolBar.TOOLBAR_ICON_SIZE, JMeterToolBar.DEFAULT_TOOLBAR_ICON_SIZE);

    start = new JButton(JMeterUtils.getResString("start")); // $NON-NLS-1$
    ImageIcon startImage = JMeterUtils.getImage("toolbar/" + iconSize + "/arrow-right-3.png");
    start.setIcon(startImage);
    start.addActionListener(this);
    start.setActionCommand(ADD_ACTION_START);
    start.setEnabled(true);

    stop = new JButton(JMeterUtils.getResString("stop")); // $NON-NLS-1$
    ImageIcon stopImage = JMeterUtils.getImage("toolbar/" + iconSize + "/process-stop-4.png");
    stop.setIcon(stopImage);
    stop.addActionListener(this);
    stop.setActionCommand(ADD_ACTION_STOP);
    stop.setEnabled(false);

    ImageIcon restartImage = JMeterUtils.getImage("toolbar/" + iconSize + "/edit-redo-7.png");
    restart = new JButton(JMeterUtils.getResString("restart")); // $NON-NLS-1$
    restart.setIcon(restartImage);
    restart.addActionListener( this);
    restart.setActionCommand(ADD_ACTION_RESTART);
    restart.setEnabled(false);
    
    panel.add(start);
    panel.add(Box.createHorizontalStrut(10));
    panel.add(stop);
    panel.add(Box.createHorizontalStrut(10));
    panel.add(restart);
    return panel;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    String action = e.getActionCommand();
    switch (action) {
      case ADD_ACTION_START:
        LOG.debug("WhenStartIsPressed");
        break;
      case ADD_ACTION_STOP:
        LOG.debug("WhenStopIsPressed");
        break;
      case ADD_ACTION_RESTART:
        LOG.debug("WhenRestartIsPressed");
        break;
      default:
        throw new UnsupportedOperationException(action);
    }
  }
  public void buttonCheck(){
    while(!start.isEnabled()){
    stop.setEnabled(true);
    restart.setEnabled(true);
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
}
