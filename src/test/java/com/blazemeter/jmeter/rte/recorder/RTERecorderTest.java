package com.blazemeter.jmeter.rte.recorder;

import static com.blazemeter.jmeter.rte.SampleResultAssertions.assertSampleResult;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;
import static org.mockito.internal.verification.VerificationModeFactory.times;

import com.blazemeter.jmeter.rte.JMeterTestUtils;
import com.blazemeter.jmeter.rte.core.AttentionKey;
import com.blazemeter.jmeter.rte.core.CoordInput;
import com.blazemeter.jmeter.rte.core.Input;
import com.blazemeter.jmeter.rte.core.Position;
import com.blazemeter.jmeter.rte.core.Protocol;
import com.blazemeter.jmeter.rte.core.RteProtocolClient;
import com.blazemeter.jmeter.rte.core.RteSampleResultBuilder;
import com.blazemeter.jmeter.rte.core.Screen;
import com.blazemeter.jmeter.rte.core.TerminalType;
import com.blazemeter.jmeter.rte.core.exceptions.RteIOException;
import com.blazemeter.jmeter.rte.core.listener.RequestListener;
import com.blazemeter.jmeter.rte.core.ssl.SSLType;
import com.blazemeter.jmeter.rte.recorder.emulator.TerminalEmulator;
import com.blazemeter.jmeter.rte.sampler.Action;
import com.blazemeter.jmeter.rte.sampler.RTESampler;
import com.blazemeter.jmeter.rte.sampler.gui.RTEConfigGui;
import com.blazemeter.jmeter.rte.sampler.gui.RTESamplerGui;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;
import org.apache.jmeter.assertions.ResponseAssertion;
import org.apache.jmeter.assertions.gui.AssertionGui;
import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.exceptions.IllegalUserActionException;
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

@RunWith(MockitoJUnitRunner.class)
public class RTERecorderTest {

  public static final String SEND_INPUT_1 = "bzm-RTE-SEND_INPUT-1";
  public static final String SENDING_USER = "SENDING_USER";
  private static final String ASSERTION_NAME = "Assertion Test";
  private static final String SELECTED_TEXT = "selected text";
  private static final Screen TEST_SCREEN = Screen.valueOf("test\n");
  private static final String SERVER = "localhost";
  private static final int PORT = 80;
  private static final Protocol PROTOCOL = Protocol.TN5250;
  private static final TerminalType TERMINAL_TYPE = PROTOCOL.createProtocolClient()
      .getDefaultTerminalType();
  private static final SSLType SSL_TYPE = SSLType.NONE;
  private static final long TIMEOUT = 10000;
  private static final Position CURSOR_POSITION = new Position(2, 1);
  private static final List<Input> INPUTS = Collections
      .singletonList(new CoordInput(CURSOR_POSITION, "testusr"));
  @Rule
  public final JUnitSoftAssertions softly = new JUnitSoftAssertions();
  private TestElement testStateListener;
  private TestElement samplerListener;
  private RTERecorder rteRecorder;

  @Mock
  private RecordingTargetFinder finder;
  @Mock
  private JMeterTreeModel treeModel;
  @Mock
  private JMeterTreeNode treeNode;
  @Mock
  private TerminalEmulator terminalEmulator;
  @Mock
  private RteProtocolClient terminalClient;
  @Mock
  private RecordingStateListener recorderListener;
  @Mock
  private JMeterTreeNode samplerNode;

  @BeforeClass
  public static void setupClass() {
    JMeterTestUtils.setupJmeterEnv();
  }

  @Before
  public void setup() throws IllegalUserActionException {
    setupTreeModel();
    when(finder.findTargetControllerNode(treeModel)).thenReturn(treeNode);
    setupTerminalClient();
    when(treeModel.addComponent(any(), eq(treeNode))).thenReturn(samplerNode);
    Supplier<TerminalEmulator> terminalEmulatorSupplier = () -> terminalEmulator;
    rteRecorder = new RTERecorder(terminalEmulatorSupplier, finder,
        p -> terminalClient, treeModel);
    setupRecorder();
  }

