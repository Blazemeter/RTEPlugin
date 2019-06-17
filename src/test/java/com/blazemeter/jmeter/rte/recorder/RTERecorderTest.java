package com.blazemeter.jmeter.rte.recorder;

import com.blazemeter.jmeter.rte.JMeterTestUtils;
import com.blazemeter.jmeter.rte.core.*;
import com.blazemeter.jmeter.rte.core.exceptions.RteIOException;
import com.blazemeter.jmeter.rte.core.listener.RequestListener;
import com.blazemeter.jmeter.rte.core.ssl.SSLType;
import com.blazemeter.jmeter.rte.recorder.emulator.TerminalEmulator;
import com.blazemeter.jmeter.rte.sampler.Action;
import com.blazemeter.jmeter.rte.sampler.RTESampler;
import org.apache.jmeter.gui.tree.JMeterTreeModel;
import org.apache.jmeter.gui.tree.JMeterTreeNode;
import org.apache.jmeter.samplers.SampleEvent;
import org.apache.jmeter.samplers.SampleListener;
import org.apache.jmeter.samplers.SampleResult;
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

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
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
  private static final List<Input> INPUTS = Collections.singletonList(new CoordInput(new Position(2, 1),
      "testusr"));

  private TestElement mockedTestStateListener;
  private TestElement mockedSamplerListener;
  private RTERecorder rteRecorder;

  private final String CONNECT_NAME = "bzm-RTE-CONNECT";
  private final String CONNECT_ACTION = "CONNECT";

  private final String DISCONNECT_NAME = "bzm-RTE-DISCONNECT";
  private final String DISCONNECT_ACTION = "DISCONNECT";

  private final String WAIT_SYNC_TIMEOUT = "10000";
  private final String INTERRUPTED_EXCEPTION_TEXT = "java.lang.InterruptedException";

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
  private TerminalEmulatorUpdater mockedTerminalEmulatorUpdater;
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
    initializeChildrenList();

    Supplier<TerminalEmulator> terminalEmulatorSupplier = () -> mockedTerminalEmulator;
    rteRecorder = new RTERecorder(terminalEmulatorSupplier, finder, mockedJMeterTreeModel, p -> terminalClient,
        mockedTerminalEmulatorUpdater);

    prepareRecorder();
  }

  public void initializeChildrenList(){
    mockedTestStateListener = mock(TestElement.class, withSettings().extraInterfaces(TestStateListener.class));
    mockedSamplerListener = mock(TestElement.class, withSettings().extraInterfaces(SampleListener.class));

    when(mockedFirstTreeNodeKid.getTestElement()).thenReturn(mockedTestStateListener);
    when(mockedSecondTreeNodeKid.getTestElement()).thenReturn(mockedSamplerListener);

    when( mockedFirstTreeNodeKid.isEnabled()).thenReturn(true);
    when(mockedSecondTreeNodeKid.isEnabled()).thenReturn(true);
    when( mockedThirdTreeNodeKid.isEnabled()).thenReturn(true);

    when(mockedJMeterTreeNode.children()).thenAnswer(a ->
        Collections.enumeration(Arrays.asList(mockedFirstTreeNodeKid, mockedSecondTreeNodeKid, mockedThirdTreeNodeKid)));
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

    assertConfigSampler(argument.getValue());
  }

  private void assertConfigSampler(TestElement argumentValue){
    softAssertTestElementProperty(argumentValue, RTESampler.CONFIG_PORT, "port", Integer.toString(PORT));
    softAssertTestElementProperty(argumentValue, RTESampler.CONFIG_SSL_TYPE, "sslType", SSL_TYPE.name());
    softAssertTestElementProperty(argumentValue, RTESampler.CONFIG_CONNECTION_TIMEOUT, "timeout", Long.toString(TIMEOUT));
    softAssertTestElementProperty(argumentValue, RTESampler.CONFIG_PROTOCOL, "protocol", PROTOCOL.name());
    softAssertTestElementProperty(argumentValue, RTESampler.CONFIG_SERVER, "server", SERVER);
    softAssertTestElementProperty(argumentValue, RTESampler.CONFIG_TERMINAL_TYPE, "terminalType", TERMINAL_TYPE.getId());
  }

  private void softAssertTestElementProperty(TestElement testElement, String propertyName, String nameTest,String expectedValue){
    softly.assertThat(testElement.getProperty(propertyName).toString()).as(nameTest).isEqualTo(expectedValue);
  }

  @Test
  public void shouldNotifyChildrenTestStateListenersWhenStart() throws Exception {
    rteRecorder.onRecordingStart();

    verify((TestStateListener) mockedTestStateListener).testStarted();
  }

  @Test
  public void shouldLockEmulatorKeyboardWhenStart() throws Exception {
    rteRecorder.onRecordingStart();

    verify(mockedTerminalEmulator).setKeyboardLock(true);
  }

  @Test
  public void shouldStartTerminalEmulatorWhenStart() throws Exception {
    rteRecorder.onRecordingStart();

    verify(mockedTerminalEmulator).start();
  }

  @Test
  public void shouldConnectTerminalClientWhenStart() throws Exception {
    rteRecorder.onRecordingStart();

    verify(terminalClient).connect(SERVER, PORT, SSL_TYPE, TERMINAL_TYPE, TIMEOUT);
  }

  @Test
  public void shouldNotifyTerminalEmulatorUpdaterWhenTerminalStateChange() throws Exception {
    rteRecorder.onRecordingStart();

    verify(mockedTerminalEmulatorUpdater).onTerminalStateChange();
  }

  @Test
  public void shouldNotifyErrorResultToChildrenWhenStartWithFailingTerminalClientConnect()
      throws Exception {
    try {
      doThrow(InterruptedException.class).when(terminalClient).connect(SERVER, PORT, SSL_TYPE,
          TERMINAL_TYPE, TIMEOUT);

      rteRecorder.onRecordingStart();
      fail("Excepted Exception not Thrown");
    } catch (InterruptedException e) {
      ArgumentCaptor<SampleEvent> argument = ArgumentCaptor.forClass(SampleEvent.class);
      verify((SampleListener) mockedSamplerListener).sampleOccurred(argument.capture());

      SampleResult connectingSample = argument.getValue().getResult();
      SampleResult expected = buildBasicSampleResult(Action.CONNECT);
      expected.setSuccessful(false);
      expected.setResponseCode(INTERRUPTED_EXCEPTION_TEXT);
      expected.setResponseMessage(null);
      expected.setDataType("text");
      expected.setResponseData(INTERRUPTED_EXCEPTION_TEXT+"\n", null);

      assertSampleResult(expected, connectingSample);
    }
  }

  @Test
    public void shouldNotifyFailedSampleResultToChildrenWhenAttentionKeyAndFailingSendingToTerminalClient ()
      throws Exception {
    rteRecorder.onRecordingStart();

    doThrow(UnsupportedOperationException.class).when(terminalClient).send(INPUTS, AttentionKey.ENTER);
    rteRecorder.onAttentionKey(AttentionKey.ENTER, INPUTS);

    ArgumentCaptor<SampleEvent> argument = ArgumentCaptor.forClass(SampleEvent.class);

    verify((SampleListener) mockedSamplerListener).sampleOccurred(argument.capture());

    SampleResult sampleResult = argument.getValue().getResult();
    SampleResult expected = buildBasicSampleResult(Action.CONNECT);
    expected.setDataType("text");

    assertSampleResult(expected, sampleResult);
  }

  @Test
  public void shouldNotifyTerminalEmulatorUpdaterWhenStart() throws Exception {
    rteRecorder.onRecordingStart();

    verify(mockedTerminalEmulatorUpdater).onTerminalStateChange();
  }

  @Test
  public void shouldAddConnectionSamplerWhenStartWithFailingTerminalClientConnect()
      throws Exception {
    try {
      doThrow(InterruptedException.class).when(terminalClient).connect(SERVER, PORT, SSL_TYPE,
          TERMINAL_TYPE, TIMEOUT);
      rteRecorder.onRecordingStart();
      fail("Excepted Exception not Thrown");
    } catch (Exception e) {
      ArgumentCaptor<TestElement> argument = ArgumentCaptor.forClass(TestElement.class);

      /*
      * The first time this method is called, is then the onRecordingStart is triggered, adding the
      * Connection Sampler
      * The second time occurs when the connection fails and the Recorder add anything that is
      * pending, in this case, an Empty Sampler is added
      * */
      verify(mockedJMeterTreeModel, times(2)).addComponent(argument.capture(), eq(mockedJMeterTreeNode));
      assertConnectionSampler(argument.getAllValues().get(1));
    }
  }



  @Test
  public void shouldAddARequestListenerAsTerminalStateListenerWhenStart() throws Exception {
    rteRecorder.onRecordingStart();

    verify(terminalClient).addTerminalStateListener(any(RequestListener.class));
  }


  @Test
  public void shouldDisconnectTerminalClientWhenStartWithFailingTerminalClientConnect() throws RteIOException {

    try {
      doThrow(InterruptedException.class).when(terminalClient).connect(SERVER, PORT, SSL_TYPE,
          TERMINAL_TYPE, TIMEOUT);
      rteRecorder.onRecordingStart();
      fail("Excepted Exception not thrown");
    } catch (Exception e) {
      verify(terminalClient).disconnect();
    }
  }


  @Test
  public void shouldNotifyChildrenTestStateListenersWhenStop() throws Exception {
    rteRecorder.onRecordingStart();
    rteRecorder.onRecordingStop();

    verify((TestStateListener) mockedTestStateListener).testEnded();
  }

  @Test
  public void shouldRemoveRecorderAsEmulatorListenerWhenStop() throws Exception {
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

  @Test
  public void shouldNotifyChildrenTestStateListenersWhenCloseTerminal() throws Exception {
      rteRecorder.onRecordingStart();
      rteRecorder.onCloseTerminal();

      verify((TestStateListener) mockedTestStateListener).testEnded();
  }

  @Test
  public void shouldRemoveRecorderAsEmulatorListenerWhenCloseTerminal() throws Exception {
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
  public void shouldNotifySuccessfulSampleResultToChildrenSampleListenersWhenAttentionKeyAndSuccessfulResult()
      throws Exception {
    rteRecorder.onRecordingStart();
    rteRecorder.onAttentionKey(AttentionKey.ENTER, INPUTS);

    ArgumentCaptor<SampleEvent> argument = ArgumentCaptor.forClass(SampleEvent.class);
    verify((SampleListener) mockedSamplerListener).sampleOccurred(argument.capture());

    SampleResult expected = buildBasicSampleResult(Action.CONNECT);
    expected.setDataType("text");

    RteSampleResult result = (RteSampleResult) argument.getAllValues().get(0).getResult();
    assertSampleResult(expected, result);
  }

  @Test
  public void shouldAddSamplerToTargetControllerNodeWhenAttentionKey() throws Exception {
    rteRecorder.onRecordingStart();
    rteRecorder.onAttentionKey(AttentionKey.ENTER, INPUTS);
    /*
    * There were two interactions with the Model
    * 1st: When the Recording Starts, a new Test Element is added
    * 2nd: When the onAttentionKey is triggered, all the pending
    * samplers are recorded and thats when the Sampler is added
    */
    ArgumentCaptor<TestElement> argument = ArgumentCaptor.forClass(TestElement.class);
    verify(mockedJMeterTreeModel, times(2)).addComponent(argument.capture(),
        eq(mockedJMeterTreeNode));

    assertConnectionSampler(argument.getValue());
  }

  @Test
  public void shouldNotifyPendingSampleAndDisconnectSampleToSampleListenersWhenStop()
      throws Exception {
    rteRecorder.onRecordingStart();
    rteRecorder.onRecordingStop();

    ArgumentCaptor<SampleEvent> argument = ArgumentCaptor.forClass(SampleEvent.class);

    verify( (SampleListener) mockedSamplerListener, times(2)).sampleOccurred(argument.capture());

    SampleResult expectedConnectSampler = buildBasicSampleResult(Action.CONNECT);
    expectedConnectSampler.setDataType("text");

    SampleResult expectedDisconnectSampler = buildBasicSampleResult(Action.DISCONNECT);
    ((RteSampleResult) expectedDisconnectSampler).setInputInhibitedRequest(false);

    SampleResult connect = argument.getAllValues().get(0).getResult();
    SampleResult disconnect = argument.getAllValues().get(1).getResult();

    assertSampleResult(expectedConnectSampler, connect);
    assertSampleResult(disconnect, expectedDisconnectSampler);
  }

  private RteSampleResult buildBasicSampleResult(Action action) {
    RteSampleResult base = new RteSampleResult();
    base.setAction(action);
    base.setSampleLabel("bzm-RTE-"+action.getLabel().toUpperCase());
    base.setProtocol(Protocol.TN5250);
    base.setTerminalType(new TerminalType("IBM-3179-2", new Dimension(80, 24)));
    base.setServer("localhost");
    base.setPort(80);
    base.setSslType(SSLType.NONE);
    base.setSuccessful(true);
    return base;
  }

  private void assertSampleResult(SampleResult expected, SampleResult result) {
    assertThat(result)
        .isEqualToComparingOnlyGivenFields(expected, "sampleLabel", "requestHeaders", "samplerData",
            "successful", "responseCode", "responseMessage", "responseHeaders", "dataType",
            "responseDataAsString");
  }

  @Test
  public void shouldAddPendingSamplerAndDisconnectSamplerToTestPlanWhenStop() throws Exception {
    rteRecorder.onRecordingStart();
    rteRecorder.onRecordingStop();

    ArgumentCaptor<TestElement> argument = ArgumentCaptor.forClass(TestElement.class);
    /*
    * There are 3 interactions on the addComponent because:
    * 1st: Corresponds to the ConfigTestElement
    * 2nd: Corresponds to the RTESampler for the Connect
    * 3nd: Corresponds to the RTESampler for the Disconnect
    */
    verify(mockedJMeterTreeModel, times(3)).addComponent(argument.capture(), eq(mockedJMeterTreeNode));
    verify(terminalClient).disconnect();

    assertConfigSampler(argument.getAllValues().get(0));
    assertConnectionSampler(argument.getAllValues().get(1));
    assertDisconnectionSampler(argument.getAllValues().get(2));
  }

  private void assertConnectionSampler(TestElement argumentValue){
    softAssertTestElementProperty(argumentValue, RTESampler.NAME, "name", CONNECT_NAME);
    softAssertTestElementProperty(argumentValue, RTESampler.ACTION_PROPERTY, "action", CONNECT_ACTION);
    softAssertTestElementProperty(argumentValue, "RTESampler.waitSyncTimeout", "waitSyncTimeout", WAIT_SYNC_TIMEOUT);
  }

  private void assertDisconnectionSampler(TestElement argumentValue){
    softAssertTestElementProperty(argumentValue, RTESampler.NAME, "name", DISCONNECT_NAME);
    softAssertTestElementProperty(argumentValue, RTESampler.ACTION_PROPERTY, "action", DISCONNECT_ACTION);
  }

  @Test
  public void shouldRegisterRequestListenerWhenAttentionKey() throws Exception {
    rteRecorder.onRecordingStart();
    rteRecorder.onAttentionKey(AttentionKey.ENTER, INPUTS);
    /*
     * The number of interactions with this clients is 2:
     * The first occur when the RTERecorder starts and one
     * registerRequestListenerFor comes into place
     *
     * The second interaction occur when the onAttentionKey
     * is triggered abd the registerRequestListenerFor is
     * invoked.
     */
    verify(terminalClient, times(2)).addTerminalStateListener(any(RequestListener.class));
  }

  @Test
  public void shouldSendInputsAndAttentionKeyToTerminalClientWhenAttentionKey() throws Exception {
    rteRecorder.onRecordingStart();
    rteRecorder.onAttentionKey(AttentionKey.ENTER, INPUTS);

    verify(terminalClient).send(INPUTS, AttentionKey.ENTER);
  }
}
