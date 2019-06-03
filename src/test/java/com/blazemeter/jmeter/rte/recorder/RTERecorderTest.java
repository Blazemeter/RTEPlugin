package com.blazemeter.jmeter.rte.recorder;

import com.blazemeter.jmeter.rte.JMeterTestUtils;
import com.blazemeter.jmeter.rte.core.Protocol;
import com.blazemeter.jmeter.rte.core.RteProtocolClient;
import com.blazemeter.jmeter.rte.recorder.emulator.TerminalEmulator;
import com.blazemeter.jmeter.rte.recorder.emulator.Xtn5250TerminalEmulator;
import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.tree.JMeterTreeModel;
import org.apache.jmeter.gui.tree.JMeterTreeNode;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.function.Function;
import java.util.function.Supplier;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RTERecorderTest {

  /**
   * TODO:
   * Comments
   * RTERecorder
   * [ ] Create a constructor of RTERecorder (Supplier<TerminalEmulator> supplier) and
   * is initialized by default to     Xtn5250TerminalEmulator::new
   * [ ] Use it instead of the new of Xtn5250TerminalEmulator.
   *
   * This will allow you to provide a different supplier for tests which return mocked
   * instances to TerminalEmulator.
   *
   * RTERecorder Test
   * [ ] Provide a MOCKED RecordingTargetFinder in constructor which always returns
   * the same tree node, and allows to verify elements added to it.
   *
   * [ ] In (@Before) setup add the RTERecorder to the tree node and a
   * MOCKED TestStateListener and SampleListener as children of recorder
   * in tree model, so you can verify if it is called in later test
   *
   * Provide a protocolFactory (as we do in RTESampler) to be able to provide
   * mocked instances of client for verifying connect and getting expected
   * logic (status of keyboard etc)
  **/

  private RTERecorder rteRecorder;

  //@Mock
  private Supplier<TerminalEmulator> terminalEmulatorSupplier;

  @Mock
  private RecordingTargetFinder finder;

  @Mock
  private JMeterTreeNode jMeterTreeNode;

  @Mock
  private TerminalEmulator mockTerminalEmulator;

  @Mock
  private JMeterTreeModel treeModel;

  private Function<Protocol, RteProtocolClient> protocolFactory;

  @BeforeClass
  public static void setupClass() {
    JMeterTestUtils.setupJmeterEnv();
  }

  @Before
  public void setup(){

    terminalEmulatorSupplier = Xtn5250TerminalEmulator::new;

    //prepareMocks();
    //terminalEmulatorSupplier = Xtn5250TerminalEmulator::new;

    //TODO: Move this to prepareMocks
    //when(terminalEmulatorSupplier.get()).thenReturn(mockTerminalEmulator);
    when(finder.findTargetControllerNode()).thenReturn(jMeterTreeNode);

    //finder = new RecordingTargetFinder(treeModel);
    rteRecorder = new RTERecorder(terminalEmulatorSupplier, finder);
  }

  public void prepareMocks(){
    GuiPackage instance = GuiPackage.getInstance();
    JMeterTreeModel treeModel = instance.getTreeModel();

    finder = new RecordingTargetFinder(treeModel);

    //We should be making them mocks instead of using real objects
    when(terminalEmulatorSupplier.get()).thenReturn(mockTerminalEmulator);

    //RecordingTargetFinder recordingTargetFinder = new RecordingTargetFinder(GuiPackage.getInstance().getTreeModel());
    //JMeterTreeNode targetControllerNode = recordingTargetFinder.findTargetControllerNode();
    //when(targetControllerNode.getUserObject()).thenReturn(JMeterTreeNode.class);
    //when(targetControllerNode.getChildCount()).thenReturn(0);
    //when(finder.findTargetControllerNode()).thenReturn(targetControllerNode);

  }

  @Test
  public void shouldAddRecorderAsEmulatorListenerWhenStart() throws Exception {
    rteRecorder.onRecordingStart();

    //verify(terminalEmulatorSupplier).get();
  }

  @Test
  public void shouldAddRteConfigToTargetControllerNodeWhenStart(){

  }

  @Test
  public void shouldNotifyChildrenTestStateListenersWhenStart(){}

  @Test
  public void shouldLockEmulatorKeyboardWhenStart(){
  }

  @Test
  public void shouldStartEmulatorWhenStart() throws Exception {
    rteRecorder.onRecordingStart();

    verify(mockTerminalEmulator).start();

  }

  @Test
  public void shouldConnectTerminalClientWhenStart(){}

  @Test
  public void shouldAddAEmulatorUpdaterAsTerminalStateListenerWhenStart(){}

  @Test
  public void shouldAddARequestListenerAsTerminalStateListenerWhenStart(){}

  @Test
  public void shouldStopTerminalEmulatorWhenStartWithFailingTerminalClientConnect(){}

  @Test
  public void shouldDisconnectTerminalClientWhenStartWithFailingTerminalClientConnect(){}

  @Test
  public void shouldNotifyChildrenTestStateListenersWhenStop(){}

  @Test
  public void shouldRemoveEmulatorUpdaterWhenStop(){}

  @Test
  public void shouldStopEmulatorWhenStop(){}

  @Test
  public void shouldDisconnectTerminalClientWhenStop(){}

  @Test
  public void shouldNotifyChildrenTestStateListenersWhenCloseTerminal(){}

  @Test
  public void shouldRemoveEmulatorUpdaterWhenCloseTerminal(){}

  @Test
  public void shouldStopEmulatorWhenCloseTerminal(){}

  @Test
  public void shouldDisconnectTerminalClientWhenCloseTerminal(){}

  @Test
  public void shouldNotifyRecordingListenerWhenCloseTerminal(){}

  @Test
  public void shouldLockEmulatorKeyboardWhenAttentionKey(){}

  @Test
  public void shouldResetTerminalClientAlarmWhenAttentionKey(){}

  @Test
  public void shouldNotifySuccessfulSampleResultToChildrenSampleListenersWhenAttentionKeyAndSuccessfulResult(){}

  @Test
  public void shouldNotifyFailedSampleResultToChildrenSampleListenersWhenAttentionKeyAndFailingSendingToTerminalClient (){}
//(I think we have a bug here, an if is missing before RTESampler.updateSampleResultResponse)

  @Test
  public void shouldAddSamplerToTargetControllerNodeWhenAttentionKey(){}

  @Test
  public void shouldRegisterRequestListenerWhenAttentionKey(){}

  @Test
  public void shouldSendInputsAndAttentionKeyToTerminalClientWhenAttentionKey(){}

  @Test
  public void shouldSetEmulatorStatusMessageWhenAttentionKeyWithFailingTerminalClientSend(){}

}
