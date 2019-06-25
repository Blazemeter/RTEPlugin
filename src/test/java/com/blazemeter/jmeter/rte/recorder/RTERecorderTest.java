package com.blazemeter.jmeter.rte.recorder;

import static com.blazemeter.jmeter.rte.SampleResultAssertions.assertSampleResult;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
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
import com.blazemeter.jmeter.rte.core.RteSampleResult;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;
import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.gui.tree.JMeterTreeModel;
import org.apache.jmeter.gui.tree.JMeterTreeNode;
import org.apache.jmeter.samplers.SampleEvent;
import org.apache.jmeter.samplers.SampleListener;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestStateListener;
import org.apache.jmeter.testelement.property.JMeterProperty;
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

  @Rule
  public final JUnitSoftAssertions softly = new JUnitSoftAssertions();

  private static final Screen TEST_SCREEN = Screen.valueOf("test\n");
  private static final String SERVER = "localhost";
  private static final int PORT = 80;
  private static final Protocol PROTOCOL = Protocol.TN5250;
  private static final TerminalType TERMINAL_TYPE = PROTOCOL.createProtocolClient()
      .getDefaultTerminalType();
  private static final SSLType SSL_TYPE = SSLType.NONE;
  private static final long TIMEOUT = 10000;
  private static final List<Input> INPUTS = Collections
      .singletonList(new CoordInput(new Position(2, 1), "testusr"));

  private TestElement mockedTestStateListener;
  private TestElement mockedSamplerListener;
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
  public void setup() {
    initializeChildrenList();

    Supplier<TerminalEmulator> terminalEmulatorSupplier = () -> mockedTerminalEmulator;
    rteRecorder = new RTERecorder(terminalEmulatorSupplier, finder, mockedJMeterTreeModel,
        p -> terminalClient, (e, c) -> mockedTerminalEmulatorUpdater);

    prepareRecorder();
  }

  public void initializeChildrenList() {
    mockedTestStateListener = mock(TestElement.class,
        withSettings().extraInterfaces(TestStateListener.class));
    mockedSamplerListener = mock(TestElement.class,
        withSettings().extraInterfaces(SampleListener.class));

    when(mockedFirstTreeNodeKid.getTestElement()).thenReturn(mockedTestStateListener);
    when(mockedSecondTreeNodeKid.getTestElement()).thenReturn(mockedSamplerListener);

    when(mockedFirstTreeNodeKid.isEnabled()).thenReturn(true);
    when(mockedSecondTreeNodeKid.isEnabled()).thenReturn(true);
    when(mockedThirdTreeNodeKid.isEnabled()).thenReturn(true);

    when(mockedJMeterTreeNode.children()).thenAnswer(a ->
        Collections.enumeration(Arrays
            .asList(mockedFirstTreeNodeKid, mockedSecondTreeNodeKid, mockedThirdTreeNodeKid)));
    when(mockedJMeterTreeModel.getNodeOf(any())).thenReturn(mockedJMeterTreeNode);

    when(finder.findTargetControllerNode()).thenReturn(mockedJMeterTreeNode);

    when(terminalClient.getScreen()).thenReturn(TEST_SCREEN);
  }

  public void prepareRecorder() {
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
    verify(mockedTerminalEmulator).addTerminalEmulatorListener(rteRecorder);
  }

  private void connect() throws TimeoutException, InterruptedException {
    rteRecorder.onRecordingStart();
    rteRecorder.awaitConnected(TIMEOUT);
  }

  @Test
  public void shouldAddRteConfigToTargetControllerNodeWhenStart() throws Exception {
    connect();
    ArgumentCaptor<TestElement> argument = ArgumentCaptor.forClass(TestElement.class);
    verify(mockedJMeterTreeModel).addComponent(argument.capture(), eq(mockedJMeterTreeNode));
    assertTestElement(buildExpectedConfig(), argument.getValue());
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

  private void assertTestElement(TestElement expected, TestElement actual) {
    assertThat(buildTestElementMap(actual)).isEqualTo(buildTestElementMap(expected));
  }

  private Map<String, String> buildTestElementMap(TestElement elem) {
    Map<String, String> ret = new HashMap<>();
    Iterator<JMeterProperty> iter = elem.propertyIterator();
    while (iter.hasNext()) {
      JMeterProperty prop = iter.next();
      ret.put(prop.getName(), prop.getStringValue());
    }
    return ret;
  }

  @Test
  public void shouldNotifyChildrenTestStartWhenStart() throws Exception {
    connect();
    verify((TestStateListener) mockedTestStateListener).testStarted();
  }

  @Test
  public void shouldLockEmulatorKeyboardWhenStart() throws Exception {
    connect();
    verify(mockedTerminalEmulator).setKeyboardLock(true);
  }

  @Test
  public void shouldStartTerminalEmulatorWhenStart() throws Exception {
    connect();
    verify(mockedTerminalEmulator).start();
  }

  @Test
  public void shouldConnectTerminalClientWhenStart() throws Exception {
    connect();
    verify(terminalClient).connect(SERVER, PORT, SSL_TYPE, TERMINAL_TYPE, TIMEOUT);
  }

  @Test
  public void shouldNotifyTerminalEmulatorUpdaterWhenTerminalStateChange() throws Exception {
    connect();
    verify(mockedTerminalEmulatorUpdater).onTerminalStateChange();
  }

  @Test
  public void shouldNotifyErrorResultToChildrenWhenStartWithFailingTerminalClientConnect()
      throws Exception {
    doThrow(RteIOException.class).when(terminalClient).connect(SERVER, PORT, SSL_TYPE,
        TERMINAL_TYPE, TIMEOUT);
    connect();
    ArgumentCaptor<SampleEvent> argument = ArgumentCaptor.forClass(SampleEvent.class);
    verify((SampleListener) mockedSamplerListener).sampleOccurred(argument.capture());
    assertSampleResult(buildExpectedConnectionErrorResult(), argument.getValue().getResult());
  }

  private SampleResult buildExpectedConnectionErrorResult() {
    RteSampleResult expected = buildBasicSampleResult(Action.CONNECT);
    updateErrorResult(expected);
    return expected;
  }

  private void updateErrorResult(RteSampleResult expected) {
    expected.setSuccessful(false);
    String exceptionName = RteIOException.class.getCanonicalName();
    expected.setResponseCode(exceptionName);
    expected.setResponseMessage(null);
    expected.setDataType("text");
    expected.setResponseData(exceptionName + "\n", null);
  }

  @Test
  public void shouldNotifyFailedResultToChildrenWhenAttentionKeyAndFailingSendingToTerminalClient()
      throws Exception {
    connect();
    doThrow(RteIOException.class).when(terminalClient).send(INPUTS, AttentionKey.ENTER);
    rteRecorder.onAttentionKey(AttentionKey.ENTER, INPUTS);

    ArgumentCaptor<SampleEvent> argument = ArgumentCaptor.forClass(SampleEvent.class);
    // 1 sample due to connection, the other due to attention key
    verify((SampleListener) mockedSamplerListener, times(2)).sampleOccurred(argument.capture());

    RteSampleResult expected = buildBasicSampleResult(Action.SEND_INPUT);
    expected.setSampleLabel(expected.getSampleLabel() + "-1");
    expected.setInputInhibitedRequest(false);
    expected.setInputs(INPUTS);
    expected.setAttentionKey(AttentionKey.ENTER);
    updateErrorResult(expected);
    assertSampleResult(expected, argument.getAllValues().get(1).getResult());
  }

  private RteSampleResult buildBasicSampleResult(Action action) {
    RteSampleResult base = new RteSampleResult();
    base.setAction(action);
    base.setSampleLabel("bzm-RTE-" + action.name());
    base.setProtocol(Protocol.TN5250);
    base.setTerminalType(new TerminalType("IBM-3179-2", new Dimension(80, 24)));
    base.setServer("localhost");
    base.setPort(80);
    base.setSslType(SSLType.NONE);
    base.setSuccessful(true);
    return base;
  }

  @Test
  public void shouldNotifyTerminalEmulatorUpdaterWhenStart() throws Exception {
    connect();
    verify(mockedTerminalEmulatorUpdater).onTerminalStateChange();
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
    verify(mockedJMeterTreeModel, times(2))
        .addComponent(argument.capture(), eq(mockedJMeterTreeNode));
    assertTestElement(buildExpectedConnectionSampler(), argument.getAllValues().get(1));
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
    verify((TestStateListener) mockedTestStateListener).testEnded();
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
    verify(mockedTerminalEmulator).stop();
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
    verify((TestStateListener) mockedTestStateListener).testEnded();
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
    verify(mockedTerminalEmulator).stop();
  }

  @Test
  public void shouldDisconnectTerminalClientWhenCloseTerminal() throws Exception {
    connect();
    rteRecorder.onCloseTerminal();
    verify(terminalClient).disconnect();
  }

  @Test
  public void shouldNotifyRecordingListenerWhenCloseTerminal() throws Exception {
    rteRecorder.setRecordingStateListener(mockedRecorderListener);
    connect();
    rteRecorder.onCloseTerminal();
    verify(mockedRecorderListener).onRecordingStop();
  }

  @Test
  public void shouldLockEmulatorKeyboardWhenAttentionKey() throws Exception {
    connect();
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
    connect();
    rteRecorder.onAttentionKey(AttentionKey.ENTER, INPUTS);
    verify(terminalClient).resetAlarm();
  }

  @Test
  public void shouldNotifyPendingResultToChildrenWhenAttentionKey() throws Exception {
    connect();
    rteRecorder.onAttentionKey(AttentionKey.ENTER, INPUTS);

    ArgumentCaptor<SampleEvent> argument = ArgumentCaptor.forClass(SampleEvent.class);
    verify((SampleListener) mockedSamplerListener).sampleOccurred(argument.capture());

    RteSampleResult expected = buildBasicSampleResult(Action.CONNECT);
    expected.setScreen(TEST_SCREEN);

    RteSampleResult result = (RteSampleResult) argument.getAllValues().get(0).getResult();
    assertSampleResult(expected, result);
  }

  @Test
  public void shouldAddSamplerToTargetControllerNodeWhenAttentionKey() throws Exception {
    connect();
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

    assertTestElement(buildExpectedConnectionSampler(), argument.getValue());
  }

  @Test
  public void shouldNotifyPendingSampleAndDisconnectSampleToSampleListenersWhenStop()
      throws Exception {
    connect();
    rteRecorder.onRecordingStop();

    ArgumentCaptor<SampleEvent> argument = ArgumentCaptor.forClass(SampleEvent.class);

    verify((SampleListener) mockedSamplerListener, times(2)).sampleOccurred(argument.capture());

    RteSampleResult expectedConnectResult = buildBasicSampleResult(Action.CONNECT);
    expectedConnectResult.setScreen(TEST_SCREEN);

    RteSampleResult expectedDisconnectResult = buildBasicSampleResult(Action.DISCONNECT);
    expectedDisconnectResult.setInputInhibitedRequest(false);

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
    verify(mockedJMeterTreeModel, times(3))
        .addComponent(argument.capture(), eq(mockedJMeterTreeNode));
    verify(terminalClient).disconnect();

    softly.assertThat(argument.getAllValues().get(0)).isEqualTo(buildExpectedConfig());
    assertTestElement(buildExpectedConnectionSampler(), argument.getAllValues().get(1));
    assertTestElement(buildExpectedDisconnectionSampler(), argument.getAllValues().get(2));
  }

  private RTESampler buildExpectedDisconnectionSampler() {
    return buildExpectedSampler("bzm-RTE-DISCONNECT", Action.DISCONNECT);
  }

  @Test
  public void shouldRegisterRequestListenerWhenAttentionKey() throws Exception {
    connect();
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
    connect();
    rteRecorder.onAttentionKey(AttentionKey.ENTER, INPUTS);
    verify(terminalClient).send(INPUTS, AttentionKey.ENTER);
  }

}
