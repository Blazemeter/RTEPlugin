package com.blazemeter.jmeter.rte.recorder;

import com.blazemeter.jmeter.rte.core.AttentionKey;
import com.blazemeter.jmeter.rte.core.Input;
import com.blazemeter.jmeter.rte.core.Protocol;
import com.blazemeter.jmeter.rte.core.RteProtocolClient;
import com.blazemeter.jmeter.rte.core.RteSampleResultBuilder;
import com.blazemeter.jmeter.rte.core.TerminalType;
import com.blazemeter.jmeter.rte.core.exceptions.RteIOException;
import com.blazemeter.jmeter.rte.core.listener.RequestListener;
import com.blazemeter.jmeter.rte.core.listener.TerminalStateListener;
import com.blazemeter.jmeter.rte.core.ssl.SSLType;
import com.blazemeter.jmeter.rte.recorder.emulator.TerminalEmulator;
import com.blazemeter.jmeter.rte.recorder.emulator.TerminalEmulatorListener;
import com.blazemeter.jmeter.rte.recorder.emulator.Xtn5250TerminalEmulator;
import com.blazemeter.jmeter.rte.recorder.wait.WaitConditionsRecorder;
import com.blazemeter.jmeter.rte.sampler.Action;
import com.blazemeter.jmeter.rte.sampler.RTESampler;
import com.blazemeter.jmeter.rte.sampler.gui.RTEConfigGui;
import com.blazemeter.jmeter.rte.sampler.gui.RTESamplerGui;
import com.helger.commons.annotation.VisibleForTesting;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
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
  private transient JMeterTreeModel treeModel;
  private transient TerminalEmulator terminalEmulator;
  private transient Supplier<TerminalEmulator> terminalEmulatorSupplier;
  private transient RecordingTargetFinder finder;
  private transient JMeterTreeNode samplersTargetNode;
  private transient RecordingStateListener recordingListener;

  private transient String samplerName;
  private transient RteSampleResultBuilder resultBuilder;
  private transient RTESampler sampler;
  private transient RequestListener requestListener;
  private transient int sampleCount;
  private transient WaitConditionsRecorder waitConditionsRecorder;
  private transient ExecutorService connectionExecutor;

  private transient Function<Protocol, RteProtocolClient> protocolFactory;
  private transient RteProtocolClient terminalClient;
  private transient List<TestElement> responseAssertions = new ArrayList<>();

  public RTERecorder() {
    this(Xtn5250TerminalEmulator::new, new RecordingTargetFinder(),
        Protocol::createProtocolClient);
  }

  public RTERecorder(Supplier<TerminalEmulator> supplier, RecordingTargetFinder finder,
      Function<Protocol,
          RteProtocolClient> factory) {
    terminalEmulatorSupplier = supplier;
    this.finder = finder;
    this.protocolFactory = factory;
  }

  @VisibleForTesting
  public RTERecorder(Supplier<TerminalEmulator> supplier, RecordingTargetFinder finder,
      Function<Protocol, RteProtocolClient> factory, JMeterTreeModel treeModel) {
    this(supplier, finder, factory);
    this.treeModel = treeModel;
  }

  private JMeterTreeModel getJmeterTreeModel() {
    JMeterTreeModel model;
    try {
      model = GuiPackage.getInstance().getTreeModel();
    } catch (NullPointerException e) {
      // will throw nullPointerException during tests
      return treeModel;
    }
    return model;
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

  @Override
  public void onRecordingStart() {
    LOG.debug("Start recording");
    sampleCount = 0;
    samplersTargetNode = finder
        .findTargetControllerNode(getJmeterTreeModel());
    addTestElementToTestPlan(buildRteConfigElement(), responseAssertions, samplersTargetNode);
    notifyChildren(TestStateListener.class, TestStateListener::testStarted);
    resultBuilder = buildSampleResultBuilder(Action.CONNECT);
    sampler = buildSampler(Action.CONNECT, null, null);
    terminalClient = protocolFactory.apply(getProtocol());
    TerminalType terminalType = getTerminalType();
    waitConditionsRecorder = new WaitConditionsRecorder(terminalClient,
        getTimeoutThresholdMillis(), RTESampler.getStableTimeout());
    waitConditionsRecorder.start();
    ExecutorService executor = Executors.newSingleThreadExecutor();
    // we use a separate variable to avoid shutting down an incorrect executor if there are two
    // threads connecting (on start, stop and start)
    this.connectionExecutor = executor;
    executor.submit(() -> {
      try {
        synchronized (this) {
          terminalClient
              .connect(getServer(), getPort(), getSSLType(), terminalType, getConnectionTimeout());
          resultBuilder.withConnectEndNow();
          initTerminalEmulator(terminalType);
          registerRequestListenerFor(resultBuilder);
        }
      } catch (Exception e) {
        onException(e);
      } finally {
        executor.shutdown();
      }
    });
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

  private RteSampleResultBuilder buildSampleResultBuilder(Action action) {
    RteSampleResultBuilder ret = new RteSampleResultBuilder()
        .withLabel(getSampleName(action))
        .withServer(getServer())
        .withPort(getPort())
        .withProtocol(getProtocol())
        .withTerminalType(getTerminalType())
        .withSslType(getSSLType())
        .withAction(action);
    if (action != Action.CONNECT) {
      ret.withInputInhibitedRequest(terminalClient.isInputInhibited());
    }
    return ret;
  }

  private String getSampleName(Action action) {
    return action == Action.SEND_INPUT ? samplerName : buildSampleName(action);
  }

  private String buildSampleName(Action action) {
    return "bzm-RTE-" + action + (action == Action.SEND_INPUT ? "-" + (sampleCount + 1) : "");
  }

  private RTESampler buildSampler(Action action, List<Input> inputs, AttentionKey attentionKey) {
    RTESampler sampler = new RTESampler();
    sampler.setProperty(TestElement.GUI_CLASS, RTESamplerGui.class.getName());
    sampler.setProperty(TestElement.TEST_CLASS, RTESampler.class.getName());
    sampler.setName(getSampleName(action));
    sampler.setAction(action);
    if (inputs != null) {
      sampler.setInputs(inputs);
    }
    if (attentionKey != null) {
      sampler.setAttentionKey(attentionKey);
    }
    return sampler;
  }

  private void initTerminalEmulator(TerminalType terminalType) {
    terminalEmulator = terminalEmulatorSupplier.get();
    terminalEmulator.addTerminalEmulatorListener(this);
    terminalEmulator.setKeyboardLock(true);
    terminalEmulator
        .setScreenSize(terminalType.getScreenSize().width, terminalType.getScreenSize().height);
    terminalEmulator.setSupportedAttentionKeys(terminalClient.getSupportedAttentionKeys());
    terminalEmulator.start();
    terminalClient.addTerminalStateListener(this);
    onTerminalStateChange();
  }

  private void registerRequestListenerFor(RteSampleResultBuilder resultBuilder) {
    requestListener = new RequestListener<>(resultBuilder, terminalClient);
    terminalClient.addTerminalStateListener(requestListener);
  }

  @Override
  public void onAttentionKey(AttentionKey attentionKey, List<Input> inputs, String screenName) {
    samplerName = screenName;
    sampleCount++;
    terminalEmulator.setKeyboardLock(true);
    requestListener.stop();
    recordPendingSample();
    terminalClient.resetAlarm();
    resultBuilder = buildSendInputSampleResultBuilder(attentionKey, inputs);
    registerRequestListenerFor(resultBuilder);
    sampler = buildSampler(Action.SEND_INPUT, inputs, attentionKey);
    try {
      waitConditionsRecorder.start();
      terminalClient.send(inputs, attentionKey);
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
      List<Input> inputs) {
    return buildSampleResultBuilder(Action.SEND_INPUT)
        .withInputs(inputs)
        .withAttentionKey(attentionKey);
  }

  @Override
  public void onRecordingStop() {
    LOG.debug("Stopping recording");
    connectionExecutor.shutdownNow();
    synchronized (this) {
      if (terminalEmulator != null) {
        recordPendingSample();
        resultBuilder = buildSampleResultBuilder(Action.DISCONNECT);
        sampler = buildSampler(Action.DISCONNECT, null, null);
        terminalEmulator.stop();
        terminalEmulator = null;
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
    samplerName = buildSampleName(Action.SEND_INPUT);
    terminalEmulator.setScreen(terminalClient.getScreen(), samplerName);
    terminalClient.getCursorPosition().ifPresent(cursorPosition -> terminalEmulator
        .setCursor(cursorPosition.getRow(), cursorPosition.getColumn()));
    terminalEmulator.setKeyboardLock(terminalClient.isInputInhibited());
    if (terminalClient.isAlarmOn()) {
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
      }
    }

    /*
     *     Disconnect must be at the end because if not is interrupting
     *     itself and is not adding sampler to test plan
     */

    try {
      terminalClient.disconnect();
    } catch (RteIOException ex) {
      LOG.error("Problem while trying to shutdown connection", e);
    }
  }
}
