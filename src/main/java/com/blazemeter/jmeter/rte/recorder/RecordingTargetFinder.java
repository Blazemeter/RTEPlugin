package com.blazemeter.jmeter.rte.recorder;

import java.util.List;
import org.apache.jmeter.gui.tree.JMeterTreeModel;
import org.apache.jmeter.gui.tree.JMeterTreeNode;
import org.apache.jmeter.protocol.http.control.RecordingController;
import org.apache.jmeter.threads.AbstractThreadGroup;

public class RecordingTargetFinder {

  public JMeterTreeNode findTargetControllerNode(
      JMeterTreeModel treeModel) {
   
    JMeterTreeNode targetNode = findFirstNodeOfType(RecordingController.class, treeModel);

    if (targetNode == null) {
      targetNode = findFirstNodeOfType(AbstractThreadGroup.class, treeModel);
    }
    if (targetNode == null) {
      throw new IllegalStateException(
          "No ThreadGroup or RecordingController was found where to add recorded samplers");
    }
    return targetNode;
  }

  private JMeterTreeNode findFirstNodeOfType(Class<?> type,
      JMeterTreeModel treeModel) {
    List<JMeterTreeNode> nodes = treeModel.getNodesOfType(type);
    for (JMeterTreeNode node : nodes) {
      if (node.isEnabled()) {
        return node;
      }
    }
    return null;
  }
}
