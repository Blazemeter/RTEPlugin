//Backup RecordingTargetFinder
package com.blazemeter.jmeter.rte.recorder;

import org.apache.jmeter.control.GenericController;
import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.tree.JMeterTreeModel;
import org.apache.jmeter.gui.tree.JMeterTreeNode;
import org.apache.jmeter.protocol.http.control.RecordingController;
import org.apache.jmeter.threads.AbstractThreadGroup;

import java.util.List;

public class RecordingTargetFinder {

  private JMeterTreeNode jMeterTreeNode;

  public RecordingTargetFinder(JMeterTreeNode treeNode) {
    jMeterTreeNode = treeNode;
  }

  public JMeterTreeNode findTargetControllerNode() {

    //JMeterTreeNode targetNode = jMeterTreeNode;
    JMeterTreeNode targetNode = findFirstNodeOfType(RecordingController.class);

    if (targetNode == null) {
      targetNode = findFirstNodeOfType(AbstractThreadGroup.class);
    }

    System.out.println(4);
    if (targetNode == null) {
      throw new IllegalStateException(
          "No ThreadGroup or RecordingController was found where to add recorded samplers");
    }
    System.out.println(5);
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
}
