package com.blazemeter.jmeter.rte.recorder;

import com.blazemeter.jmeter.rte.JMeterTestUtils;
import com.blazemeter.jmeter.rte.core.*;
import com.blazemeter.jmeter.rte.core.exceptions.RteIOException;
import com.blazemeter.jmeter.rte.core.listener.RequestListener;
import com.blazemeter.jmeter.rte.core.ssl.SSLType;
import com.blazemeter.jmeter.rte.recorder.emulator.TerminalEmulator;
import com.blazemeter.jmeter.rte.sampler.RTESampler;
import org.apache.jmeter.gui.tree.JMeterTreeModel;
import org.apache.jmeter.gui.tree.JMeterTreeNode;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestStateListener;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.*;
import java.util.function.Supplier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.internal.verification.VerificationModeFactory.times;

@RunWith(MockitoJUnitRunner.class)
public class RTERecorderTest {

  @Rule
  public final JUnitSoftAssertions softly = new JUnitSoftAssertions();
  private static final String SERVER = "localhost";
  private static final int PORT = 80;
  private static final Protocol PROTOCOL = Protocol.TN5250;
  private static final TerminalType TERMINAL_TYPE = PROTOCOL.createProtocolClient().getDefaultTerminalType();
  private static final SSLType SSL_TYPE = SSLType.NONE;
  private static final long TIMEOUT = 10000;
  private static final List<Input> INPUTS = Collections.singletonList(new CoordInput(new Position(2, 1), "testusr"));

  private TestElement mockedTestElementWithTestStateListener;
  private RTERecorder rteRecorder;

  @Mock
  private RecordingTargetFinder finder;

  @Mock
  private JMeterTreeModel mockedJMeterTreeModel;

  @Mock
  private JMeterTreeNode mockedJMeterTreeNode;

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

  @Mock
  private RecordingStateListener mockedRecorderListener;

  @BeforeClass
  public static void setupClass() {
    JMeterTestUtils.setupJmeterEnv();
  }

  @Before
  public void setup(){
    mockedTestElementWithTestStateListener = mock(TestElement.class, withSettings().extraInterfaces(TestStateListener.class));
    when(mockedFirstTreeNodeKid.getTestElement()).thenReturn(mockedTestElementWithTestStateListener);

    initializeChildrenList();

    Supplier<TerminalEmulator> terminalEmulatorSupplier = () -> mockedTerminalEmulator;
    rteRecorder = new RTERecorder(terminalEmulatorSupplier, finder, mockedJMeterTreeModel, p -> terminalClient);

    prepareRecorder();
  }

  public void initializeChildrenList(){
    when( mockedFirstTreeNodeKid.isEnabled()).thenReturn(true);
    when(mockedSecondTreeNodeKid.isEnabled()).thenReturn(true);
    when( mockedThirdTreeNodeKid.isEnabled()).thenReturn(true);

    when(mockedJMeterTreeNode.children()).thenReturn(Collections.enumeration(Arrays.asList(mockedFirstTreeNodeKid,
        mockedSecondTreeNodeKid, mockedThirdTreeNodeKid)));

    when(mockedJMeterTreeModel.getNodeOf(any())).thenReturn(mockedJMeterTreeNode);
    when(finder.findTargetControllerNode()).thenReturn(mockedJMeterTreeNode);
  }

  public void prepareRecorder(){
    rteRecorder.setPort(Integer.toString(PORT));
    rteRecorder.setTimeoutThresholdMillis(Long.toString(TIMEOUT));
    rteRecorder.setConnectionTimeout(Long.toString(TIMEOUT));
    rteRecorder.setServer(SERVER);
    rteRecorder.setSSLType(SSL_TYPE);
    rteRecorder.setTerminalType(TERMINAL_TYPE);
  }

  @Test
  public void shouldAddRecorderAsEmulatorListenerWhenStart() throws Exception {
    rteRecorder.onRecordingStart();

    verify(mockedTerminalEmulator).addTerminalEmulatorListener(rteRecorder);
  }

