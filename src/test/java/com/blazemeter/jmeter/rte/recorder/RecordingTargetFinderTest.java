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
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RecordingTargetFinderTest {

  private JMeterTreeModel model;
  private RecordingTargetFinder finder;

  @Mock
  private RecordingController firstRecordingController;
  @Mock
  private RecordingController secondRecordingController;
  @Mock
  private ThreadGroup firstThreadGroup;
  @Mock
  private ThreadGroup baseThreadGroup;

  @BeforeClass
  public static void setupClass() {
    JMeterTestUtils.setupJmeterEnv();
  }

  @Before
  public void setup() {
    when(firstRecordingController.isEnabled()).thenReturn(true);
    when(secondRecordingController.isEnabled()).thenReturn(true);
    when(baseThreadGroup.isEnabled()).thenReturn(true);
  }

  @Test
  public void shouldGetFirstThreadGroupWhenFindTargetControllerNodeWithThreadGroups() {
    model  = new JMeterTreeModel(baseThreadGroup, null);
    finder = new RecordingTargetFinder(model);
    JMeterTreeNode found = finder.findTargetControllerNode();

    assertEquals(baseThreadGroup, found.getTestElement());
  }

  @Test
  public void shouldGetFirstRecordingControllerWhenFindTargetControllerNodeWithRecordingControllers() throws IllegalUserActionException {
    model  = new JMeterTreeModel(baseThreadGroup, null);
    JMeterTreeNode modelRoot = (JMeterTreeNode) model.getRoot();

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