  public void setupTreeModel() {
    testStateListener = buildTestElementMock(TestStateListener.class);
    samplerListener = buildTestElementMock(SampleListener.class);
    JMeterTreeNode testStateListenerTreeNode = buildTestElementTreeNode(testStateListener);
    JMeterTreeNode samplerListenerTreeNode = buildTestElementTreeNode(samplerListener);
    when(treeNode.children()).thenAnswer(a ->
        Collections.enumeration(Arrays.asList(testStateListenerTreeNode, samplerListenerTreeNode)));
    when(treeModel.getNodeOf(any())).thenReturn(treeNode);
  }

  private TestElement buildTestElementMock(Class<?> extraInterface) {
    return mock(TestElement.class, withSettings().extraInterfaces(extraInterface));
  }

  private JMeterTreeNode buildTestElementTreeNode(TestElement testStateListener) {
    JMeterTreeNode testStateListenerTreeNode = mock(JMeterTreeNode.class);
    when(testStateListenerTreeNode.getTestElement()).thenReturn(testStateListener);
    when(testStateListenerTreeNode.isEnabled()).thenReturn(true);
    return testStateListenerTreeNode;
  }

  private void setupTerminalClient() {
    when(terminalClient.getScreen()).thenReturn(TEST_SCREEN);
    when(terminalClient.getCursorPosition()).thenReturn(Optional.of(CURSOR_POSITION));
    when(terminalClient.isAlarmOn()).thenReturn(true);
  }

  public void setupRecorder() {
    rteRecorder.setPort(Integer.toString(PORT));
    rteRecorder.setTimeoutThresholdMillis(Long.toString(TIMEOUT));
    rteRecorder.setConnectionTimeout(Long.toString(TIMEOUT));
    rteRecorder.setServer(SERVER);
    rteRecorder.setSSLType(SSL_TYPE);
    rteRecorder.setTerminalType(TERMINAL_TYPE);
  }

  @Test
  public void shouldAddRecorderAsEmulatorListenerWhenStart() throws Exception {
    connect();
    verify(terminalEmulator).addTerminalEmulatorListener(rteRecorder);
  }

  private void connect() throws TimeoutException, InterruptedException {
    rteRecorder.onRecordingStart();
    rteRecorder.awaitConnected(TIMEOUT);
  }

  @Test
  public void shouldAddRteConfigToTargetControllerNodeWhenStart() throws Exception {
    connect();
    ArgumentCaptor<TestElement> argument = ArgumentCaptor.forClass(TestElement.class);
    verify(treeModel).addComponent(argument.capture(), eq(treeNode));
    assertThat(argument.getValue()).isEqualTo(buildExpectedConfig());
  }

  private ConfigTestElement buildExpectedConfig() {
    ConfigTestElement expectedConfig = new ConfigTestElement();
    expectedConfig.setName("bzm-RTE-config");
    expectedConfig.setProperty(TestElement.GUI_CLASS, RTEConfigGui.class.getName());
    expectedConfig.setProperty(RTESampler.CONFIG_SERVER, SERVER);
    expectedConfig.setProperty(RTESampler.CONFIG_PORT, Integer.toString(PORT));
    expectedConfig.setProperty(RTESampler.CONFIG_PROTOCOL, PROTOCOL.name());
    expectedConfig.setProperty(RTESampler.CONFIG_SSL_TYPE, SSL_TYPE.name());
    expectedConfig.setProperty(RTESampler.CONFIG_TERMINAL_TYPE, TERMINAL_TYPE.getId());
    expectedConfig.setProperty(RTESampler.CONFIG_CONNECTION_TIMEOUT, Long.toString(TIMEOUT));
    return expectedConfig;
  }

  @Test
  public void shouldNotifyChildrenTestStartWhenStart() throws Exception {
    connect();
    verify((TestStateListener) testStateListener).testStarted();
  }

  @Test
  public void shouldLockEmulatorKeyboardWhenStart() throws Exception {
    connect();
    verify(terminalEmulator).setKeyboardLock(true);
  }

  @Test
  public void shouldStartTerminalEmulatorWhenStart() throws Exception {
    connect();
    verify(terminalEmulator).start();
  }

  @Test
  public void shouldConnectTerminalClientWhenStart() throws Exception {
    connect();
    verify(terminalClient).connect(SERVER, PORT, SSL_TYPE, TERMINAL_TYPE, TIMEOUT);
  }

  @Test
  public void shouldSetEmulatorScreenWhenTerminalStateChange() throws Exception {
    connect();
    verify(terminalEmulator).setScreen(TEST_SCREEN, SEND_INPUT_1);
  }