  @Test
  public void shouldAddRteConfigToTargetControllerNodeWhenStart() throws Exception {
    rteRecorder.onRecordingStart();

    ArgumentCaptor<TestElement> argument = ArgumentCaptor.forClass(TestElement.class);
    verify(mockedJMeterTreeModel).addComponent(argument.capture(), eq(mockedJMeterTreeNode));

    softly.assertThat(argument.getValue().getProperty(RTESampler.CONFIG_PORT).toString()).as("port").isEqualTo(Integer.toString(PORT));
    softly.assertThat(argument.getValue().getProperty(RTESampler.CONFIG_SSL_TYPE).toString()).as("sslType").isEqualTo(SSL_TYPE.name());
    softly.assertThat(Long.parseLong(argument.getValue().getProperty(RTESampler.CONFIG_CONNECTION_TIMEOUT).toString())).as("timeout").isEqualTo(TIMEOUT);
    softly.assertThat(argument.getValue().getProperty(RTESampler.CONFIG_PROTOCOL).getStringValue()).as("protocol").isEqualTo(PROTOCOL.name());
    softly.assertThat(argument.getValue().getProperty(RTESampler.CONFIG_SERVER).getStringValue()).as("server").isEqualTo(SERVER);
    softly.assertThat(argument.getValue().getProperty(RTESampler.CONFIG_TERMINAL_TYPE).getStringValue()).as("terminalType").isEqualTo(TERMINAL_TYPE.getId());
  }

  //TODO
  @Test
  public void shouldNotifyChildrenTestStateListenersWhenStart() throws Exception {
    rteRecorder.onRecordingStart();

    verify((TestStateListener) mockedTestElementWithTestStateListener).testStarted();
  }

  @Test
  public void shouldLockEmulatorKeyboardWhenStart() throws Exception {
    rteRecorder.onRecordingStart();

    verify(mockedTerminalEmulator).setKeyboardLock(true);
  }

  @Test
  public void shouldStartEmulatorWhenStart() throws Exception {
    rteRecorder.onRecordingStart();

    verify(mockedTerminalEmulator).start();
  }

  @Test
  public void shouldConnectTerminalClientWhenStart() throws Exception {
    rteRecorder.onRecordingStart();
    verify(terminalClient).connect(SERVER, PORT, SSL_TYPE, TERMINAL_TYPE, TIMEOUT);
  }

  /**
   *  For implementing these tests you would need to provide a supplier of
   * terminalEmulatorUpdater to be able to mock them and verify the invocation of onTerminalStateChange.
   * */
  @Test
  public void shouldNotifyTerminalEmulatorUpdaterWhenTerminalStateChange(){

  }

  @Test
  public void shouldNotifyTerminalEmulatorUpdaterWhenStart() throws Exception {
    rteRecorder.onRecordingStart();
  }

  @Test
  public void shouldAddConnectionSamplerWhenStartWithFailingTerminalClientConnect() throws Exception {
    try {
      doThrow(InterruptedException.class).when(terminalClient).connect(SERVER, PORT, SSL_TYPE,
          TERMINAL_TYPE, TIMEOUT);
      rteRecorder.onRecordingStart();
    } catch (Exception e) {
      //TODO: Fix the relation with the terminal client, since, even is the Client has a dothrow
      // The client don't throw any exception
    }
  }

  @Test
  public void shouldNotifyErrorResultToChildrenWhenStartWithFailingTerminalClientConnect() throws Exception {
    rteRecorder.onRecordingStart();

  }

  @Test
  public void shouldAddARequestListenerAsTerminalStateListenerWhenStart() throws Exception {
    rteRecorder.onRecordingStart();

    verify(terminalClient).addTerminalStateListener(any(RequestListener.class));
  }

