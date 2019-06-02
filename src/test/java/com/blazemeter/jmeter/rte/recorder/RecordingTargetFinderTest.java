//Backup RecordingTargetFinderTest
package com.blazemeter.jmeter.rte.recorder;

import org.apache.jmeter.gui.tree.JMeterTreeNode;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import javax.swing.tree.MutableTreeNode;

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
   *Additionally remove logic which checks target != null, while, etc (that is for grouping and we are not grouping).
   *
   *
   * Create a constructor of RTERecorder that receives a Supplier<TerminalEmulator> and is initialized by default to
   *Xtn5250TerminalEmulator::new, and use it instead of the new of Xtn5250TerminalEmulator. This will allow you
   *to provide a different supplier for tests which return mocked instances to TerminalEmulator.
   *Provide a mocked RecordingTargetFinder in constructor which always returns the same tree
   *node, and allows to verify elements added to it. In setup add the rte recorder to the
   *tree node and a mocked TestStateListener and SampleListener as children of recorder
   *in tree model, so you can verify if it is called in later test Provide a
   *protocolFactory (as we do in RTESampler) to be able to provide mocked
   *instances of client for verifying connect and getting expected
   *logic (status of keyboard etc)
   *
   * shouldAddRecorderAsEmulatorListenerWhenStart
   * shouldAddRteConfigToTargetControllerNodeWhenStart
   * shouldNotifyChildrenTestStateListenersWhenStart
   * shouldLockEmulatorKeyboardWhenStart
   * shouldStartEmulatorWhenStart
   * shouldConnectTerminalClientWhenStart
   * shouldAddAEmulatorUpdaterAsTerminalStateListenerWhenStart
   * shouldAddARequestListenerAsTerminalStateListenerWhenStart
   * shouldStopTerminalEmulatorWhenStartWithFailingTerminalClientConnect
   * shouldDisconnectTerminalClientWhenStartWithFailingTerminalClientConnect
   *
   *
   * shouldNotifyChildrenTestStateListenersWhenStop
   * shouldRemoveEmulatorUpdaterWhenStop
   * shouldStopEmulatorWhenStop
   * shouldDisconnectTerminalClientWhenStop
   *
   * shouldNotifyChildrenTestStateListenersWhenCloseTerminal
   * shouldRemoveEmulatorUpdaterWhenCloseTerminal
   * shouldStopEmulatorWhenCloseTerminal
   * shouldDisconnectTerminalClientWhenCloseTerminal
   * shouldNotifyRecordingListenerWhenCloseTerminal
   *
   * shouldLockEmulatorKeyboardWhenAttentionKey
   * shouldResetTerminalClientAlarmWhenAttentionKey
   * shouldNotifySuccessfulSampleResultToChildrenSampleListenersWhenAttentionKeyAndSuccessfulResult
   * shouldNotifyFailedSampleResultToChildrenSampleListenersWhenAttentionKeyAndFailingSendingToTerminalClient
   * (I think we have a bug here, an if is missing before RTESampler.updateSampleResultResponse)
   *
   * shouldAddSamplerToTargetControllerNodeWhenAttentionKey
   * shouldRegisterRequestListenerWhenAttentionKey
   * shouldSendInputsAndAttentionKeyToTerminalClientWhenAttentionKey
   * shouldSetEmulatorStatusMessageWhenAttentionKeyWithFailingTerminalClientSend
   *
   */
  @Mock
  JMeterTreeNode jMeterTreeNode;

  @BeforeClass
  public static void setupClass() {
    //JMeterTestUtils.setupJmeterEnv();
  }

  @Before
  public void setup() {
    MutableTreeNode treeNode = Mockito.mock(MutableTreeNode.class);
    jMeterTreeNode.add(treeNode);
  }

  @Test
  public void shouldGetFirstThreadGroupWhenFindTargetControllerNodeWithThreadGroups() {
    //RecordingTargetFinder recordingTargetFinder = new RecordingTargetFinder(jMeterTreeNode);
    //JMeterTreeNode targetControllerNode1 = recordingTargetFinder.findTargetControllerNode();
  }

  @Test
  public void shouldGetFirstRecordingControllerWhenFindTargetControllerNodeWithRecordingControllers() {
  }

  @Test
  public void shouldGetRecordingControllerWhenFindTargetControllerNodeWithThreadGroupAndRecordingController() {
  }

  @Test
  public void shouldGetGenericControllerWhenFindTargetControllerNodeWithRecordingControllerANdNestedGenericController() {
  }

  @Test
  public void shouldThrowIllegalStateExceptionWhenFindTargetControllerNodeWithNoThreadGroupsOrRecordingControllers() {
  }
}
