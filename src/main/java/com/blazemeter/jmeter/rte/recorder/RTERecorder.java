package com.blazemeter.jmeter.rte.recorder;

import com.blazemeter.jmeter.rte.recorder.gui.RTERecorderGui;
import com.blazemeter.jmeter.rte.sampler.RTESampler;

import java.util.List;

import org.apache.jmeter.assertions.ResponseAssertion;
import org.apache.jmeter.assertions.gui.AssertionGui;
import org.apache.jmeter.control.GenericController;
import org.apache.jmeter.exceptions.IllegalUserActionException;
import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.tree.JMeterTreeModel;
import org.apache.jmeter.gui.tree.JMeterTreeNode;
import org.apache.jmeter.protocol.http.control.RecordingController;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.StringProperty;
import org.apache.jmeter.threads.AbstractThreadGroup;
import org.apache.jmeter.util.JMeterUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RTERecorder extends GenericController {

  private static final Logger LOG = LoggerFactory.getLogger(RTERecorder.class);
  private static final String ASSERTION_GUI = AssertionGui.class.getName();
  private JMeterTreeModel nonGuiTreeModel;
  private JMeterTreeNode myTarget = findTargetControllerNode();
  
  public void placeSampler(RTESampler sampler) {
    //TODO
    samplerConfig(sampler); 
    try {
      JMeterTreeModel treeModel = this.getJmeterTreeModel();
      assert myTarget != null;
      for (int i = myTarget.getChildCount() - 1; i >= 0; --i) { 
        JMeterTreeNode c = (JMeterTreeNode) myTarget.getChildAt(i);
          
        if (c.getTestElement() instanceof GenericController) { 
          myTarget = c;
          break;
        }
      }   
      
      JMeterUtils.runSafe(true, () -> {
        try {
          JMeterTreeNode newNode = treeModel.addComponent(sampler, myTarget);
          this.addAssertion(treeModel, newNode);
              
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

  private JMeterTreeModel getJmeterTreeModel() {
    if (this.nonGuiTreeModel == null) {
      return GuiPackage.getInstance().getTreeModel();
    }
    return this.nonGuiTreeModel;
  }

  private void addAssertion(JMeterTreeModel model, JMeterTreeNode node)
      throws IllegalUserActionException {
    ResponseAssertion ra = new ResponseAssertion();
    ra.setProperty("TestElement.gui_class", ASSERTION_GUI);
    ra.setName(JMeterUtils.getResString("assertion_title"));
    ra.setTestFieldResponseData();
    model.addComponent(ra, node);
  }

  private JMeterTreeNode findTargetControllerNode() {

    JMeterTreeNode myTarget = findFirstNodeOfType(RecordingController.class);
    if (myTarget != null) {
      return myTarget;
    }
    myTarget = findFirstNodeOfType(AbstractThreadGroup.class);
    if (myTarget != null) {
      return myTarget;
    }
    LOG.error("Program error: test script recording target not found.");
    return null;
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

  private void samplerConfig(RTESampler rteSampler) {
    RTERecorderGui recorderGui = new RTERecorderGui();
    rteSampler.setName(getName());

    rteSampler.setProperty(new StringProperty(TestElement.GUI_CLASS,
        recorderGui.getClass().getName()));
    rteSampler.setProperty(new StringProperty(TestElement.TEST_CLASS,
        rteSampler.getClass().getName()));
  }
}
