package com.blazemeter.jmeter.rte.recorder;

import static org.apache.tika.parser.ner.NamedEntityParser.LOG;

import com.blazemeter.jmeter.rte.core.Protocol;
import com.blazemeter.jmeter.rte.core.TerminalType;
import com.blazemeter.jmeter.rte.core.exceptions.RteIOException;
import com.blazemeter.jmeter.rte.core.ssl.SSLType;
import com.blazemeter.jmeter.rte.sampler.gui.RTEConfigPanel;
import com.blazemeter.jmeter.rte.sampler.gui.SwingUtils;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.TimeoutException;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.LayoutStyle;
import org.apache.jmeter.gui.util.JMeterToolBar;
import org.apache.jmeter.util.JMeterUtils;

public class RTERecorderPanel extends JPanel implements ActionListener, RecordingStateListener {

  private static final String ADD_ACTION_START = "addActionStart";
  private static final String ADD_ACTION_STOP = "addActionStop";
  private static final String ADD_ACTION_RESTART = "addActionRestart";
  private static JTextField waitConditionsTimeoutThreshold = SwingUtils
      .createComponent("waitConditionsTimeoutThreshold", new JTextField());
  private final RecordingStateListener recordingStateListener;
  private final RTEConfigPanel configPanel;
  private JButton startButton;
  private JButton stopButton;
  private JButton restartButton;

  public RTERecorderPanel(RecordingStateListener recordingStateListener) {
    this.recordingStateListener = recordingStateListener;

    GroupLayout layout = new GroupLayout(this);
    layout.setAutoCreateGaps(true);
    this.setLayout(layout);
    JPanel waitConditionsPanel = buildTimeThresholdPanel();
    JPanel statePanel = buildStatePanel();
    configPanel = new RTEConfigPanel();

    layout.setHorizontalGroup(layout.createParallelGroup()
        .addComponent(statePanel)
        .addComponent(configPanel)
        .addComponent(waitConditionsPanel)
    );
    layout.setVerticalGroup(layout.createSequentialGroup()
        .addComponent(statePanel, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE,
            GroupLayout.PREFERRED_SIZE)
        .addComponent(configPanel)
        .addComponent(waitConditionsPanel)
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
    button.setName(resourceString);
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

  private JPanel buildTimeThresholdPanel() {
    JPanel panel = SwingUtils.createComponent("timeThresholdPanel", new JPanel());
    panel.setBorder(BorderFactory.createTitledBorder("Wait conditions"));
    GroupLayout layout = new GroupLayout(panel);
    layout.setAutoCreateContainerGaps(true);
    panel.setLayout(layout);

    JLabel waitConditionTimeoutThreshold = SwingUtils.createComponent(
        "waitConditionTimeoutThreshold",
        new JLabel("Timeout threshold (ms)"));
    layout.setHorizontalGroup(layout.createSequentialGroup()
        .addComponent(waitConditionTimeoutThreshold)
        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(waitConditionsTimeoutThreshold, GroupLayout.PREFERRED_SIZE, 150,
            GroupLayout.PREFERRED_SIZE));
    layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
        .addComponent(waitConditionTimeoutThreshold)
        .addComponent(waitConditionsTimeoutThreshold, GroupLayout.PREFERRED_SIZE,
            GroupLayout.DEFAULT_SIZE,
            GroupLayout.PREFERRED_SIZE));
    return panel;
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

  public String getWaitConditionsTimeoutThresholdMillis() {
    return waitConditionsTimeoutThreshold.getText();
  }

  public void setWaitConditionsTimeoutThresholdMillis(String thresholdTime) {
    waitConditionsTimeoutThreshold.setText(thresholdTime);
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    String action = e.getActionCommand();
    try {
      switch (action) {
        case ADD_ACTION_START:
          LOG.debug("WhenStartIsPressed");
          recordingStateListener.onRecordingStart();
          updateButtonsIfRunning(true);
          break;
        case ADD_ACTION_STOP:
          LOG.debug("WhenStopIsPressed");
          recordingStateListener.onRecordingStop();
          updateButtonsIfRunning(false);
          break;
        case ADD_ACTION_RESTART:
          LOG.debug("WhenRestartIsPressed");
          recordingStateListener.onRecordingStop();
          recordingStateListener.onRecordingStart();
          updateButtonsIfRunning(true);
          break;
        default:
          throw new UnsupportedOperationException(action);
      }
    } catch (UnsupportedOperationException ex) {
      LOG.error("Problem performing requested action {}", action, ex);
      JMeterUtils.reportErrorToUser(ex.getMessage());
    }
  }

  @Override
  public void onRecordingStart() {
    updateButtonsIfRunning(true);
  }

  @Override
  public void onRecordingStop() {
    updateButtonsIfRunning(false);
  }

  @Override
  public void onRecordingException(Exception e) {
    if (!(e instanceof InterruptedException)) {
      reportExceptionToUser(e);
      if (!(e instanceof UnsupportedOperationException)) {
        updateButtonsIfRunning(false);
      }
    }
  }

  private void reportExceptionToUser(Exception e) {
    String errorMsg;
    if (e.getClass().getPackage().equals(RteIOException.class.getPackage())
        || e instanceof TimeoutException || e instanceof UnsupportedOperationException) {
      errorMsg = e.getMessage();
    } else {
      errorMsg = "Unexpected error occurred - see log file or contact technical support";
    }
    JMeterUtils.reportErrorToUser(errorMsg, "Connection Error");
  }
}
