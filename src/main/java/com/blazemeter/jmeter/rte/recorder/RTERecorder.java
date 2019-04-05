package com.blazemeter.jmeter.rte.recorder;

import com.blazemeter.jmeter.rte.core.AttentionKey;
import com.blazemeter.jmeter.rte.core.Input;
import com.blazemeter.jmeter.rte.sampler.RTESampler;
import com.blazemeter.jmeter.rte.sampler.gui.RTEConfigPanel;
import com.blazemeter.jmeter.rte.sampler.gui.RTESamplerGui;

import java.util.Enumeration;
import java.util.List;
import java.util.stream.Collectors;

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
  private JMeterTreeNode myTarget = findTargetControllerNode();

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
  
  public void placeSampler(RTESampler sampler) {
    try {
      
      if (myTarget == null) {
        LOG.error("Program error: test script recording target not found.");
        JMeterUtils.reportErrorToUser(new Exception().getMessage());
      }

      JMeterTreeModel treeModel = getJmeterTreeModel();
      JMeterUtils.runSafe(true, () -> { 
        try {
          treeModel.addComponent(sampler, myTarget);
        } catch (IllegalUserActionException illegalUserAction) {
          LOG.error("Error placing sampler", illegalUserAction);
          JMeterUtils.reportErrorToUser(illegalUserAction.getMessage());
        }

      });
    } catch (Exception exception) {
      LOG.error("Error placing sampler", exception);
      JMeterUtils.reportErrorToUser(exception.getMessage());
    }

  }

  public void configureSampler(RTESampler rteSampler) {
    rteSampler.setName(getName());
    rteSampler.setProperty(TestElement.GUI_CLASS, RTESamplerGui.class.getName());
    rteSampler.setProperty(TestElement.TEST_CLASS, RTESampler.class.getName());
    //Next lines are just tests
    rteSampler.setAttentionKey(AttentionKey.F1);
    rteSampler.setName("TestName in configureSampler");
  }

  public SampleResult sampleResult(RTESampler rteSampler, RTEConfigPanel rteConfigPanel) {
    SampleResult res = new SampleResult();
    res.setRequestHeaders(buildRequestHeaders(rteConfigPanel));
    res.setSamplerData(buildRequestBody(rteSampler));
    return res;

  }

  private String buildRequestHeaders(RTEConfigPanel c) {
    return "Server: " + c.getServer() + "\n" +
            "Port: " + c.getPort() + "\n" +
            "Protocol: " + c.getProtocol().toString() + "\n" +
            "Terminal-type: " + c.getTerminalType() + "\n" +
            "Security: " + c.getSSLType() + "\n";
  }

  private String buildRequestBody(RTESampler sampler) {
    return new StringBuilder()
            .append("AttentionKey: ")
            .append(sampler.getAttentionKey())
            .append("\n")
            .append("Inputs:\n")
            .append(sampler.getInputs().stream()
                    .map(Input::getCsv)
                    .collect(Collectors.joining("\n")))
            .append("\n")
            .toString();
  }

  public void notifySampleListeners(SampleEvent event) {
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
}