  @Test
  public void shouldSetEmulatorKeyboardLockWhenTerminalStateChange() throws Exception {
    connect();
    verify(terminalEmulator).setKeyboardLock(false);
  }

  @Test
  public void shouldSoundEmulatorAlarmWhenTerminalStateChange() throws Exception {
    connect();
    verify(terminalEmulator).soundAlarm();
  }

  @Test
  public void shouldSetEmulatorCursorWhenTerminalStateChange() throws Exception {
    connect();
    verify(terminalEmulator).setCursor(CURSOR_POSITION.getRow(), CURSOR_POSITION.getColumn());
  }

  @Test
  public void shouldNotifyErrorResultToChildrenWhenStartWithFailingTerminalClientConnect()
      throws Exception {
    RteIOException connectionException = new RteIOException(null, "localhost");
    doThrow(connectionException).when(terminalClient)
        .connect(anyString(), anyInt(), any(), any(), anyLong());
    connect();
    ArgumentCaptor<SampleEvent> argument = ArgumentCaptor.forClass(SampleEvent.class);
    verify((SampleListener) samplerListener).sampleOccurred(argument.capture());
    assertSampleResult(buildConnectionErrorResult(connectionException),
        argument.getValue().getResult());
  }

  private SampleResult buildConnectionErrorResult(RteIOException connectionException) {
    return buildBasicSampleResultBuilder(Action.CONNECT)
        .withFailure(connectionException)
        .build();
  }

  private RteSampleResultBuilder buildBasicSampleResultBuilder(Action action) {
    return new RteSampleResultBuilder()
        .withAction(action)
        .withLabel("bzm-RTE-" + action.name())
        .withProtocol(Protocol.TN5250)
        .withTerminalType(new TerminalType("IBM-3179-2", new Dimension(80, 24)))
        .withServer("localhost")
        .withPort(80)
        .withSslType(SSLType.NONE);
  }

  @Test
  public void shouldSetEmulatorScreenWhenStart() throws Exception {
    connect();
    verify(terminalEmulator).setScreen(TEST_SCREEN, SEND_INPUT_1);
  }

  @Test
  public void shouldSetEmulatorCursorWhenStart() throws Exception {
    connect();
    verify(terminalEmulator).setCursor(CURSOR_POSITION.getRow(), CURSOR_POSITION.getColumn());
  }

  @Test
  public void shouldSetEmulatorKeyboardLockWhenStart() throws Exception {
    connect();
    verify(terminalEmulator).setKeyboardLock(false);
  }

  @Test
  public void shouldSoundEmulatorAlarmWhenStart() throws Exception {
    connect();
    verify(terminalEmulator).soundAlarm();
  }

  @Test
  public void shouldAddConnectionSamplerWhenStartWithFailingTerminalClientConnect()
      throws Exception {
    doThrow(RteIOException.class).when(terminalClient).connect(SERVER, PORT, SSL_TYPE,
        TERMINAL_TYPE, TIMEOUT);
    connect();
    ArgumentCaptor<TestElement> argument = ArgumentCaptor.forClass(TestElement.class);

    /*
     * The first time this method is called, is then the onRecordingStart is triggered, adding the
     * Connection Sampler
     * The second time occurs when the connection fails and the Recorder add anything that is
     * pending, in this case, an Empty Sampler is added
     * */
    verify(treeModel, times(2))
        .addComponent(argument.capture(), eq(treeNode));
    assertThat(argument.getAllValues().get(1)).isEqualTo(buildExpectedConnectionSampler());
  }

  private RTESampler buildExpectedConnectionSampler() {
    RTESampler sampler = buildExpectedSampler("bzm-RTE-CONNECT", Action.CONNECT);
    sampler.setWaitSync(true);
    sampler.setWaitSyncTimeout(String.valueOf(TIMEOUT));
    return sampler;
  }

  private RTESampler buildExpectedSampler(String name, Action action) {
    RTESampler sampler = new RTESampler();
    sampler.setProperty(TestElement.GUI_CLASS, RTESamplerGui.class.getCanonicalName());
    sampler.setProperty(TestElement.TEST_CLASS, RTESampler.class.getCanonicalName());
    sampler.setName(name);
    sampler.setAction(action);
    return sampler;
  }

