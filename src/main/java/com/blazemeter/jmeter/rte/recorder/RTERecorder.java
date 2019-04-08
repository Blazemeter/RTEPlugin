package com.blazemeter.jmeter.rte.recorder;

import com.blazemeter.jmeter.rte.recorder.gui.RTERecorderGui;
import com.blazemeter.jmeter.rte.sampler.RTESampler;

import java.util.List;

import org.apache.jmeter.control.GenericController;
import org.apache.jmeter.exceptions.IllegalUserActionException;
import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.tree.JMeterTreeModel;
import org.apache.jmeter.gui.tree.JMeterTreeNode;
import org.apache.jmeter.protocol.http.control.RecordingController;
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
    rteSampler.setProperty(TestElement.GUI_CLASS, RTERecorderGui.class.getName());
    rteSampler.setProperty(TestElement.TEST_CLASS, RTESampler.class.getName());
  }

}
