package com.blazemeter.jmeter.rte.recorder;

import com.blazemeter.jmeter.rte.JMeterTestUtils;
import org.apache.jmeter.exceptions.IllegalUserActionException;
import org.apache.jmeter.gui.tree.JMeterTreeModel;
import org.apache.jmeter.gui.tree.JMeterTreeNode;
import org.apache.jmeter.protocol.http.control.RecordingController;
import org.apache.jmeter.threads.ThreadGroup;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class RecordingTargetFinderTest {

  private JMeterTreeModel model;
  private RecordingTargetFinder finder;
  private ThreadGroup baseThreadGroup = new ThreadGroup();

  @BeforeClass
  public static void setupClass() {
    JMeterTestUtils.setupJmeterEnv();
  }

  @Before
  public void setup() {}

  @Test
  public void shouldGetFirstThreadGroupWhenFindTargetControllerNodeWithThreadGroups() {
    model  = new JMeterTreeModel(baseThreadGroup, null);
    finder = new RecordingTargetFinder(model);
    JMeterTreeNode found = finder.findTargetControllerNode();

    assertEquals(ThreadGroup.class, found.getTestElement().getClass());
  }


  @Test
  public void shouldGetFirstRecordingControllerWhenFindTargetControllerNodeWithRecordingControllers() throws IllegalUserActionException {
    model  = new JMeterTreeModel(baseThreadGroup, null);
    JMeterTreeNode modelRoot = (JMeterTreeNode) model.getRoot();

    RecordingController firstRecordingController = new RecordingController();
    firstRecordingController.setName("First Recording Controller");
    RecordingController secondRecordingController = new RecordingController();
    secondRecordingController.setName("Second Recording Controller");

    model.addComponent(firstRecordingController, modelRoot);
    model.addComponent(secondRecordingController, modelRoot);

    finder = new RecordingTargetFinder(model);

    JMeterTreeNode found = finder.findTargetControllerNode();

    assertEquals(firstRecordingController, found.getTestElement());
  }

  @Test
  public void shouldGetRecordingControllerWhenFindTargetControllerNodeWithThreadGroupAndRecordingController() throws IllegalUserActionException {
    model  = new JMeterTreeModel(baseThreadGroup, null);
    JMeterTreeNode modelRoot = (JMeterTreeNode) model.getRoot();

    RecordingController firstRecordingController = new RecordingController();
    firstRecordingController.setName("First Recording Controller");

    ThreadGroup firstThreadGroup = new ThreadGroup();
    firstThreadGroup.setName("First Thread Groud");

    model.addComponent(firstRecordingController, modelRoot);
    model.addComponent(firstThreadGroup, modelRoot);

    finder = new RecordingTargetFinder(model);

    JMeterTreeNode found = finder.findTargetControllerNode();

    assertEquals(firstRecordingController, found.getTestElement());
  }

  @Test(expected = IllegalStateException.class)
  public void shouldThrowIllegalStateExceptionWhenFindTargetControllerNodeWithNoThreadGroupsOrRecordingControllers() {
    finder = new RecordingTargetFinder(new JMeterTreeModel());
    finder.findTargetControllerNode();
  }
}