  @Test
  public void shouldAddARequestListenerAsTerminalStateListenerWhenStart() throws Exception {
    connect();
    verify(terminalClient).addTerminalStateListener(any(RequestListener.class));
  }

  @Test
  public void shouldDisconnectTerminalClientWhenStartWithFailingTerminalClientConnect()
      throws Exception {
    doThrow(RteIOException.class).when(terminalClient).connect(SERVER, PORT, SSL_TYPE,
        TERMINAL_TYPE, TIMEOUT);
    connect();
    verify(terminalClient).disconnect();
  }

  @Test
  public void shouldNotifyChildrenTestEndWhenStop() throws Exception {
    connect();
    rteRecorder.onRecordingStop();
    verify((TestStateListener) testStateListener).testEnded();
  }

  @Test
  public void shouldRemoveRecorderAsEmulatorListenerWhenStop() throws Exception {
    connect();
    rteRecorder.onRecordingStop();
    verify(terminalClient).removeTerminalStateListener(rteRecorder);
  }

  @Test
  public void shouldStopEmulatorListenerWhenStop() throws Exception {
    connect();
    rteRecorder.onRecordingStop();
    verify(terminalEmulator).stop();
  }

  @Test
  public void shouldDisconnectTerminalClientWhenStop() throws Exception {
    connect();
    rteRecorder.onRecordingStop();
    verify(terminalClient).disconnect();
  }

  @Test
  public void shouldNotifyChildrenTestEndWhenCloseTerminal() throws Exception {
    connect();
    rteRecorder.onCloseTerminal();
    verify((TestStateListener) testStateListener).testEnded();
  }

  @Test
  public void shouldRemoveRecorderAsEmulatorListenerWhenCloseTerminal() throws Exception {
    connect();
    rteRecorder.onCloseTerminal();
    verify(terminalClient).removeTerminalStateListener(rteRecorder);
  }

  @Test
  public void shouldStopEmulatorWhenCloseTerminal() throws Exception {
    connect();
    rteRecorder.onCloseTerminal();
    verify(terminalEmulator).stop();
  }

  @Test
  public void shouldDisconnectTerminalClientWhenCloseTerminal() throws Exception {
    connect();
    rteRecorder.onCloseTerminal();
    verify(terminalClient).disconnect();
  }

  @Test
  public void shouldNotifyRecordingListenerWhenCloseTerminal() throws Exception {
    rteRecorder.setRecordingStateListener(recorderListener);
    connect();
    rteRecorder.onCloseTerminal();
    verify(recorderListener).onRecordingStop();
  }

  @Test
  public void shouldLockEmulatorKeyboardWhenAttentionKey() throws Exception {
    connect();
    rteRecorder.onAttentionKey(AttentionKey.ENTER, INPUTS, "");
    /*
     * The block of the Keyboard occurs 2 times:
     * At the beginning when the terminal setup (initTerminalEmulator)
     * And when the attention key happens
     * */
    verify(terminalEmulator, times(2)).setKeyboardLock(true);
  }


  @Test
  public void shouldResetTerminalClientAlarmWhenAttentionKey() throws Exception {
    connect();
    rteRecorder.onAttentionKey(AttentionKey.ENTER, INPUTS, "");
    verify(terminalClient).resetAlarm();
  }

  @Test
  public void shouldNotifyPendingResultToChildrenWhenAttentionKey() throws Exception {
    connect();
    rteRecorder.onAttentionKey(AttentionKey.ENTER, INPUTS, "");

    ArgumentCaptor<SampleEvent> argument = ArgumentCaptor.forClass(SampleEvent.class);
    verify((SampleListener) samplerListener).sampleOccurred(argument.capture());
    SampleResult result = argument.getAllValues().get(0).getResult();
    SampleResult expected = buildBasicSampleResultBuilder(Action.CONNECT)
        .withSuccessResponse(terminalClient)
        .build();
    assertSampleResult(expected, result);
  }

  @Test
  public void shouldAddSamplerToTargetControllerNodeWhenAttentionKey() throws Exception {
    connect();
    rteRecorder.onAttentionKey(AttentionKey.ENTER, INPUTS, "");
    /*
     * There were two interactions with the Model
     * 1st: When the Recording Starts, a new Test Element is added
     * 2nd: When the onAttentionKey is triggered, all the pending
     * samplers are recorded and that is when the Sampler is added
     */
    ArgumentCaptor<TestElement> argument = ArgumentCaptor.forClass(TestElement.class);
    verify(treeModel, times(2)).addComponent(argument.capture(),
        eq(treeNode));
    assertThat(argument.getValue()).isEqualTo(buildExpectedConnectionSampler());
  }

