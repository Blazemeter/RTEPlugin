package com.blazemeter.jmeter.rte.recorder;

import com.blazemeter.jmeter.rte.core.AttentionKey;
import com.blazemeter.jmeter.rte.core.Input;
import com.blazemeter.jmeter.rte.core.Protocol;
import com.blazemeter.jmeter.rte.core.RteProtocolClient;
import com.blazemeter.jmeter.rte.core.RteSampleResult;
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
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.control.GenericController;
import org.apache.jmeter.exceptions.IllegalUserActionException;
import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.tree.JMeterTreeModel;
import org.apache.jmeter.gui.tree.JMeterTreeNode;
import org.apache.jmeter.samplers.SampleEvent;
import org.apache.jmeter.samplers.SampleListener;
import org.apache.jmeter.samplers.SampleResult;
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
  private transient TerminalEmulator terminalEmulator;
  private transient Supplier<TerminalEmulator> terminalEmulatorSupplier;
  private transient RecordingTargetFinder finder;
  private transient JMeterTreeNode samplersTargetNode;
  private transient RecordingStateListener recordingListener;

  private transient RteSampleResult sampleResult;
  private transient RTESampler sampler;
  private transient RequestListener requestListener;
  private transient int sampleCount;
  private transient WaitConditionsRecorder waitConditionsRecorder;
  private transient ExecutorService connectionExecutor;

  private transient Function<Protocol, RteProtocolClient> protocolFactory;
  private transient JMeterTreeModel jMeterTreeModel;
  private transient RteProtocolClient terminalClient;

  public RTERecorder() {
    this(Xtn5250TerminalEmulator::new, new RecordingTargetFinder(getJmeterTreeModel()),
        getJmeterTreeModel(), Protocol::createProtocolClient);
  }

  public RTERecorder(Supplier<TerminalEmulator> supplier, RecordingTargetFinder finder,
      JMeterTreeModel jMeterTreeModel, Function<Protocol,
      RteProtocolClient> factory) {
    terminalEmulatorSupplier = supplier;
    this.finder = finder;
    this.jMeterTreeModel = jMeterTreeModel;
    this.protocolFactory = factory;
  }

  private static JMeterTreeModel getJmeterTreeModel() {
    return GuiPackage.getInstance().getTreeModel();
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
    samplersTargetNode = finder.findTargetControllerNode();
    addTestElementToTestPlan(buildRteConfigElement(), samplersTargetNode);
    notifyChildren(TestStateListener.class, TestStateListener::testStarted);
    sampleResult = buildSampleResult(Action.CONNECT);
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
          sampleResult.connectEnd();
          initTerminalEmulator(terminalType);
          registerRequestListenerFor(sampleResult);
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

  private void addTestElementToTestPlan(TestElement testElement, JMeterTreeNode targetNode) {
    try {
      JMeterUtils.runSafe(true, () -> {
        try {
          jMeterTreeModel.addComponent(testElement, targetNode);
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
    JMeterTreeNode treeNode = jMeterTreeModel.getNodeOf(this);
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

  private RteSampleResult buildSampleResult(Action action) {
    RteSampleResult sampleResult = new RteSampleResult();
    sampleResult.setSampleLabel(buildSampleName(action));
    sampleResult.setServer(getServer());
    sampleResult.setPort(getPort());
    sampleResult.setProtocol(getProtocol());
    sampleResult.setTerminalType(getTerminalType());
    sampleResult.setSslType(getSSLType());
    sampleResult.setAction(action);
    if (action != Action.CONNECT) {
      sampleResult.setInputInhibitedRequest(terminalClient.isInputInhibited());
    }
    sampleResult.sampleStart();
    return sampleResult;
  }

  private String buildSampleName(Action action) {
    return "bzm-RTE-" + action + (action == Action.SEND_INPUT ? "-" + sampleCount : "");
  }

  private RTESampler buildSampler(Action action, List<Input> inputs, AttentionKey attentionKey) {
    RTESampler sampler = new RTESampler();
    sampler.setProperty(TestElement.GUI_CLASS, RTESamplerGui.class.getName());
    sampler.setProperty(TestElement.TEST_CLASS, RTESampler.class.getName());
    sampler.setName(buildSampleName(action));
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

  private void registerRequestListenerFor(SampleResult sampleResult) {
    requestListener = new RequestListener<>(sampleResult, terminalClient);
    terminalClient.addTerminalStateListener(requestListener);
  }

  @Override
  public void onAttentionKey(AttentionKey attentionKey, List<Input> inputs) {
    sampleCount++;
    terminalEmulator.setKeyboardLock(true);
    requestListener.stop();
    recordPendingSample();
    terminalClient.resetAlarm();
    sampleResult = buildSendInputSampleResult(attentionKey, inputs);
    registerRequestListenerFor(sampleResult);
    sampler = buildSampler(Action.SEND_INPUT, inputs, attentionKey);
    try {
      waitConditionsRecorder.start();
      terminalClient.send(inputs, attentionKey);
    } catch (Exception e) {
      onException(e); 
    }
  }

  private void recordPendingSample() {
    if (sampleResult.getResponseCode().isEmpty()) {
      RTESampler.updateSampleResultResponse(sampleResult, terminalClient);
    }
    notifySampleOccurred();
    sampler.setWaitConditions(waitConditionsRecorder.stop());
    addTestElementToTestPlan(sampler, samplersTargetNode);
  }

  private void notifySampleOccurred() {
    notifyChildren(SampleListener.class,
        t -> t.sampleOccurred(new SampleEvent(sampleResult, "Thread Group", "Recorded")));
  }

  private RteSampleResult buildSendInputSampleResult(AttentionKey attentionKey,
      List<Input> inputs) {
    RteSampleResult ret = buildSampleResult(Action.SEND_INPUT);
    ret.setInputs(inputs);
    ret.setAttentionKey(attentionKey);
    return ret;
  }

  @Override
  public void onRecordingStop() {
    LOG.debug("Stopping recording");
    connectionExecutor.shutdownNow();
    synchronized (this) {
      if (terminalEmulator != null) {
        recordPendingSample();
        sampleResult = buildSampleResult(Action.DISCONNECT);
        sampler = buildSampler(Action.DISCONNECT, null, null);
        terminalEmulator.stop();
        terminalEmulator = null;
        requestListener.stop();
        try {
          terminalClient.disconnect();
        } catch (RteIOException e) {
          LOG.error("Problem while disconnecting from server", e);
          RTESampler.updateErrorResult(e, sampleResult);
        } finally {
          if (sampleResult.getResponseCode().isEmpty()) {
            sampleResult.setSuccessful(true);
            sampleResult.sampleEnd();
          }
          notifySampleOccurred();
          addTestElementToTestPlan(sampler, samplersTargetNode);
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
    terminalEmulator.setScreen(terminalClient.getScreen());
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

    recordPendingSample();
    RTESampler.updateErrorResult(e, sampleResult);
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
