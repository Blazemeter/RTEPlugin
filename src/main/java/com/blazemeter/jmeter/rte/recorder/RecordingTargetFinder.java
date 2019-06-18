package com.blazemeter.jmeter.rte.recorder;

import java.util.List;
import org.apache.jmeter.gui.tree.JMeterTreeModel;
import org.apache.jmeter.gui.tree.JMeterTreeNode;
import org.apache.jmeter.protocol.http.control.RecordingController;
import org.apache.jmeter.threads.AbstractThreadGroup;

public class RecordingTargetFinder {

  private final JMeterTreeModel treeModel;

  public RecordingTargetFinder(JMeterTreeModel treeModel) {
    this.treeModel = treeModel;
  }

  public JMeterTreeNode findTargetControllerNode() {
    JMeterTreeNode targetNode = findFirstNodeOfType(RecordingController.class);

    if (targetNode == null) {
      targetNode = findFirstNodeOfType(AbstractThreadGroup.class);
    }
    if (targetNode == null) {
      throw new IllegalStateException(
          "No ThreadGroup or RecordingController was found where to add recorded samplers");
    }
    return targetNode;
  }

  private JMeterTreeNode findFirstNodeOfType(Class<?> type) {
    List<JMeterTreeNode> nodes = treeModel.getNodesOfType(type);
    for (JMeterTreeNode node : nodes) {
      if (node.isEnabled()) {
        return node;
      }
    }
    return null;
  }
}