  @Test
  public void shouldNotifyPendingSampleAndDisconnectSampleToSampleListenersWhenStop()
      throws Exception {
    connect();
    rteRecorder.onRecordingStop();

    ArgumentCaptor<SampleEvent> argument = ArgumentCaptor.forClass(SampleEvent.class);

    verify((SampleListener) samplerListener, times(2)).sampleOccurred(argument.capture());

    SampleResult expectedConnectResult = buildBasicSampleResultBuilder(Action.CONNECT)
        .withSuccessResponse(terminalClient)
        .build();

    SampleResult expectedDisconnectResult = buildBasicSampleResultBuilder(Action.DISCONNECT)
        .withInputInhibitedRequest(false)
        .withSuccessResponse(null)
        .build();

    assertSampleResult(expectedConnectResult, argument.getAllValues().get(0).getResult());
    assertSampleResult(expectedDisconnectResult, argument.getAllValues().get(1).getResult());
  }

  @Test
  public void shouldAddPendingSamplerAndDisconnectSamplerToTestPlanWhenStop() throws Exception {
    connect();
    rteRecorder.onRecordingStop();

    ArgumentCaptor<TestElement> argument = ArgumentCaptor.forClass(TestElement.class);
    /*
     * There are 3 interactions on the addComponent because:
     * 1st: Corresponds to the ConfigTestElement
     * 2nd: Corresponds to the RTESampler for the Connect
     * 3nd: Corresponds to the RTESampler for the Disconnect
     */
    verify(treeModel, times(3))
        .addComponent(argument.capture(), eq(treeNode));
    assertThat(Arrays.asList(buildExpectedConfig(), buildExpectedConnectionSampler(),
        buildExpectedDisconnectionSampler())).isEqualTo(argument.getAllValues());
  }

  private RTESampler buildExpectedDisconnectionSampler() {
    return buildExpectedSampler("bzm-RTE-DISCONNECT", Action.DISCONNECT);
  }

  @Test
  public void shouldRegisterRequestListenerWhenAttentionKey() throws Exception {
    connect();
    rteRecorder.onAttentionKey(AttentionKey.ENTER, INPUTS, "");
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
    connect();
    rteRecorder.onAttentionKey(AttentionKey.ENTER, INPUTS, "");
    verify(terminalClient).send(INPUTS, AttentionKey.ENTER);
  }

  @Test
  public void shouldNotifyRecordingStateListenerWhenExceptionWhileConnecting()
      throws InterruptedException, TimeoutException, RteIOException {
    TimeoutException e = new TimeoutException("Timeout Error");
    doThrow(e).when(terminalClient).connect(any(), anyInt(), any(), any(), anyLong());
    rteRecorder.setRecordingStateListener(recorderListener);
    connect();
    verify(recorderListener, timeout(TIMEOUT)).onRecordingException(e);

  }

  @Test
  public void shouldNotifyRecordingStateListenerWhenUnsupportedOperationOnAttentionKey()
      throws TimeoutException, InterruptedException, RteIOException {
    Exception exception = new UnsupportedOperationException();
    setupExceptionOnAttentionKey(exception);
    rteRecorder.setRecordingStateListener(recorderListener);
    connect();
    rteRecorder.onAttentionKey(AttentionKey.ENTER, INPUTS, "");
    verify(recorderListener, timeout(TIMEOUT)).onRecordingException(exception);
  }

  private void setupExceptionOnAttentionKey(Exception exception) throws RteIOException {
    doThrow(exception).when(terminalClient).send(any(), any());
  }

  @Test
  public void shouldNotifyRecordingStateListenerOfExceptionWhenOnAttentionKey()
      throws RteIOException, TimeoutException, InterruptedException {
    setupExceptionOnTerminalClientSend();
    RteIOException exception = new RteIOException(null, SERVER);
    doThrow(exception).when(terminalClient).send(any(), any());
    rteRecorder.setRecordingStateListener(recorderListener);
    connect();
    rteRecorder.onAttentionKey(AttentionKey.ENTER, INPUTS, "");
    verify(recorderListener, timeout(TIMEOUT)).onRecordingException(exception);
  }

