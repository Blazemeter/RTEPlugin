package com.blazemeter.jmeter.rte.recorder;

import com.blazemeter.jmeter.rte.core.AttentionKey;
import com.blazemeter.jmeter.rte.core.CharacterBasedProtocolClient;
import com.blazemeter.jmeter.rte.core.Input;
import com.blazemeter.jmeter.rte.core.Position;
import com.blazemeter.jmeter.rte.core.Protocol;
import com.blazemeter.jmeter.rte.core.RteProtocolClient;
import com.blazemeter.jmeter.rte.core.RteSampleResultBuilder;
import com.blazemeter.jmeter.rte.core.Screen;
import com.blazemeter.jmeter.rte.core.ServerDisconnectHandler;
import com.blazemeter.jmeter.rte.core.TerminalType;
import com.blazemeter.jmeter.rte.core.exceptions.RteIOException;
import com.blazemeter.jmeter.rte.core.listener.ExceptionHandler;
import com.blazemeter.jmeter.rte.core.listener.RequestListener;
import com.blazemeter.jmeter.rte.core.listener.TerminalStateListener;
import com.blazemeter.jmeter.rte.core.ssl.SSLType;
import com.blazemeter.jmeter.rte.recorder.emulator.CharacterBasedEmulator;
import com.blazemeter.jmeter.rte.recorder.emulator.FieldBasedEmulator;
import com.blazemeter.jmeter.rte.recorder.emulator.TerminalEmulator;
import com.blazemeter.jmeter.rte.recorder.emulator.TerminalEmulatorListener;
import com.blazemeter.jmeter.rte.recorder.emulator.Xtn5250TerminalEmulator;
import com.blazemeter.jmeter.rte.recorder.wait.WaitConditionsRecorder;
import com.blazemeter.jmeter.rte.sampler.Action;
import com.blazemeter.jmeter.rte.sampler.RTESampler;
import com.blazemeter.jmeter.rte.sampler.gui.RTEConfigGui;
import com.blazemeter.jmeter.rte.sampler.gui.RTESamplerGui;
import com.helger.commons.annotation.VisibleForTesting;
import java.awt.Component;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import org.apache.jmeter.assertions.ResponseAssertion;
import org.apache.jmeter.assertions.gui.AssertionGui;
import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.control.GenericController;
import org.apache.jmeter.exceptions.IllegalUserActionException;
import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.tree.JMeterTreeModel;
import org.apache.jmeter.gui.tree.JMeterTreeNode;
import org.apache.jmeter.samplers.SampleEvent;
import org.apache.jmeter.samplers.SampleListener;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestStateListener;
import org.apache.jmeter.util.JMeterUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RTERecorder extends GenericController implements TerminalEmulatorListener,
    RecordingStateListener, TerminalStateListener {

  private static final Logger LOG = LoggerFactory.getLogger(RTERecorder.class);
  private static final long DEFAULT_WAIT_CONDITION_TIMEOUT_THRESHOLD_MILLIS = 10000;
  private static final String WAIT_CONDITION_TIMEOUT_THRESHOLD_MILLIS_PROPERTY
      = "waitConditionTimeoutThresholdMillis";
  private transient JMeterTreeModel treeModelMock;
  private transient TerminalEmulator terminalEmulator;
  private transient Supplier<TerminalEmulator> terminalEmulatorSupplier;
  private final transient RecordingTargetFinder finder;
  private transient JMeterTreeNode samplersTargetNode;
  private transient RecordingStateListener recordingListener;

  private transient RteSampleResultBuilder resultBuilder;
  private transient RTESampler sampler;
  private transient RequestListener<?> requestListener;
  private transient int sampleCount;
  private transient WaitConditionsRecorder waitConditionsRecorder;
  private transient ExecutorService connectionExecutor;
  private transient Function<List<Input>, List<Input>> inputProvider;
  private final transient Function<Protocol, RteProtocolClient> protocolFactory;
  private transient RteProtocolClient terminalClient;
  private final transient List<TestElement> responseAssertions = new ArrayList<>();

  public RTERecorder() {
    this(new RecordingTargetFinder(),
        Protocol::createProtocolClient);
  }

  public RTERecorder(RecordingTargetFinder finder,
      Function<Protocol,
          RteProtocolClient> factory) {
    this.finder = finder;
    this.protocolFactory = factory;
    this.terminalEmulatorSupplier = () -> null;
  }

  @VisibleForTesting
  public RTERecorder(Supplier<TerminalEmulator> supplier, RecordingTargetFinder finder,
      Function<Protocol, RteProtocolClient> factory, JMeterTreeModel treeModelMock) {
    this(finder, factory);
    this.terminalEmulatorSupplier = supplier;
    this.treeModelMock = treeModelMock;
  }

  private JMeterTreeModel getJmeterTreeModel() {
    return treeModelMock != null ? treeModelMock : GuiPackage.getInstance().getTreeModel();
  }

  @Override
  public String getName() {
    return getPropertyAsString(TestElement.NAME);
  }

  public String getServer() {
    return getPropertyAsString(RTESampler.CONFIG_SERVER);
  }

  public void setServer(String server) {
    setProperty(RTESampler.CONFIG_SERVER, server);
  }

  public int getPort() {
    return getPropertyAsInt(RTESampler.CONFIG_PORT, RTESampler.DEFAULT_PORT);
  }

  public void setPort(String strPort) {
    int port = RTESampler.DEFAULT_PORT;
    try {
      port = Integer.parseInt(strPort);
    } catch (NumberFormatException e) {
      LOG.warn("Invalid port value '{}', defaulting to {}", strPort, RTESampler.DEFAULT_PORT);
    }
    setProperty(RTESampler.CONFIG_PORT, port);
  }

  public Protocol getProtocol() {
    return Protocol
        .valueOf(getPropertyAsString(RTESampler.CONFIG_PROTOCOL,
            RTESampler.DEFAULT_PROTOCOL.name()));
  }

  public void setProtocol(Protocol protocol) {
    setProperty(RTESampler.CONFIG_PROTOCOL, protocol.name());
  }

  public TerminalType getTerminalType() {
    return getProtocol().createProtocolClient().getTerminalTypeById(
        getPropertyAsString(RTESampler.CONFIG_TERMINAL_TYPE,
            RTESampler.DEFAULT_TERMINAL_TYPE.getId()));
  }

  public void setTerminalType(TerminalType terminalType) {
    setProperty(RTESampler.CONFIG_TERMINAL_TYPE,
        terminalType.getId());
  }

  public SSLType getSSLType() {
    return SSLType.valueOf((
        getPropertyAsString(RTESampler.CONFIG_SSL_TYPE, RTESampler.DEFAULT_SSL_TYPE.name())));
  }

  public void setSSLType(SSLType sslType) {
    setProperty(RTESampler.CONFIG_SSL_TYPE, sslType.name());
  }

  public long getConnectionTimeout() {
    return getPropertyAsLong(RTESampler.CONFIG_CONNECTION_TIMEOUT,
        RTESampler.DEFAULT_CONNECTION_TIMEOUT_MILLIS);
  }

  public void setConnectionTimeout(String strConnectionTimeout) {
    long connectionTimeout = RTESampler.DEFAULT_CONNECTION_TIMEOUT_MILLIS;
    try {
      connectionTimeout = Integer.parseInt(strConnectionTimeout);
    } catch (NumberFormatException e) {
      LOG.warn("Invalid connection timeout value '{}', defaulting to {}", strConnectionTimeout,
          RTESampler.DEFAULT_CONNECTION_TIMEOUT_MILLIS);
    }
    setProperty(RTESampler.CONFIG_CONNECTION_TIMEOUT, connectionTimeout);
  }

  public void setRecordingStateListener(RecordingStateListener listener) {
    recordingListener = listener;
  }

  public long getTimeoutThresholdMillis() {
    return getPropertyAsLong(WAIT_CONDITION_TIMEOUT_THRESHOLD_MILLIS_PROPERTY,
        DEFAULT_WAIT_CONDITION_TIMEOUT_THRESHOLD_MILLIS);
  }

  public void setTimeoutThresholdMillis(String waitConditionsTimeoutThresholdMillis) {
    long timeoutThresholdMillis = DEFAULT_WAIT_CONDITION_TIMEOUT_THRESHOLD_MILLIS;
    try {
      timeoutThresholdMillis = Integer.parseInt(waitConditionsTimeoutThresholdMillis);
    } catch (NumberFormatException e) {
      LOG.warn("Invalid timeout threshold value '{}', defaulting to {}",
          waitConditionsTimeoutThresholdMillis,
          DEFAULT_WAIT_CONDITION_TIMEOUT_THRESHOLD_MILLIS);
    }
    setProperty(WAIT_CONDITION_TIMEOUT_THRESHOLD_MILLIS_PROPERTY, timeoutThresholdMillis);
  }

  @VisibleForTesting
  public void setInputProvider(Function<List<Input>, List<Input>> inputProvider) {
    this.inputProvider = inputProvider;
  }

  @Override
  public void onRecordingStart() {
    LOG.debug("Start recording");
    sampleCount = 0;
    samplersTargetNode = finder
        .findTargetControllerNode(getJmeterTreeModel());
    addTestElementToTestPlan(buildRteConfigElement(), responseAssertions, samplersTargetNode);
    notifyChildren(TestStateListener.class, TestStateListener::testStarted);
    String sampleName = buildSampleName(Action.CONNECT);
    resultBuilder = buildSampleResultBuilder(Action.CONNECT, sampleName);
    sampler = buildSampler(Action.CONNECT, null, null, sampleName);
    terminalClient = protocolFactory.apply(getProtocol());
    terminalClient.setDisconnectionHandler(buildDisconnectionHandler());
    TerminalType terminalType = getTerminalType();
    waitConditionsRecorder = new WaitConditionsRecorder(terminalClient,
        getTimeoutThresholdMillis(), RTESampler.getStableTimeout());
    waitConditionsRecorder.start();
    ExecutorService executor = Executors.newSingleThreadExecutor();
    /* 
      we use a separate variable to avoid shutting down an incorrect executor if there are two
      threads connecting (on start, stop and start) 
     */
    this.connectionExecutor = executor;
    executor.submit(() -> {
      try {
        synchronized (this) {
          terminalClient
              .connect(getServer(), getPort(), getSSLType(), terminalType, getConnectionTimeout());
          resultBuilder.withConnectEndNow();
          initTerminalEmulatorSupplier();
          initTerminalEmulator(terminalType);
          registerRequestListenerFor();
        }
      } catch (Exception e) {
        onException(e);
      } finally {
        executor.shutdown();
      }
    });
  }

  private void initTerminalEmulatorSupplier() {
    //verification done in order to not override the mock when testing
    if (terminalEmulatorSupplier.get() == null) {
      boolean isCharacterBased = terminalClient instanceof CharacterBasedProtocolClient;
      inputProvider = inputs -> isCharacterBased ? Collections
          .emptyList() : inputs;
      if (isCharacterBased) {
        CharacterBasedEmulator characterBasedEmulator = new CharacterBasedEmulator();
        terminalEmulatorSupplier = () -> new Xtn5250TerminalEmulator(characterBasedEmulator);
        ((CharacterBasedProtocolClient) terminalClient)
            .addScreenChangeListener(characterBasedEmulator);
      } else {
        terminalEmulatorSupplier = () -> new Xtn5250TerminalEmulator(new FieldBasedEmulator());
      }
    }
  }

  @VisibleForTesting
  protected void awaitConnected(long timeout) throws InterruptedException, TimeoutException {
    connectionExecutor.shutdown();
    if (!connectionExecutor.awaitTermination(timeout, TimeUnit.MILLISECONDS)) {
      throw new TimeoutException("Timeout waiting for connection to end after " + timeout + "ms");
    }
  }

  private ConfigTestElement buildRteConfigElement() {
    ConfigTestElement configTestElement = new ConfigTestElement();
    configTestElement.setName("bzm-RTE-config");
    configTestElement.setProperty(TestElement.GUI_CLASS, RTEConfigGui.class.getName());
    configTestElement.setProperty(RTESampler.CONFIG_PORT, String.valueOf(getPort()));
    configTestElement.setProperty(RTESampler.CONFIG_SERVER, getServer());
    configTestElement.setProperty(RTESampler.CONFIG_PROTOCOL,
        getProtocol().name());
    configTestElement.setProperty(RTESampler.CONFIG_TERMINAL_TYPE,
        getTerminalType().getId());
    configTestElement.setProperty(RTESampler.CONFIG_SSL_TYPE, getSSLType().name());
    configTestElement
        .setProperty(RTESampler.CONFIG_CONNECTION_TIMEOUT, String.valueOf(getConnectionTimeout()));
    return configTestElement;
  }

  private void addTestElementToTestPlan(TestElement testElement, List<TestElement> children,
      JMeterTreeNode targetNode) {
    JMeterTreeModel jMeterTreeModel = getJmeterTreeModel();
    try {
      JMeterUtils.runSafe(true, () -> {
        try {
          JMeterTreeNode samplerNode = jMeterTreeModel.addComponent(testElement, targetNode);
          for (TestElement element : children) {
            jMeterTreeModel.addComponent(element, samplerNode);
          }
        } catch (IllegalUserActionException illegalUserAction) {
          LOG.error("Error placing sample configTestElement", illegalUserAction);
          JMeterUtils.reportErrorToUser(illegalUserAction.getMessage());
        }
      });
    } catch (Exception exception) {
      LOG.error("Error placing sampler", exception);
      JMeterUtils.reportErrorToUser(exception.getMessage());
    }
  }

  private <T> void notifyChildren(Class<T> classFilter, Consumer<T> notificationMethod) {
    JMeterTreeNode treeNode = getJmeterTreeModel().getNodeOf(this);
    if (treeNode != null) {
      Enumeration<?> kids = treeNode.children();
      while (kids.hasMoreElements()) {
        JMeterTreeNode subNode = (JMeterTreeNode) kids.nextElement();
        if (subNode.isEnabled()) {
          TestElement testElement = subNode.getTestElement();
          if (classFilter.isInstance(testElement)) {
            notificationMethod.accept(classFilter.cast(testElement));
          }
        }
      }
    }
  }

  private RteSampleResultBuilder buildSampleResultBuilder(Action action, String name) {
    RteSampleResultBuilder ret = new RteSampleResultBuilder()
        .withLabel(name)
        .withServer(getServer())
        .withPort(getPort())
        .withProtocol(getProtocol())
        .withTerminalType(getTerminalType())
        .withSslType(getSSLType())
        .withAction(action);
    if (action != Action.CONNECT) {
      ret.withInputInhibitedRequest(terminalClient.isInputInhibited().orElse(false));
    }
    return ret;
  }

  private RTESampler buildSampler(Action action, List<Input> inputs, AttentionKey attentionKey,
      String screenName) {
    RTESampler sampler = new RTESampler();
    sampler.setProperty(TestElement.GUI_CLASS, RTESamplerGui.class.getName());
    sampler.setProperty(TestElement.TEST_CLASS, RTESampler.class.getName());
    sampler.setName(screenName);
    sampler.setAction(action);
    if (inputs != null) {
      sampler.setInputs(inputs);
    }
    if (attentionKey != null) {
      sampler.setAttentionKey(attentionKey);
    }
    return sampler;
  }

  private ServerDisconnectHandler buildDisconnectionHandler() {
    return new ServerDisconnectHandler(false) {
      @Override
      public void onDisconnection(ExceptionHandler exceptionHandler) {
        this.isExpectedDisconnection = true;
        JOptionPane.showMessageDialog((Component) terminalEmulator,
            "Server has closed the connection, wait for\n "
                + "disconnect is added in order to validate the intervention.");
        if (terminalEmulator != null) {
          recordPendingSample();
          terminalEmulator.stop();
          terminalEmulator = null;
        }
        terminalClient.removeTerminalStateListener(RTERecorder.this);
        notifyChildren(TestStateListener.class, TestStateListener::testEnded);
        terminalClient = null;
        recordingListener.onRecordingStop();
      }
    };
  }

  private void initTerminalEmulator(TerminalType terminalType) {
    terminalEmulator = terminalEmulatorSupplier.get();
    terminalEmulator.addTerminalEmulatorListener(this);
    terminalEmulator.setKeyboardLock(true);
    terminalEmulator
        .setScreenSize(terminalType.getScreenSize().width, terminalType.getScreenSize().height);
    terminalEmulator.setSupportedAttentionKeys(terminalClient.getSupportedAttentionKeys());
    terminalEmulator.setProtocolClient(terminalClient);
    terminalEmulator.start();
    terminalEmulator.setScreenName(sampler.getName());
    terminalClient.addTerminalStateListener(this);

    onTerminalStateChange();
  }

  private void registerRequestListenerFor() {
    requestListener = new RequestListener<>(resultBuilder, terminalClient);
    terminalClient.addTerminalStateListener(requestListener);
  }

  @Override
  public void onAttentionKey(AttentionKey attentionKey, List<Input> inputs, String screenName) {
    sampleCount++;
    recordPendingSample();
    requestListener.stop();
    terminalClient.resetAlarm();
    String sampleName = buildSampleName(Action.SEND_INPUT);
    terminalEmulator.setScreenName(sampleName);
    resultBuilder = buildSendInputSampleResultBuilder(attentionKey, inputs, sampleName);
    registerRequestListenerFor();
    sampler = buildSampler(Action.SEND_INPUT, inputs, attentionKey, sampleName);
    resultBuilder.withLabel(screenName);
    sampler.setName(screenName);
    try {
      waitConditionsRecorder.start();
      terminalClient.send(inputProvider.apply(inputs), attentionKey,
          RTESampler.DEFAULT_CONNECTION_TIMEOUT_MILLIS);
    } catch (Exception e) {
      onException(e);
    }
  }

  @Override
  public void onWaitForText(String text) {
    waitConditionsRecorder.setWaitForTextCondition(text);
  }

  @Override
  public void onAssertionScreen(String name, String text) {
    ResponseAssertion assertion = new ResponseAssertion();
    assertion.setName(name);
    assertion.setProperty(TestElement.GUI_CLASS, AssertionGui.class.getName());
    assertion.setProperty(TestElement.TEST_CLASS, ResponseAssertion.class.getName());
    assertion.setTestFieldResponseData();
    assertion.setToContainsType();
    assertion.addTestString(text);
    assertion.setAssumeSuccess(false);
    responseAssertions.add(assertion);
  }

  private void recordPendingSample() {
    if (!resultBuilder.hasFailure()) {
      resultBuilder.withSuccessResponse(terminalClient);
    }
    notifySampleOccurred();
    sampler.setWaitConditions(waitConditionsRecorder.stop());
    addTestElementToTestPlan(sampler, responseAssertions, samplersTargetNode);
    responseAssertions.clear();
  }

  private void notifySampleOccurred() {
    notifyChildren(SampleListener.class,
        t -> t.sampleOccurred(new SampleEvent(resultBuilder.build(), "Thread Group", "Recorded")));
  }

  private RteSampleResultBuilder buildSendInputSampleResultBuilder(AttentionKey attentionKey,
      List<Input> inputs, String name) {
    return buildSampleResultBuilder(Action.SEND_INPUT, name)
        .withInputs(inputs)
        .withAttentionKey(attentionKey);
  }

  private String buildSampleName(Action action) {
    return "bzm-RTE-" + action + (action == Action.SEND_INPUT ? "-" + (sampleCount + 1) : "");
  }

  @Override
  public void onRecordingStop() {
    LOG.debug("Stopping recording");
    connectionExecutor.shutdownNow();
    synchronized (this) {
      if (terminalEmulator != null) {
        recordPendingSample();
        String sampleName = buildSampleName(Action.DISCONNECT);
        resultBuilder = buildSampleResultBuilder(Action.DISCONNECT, sampleName);
        sampler = buildSampler(Action.DISCONNECT, null, null, sampleName);
        terminalEmulator.stop();
        terminalEmulator = null;
        terminalEmulatorSupplier = () -> null;
        requestListener.stop();
        try {
          terminalClient.disconnect();
        } catch (RteIOException e) {
          LOG.error("Problem while disconnecting from server", e);
          resultBuilder.withFailure(e);
        } finally {
          if (!resultBuilder.hasFailure()) {
            resultBuilder.withSuccessResponse(null);
          }
          notifySampleOccurred();
          addTestElementToTestPlan(sampler, responseAssertions, samplersTargetNode);
          responseAssertions.clear();
        }
        terminalClient.removeTerminalStateListener(this);
        notifyChildren(TestStateListener.class, TestStateListener::testEnded);
        terminalClient = null;
      }
    }
  }

  @Override
  public void onRecordingException(Exception e) {

  }

  @Override
  public void onCloseTerminal() {
    onRecordingStop();
    if (recordingListener != null) {
      recordingListener.onRecordingStop();
    }
  }

  @Override
  public void onTerminalStateChange() {
    SwingUtilities.invokeLater(() -> RTERecorder.this.updateTerminalEmulator(
        terminalClient.getScreen(), terminalClient.getCursorPosition(),
        terminalClient.isInputInhibited(), terminalClient.isAlarmOn()));
  }

  private void updateTerminalEmulator(Screen screen, Optional<Position> position,
      Optional<Boolean> isInputInhibited, boolean isAlarmOn) {
    /*
    Due to incorporation of VT protocol where the screen is changing constantly
    without attention keys pressed, it is not possible to update screen name every time
    terminal state changed. Therefore, first screen sampler name is placed manually.
    */
    if (sampleCount == 0) {
      terminalEmulator.setScreenName(buildSampleName(Action.SEND_INPUT));
    }
    terminalEmulator.setScreen(screen);
    position.ifPresent(cursorPosition -> terminalEmulator
        .setCursor(cursorPosition.getRow(), cursorPosition.getColumn()));
    isInputInhibited.ifPresent(terminalEmulator::setKeyboardLock);
    if (isAlarmOn) {
      terminalEmulator.soundAlarm();
    }
  }

  @Override
  public void onException(Throwable e) {
    if (e instanceof InterruptedException) {
      return;
    }

    LOG.error(e.getMessage(), e);

    if (recordingListener != null) {
      recordingListener.onRecordingException((Exception) e);
    }

    resultBuilder.withFailure(e);
    recordPendingSample();
    synchronized (this) {

      if (terminalEmulator != null) {
        terminalClient.removeTerminalStateListener(this);
        terminalEmulator.stop();
        terminalEmulator = null;
        terminalEmulatorSupplier = () -> null;
      }
    }

    /*
     *     Disconnect must be at the end because if not is interrupting
     *     itself and is not adding sampler to test plan
     */

    try {
      terminalClient.disconnect();
      terminalClient = null;
    } catch (RteIOException ex) {
      LOG.error("Problem while trying to shutdown connection", e);
    }
  }
}
