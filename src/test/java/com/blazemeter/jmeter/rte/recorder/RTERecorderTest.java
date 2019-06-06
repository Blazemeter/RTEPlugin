package com.blazemeter.jmeter.rte.recorder;

import com.blazemeter.jmeter.rte.JMeterTestUtils;
import com.blazemeter.jmeter.rte.core.*;
import com.blazemeter.jmeter.rte.core.ssl.SSLType;
import com.blazemeter.jmeter.rte.recorder.emulator.TerminalEmulator;
import org.apache.jmeter.gui.tree.JMeterTreeModel;
import org.apache.jmeter.gui.tree.JMeterTreeNode;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestStateListener;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.internal.verification.VerificationModeFactory.times;

@RunWith(MockitoJUnitRunner.class)
public class RTERecorderTest {

  /**
   * TODO:
   * Comments
   * RTERecorder
   * [X] Create a constructor of RTERecorder (Supplier<TerminalEmulator> supplier) and
   * is initialized by default to     Xtn5250TerminalEmulator::new
   * [X] Use it instead of the new of Xtn5250TerminalEmulator.
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

  @Mock
  private RecordingTargetFinder finder;

  @Mock
  private JMeterTreeModel mockedJMeterTreeModel;
  @Mock
  private JMeterTreeNode mockedJMeterTreeNode;

  private Enumeration<JMeterTreeNode> treeNodeChildrenList;
  private TestElement mockedTestElementWithTestStateListener;
  private ArrayList<JMeterTreeNode> childrenList;

  @Mock
  private JMeterTreeNode mockedFirstTreeNodeKid;

  @Mock
  private JMeterTreeNode mockedSecondTreeNodeKid;

  @Mock
  private JMeterTreeNode mockedThirdTreeNodeKid;


  @Mock
  private TerminalEmulator mockedTerminalEmulator;
  @Mock
  private RteProtocolClient terminalClient;



  @BeforeClass
  public static void setupClass() {
    JMeterTestUtils.setupJmeterEnv();
  }

  private final String server = "localhost";
  private final String port = "80";
  private final Protocol protocol = Protocol.TN5250;
  private final TerminalType terminalType = protocol.createProtocolClient().getDefaultTerminalType();
  private final SSLType sslType = SSLType.NONE;
  private final String timeout = "10000";
  private final String INVALID_SERVER = "Invalid server";

  private final List<Input> INPUTS = Collections.singletonList(new CoordInput(new Position(2, 1), "testusr"));


  @BeforeClass
  public static void prepareMocks(){

  }

  @Before
  public void setup(){
    //Mocking the Children of the Forest
    when( mockedFirstTreeNodeKid.isEnabled()).thenReturn(true);
    when(mockedSecondTreeNodeKid.isEnabled()).thenReturn(true);
    when( mockedThirdTreeNodeKid.isEnabled()).thenReturn(true);

    //First Kid: The Test State Listener
    mockedTestElementWithTestStateListener = mock(TestElement.class, withSettings().extraInterfaces(TestStateListener.class));
    when(mockedFirstTreeNodeKid.getTestElement()).thenReturn(mockedTestElementWithTestStateListener);

    //Second Kid: The Recording Listener
    //Third Kid:  The CCC Listener

    initializeChildrenList();

    //Definitions
    Supplier<TerminalEmulator> terminalEmulatorSupplier = () -> mockedTerminalEmulator;
    rteRecorder = new RTERecorder(terminalEmulatorSupplier, finder, mockedJMeterTreeModel, terminalClient);

    prepareRecorder();
  }

  public void initializeChildrenList(){
    childrenList = new ArrayList<>();
    childrenList.add(mockedFirstTreeNodeKid);
    childrenList.add(mockedSecondTreeNodeKid);
    childrenList.add(mockedThirdTreeNodeKid);
    treeNodeChildrenList = Collections.enumeration(childrenList);

    //The ones that holds them all
    when(mockedJMeterTreeNode.children()).thenReturn(treeNodeChildrenList);
    when(mockedJMeterTreeModel.getNodeOf(any())).thenReturn(mockedJMeterTreeNode);
    when(finder.findTargetControllerNode()).thenReturn(mockedJMeterTreeNode);
  }

  public void prepareRecorder(){
    rteRecorder.setPort(port);
    rteRecorder.setTimeoutThresholdMillis(timeout);
    rteRecorder.setConnectionTimeout(timeout);
    rteRecorder.setServer(server);
    rteRecorder.setSSLType(sslType);
    rteRecorder.setTerminalType(terminalType);
  }

  @Test
  public void shouldAddRecorderAsEmulatorListenerWhenStart() throws Exception {
    rteRecorder.onRecordingStart();
    verify(mockedTerminalEmulator).addTerminalEmulatorListener(rteRecorder);
  }

  //TODO
  @Test
  public void shouldAddRteConfigToTargetControllerNodeWhenStart() throws Exception {
    rteRecorder.onRecordingStart(); //Realmente no lo agrega porque el Node resulta null
  }

  //TODO
  @Test
  public void shouldNotifyChildrenTestStateListenersWhenStart() throws Exception {
    rteRecorder.onRecordingStart();

    TestStateListener mockTestStateListener = (TestStateListener) mockedTestElementWithTestStateListener;
    verify(mockTestStateListener).testStarted();
  }

  @Test
  public void shouldLockEmulatorKeyboardWhenStart() throws Exception {
    rteRecorder.onRecordingStart();
    /*
     * The block of the Keyboard occurs 1 time1:
     * At the beginning when the terminal setup (initTerminalEmulator)
     * */
    verify(mockedTerminalEmulator, times(1)).setKeyboardLock(true);
  }

  @Test
  public void shouldStartEmulatorWhenStart() throws Exception {
    rteRecorder.onRecordingStart();

    verify(mockedTerminalEmulator).start();
  }

  @Test
  public void shouldConnectTerminalClientWhenStart() throws Exception {
    rteRecorder.onRecordingStart();
    verify(terminalClient).connect(server, Integer.parseInt(port), sslType, terminalType, Long.parseLong(timeout));
  }

  @Test
  public void shouldAddAEmulatorUpdaterAsTerminalStateListenerWhenStart() throws Exception {
    rteRecorder.onRecordingStart();

    verify(mockedTerminalEmulator).addTerminalEmulatorListener(rteRecorder);
  }

  @Test
  public void shouldAddARequestListenerAsTerminalStateListenerWhenStart() throws Exception {
    rteRecorder.onRecordingStart();

    verify(terminalClient).addTerminalStateListener(rteRecorder);
  }

  @Test
  public void shouldStopTerminalEmulatorWhenStartWithFailingTerminalClientConnect() throws Exception {
    rteRecorder.setServer(INVALID_SERVER);
    rteRecorder.onRecordingStart();

    verify(mockedTerminalEmulator).stop();
  }

  @Test
  public void shouldDisconnectTerminalClientWhenStartWithFailingTerminalClientConnect(){
    //assertTrue(false);
  }

  @Test
  public void shouldNotifyChildrenTestStateListenersWhenStop() throws Exception {
    rteRecorder.onRecordingStart();
    initializeChildrenList();

    rteRecorder.onRecordingStop();

    TestStateListener mockTestStateListener = (TestStateListener) mockedTestElementWithTestStateListener;
    verify(mockTestStateListener).testEnded();
  }

  @Test
  public void shouldRemoveEmulatorUpdaterWhenStop() throws Exception {
    rteRecorder.onRecordingStart();
    rteRecorder.onRecordingStop();

    verify(terminalClient).removeTerminalStateListener(rteRecorder);
  }

  @Test
  public void shouldStopEmulatorWhenStop() throws Exception {
    rteRecorder.onRecordingStart();
    rteRecorder.onRecordingStop();

    verify(mockedTerminalEmulator).stop();
  }

  @Test
  public void shouldDisconnectTerminalClientWhenStop() throws Exception {
    rteRecorder.onRecordingStart();
    rteRecorder.onRecordingStop();

    verify(terminalClient).disconnect();
  }

  @Test
  public void shouldNotifyChildrenTestStateListenersWhenCloseTerminal(){
  }

  @Test
  public void shouldRemoveEmulatorUpdaterWhenCloseTerminal(){
  }

  @Test
  public void shouldStopEmulatorWhenCloseTerminal() throws Exception {
    rteRecorder.onRecordingStart();
    rteRecorder.onCloseTerminal();

    verify(mockedTerminalEmulator).stop();
  }

  @Test
  public void shouldDisconnectTerminalClientWhenCloseTerminal() throws Exception {
    rteRecorder.onRecordingStart();
    rteRecorder.onCloseTerminal();

    verify(terminalClient).disconnect();
  }

  @Test
  public void shouldNotifyRecordingListenerWhenCloseTerminal() throws Exception {
    rteRecorder.onRecordingStart();
    rteRecorder.onCloseTerminal();

    //TODO: Mock the Recording Listener
    //TODO: Verify the listener been notified
  }

  @Test
  public void shouldLockEmulatorKeyboardWhenAttentionKey() throws Exception {
    rteRecorder.onRecordingStart();
    rteRecorder.onAttentionKey(AttentionKey.ENTER, INPUTS);

    /*
     * The block of the Keyboard occurs 2 times:
     * At the beginning when the terminal setup (initTerminalEmulator)
     * And when the attention key happens
     * */
    verify(mockedTerminalEmulator, times(1)).setKeyboardLock(true);
  }


  @Test
  public void shouldResetTerminalClientAlarmWhenAttentionKey() throws Exception {
    rteRecorder.onRecordingStart();
    rteRecorder.onAttentionKey(AttentionKey.ENTER, INPUTS);

    verify(terminalClient).resetAlarm();
  }

  @Test
  public void shouldNotifySuccessfulSampleResultToChildrenSampleListenersWhenAttentionKeyAndSuccessfulResult(){
  }

  @Test
  public void shouldNotifyFailedSampleResultToChildrenSampleListenersWhenAttentionKeyAndFailingSendingToTerminalClient (){
    //TODO: Review this comment (I think we have a bug here, an if is missing before RTESampler.updateSampleResultResponse)
  }


  @Test
  public void shouldAddSamplerToTargetControllerNodeWhenAttentionKey() throws Exception {
    rteRecorder.onRecordingStart();
    rteRecorder.onAttentionKey(AttentionKey.ENTER, INPUTS);
    //assertEquals(true, false);
  }

  @Test
  public void shouldRegisterRequestListenerWhenAttentionKey() throws Exception {
    rteRecorder.onRecordingStart();
    rteRecorder.onAttentionKey(AttentionKey.ENTER, INPUTS);


    /**
     * The number of interactions with this clients is 7 but, is divided in 2 events or triggers
     * The first 4 occur when the RTERecorder starts recording:
     *  There is 2 interactions when the WaitConditionRecorder is triggered
     *  another 1 when the method initTerminalEmulator is called
     *  and finally 1 for the registerRequestListenerFor comes into place
     *
     * The remaining 3 interactions occur based on these events:
     * 1 for the method registerRequestListenerFor been called
     * and the remaining 2 for the WaitConditionRecorder been called
     * */
    verify(terminalClient, times(7)).addTerminalStateListener(any());
  }


  @Test
  public void shouldSendInputsAndAttentionKeyToTerminalClientWhenAttentionKey() throws Exception {
    rteRecorder.onRecordingStart();
    rteRecorder.onAttentionKey(AttentionKey.ENTER, INPUTS);

    verify(terminalClient).send(INPUTS, AttentionKey.ENTER);
  }


  @Test
  public void shouldSetEmulatorStatusMessageWhenAttentionKeyWithFailingTerminalClientSend() throws Exception {
    rteRecorder.onRecordingStart();
    rteRecorder.onAttentionKey(AttentionKey.ENTER, INPUTS);

    /*
    * TODO: Review how to generate the FailingTerminal.
    *  And replace the any() with the actual message
    */
    //verify(mockedTerminalEmulator).setStatusMessage(any());
  }

}