  private void setupExceptionOnTerminalClientSend() throws RteIOException {
    doThrow(new RteIOException(null, SERVER)).when(terminalClient).send(any(), any());
  }

  @Test
  public void shouldStopEmulatorWhenExceptionOnAttentionKey()
      throws RteIOException, TimeoutException, InterruptedException {
    setupExceptionOnTerminalClientSend();
    connect();
    rteRecorder.onAttentionKey(AttentionKey.ENTER, INPUTS, "");
    verify(terminalEmulator).stop();
  }

  @Test
  public void shouldDisconnectWhenExceptionOnAttentionKey()
      throws RteIOException, TimeoutException, InterruptedException {
    setupExceptionOnTerminalClientSend();
    connect();
    rteRecorder.onAttentionKey(AttentionKey.ENTER, INPUTS, "");
    verify(terminalClient).disconnect();
  }

  @Test(timeout = TIMEOUT)
  public void shouldConnectWithoutBlockingThreadWhenConnect()
      throws TimeoutException, InterruptedException, RteIOException {
    setupLongConnectingTerminalClient();
    rteRecorder.onRecordingStart();
  }

  private void setupLongConnectingTerminalClient()
      throws RteIOException, InterruptedException, TimeoutException {
    doAnswer(args -> {
      Thread.sleep(TIMEOUT * 2);
      return null;
    }).when(terminalClient).connect(any(), anyInt(), any(), any(), anyLong());
  }

  @Test(timeout = TIMEOUT)
  public void shouldStopConnectingWhenStopWhileConnecting() throws Exception {
    setupLongConnectingTerminalClient();
    rteRecorder.onRecordingStart();
    rteRecorder.onRecordingStop();
    rteRecorder.awaitConnected(TIMEOUT);
  }

  @Test
  public void shouldAddResponseAssertionAsChildOfPendingSamplerWhenOnAssertionScreen()
      throws Exception {
    connect();
    rteRecorder.onAssertionScreen(ASSERTION_NAME, SELECTED_TEXT);
    rteRecorder.onAttentionKey(AttentionKey.ENTER, new ArrayList<>(), "");

    rteRecorder.onRecordingStop();

    ArgumentCaptor<ResponseAssertion> argumentCaptor = ArgumentCaptor
        .forClass(ResponseAssertion.class);

    verify(treeModel).addComponent(argumentCaptor.capture(), eq(samplerNode));
    assertThat(argumentCaptor.getValue())
        .isEqualTo(buildExpectedAssertion());
  }

  private ResponseAssertion buildExpectedAssertion() {
    ResponseAssertion assertion = new ResponseAssertion();
    assertion.setName(ASSERTION_NAME);
    assertion.setProperty(TestElement.GUI_CLASS, AssertionGui.class.getName());
    assertion.setProperty(TestElement.TEST_CLASS, ResponseAssertion.class.getName());
    assertion.setTestFieldResponseData();
    assertion.setToContainsType();
    assertion.addTestString(SELECTED_TEXT);
    assertion.setAssumeSuccess(false);
    return assertion;
  }

  @Test
  public void shouldAddSamplerToTargetControllerNodeWhenOnAttentionKey()
      throws Exception {
    connect();
    rteRecorder.onAttentionKey(AttentionKey.ENTER, INPUTS, SENDING_USER);
    rteRecorder.onRecordingStop();
    ArgumentCaptor<TestElement> argument = ArgumentCaptor.forClass(TestElement.class);
    verify(treeModel, times(4)).addComponent(argument.capture(),
        eq(treeNode));
    assertThat(argument.getAllValues().get(2).getName()).isEqualTo(SENDING_USER);
  }

  @Test
  public void shouldAddSamplerWithDefaultSampleNameWhenConnectAndDisconnect()
      throws TimeoutException, InterruptedException, IllegalUserActionException {
    connect();
    rteRecorder.onRecordingStop();
    ArgumentCaptor<TestElement> argument = ArgumentCaptor.forClass(TestElement.class);
    verify(treeModel, times(3)).addComponent(argument.capture(),
        eq(treeNode));
    softly.assertThat(argument.getAllValues().get(2).getName()).isEqualTo("bzm-RTE-DISCONNECT");
    softly.assertThat(argument.getAllValues().get(1).getName()).isEqualTo("bzm-RTE-CONNECT");
  }
}

