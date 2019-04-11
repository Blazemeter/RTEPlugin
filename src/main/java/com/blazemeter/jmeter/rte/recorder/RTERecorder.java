package com.blazemeter.jmeter.rte.recorder;

import com.blazemeter.jmeter.rte.core.AttentionKey;
import com.blazemeter.jmeter.rte.core.CoordInput;
import com.blazemeter.jmeter.rte.core.Input;
import com.blazemeter.jmeter.rte.core.LabelInput;
import com.blazemeter.jmeter.rte.core.Position;
import com.blazemeter.jmeter.rte.core.Protocol;
import com.blazemeter.jmeter.rte.core.RteSampleResult;
import com.blazemeter.jmeter.rte.core.TerminalType;
import com.blazemeter.jmeter.rte.core.ssl.SSLType;
import com.blazemeter.jmeter.rte.core.wait.SilentWaitCondition;
import com.blazemeter.jmeter.rte.core.wait.WaitCondition;
import com.blazemeter.jmeter.rte.sampler.Action;
import com.blazemeter.jmeter.rte.sampler.RTESampler;
import com.blazemeter.jmeter.rte.sampler.gui.RTEConfigGui;
import com.blazemeter.jmeter.rte.sampler.gui.RTESamplerGui;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.function.Consumer;
import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.control.GenericController;
import org.apache.jmeter.exceptions.IllegalUserActionException;
import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.tree.JMeterTreeModel;
import org.apache.jmeter.gui.tree.JMeterTreeNode;
import org.apache.jmeter.protocol.http.control.RecordingController;
import org.apache.jmeter.samplers.SampleEvent;
import org.apache.jmeter.samplers.SampleListener;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestStateListener;
import org.apache.jmeter.threads.AbstractThreadGroup;
import org.apache.jmeter.util.JMeterUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RTERecorder extends GenericController {

  private static final Logger LOG = LoggerFactory.getLogger(RTERecorder.class);

  @Override
  public String getName() {
    return getPropertyAsString(TestElement.NAME);
  }

  public String getServer() {
    return getPropertyAsString(RTESampler.CONFIG_SERVER);
  }

  public void setServer(String server) {
    setProperty(RTESampler.CONFIG_SERVER, server);
  }

  public int getPort() {
    return getPropertyAsInt(RTESampler.CONFIG_PORT, RTESampler.DEFAULT_PORT);
  }

  public void setPort(String strPort) {
    int port = RTESampler.DEFAULT_PORT;
    try {
      port = Integer.parseInt(strPort);
    } catch (NumberFormatException e) {
      LOG.warn("Invalid port value '{}', defaulting to {}", strPort, RTESampler.DEFAULT_PORT);
    }
    setProperty(RTESampler.CONFIG_PORT, port);
  }

  public Protocol getProtocol() {
    return Protocol
        .valueOf(getPropertyAsString(RTESampler.CONFIG_PROTOCOL,
            RTESampler.DEFAULT_PROTOCOL.name()));
  }

  public void setProtocol(Protocol protocol) {
    setProperty(RTESampler.CONFIG_PROTOCOL, protocol.name());
  }

  public TerminalType getTerminalType() {
    return getProtocol().createProtocolClient().getTerminalTypeById(
        getPropertyAsString(RTESampler.CONFIG_TERMINAL_TYPE,
            RTESampler.DEFAULT_TERMINAL_TYPE.getId()));
  }

  public void setTerminalType(TerminalType terminalType) {
    setProperty(RTESampler.CONFIG_TERMINAL_TYPE,
        terminalType.getId());
  }

  public SSLType getSSLType() {
    return SSLType.valueOf((
        getPropertyAsString(RTESampler.CONFIG_SSL_TYPE, RTESampler.DEFAULT_SSLTYPE.name())));
  }

  public void setSSLType(SSLType sslType) {
    setProperty(RTESampler.CONFIG_SSL_TYPE, sslType.name());
  }

  public long getConnectionTimeout() {
    return getPropertyAsLong(RTESampler.CONFIG_CONNECTION_TIMEOUT,
        RTESampler.DEFAULT_CONNECTION_TIMEOUT_MILLIS);
  }

  public void setConnectionTimeout(String strConnectionTimeout) {
    long connectionTimeout = RTESampler.DEFAULT_CONNECTION_TIMEOUT_MILLIS;
    try {
      connectionTimeout = Integer.parseInt(strConnectionTimeout);
    } catch (NumberFormatException e) {
      LOG.warn("Invalid connection timeout value '{}', defaulting to {}", strConnectionTimeout,
          RTESampler.DEFAULT_CONNECTION_TIMEOUT_MILLIS);
    }
    setProperty(RTESampler.CONFIG_CONNECTION_TIMEOUT, connectionTimeout);
  }

  public void start() {
    JMeterTreeNode samplersTargetNode = findTargetControllerNode();
    notifyChildren(TestStateListener.class, TestStateListener::testStarted);
    addTestElementToTestPlan(buildRteConfigElement(), samplersTargetNode);
    addDummyInteraction(samplersTargetNode);
  }

  private JMeterTreeNode findTargetControllerNode() {
    JMeterTreeNode targetNode = findFirstNodeOfType(RecordingController.class);
    if (targetNode == null) {
      targetNode = findFirstNodeOfType(AbstractThreadGroup.class);
    }

    if (targetNode != null) {
      int i = targetNode.getChildCount() - 1;
      JMeterTreeNode c = null;
      while ((i >= 0) && (targetNode != c)) {
        c = (JMeterTreeNode) targetNode.getChildAt(i);
        if (c.getTestElement() instanceof GenericController) {
          targetNode = c;
        }
        i--;
      }
    }
    if (targetNode == null) {
      throw new IllegalStateException(
          "No ThreadGroup or RecordingController was found where to add recorded samplers");
    }
    return targetNode;
  }

  private JMeterTreeNode findFirstNodeOfType(Class<?> type) {
    JMeterTreeModel treeModel = getJmeterTreeModel();
    List<JMeterTreeNode> nodes = treeModel.getNodesOfType(type);
    for (JMeterTreeNode node : nodes) {
      if (node.isEnabled()) {
        return node;
      }
    }
    return null;
  }

  private JMeterTreeModel getJmeterTreeModel() {
    return GuiPackage.getInstance().getTreeModel();
  }

  private <T> void notifyChildren(Class<T> classFilter, Consumer<T> notificationMethod) {
    JMeterTreeModel treeModel = getJmeterTreeModel();
    JMeterTreeNode treeNode = treeModel.getNodeOf(this);
    if (treeNode != null) {
      Enumeration<?> kids = treeNode.children();
      while (kids.hasMoreElements()) {
        JMeterTreeNode subNode = (JMeterTreeNode) kids.nextElement();
        if (subNode.isEnabled()) {
          TestElement testElement = subNode.getTestElement();
          if (classFilter.isInstance(testElement)) {
            notificationMethod.accept(classFilter.cast(testElement));
          }
        }
      }
    }
  }

  private ConfigTestElement buildRteConfigElement() {
    ConfigTestElement configTestElement = new ConfigTestElement();
    configTestElement.setName(RTEConfigGui.class.getSimpleName());
    configTestElement.setProperty(TestElement.GUI_CLASS, RTEConfigGui.class.getName());
    configTestElement.setProperty(RTESampler.CONFIG_PORT, String.valueOf(getPort()));
    configTestElement.setProperty(RTESampler.CONFIG_SERVER, getServer());
    configTestElement.setProperty(RTESampler.CONFIG_PROTOCOL,
        getProtocol().name());
    configTestElement.setProperty(RTESampler.CONFIG_TERMINAL_TYPE,
        getTerminalType().getId());
    configTestElement.setProperty(RTESampler.CONFIG_SSL_TYPE, getSSLType().name());
    configTestElement
        .setProperty(RTESampler.CONFIG_CONNECTION_TIMEOUT, String.valueOf(getConnectionTimeout()));
    return configTestElement;
  }

  private void addTestElementToTestPlan(TestElement testElement, JMeterTreeNode targetNode) {
    try {
      JMeterTreeModel treeModel = getJmeterTreeModel();
      JMeterUtils.runSafe(true, () -> {
        try {
          treeModel.addComponent(testElement, targetNode);
        } catch (IllegalUserActionException illegalUserAction) {
          LOG.error("Error placing sample configTestElement", illegalUserAction);
          JMeterUtils.reportErrorToUser(illegalUserAction.getMessage());
        }
      });
    } catch (Exception exception) {
      LOG.error("Error placing sampler", exception);
      JMeterUtils.reportErrorToUser(exception.getMessage());
    }

  }

  private void addDummyInteraction(JMeterTreeNode samplersTarget) {
    //Dummy values for test
    Action action = Action.SEND_INPUT;
    AttentionKey attentionKey = AttentionKey.F1;
    Position position = new Position(1, 5);
    List<Input> inputs = Arrays.asList(new CoordInput(new Position(2, 3), "testusr"),
        new LabelInput("PASSWORD", "testpsw"));
    String screen = "screen";
    boolean requestInputInhibited = false;
    boolean responseInputInhibited = false;
    boolean alarm = true;
    List<WaitCondition> waitConditions = Collections
        .singletonList(new SilentWaitCondition(5000, 1000));

    addTestElementToTestPlan(buildRteSampler(action, attentionKey, inputs, waitConditions),
        samplersTarget);
    SampleEvent sampleEvent = new SampleEvent(
        buildSampleResult(action, requestInputInhibited, inputs, attentionKey,
            responseInputInhibited, position, alarm, screen), "Thread Group", "Recorded");
    notifyChildren(SampleListener.class, t -> t.sampleOccurred(sampleEvent));
  }

  private RTESampler buildRteSampler(Action action, AttentionKey attentionKey, List<Input> inputs,
      List<WaitCondition> waitConditions) {
    RTESampler rteSampler = new RTESampler();
    rteSampler.setWaitSync(false);
    rteSampler.setAction(action);
    rteSampler.setName(RTESampler.class.getSimpleName());
    rteSampler.setProperty(TestElement.GUI_CLASS, RTESamplerGui.class.getName());
    rteSampler.setProperty(TestElement.TEST_CLASS, RTESampler.class.getName());
    rteSampler.setInputs(inputs);
    rteSampler.setAttentionKey(attentionKey);
    rteSampler.setWaitConditions(waitConditions);
    return rteSampler;
  }

  private RteSampleResult buildSampleResult(Action action, boolean inputInhibitedRequest,
      List<Input> inputs, AttentionKey attentionKey, boolean responseInputInhibited,
      Position position, boolean alarm, String screen) {
    RteSampleResult sampleResult = new RteSampleResult();
    sampleResult.setSampleLabel(getName());
    sampleResult.setServer(getServer());
    sampleResult.setPort(getPort());
    sampleResult.setProtocol(getProtocol());
    sampleResult.setTerminalType(getTerminalType());
    sampleResult.setSslType(getSSLType());

    sampleResult.setAction(action);
    sampleResult.setInputInhibitedRequest(inputInhibitedRequest);
    sampleResult.setInputs(inputs);
    sampleResult.setAttentionKey(attentionKey);
    sampleResult.sampleStart();

    sampleResult.setSuccessful(true);
    sampleResult.setInputInhibitedResponse(responseInputInhibited);
    sampleResult.setCursorPosition(position);
    sampleResult.setSoundedAlarm(alarm);
    sampleResult.setScreen(screen);
    sampleResult.sampleEnd();
    return sampleResult;
  }

  public void stop() {
    notifyChildren(TestStateListener.class, TestStateListener::testEnded);
  }

}
