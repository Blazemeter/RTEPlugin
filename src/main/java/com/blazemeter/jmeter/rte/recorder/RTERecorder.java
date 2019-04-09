package com.blazemeter.jmeter.rte.recorder;

import com.blazemeter.jmeter.rte.core.AttentionKey;
import com.blazemeter.jmeter.rte.core.Protocol;
import com.blazemeter.jmeter.rte.core.RteSampleResult;
import com.blazemeter.jmeter.rte.core.TerminalType;
import com.blazemeter.jmeter.rte.core.ssl.SSLType;
import com.blazemeter.jmeter.rte.sampler.CoordInputRowGUI;
import com.blazemeter.jmeter.rte.sampler.Inputs;
import com.blazemeter.jmeter.rte.sampler.RTESampler;
import com.blazemeter.jmeter.rte.sampler.gui.RTEConfigGui;
import com.blazemeter.jmeter.rte.sampler.gui.RTESamplerGui;

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
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.threads.AbstractThreadGroup;
import org.apache.jmeter.util.JMeterUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RTERecorder extends GenericController {

  private static final Logger LOG = LoggerFactory.getLogger(RTERecorder.class);
  
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
  
  // TODO rename to something more generic like addTestElementToTestPlan 
  //  to be able to also add ConfigTestElement
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
  
  private void notifySampleListeners(SampleEvent event) {
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
  
  public void startActionButton() {
    
    //TODO create ConfigTestElement with RteConfigGui as gui class 
    // and all properties properly set and place Sample
    ConfigTestElement configTestElement = new ConfigTestElement();
    RTESampler rteSampler = new RTESampler();
    configTestElement.setName(RTEConfigGui.class.getSimpleName());
    configTestElement.setProperty(TestElement.GUI_CLASS, RTEConfigGui.class.getName());
    configTestElement.setProperty(RTESampler.CONFIG_PORT, getPort());
    configTestElement.setProperty(RTESampler.CONFIG_SERVER, getServer());
    configTestElement.setProperty(RTESampler.CONFIG_PROTOCOL, 
        getProtocol().toString());
    configTestElement.setProperty(RTESampler.CONFIG_TERMINAL_TYPE, 
        getTerminalType().getId());
    configTestElement.setProperty(RTESampler.CONFIG_SSL_TYPE, 
        getSSLType().toString());
    JMeterTreeNode myTarget = findTargetControllerNode();                              
    addTestElementToTestPlan(configTestElement, myTarget);

    //TODO set in sampler only attention keys, inputs and waits (with dummy values)
    configureSampler(rteSampler);
    Inputs payload = new Inputs();
    CoordInputRowGUI input = new CoordInputRowGUI();
    input.setRow("1");
    input.setColumn("1");
    input.setInput("TestUser");
    payload.addInput(input);
    rteSampler.setPayload(payload);
    rteSampler.setAttentionKey(AttentionKey.F1);
    rteSampler.setWaitSilentTimeout("234");
    addTestElementToTestPlan(rteSampler, myTarget);
    //TODO use same values (variables) from sampler and from connection properties to create result
    RteSampleResult.Builder sampleResult = new RteSampleResult.Builder()
        .withPort(Integer.parseInt(getPort()))
        .withServer(getServer())
        .withProtocol(getProtocol())
        .withTerminalType(getTerminalType())
        .withSslType(getSSLType());
    sampleResult.build().setRequestHeaders(sampleResult.build().getRequestHeaders());
    sampleResult.build().setSamplerData(sampleResult.build().getRequestBody());
    notifySampleListeners((new SampleEvent((sampleResult.build()), "Thread Group")));
    
  }

  public String getPort() {
    return getPropertyAsString(RTESampler.CONFIG_PORT,
        String.valueOf(RTESampler.DEFAULT_PORT));
  }

  public String getServer() {
    
    return  getPropertyAsString(RTESampler.CONFIG_SERVER);
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

  public void setServer(String server) {
    setProperty(RTESampler.CONFIG_SERVER, server);
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
}
