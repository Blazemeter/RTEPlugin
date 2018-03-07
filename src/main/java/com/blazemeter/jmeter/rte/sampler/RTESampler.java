package com.blazemeter.jmeter.rte.sampler;

import com.blazemeter.jmeter.rte.core.Action;
import com.blazemeter.jmeter.rte.core.CoordInput;
import com.blazemeter.jmeter.rte.core.Protocol;
import com.blazemeter.jmeter.rte.core.RteIOException;
import com.blazemeter.jmeter.rte.core.RteProtocolClient;
import com.blazemeter.jmeter.rte.core.SSLType;
import com.blazemeter.jmeter.rte.core.SyncWaitCondition;
import com.blazemeter.jmeter.rte.core.TerminalType;
import com.blazemeter.jmeter.rte.core.WaitCondition;
import com.blazemeter.jmeter.rte.protocols.tn5250.SSLData;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import org.apache.jmeter.samplers.AbstractSampler;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.ThreadListener;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.testelement.property.TestElementProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RTESampler extends AbstractSampler implements ThreadListener {

  public static final String CONFIG_PORT = "RTEConnectionConfig.port";
  public static final String CONFIG_SERVER = "RTEConnectionConfig.server";
  public static final String CONFIG_USER = "RTEConnectionConfig.user";
  public static final String CONFIG_PASS = "RTEConnectionConfig.pass";
  public static final String CONFIG_PROTOCOL = "RTEConnectionConfig.protocol";
  public static final String CONFIG_SSL_TYPE = "RTEConnectionConfig.sslType";
  public static final String CONFIG_CONNECTION_TIMEOUT = "RTEConnectionConfig.connectTimeout";
  public static final String CONFIG_TERMINAL_TYPE = "RTEConnectionConfig.terminalType";
  public static final String TYPING_STYLE_FAST = "Fast";
  public static final String TYPING_STYLE_HUMAN = "Human";
  public static final Action DEFAULT_ACTION = Action.ENTER;
  public static final Protocol DEFAULT_PROTOCOL = Protocol.TN5250;
  public static final TerminalType DEFAULT_TERMINAL_TYPE = TerminalType.IBM_3179_2;
  public static final SSLType DEFAULT_SSLTYPE = SSLType.NONE;
  private static final long DEFAULT_CONNECTION_TIMEOUT_MILLIS = 60000;
  private static final long DEFAULT_WAIT_SYNC_TIMEOUT_MILLIS = 60000;
  private static final int DEFAULT_PORT = 23;

  private static final Logger LOG = LoggerFactory.getLogger(RTESampler.class);
  private static final String CONFIG_STABLE_TIMEOUT = "RTEConnectionConfig.stableTimeout";
  private static final long DEFAULT_STABLE_TIMEOUT_MILLIS = 1000;
  private static ThreadLocal<Map<String, RteProtocolClient>> connections = ThreadLocal
      .withInitial(HashMap::new);
  private final Function<Protocol, RteProtocolClient> protocolFactory;
  private SampleResult sampleResult;

  public RTESampler() {
    this(Protocol::createProtocolClient);
  }

  public RTESampler(Function<Protocol, RteProtocolClient> protocolFactory) {
    setName("RTE");
    this.protocolFactory = protocolFactory;
  }

  @Override
  public String getName() {
    return getPropertyAsString(TestElement.NAME);
  }

  @Override
  public void setName(String name) {
    if (name != null) {
      setProperty(TestElement.NAME, name);
    }
  }

  private Protocol getProtocol() {
    return Protocol.valueOf(getPropertyAsString(CONFIG_PROTOCOL));
  }

  private String getServer() {
    return getPropertyAsString(CONFIG_SERVER);
  }

  private int getPort() {
    return getPropertyAsInt(CONFIG_PORT, DEFAULT_PORT);
  }

  private TerminalType getTerminalType() {
    return TerminalType.valueOf(getPropertyAsString(CONFIG_TERMINAL_TYPE));
  }

  private long getConnectionTimeout() {
    return getPropertyAsLong(CONFIG_CONNECTION_TIMEOUT, DEFAULT_CONNECTION_TIMEOUT_MILLIS);
  }

  private long getStableTimeout() {
    return getPropertyAsLong(CONFIG_STABLE_TIMEOUT, DEFAULT_STABLE_TIMEOUT_MILLIS);
  }

  public boolean getDisconnect() {
    return getPropertyAsBoolean("Disconnect");
  }

  public void setDisconnect(boolean disconnect) {
    setProperty("Disconnect", disconnect);
  }

  private String getUser() {
    return getPropertyAsString(CONFIG_USER);
  }

  private String getPass() {
    return getPropertyAsString(CONFIG_PASS);
  }

  private SSLType getSSLType() {
    return SSLType.valueOf(getPropertyAsString(CONFIG_SSL_TYPE));
  }

  public String getTypingStyle() {
    return getPropertyAsString("TypingStyle");
  }

  public void setTypingStyle(String typingStyle) {
    setProperty("TypingStyle", typingStyle);
  }

  public void setPayload(Inputs payload) {
    setProperty(new TestElementProperty(Inputs.INPUTS, payload));
  }

  public boolean getWaitSync() {
    return getPropertyAsBoolean("WaitSync", true);
  }

  public void setWaitSync(boolean waitSync) {
    setProperty("WaitSync", waitSync);
  }

  public String getWaitTimeoutSync() {
    return getPropertyAsString("WaitTimeoutSync", "" + DEFAULT_WAIT_SYNC_TIMEOUT_MILLIS);
  }

  private long getWaitTimeoutSyncValue() {
    return getPropertyAsLong("WaitTimeoutSync", DEFAULT_WAIT_SYNC_TIMEOUT_MILLIS);
  }

  public void setWaitTimeoutSync(String waitTimeoutSync) {
    setProperty("WaitTimeoutSync", waitTimeoutSync);
  }

  public boolean getWaitCursor() {
    return getPropertyAsBoolean("WaitCursor");
  }

  public void setWaitCursor(boolean waitCursor) {
    setProperty("WaitCursor", waitCursor);
  }

  public String getWaitTimeoutCursor() {
    return getPropertyAsString("WaitTimeoutCursor");
  }

  public void setWaitTimeoutCursor(String waitTimeoutCursor) {
    setProperty("WaitTimeoutCursor", waitTimeoutCursor);
  }

  public boolean getWaitSilent() {
    return getPropertyAsBoolean("WaitSilent");
  }

  public void setWaitSilent(boolean waitSilent) {
    setProperty("WaitSilent", waitSilent);
  }

  public String getWaitForSilent() {
    return getPropertyAsString("WaitForSilent");
  }

  public void setWaitForSilent(String waitForSilent) {
    setProperty("WaitForSilent", waitForSilent);
  }

  public String getWaitTimeoutSilent() {
    return getPropertyAsString("WaitTimeoutSilent");
  }

  public void setWaitTimeoutSilent(String waitTimeoutSilent) {
    setProperty("WaitTimeoutSilent", waitTimeoutSilent);
  }

  public boolean getWaitText() {
    return getPropertyAsBoolean("WaitText");
  }

  public void setWaitText(boolean waitText) {
    setProperty("WaitText", waitText);
  }

  public String getTextToWait() {
    return getPropertyAsString("TextToWait");
  }

  public void setTextToWait(String textToWait) {
    setProperty("TextToWait", textToWait);
  }

  public String getCoordXToWait() {
    return getPropertyAsString("CoordXToWait");
  }

  public void setCoordXToWait(String coordXToWait) {
    setProperty("CoordXToWait", coordXToWait);
  }

  public String getCoordYToWait() {
    return getPropertyAsString("CoordYToWait");
  }

  public void setCoordYToWait(String coordYToWait) {
    setProperty("CoordYToWait", coordYToWait);
  }

  public String getWaitTimeoutText() {
    return getPropertyAsString("WaitTimeoutText");
  }

  public void setWaitTimeoutText(String waitTimeoutText) {
    setProperty("WaitTimeoutText", waitTimeoutText);
  }

  public Action getAction() {
    if (getPropertyAsString("Action").isEmpty()) {
      return DEFAULT_ACTION;
    }
    return Action.valueOf(getPropertyAsString("Action"));
  }

  public void setAction(Action action) {
    setProperty("Action", action.name());
  }

  @Override
  public SampleResult sample(Entry entry) {

    sampleResult = new SampleResult();
    sampleResult.setSampleLabel(getName());
    sampleResult.sampleStart();

    try {
      RteProtocolClient client = getClient();
      try {
        client.send(getCoordInputs(), getAction(), getWaitersList());
        sampleResult.setSuccessful(true);
        sampleResult.setResponseData(client.getScreen(), "utf-8");
        sampleResult.setDataType(SampleResult.TEXT);
        sampleResult.sampleEnd();
        return sampleResult;
      } finally {
        if (getDisconnect()) {
          disconnect(client);
        }
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      return errorResult("The sampling has been interrupted", e);
    } catch (Exception e) {
      return errorResult("Error while sampling the remote terminal", e);
    }
  }

  private RteProtocolClient getClient()
      throws RteIOException, InterruptedException, TimeoutException {
    String clientId = buildConnectionId();
    Map<String, RteProtocolClient> clients = connections.get();

    if (clients.containsKey(clientId)) {
      return clients.get(clientId);
    }

    RteProtocolClient client = protocolFactory.apply(getProtocol());
    SSLData ssldata = new SSLData(DEFAULT_SSLTYPE, null, null);
    client.connect(getServer(), getPort(), ssldata, getTerminalType(), getConnectionTimeout(),
        getStableTimeout()); //TODO: Change hardcoded values from
    // functions in order to take the values from GUI
    clients.put(clientId, client);
    sampleResult.connectEnd();
    return client;
  }

  private String buildConnectionId() {
    return getServer() + ":" + getPort();
  }

  private List<CoordInput> getCoordInputs() {
    List<CoordInput> inputs = new ArrayList<>();
    for (JMeterProperty p : getInputs()) {
      CoordInputRowGUI c = (CoordInputRowGUI) p.getObjectValue();
      inputs.add(c.toCoordInput());
    }
    return inputs;
  }

  public Inputs getInputs() {
    return (Inputs) getProperty(Inputs.INPUTS).getObjectValue();
  }

  private List<WaitCondition> getWaitersList() {
    List<WaitCondition> waiters = new ArrayList<>();
    if (getWaitSync()) {
      waiters.add(new SyncWaitCondition(getWaitTimeoutSyncValue(), getStableTimeout()));
    }
    return waiters;
  }

  private void disconnect(RteProtocolClient client) throws RteIOException {
    client.disconnect();
    connections.get().remove(buildConnectionId());
  }

  private SampleResult errorResult(String message, Throwable e) {
    StringWriter sw = new StringWriter();
    e.printStackTrace(new PrintWriter(sw));
    sampleResult.setDataType(SampleResult.TEXT);
    sampleResult.setResponseCode(e.getClass().getName());
    sampleResult.setResponseMessage(e.getMessage());
    sampleResult.setResponseData(sw.toString(), SampleResult.DEFAULT_HTTP_ENCODING);
    sampleResult.setSuccessful(false);
    sampleResult.sampleEnd();
    LOG.error(message, e);
    return sampleResult;
  }

  @Override
  public void threadStarted() {
  }

  @Override
  public void threadFinished() {
    closeConnections();
  }

  private void closeConnections() {
    connections.get().values().forEach(c -> {
      try {
        c.disconnect();
      } catch (Exception e) {
        LOG.error("Problem while closing RTE connection", e);
      }
    });
    connections.get().clear();
  }

}
