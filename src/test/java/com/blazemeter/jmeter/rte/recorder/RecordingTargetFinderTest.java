//Backup RecordingTargetFinderTest
package com.blazemeter.jmeter.rte.recorder;

import com.blazemeter.jmeter.rte.JMeterTestUtils;
import org.apache.jmeter.exceptions.IllegalUserActionException;
import org.apache.jmeter.gui.tree.JMeterTreeModel;
import org.apache.jmeter.gui.tree.JMeterTreeNode;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestStateListener;
import org.apache.jmeter.threads.AbstractThreadGroup;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;

import javax.swing.tree.MutableTreeNode;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

public class RecordingTargetFinderTest {


  /*
   * COMMENTS
   *
   * Create a class RecordingTargetFinder and move findTargetControllerNode and findFirstNodeOfType to it, inject it
   *in RTERecorder in constructor for usage, and implement RecordingTargetFinderTest
   *
   * shouldGetFirstThreadGroupWhenFindTargetControllerNodeWithThreadGroups
   * shouldGetFirstRecordingControllerWhenFindTargetControllerNodeWithRecordingControllers
   * shouldGetRecordingControllerWhenFindTargetControllerNodeWithThreadGroupAndRecordingController
   * shouldGetGenericControllerWhenFindTargetControllerNodeWithRecordingControllerANdNestedGenericController
   * shouldThrowIllegalStateExceptionWhenFindTargetControllerNodeWithNoThreadGroupsOrRecordingControllers
   *
   * would need to initialize jmeter environment and check on jmeter treemodel, we have never tested this. Another
   *option might be to pass JMeterTreeModel in constructor to RecordingTargetFinder and in tests you can use a
   *custom instance of the tree model instead of getting it from singleton and checking against that, that
   *might be cleaner.
   *
   *
   */

  RecordingTargetFinder finder;

  @Mock
  private JMeterTreeModel jMeterTreeModel;

  private JMeterTreeNode mockedTargetNode; //Doubt, do I need to add the @Mock?
  @Mock
  private JMeterTreeNode mockedSecondNode;
  @Mock
  private JMeterTreeNode mockedThirdNode;


  @Mock
  private TestElement tp;
  @Mock
  private TestElement wp;

  @BeforeClass
  public static void setupClass() {
    JMeterTestUtils.setupJmeterEnv();
  }

  /*
  * TODO: DELETE ME if this apporach works
  * */
  public void setupBackup(){
    mockedTargetNode = mock(JMeterTreeNode.class, withSettings().extraInterfaces(AbstractThreadGroup.class));

    List<JMeterTreeNode> nodes = new ArrayList<>();
    nodes.add(mockedTargetNode);
    nodes.add(mockedSecondNode);
    nodes.add(mockedThirdNode);


    when(jMeterTreeModel.getNodesOfType(AbstractThreadGroup.class)).thenReturn(nodes);

    finder = new RecordingTargetFinder(jMeterTreeModel);
  }

  @Before
  public void setup() throws IllegalUserActionException {
    JMeterTreeModel model = new  JMeterTreeModel(tp, wp);
    model.addComponent(tp, mockedTargetNode);
    model.addComponent(wp, mockedSecondNode);
    model.addComponent(tp, mockedThirdNode);

  }

  @Test
  public void shouldGetFirstThreadGroupWhenFindTargetControllerNodeWithThreadGroups() {

    JMeterTreeNode found = finder.findTargetControllerNode();

    assertEquals(mockedTargetNode, found);
  }

  @Test
  public void shouldGetFirstRecordingControllerWhenFindTargetControllerNodeWithRecordingControllers() {
    JMeterTreeNode targetControllerNode = finder.findTargetControllerNode();
  }

  @Test
  public void shouldGetRecordingControllerWhenFindTargetControllerNodeWithThreadGroupAndRecordingController() {
    JMeterTreeNode targetControllerNode = finder.findTargetControllerNode();
  }

  @Test
  public void shouldGetGenericControllerWhenFindTargetControllerNodeWithRecordingControllerANdNestedGenericController() {
    JMeterTreeNode targetControllerNode = finder.findTargetControllerNode();
  }

  @Test
  public void shouldThrowIllegalStateExceptionWhenFindTargetControllerNodeWithNoThreadGroupsOrRecordingControllers() {
    JMeterTreeNode targetControllerNode = finder.findTargetControllerNode();
  }
}