  /**
   * TODO: Fix the issue in the test that the terminalEmulator.stop is not called.
   *  Probably is missing some step.
   *
   * Without the Try Catch, it throw this:
   * 12:51:51.500 [main] ERROR c.b.jmeter.rte.recorder.RTERecorder  - Problem while connecting to localhost
   * java.lang.InterruptedException: null
   *
   * java.lang.InterruptedException
   *
   *
   * Process finished with exit code 255
   * */
  @Test
  public void shouldDisconnectTerminalClientWhenStartWithFailingTerminalClientConnect() throws RteIOException {

    try {
      doThrow(InterruptedException.class).when(terminalClient).connect(SERVER, PORT, SSL_TYPE,
          TERMINAL_TYPE, TIMEOUT);
      rteRecorder.onRecordingStart();
    } catch (Exception e) {
      verify(terminalClient).disconnect();
    }
  }

  //TODO: Pending
  @Test
  public void shouldNotifyChildrenTestStateListenersWhenStop() throws Exception {
    rteRecorder.onRecordingStart();
    rteRecorder.onRecordingStop();

    TestStateListener mockTestStateListener = (TestStateListener) mockedTestElementWithTestStateListener;
    verify(mockTestStateListener).testEnded();
  }

  @Test
  public void shouldRemoveEmulatorUpdaterListenerWhenStop() throws Exception {
    rteRecorder.onRecordingStart();
    rteRecorder.onRecordingStop();

    verify(terminalClient).removeTerminalStateListener(rteRecorder);
  }

  @Test
  public void shouldStopEmulatorListenerWhenStop() throws Exception {
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

  //TODO: Pending
  @Test
  public void shouldNotifyChildrenTestStateListenersWhenCloseTerminal() throws Exception {
      rteRecorder.onRecordingStart();
      rteRecorder.onCloseTerminal();
      TestStateListener mockTestStateListener = (TestStateListener) mockedTestElementWithTestStateListener;

      verify(mockTestStateListener).testEnded();
  }

  @Test
  public void shouldRemoveEmulatorUpdaterWhenCloseTerminal() throws Exception {
    rteRecorder.onRecordingStart();
    rteRecorder.onCloseTerminal();

    verify(terminalClient).removeTerminalStateListener(rteRecorder);
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
    rteRecorder.setRecordingStateListener(mockedRecorderListener);
    rteRecorder.onRecordingStart();
    rteRecorder.onCloseTerminal();

    verify(mockedRecorderListener).onRecordingStop();
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
    verify(mockedTerminalEmulator, times(2)).setKeyboardLock(true);
  }


  @Test
  public void shouldResetTerminalClientAlarmWhenAttentionKey() throws Exception {
    rteRecorder.onRecordingStart();
    rteRecorder.onAttentionKey(AttentionKey.ENTER, INPUTS);

    verify(terminalClient).resetAlarm();
  }

  @Test
  public void shouldNotifySuccessfulSampleResultToChildrenSampleListenersWhenAttentionKeyAndSuccessfulResult() throws Exception {
    rteRecorder.onRecordingStart();

    rteRecorder.onAttentionKey(AttentionKey.ENTER, INPUTS);
  }

  @Test
  public void shouldNotifyFailedSampleResultToChildrenSampleListenersWhenAttentionKeyAndFailingSendingToTerminalClient (){
    //TODO: Review this comment (I think we have a bug here, an if is missing before RTESampler.updateSampleResultResponse)
  }


  @Test
  public void shouldAddSamplerToTargetControllerNodeWhenAttentionKey() throws Exception {
    rteRecorder.onRecordingStart();
    rteRecorder.onAttentionKey(AttentionKey.ENTER, INPUTS);

    /**
    * There were two interactions with the Model
     * 1st: When the Recording Starts, a new Test Element is added
     * 2nd: When the onAttentionKey is triggered, all the pending
     * samplers are recorded and thats when the Sampler is added
    * */
    verify(mockedJMeterTreeModel, times(2)).addComponent(any(), eq(mockedJMeterTreeNode));
  }

  @Test
  public void shouldNotifyPendingSampleAndDisconnectSampleToSampleListenersWhenStop(){

  }

  @Test
  public void shouldAddPendingSamplerAndDisconnectSamplerToTestPlanWhenStop(){

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
