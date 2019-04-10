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
import com.blazemeter.jmeter.rte.sampler.CoordInputRowGUI;
import com.blazemeter.jmeter.rte.sampler.InputTestElement;
import com.blazemeter.jmeter.rte.sampler.Inputs;
import com.blazemeter.jmeter.rte.sampler.LabelInputRowGUI;
import com.blazemeter.jmeter.rte.sampler.RTESampler;
import com.blazemeter.jmeter.rte.sampler.gui.RTEConfigGui;
import com.blazemeter.jmeter.rte.sampler.gui.RTESamplerGui;

import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;

import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.control.GenericController;
import org.apache.jmeter.exceptions.IllegalUserActionException;
import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.tree.JMeterTreeModel;
import org.apache.jmeter.gui.tree.JMeterTreeNode;
import org.apache.jmeter.protocol.http.control.RecordingController;
import org.apache.jmeter.samplers.SampleEvent;
import org.apache.jmeter.samplers.SampleListener;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.threads.AbstractThreadGroup;
import org.apache.jmeter.util.JMeterUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RTERecorder extends GenericController {

  private static final Logger LOG = LoggerFactory.getLogger(RTERecorder.class);

  public String getPort() {
    return getPropertyAsString(RTESampler.CONFIG_PORT,
        String.valueOf(RTESampler.DEFAULT_PORT));
  }

  public String getServer() {
    return  getPropertyAsString(RTESampler.CONFIG_SERVER);
  }

  public void setServer(String server) {
    setProperty(RTESampler.CONFIG_SERVER, server);
  }

  public SSLType getSSLType() {
    return SSLType.valueOf((
        getPropertyAsString(RTESampler.CONFIG_SSL_TYPE, RTESampler.DEFAULT_SSLTYPE.name())));
  }

  public String getConnectionTimeout() {
    return
        getPropertyAsString(RTESampler.CONFIG_CONNECTION_TIMEOUT,
            String.valueOf(RTESampler.DEFAULT_CONNECTION_TIMEOUT_MILLIS));
  }

  public Protocol getProtocol() {
    return Protocol
        .valueOf(getPropertyAsString(RTESampler.CONFIG_PROTOCOL,
            RTESampler.DEFAULT_PROTOCOL.name()));
  }

  public TerminalType getTerminalType() {
    return getProtocol().createProtocolClient().getTerminalTypeById(
        getPropertyAsString(RTESampler.CONFIG_TERMINAL_TYPE,
            RTESampler.DEFAULT_TERMINAL_TYPE.getId()));
  }

  public void setPort(String port) {
    setProperty(RTESampler.CONFIG_PORT, port);
  }

  public void setProtocol(Protocol protocol) {
    setProperty(RTESampler.CONFIG_PROTOCOL, protocol.name());
  }

  public void setSSLType(SSLType sslType) {
    setProperty(RTESampler.CONFIG_SSL_TYPE, sslType.name());
  }

  public void setTerminalType(TerminalType terminalType) {
    setProperty(RTESampler.CONFIG_TERMINAL_TYPE,
        terminalType.getId());
  }

  public void setConnectionTimeout(String connectionTimeout) {
    setProperty(RTESampler.CONFIG_CONNECTION_TIMEOUT,
        connectionTimeout);
  }
  
  public void startActionButton() {

    JMeterTreeNode myTarget = findTargetControllerNode();                              
    addTestElementToTestPlan(buildRteConfigElement(), myTarget);

    AttentionKey attentionKey = AttentionKey.F1;
    Position position = new Position(1, 5);
    List<Input> inputs = Arrays.asList(new CoordInput(new Position(2, 3), "testusr"),
        new LabelInput("PASSWORD", "testpsw"));
    String screen = "screen";
    boolean requestInputInhibited = true;
    boolean responseInputInhibited = true;
    boolean alarm = true; 
    addTestElementToTestPlan(buildRteSampler(attentionKey, inputs), myTarget);
    
    //TODO setScreen, cursor, requestInhibitedInput, responseInhibitedInput, etc
    notifySampleListeners(buildSampleResult(attentionKey, inputs, position, screen, alarm,
        requestInputInhibited, responseInputInhibited));
    
  }

  private JMeterTreeNode findTargetControllerNode() {
    JMeterTreeNode myTarget = findFirstNodeOfType(RecordingController.class);
    if (myTarget == null) {
      myTarget = findFirstNodeOfType(AbstractThreadGroup.class);
    }

    if (myTarget != null) {
      int i = myTarget.getChildCount() - 1;
      JMeterTreeNode c = null;
      while ((i >= 0) && (myTarget !=  c)) {
        c = (JMeterTreeNode) myTarget.getChildAt(i);
        if (c.getTestElement() instanceof GenericController) {
          myTarget = c;
        }
        i--;
      }
    }
    return myTarget;
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

  private ConfigTestElement buildRteConfigElement() {
    ConfigTestElement configTestElement = new ConfigTestElement();
    configTestElement.setName(RTEConfigGui.class.getSimpleName());
    configTestElement.setProperty(TestElement.GUI_CLASS, RTEConfigGui.class.getName());
    configTestElement.setProperty(RTESampler.CONFIG_PORT, getPort());
    configTestElement.setProperty(RTESampler.CONFIG_SERVER, getServer());
    configTestElement.setProperty(RTESampler.CONFIG_PROTOCOL,
        getProtocol().name());
    configTestElement.setProperty(RTESampler.CONFIG_TERMINAL_TYPE,
        getTerminalType().getId());
    configTestElement.setProperty(RTESampler.CONFIG_SSL_TYPE,
        getSSLType().name());
    return configTestElement;
  }
  
  private void addTestElementToTestPlan(TestElement testElement, JMeterTreeNode myTarget) {
    try {

      if (myTarget == null) {
        LOG.error("Program error: test script recording target not found.");
        JMeterUtils.reportErrorToUser(new Exception().getMessage());
      }

      JMeterTreeModel treeModel = getJmeterTreeModel();
      JMeterUtils.runSafe(true, () -> {
        try {
          treeModel.addComponent(testElement, myTarget);

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

  private void configureSampler(RTESampler rteSampler) {
    rteSampler.setName(RTESampler.class.getSimpleName());
    rteSampler.setProperty(TestElement.GUI_CLASS, RTESamplerGui.class.getName());
    rteSampler.setProperty(TestElement.TEST_CLASS, RTESampler.class.getName());
  }

  private void notifySampleListeners(SampleResult result) {
    SampleEvent event = new SampleEvent(result, "Thread Group", "Recorded");
    JMeterTreeModel treeModel = getJmeterTreeModel();
    JMeterTreeNode myNode = treeModel.getNodeOf(this);
    if (myNode != null) {
      Enumeration<?> kids = myNode.children();
      while (kids.hasMoreElements()) {
        JMeterTreeNode subNode = (JMeterTreeNode) kids.nextElement();
        if (subNode.isEnabled()) {
          TestElement testElement = subNode.getTestElement();
          if (testElement instanceof SampleListener) {
            ((SampleListener) testElement).sampleOccurred(event);
          }
        }
      }
    }
  }

  private RTESampler buildRteSampler(AttentionKey attentionKey, List<Input> inputs) {
    RTESampler rteSampler = new RTESampler();
    configureSampler(rteSampler);
    rteSampler.setPayload(buildInputsTestElement(inputs));
    rteSampler.setAttentionKey(attentionKey);
    rteSampler.setWaitSilentTimeout("234");
    return rteSampler;
  }

  private Inputs buildInputsTestElement(List<Input> inputs) {
    Inputs ret = new Inputs();
    for (Input input : inputs) {
      ret.addInput(buildInputTestElement(input));
    }
    return ret;
  }

  private InputTestElement buildInputTestElement(Input input) {
    if (input instanceof CoordInput) {
      CoordInput coordInput = (CoordInput) input;
      CoordInputRowGUI ret = new CoordInputRowGUI();
      Position position = coordInput.getPosition();
      ret.setColumn(String.valueOf(position.getColumn()));
      ret.setRow(String.valueOf(position.getRow()));
      ret.setInput(coordInput.getInput());
      return ret;
    } else if (input instanceof LabelInput) {
      LabelInput labelInput = (LabelInput) input;
      LabelInputRowGUI ret = new LabelInputRowGUI();
      ret.setLabel(labelInput.getLabel());
      ret.setInput(labelInput.getInput());
      return ret;
    } else {
      throw new IllegalArgumentException("Unsupported input type " + input.getClass());
    }
  }

  private RteSampleResult buildSampleResult(AttentionKey attentionKey, List<Input> inputs,
                                            Position position, String screen,
                                            boolean resposeInputInhibited,
                                            boolean alarm, boolean inputInhibitedRequest) {
    
    RteSampleResult sampleResult = new RteSampleResult.Builder()
        .withPort(Integer.parseInt(getPort()))
        .withServer(getServer())
        .withProtocol(getProtocol())
        .withTerminalType(getTerminalType())
        .withSslType(getSSLType())
        .withLabel("test")
        .build();
    sampleResult.setInputs(inputs);
    sampleResult.setAttentionKey(attentionKey);
    sampleResult.setCursorPosition(position);
    sampleResult.setScreen(screen);
    sampleResult.setSoundedAlarm(alarm);
    sampleResult.setInputInhibitedRequest(inputInhibitedRequest);
    sampleResult.setInputInhibitedResponse(resposeInputInhibited);
    
    return sampleResult;
  }
  
}
