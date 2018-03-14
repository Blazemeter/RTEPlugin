package com.blazemeter.jmeter.rte.sampler;

import static org.apache.jmeter.util.SSLManager.JAVAX_NET_SSL_KEY_STORE;

import com.blazemeter.jmeter.rte.core.Action;
import com.blazemeter.jmeter.rte.core.CoordInput;
import com.blazemeter.jmeter.rte.core.Position;
import com.blazemeter.jmeter.rte.core.Protocol;
import com.blazemeter.jmeter.rte.core.RteIOException;
import com.blazemeter.jmeter.rte.core.RteProtocolClient;
import com.blazemeter.jmeter.rte.core.TerminalType;
import com.blazemeter.jmeter.rte.core.ssl.SSLData;
import com.blazemeter.jmeter.rte.core.ssl.SSLType;
import com.blazemeter.jmeter.rte.core.wait.Area;
import com.blazemeter.jmeter.rte.core.wait.ConditionWaiter;
import com.blazemeter.jmeter.rte.core.wait.CursorWaitCondition;
import com.blazemeter.jmeter.rte.core.wait.SilentWaitCondition;
import com.blazemeter.jmeter.rte.core.wait.SyncWaitCondition;
import com.blazemeter.jmeter.rte.core.wait.TextWaitCondition;
import com.blazemeter.jmeter.rte.core.wait.WaitCondition;
import com.helger.commons.annotation.VisibleForTesting;
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
import org.apache.jmeter.util.JMeterUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RTESampler extends AbstractSampler implements ThreadListener {

  public static final String CONFIG_PORT = "RTEConnectionConfig.port";
  public static final String CONFIG_SERVER = "RTEConnectionConfig.server";
  public static final String CONFIG_PROTOCOL = "RTEConnectionConfig.protocol";
  public static final String CONFIG_SSL_TYPE = "RTEConnectionConfig.sslType";
  public static final String CONFIG_CONNECTION_TIMEOUT = "RTEConnectionConfig.connectTimeout";
  public static final String CONFIG_TERMINAL_TYPE = "RTEConnectionConfig.terminalType";
  public static final int DEFAULT_PORT = 23;
  public static final long DEFAULT_CONNECTION_TIMEOUT_MILLIS = 60000;
  public static final Action DEFAULT_ACTION = Action.ENTER;
  public static final Protocol DEFAULT_PROTOCOL = Protocol.TN5250;
  public static final TerminalType DEFAULT_TERMINAL_TYPE = TerminalType.IBM_3179_2;
  public static final SSLType DEFAULT_SSLTYPE = SSLType.NONE;
  @VisibleForTesting
  protected static final long DEFAULT_STABLE_TIMEOUT_MILLIS = 1000;
  @VisibleForTesting
  protected static final long DEFAULT_WAIT_SYNC_TIMEOUT_MILLIS = 60000;
  @VisibleForTesting
  protected static final long DEFAULT_WAIT_SILENT_TIME_MILLIS = 1000;
  @VisibleForTesting
  protected static final long DEFAULT_WAIT_SILENT_TIMEOUT_MILLIS = 60000;
  @VisibleForTesting
  protected static final long DEFAULT_WAIT_TEXT_TIMEOUT_MILLIS = 30000;
  @VisibleForTesting
  protected static final long DEFAULT_WAIT_CURSOR_TIMEOUT_MILLIS = 30000;


  //If users wants to change Stable Timeout value it should be specified in
  // jmeter.properties by adding a line like ths one:
  // "RTEConnectionConfig.stableTimeoutMillis=value"
  private static final String CONFIG_STABLE_TIMEOUT = "RTEConnectionConfig.stableTimeoutMillis";
  private static final String ACTION_PROPERTY = "RTESampler.action";
  private static final String DISCONNECT_PROPERTY = "RTESampler.disconnect";
  private static final String JUST_CONNECT_PROPERTY = "RTESampler.justConnect";
  private static final String WAIT_SYNC_PROPERTY = "RTESampler.waitSync";
  private static final String WAIT_SYNC_TIMEOUT_PROPERTY = "RTESampler.waitSyncTimeout";
  private static final String WAIT_CURSOR_PROPERTY = "RTESampler.waitCursor";
  private static final String WAIT_CURSOR_ROW_PROPERTY = "RTESampler.waitCursorRow";
  private static final String WAIT_CURSOR_COLUMN_PROPERTY = "RTESampler.waitCursorColumn";
  private static final String WAIT_CURSOR_TIMEOUT_PROPERTY = "RTESampler.waitCursorTimeout";
  private static final String WAIT_SILENT_PROPERTY = "RTESampler.waitSilent";
  private static final String WAIT_SILENT_TIME_PROPERTY = "RTESampler.waitSilentTime";
  private static final String WAIT_SILENT_TIMEOUT_PROPERTY = "RTESampler.waitSilentTimeout";
  private static final String WAIT_TEXT_PROPERTY = "RTESampler.waitText";
  private static final String WAIT_TEXT_REGEX_PROPERTY = "RTESampler.waitTextRegex";
  private static final String WAIT_TEXT_AREA_TOP_PROPERTY = "RTESampler.waitTextAreaTop";
  private static final String WAIT_TEXT_AREA_LEFT_PROPERTY = "RTESampler.waitTextAreaLeft";
  private static final String WAIT_TEXT_AREA_BOTTOM_PROPERTY = "RTESampler.waitTextAreaBottom";
  private static final String WAIT_TEXT_AREA_RIGHT_PROPERTY = "RTESampler.waitTextAreaRight";
  private static final String WAIT_TEXT_TIMEOUT_PROPERTY = "RTESampler.waitTextTimeout";
  private static final String JAVAX_NET_SSL_KEY_STORE_PASSWORD = "javax.net.ssl.keyStorePassword";

  private static final Logger LOG = LoggerFactory.getLogger(RTESampler.class);
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
    return JMeterUtils.getPropDefault(CONFIG_STABLE_TIMEOUT, DEFAULT_STABLE_TIMEOUT_MILLIS);
  }

  @VisibleForTesting
  protected void setStableTimeout(Long timeoutMillis) {
    if (timeoutMillis == null) {
      JMeterUtils.getJMeterProperties().remove(CONFIG_STABLE_TIMEOUT);
    } else {
      JMeterUtils.setProperty(CONFIG_STABLE_TIMEOUT, String.valueOf(timeoutMillis));
    }
  }

  @VisibleForTesting
  protected void setKeyStore(String keystore) {
    if (keystore == null) {
      System.getProperties().remove(JAVAX_NET_SSL_KEY_STORE);
    } else {
      System.setProperty(JAVAX_NET_SSL_KEY_STORE, keystore);
    }
  }

  @VisibleForTesting
  protected void setKeyStorePassword(String keystorePwd) {
    if (keystorePwd == null) {
      System.getProperties().remove(JAVAX_NET_SSL_KEY_STORE_PASSWORD);
    } else {
      System.setProperty(JAVAX_NET_SSL_KEY_STORE_PASSWORD, keystorePwd);
    }
  }

  private SSLType getSSLType() {
    return SSLType.valueOf(getPropertyAsString(CONFIG_SSL_TYPE));
  }

  public void setPayload(Inputs payload) {
    setProperty(new TestElementProperty(Inputs.INPUTS, payload));
  }

  public Action getAction() {
    if (getPropertyAsString(ACTION_PROPERTY).isEmpty()) {
      return DEFAULT_ACTION;
    }
    return Action.valueOf(getPropertyAsString(ACTION_PROPERTY));
  }

  public void setAction(Action action) {
    setProperty(ACTION_PROPERTY, action.name());
  }

  public boolean getDisconnect() {
    return getPropertyAsBoolean(DISCONNECT_PROPERTY);
  }

  public void setDisconnect(boolean disconnect) {
    setProperty(DISCONNECT_PROPERTY, disconnect);
  }

  public boolean getJustConnect() {
    return getPropertyAsBoolean(JUST_CONNECT_PROPERTY);
  }

  public void setJustConnect(boolean disconnect) {
    setProperty(JUST_CONNECT_PROPERTY, disconnect);
  }

  public boolean getWaitSync() {
    return getPropertyAsBoolean(WAIT_SYNC_PROPERTY, true);
  }

  public void setWaitSync(boolean waitSync) {
    setProperty(WAIT_SYNC_PROPERTY, waitSync);
  }

  public String getWaitSyncTimeout() {
    return getPropertyAsString(WAIT_SYNC_TIMEOUT_PROPERTY, "" + DEFAULT_WAIT_SYNC_TIMEOUT_MILLIS);
  }

  public void setWaitSyncTimeout(String waitTimeoutSync) {
    setProperty(WAIT_SYNC_TIMEOUT_PROPERTY, waitTimeoutSync);
  }

  private long getWaitSyncTimeoutValue() {
    return getPropertyAsLong(WAIT_SYNC_TIMEOUT_PROPERTY, DEFAULT_WAIT_SYNC_TIMEOUT_MILLIS);
  }

  public boolean getWaitCursor() {
    return getPropertyAsBoolean(WAIT_CURSOR_PROPERTY);
  }

  public void setWaitCursor(boolean waitCursor) {
    setProperty(WAIT_CURSOR_PROPERTY, waitCursor);
  }

  private long getWaitCursorTimeoutValue() {
    return getPropertyAsLong(WAIT_CURSOR_TIMEOUT_PROPERTY, DEFAULT_WAIT_CURSOR_TIMEOUT_MILLIS);
  }

  public String getWaitCursorRow() {
    return getPropertyAsString(WAIT_CURSOR_ROW_PROPERTY, String.valueOf(1));
  }

  public void setWaitCursorRow(String row) {
    setProperty(WAIT_CURSOR_ROW_PROPERTY, row);
  }

  private int getWaitCursorRowValue() {
    return getPropertyAsInt(WAIT_CURSOR_ROW_PROPERTY, 1);
  }

  public String getWaitCursorColumn() {
    return getPropertyAsString(WAIT_CURSOR_COLUMN_PROPERTY, String.valueOf(1));
  }

  public void setWaitCursorColumn(String row) {
    setProperty(WAIT_CURSOR_COLUMN_PROPERTY, row);
  }

  private int getWaitCursorColumnValue() {
    return getPropertyAsInt(WAIT_CURSOR_COLUMN_PROPERTY, 1);
  }

  public String getWaitCursorTimeout() {
    return getPropertyAsString(WAIT_CURSOR_TIMEOUT_PROPERTY,
        String.valueOf(DEFAULT_WAIT_CURSOR_TIMEOUT_MILLIS));
  }

  public void setWaitCursorTimeout(String waitTimeoutCursor) {
    setProperty(WAIT_CURSOR_TIMEOUT_PROPERTY, waitTimeoutCursor);
  }

  public boolean getWaitSilent() {
    return getPropertyAsBoolean(WAIT_SILENT_PROPERTY);
  }

  public void setWaitSilent(boolean waitSilent) {
    setProperty(WAIT_SILENT_PROPERTY, waitSilent);
  }

  public String getWaitSilentTime() {
    return getPropertyAsString(WAIT_SILENT_TIME_PROPERTY,
        String.valueOf(DEFAULT_WAIT_SILENT_TIME_MILLIS));
  }

  public void setWaitSilentTime(String waitSilentTime) {
    setProperty(WAIT_SILENT_TIME_PROPERTY, waitSilentTime);
  }

  private long getWaitSilentTimeValue() {
    return getPropertyAsLong(WAIT_SILENT_TIME_PROPERTY, DEFAULT_WAIT_SILENT_TIME_MILLIS);
  }

  public String getWaitSilentTimeout() {
    return getPropertyAsString(WAIT_SILENT_TIMEOUT_PROPERTY,
        String.valueOf(DEFAULT_WAIT_SILENT_TIMEOUT_MILLIS));
  }

  public void setWaitSilentTimeout(String waitSilentTimeout) {
    setProperty(WAIT_SILENT_TIMEOUT_PROPERTY, waitSilentTimeout);
  }

  private long getWaitSilentTimeoutValue() {
    return getPropertyAsLong(WAIT_SILENT_TIMEOUT_PROPERTY, DEFAULT_WAIT_SILENT_TIMEOUT_MILLIS);
  }

  public boolean getWaitText() {
    return getPropertyAsBoolean(WAIT_TEXT_PROPERTY);
  }

  public void setWaitText(boolean waitText) {
    setProperty(WAIT_TEXT_PROPERTY, waitText);
  }

  public String getWaitTextRegex() {
    return getPropertyAsString(WAIT_TEXT_REGEX_PROPERTY);
  }

  public void setWaitTextRegex(String regex) {
    setProperty(WAIT_TEXT_REGEX_PROPERTY, regex);
  }

  public String getWaitTextAreaTop() {
    return getPropertyAsString(WAIT_TEXT_AREA_TOP_PROPERTY, String.valueOf(1));
  }

  public void setWaitTextAreaTop(String row) {
    setProperty(WAIT_TEXT_AREA_TOP_PROPERTY, row);
  }

  private int getWaitTextAreaTopValue() {
    return getPropertyAsInt(WAIT_TEXT_AREA_TOP_PROPERTY, 1);
  }

  public String getWaitTextAreaLeft() {
    return getPropertyAsString(WAIT_TEXT_AREA_LEFT_PROPERTY, String.valueOf(1));
  }

  public void setWaitTextAreaLeft(String column) {
    setProperty(WAIT_TEXT_AREA_LEFT_PROPERTY, column);
  }

  private int getWaitTextAreaLeftValue() {
    return getPropertyAsInt(WAIT_TEXT_AREA_LEFT_PROPERTY, 1);
  }

  public String getWaitTextAreaBottom() {
    return getPropertyAsString(WAIT_TEXT_AREA_BOTTOM_PROPERTY);
  }

  public void setWaitTextAreaBottom(String row) {
    setProperty(WAIT_TEXT_AREA_BOTTOM_PROPERTY, row);
  }

  private int getWaitTextAreaBottomValue() {
    return getPropertyAsInt(WAIT_TEXT_AREA_BOTTOM_PROPERTY, Position.UNSPECIFIED_INDEX);
  }

  public String getWaitTextAreaRight() {
    return getPropertyAsString(WAIT_TEXT_AREA_RIGHT_PROPERTY);
  }

  public void setWaitTextAreaRight(String column) {
    setProperty(WAIT_TEXT_AREA_RIGHT_PROPERTY, column);
  }

  private int getWaitTextAreaRightValue() {
    return getPropertyAsInt(WAIT_TEXT_AREA_RIGHT_PROPERTY, Position.UNSPECIFIED_INDEX);
  }

  public String getWaitTextTimeout() {
    return getPropertyAsString(WAIT_TEXT_TIMEOUT_PROPERTY,
        String.valueOf(DEFAULT_WAIT_TEXT_TIMEOUT_MILLIS));
  }

  public void setWaitTextTimeout(String timeout) {
    setProperty(WAIT_TEXT_TIMEOUT_PROPERTY, timeout);
  }

  private long getWaitTextTimeoutValue() {
    return getPropertyAsLong(WAIT_TEXT_TIMEOUT_PROPERTY, DEFAULT_WAIT_TEXT_TIMEOUT_MILLIS);
  }

  @Override
  public SampleResult sample(Entry entry) {

    sampleResult = new SampleResult();
    sampleResult.setSampleLabel(getName());
    sampleResult.sampleStart();

    try {
      RteProtocolClient client = getClient();
      List<? extends ConditionWaiter> waiters = client.buildConditionWaiters(getWaitersList());
      try {
        if (!getJustConnect()) {
          client.send(getCoordInputs(), getAction());
        }
        for (ConditionWaiter waiter : waiters) {
          waiter.await();
        }
        sampleResult.setRequestHeaders(
            buildRequestHeader(client.isInputInhibited(), getAction(), getCoordInputs()));
        sampleResult.setSuccessful(true);
        sampleResult
            .setResponseHeaders(
                buildResponseHeader(client.getCursorPosition(), client.isInputInhibited()));
        sampleResult.setResponseData(client.getScreen(), "utf-8");
        sampleResult.setDataType(SampleResult.TEXT);
        sampleResult.latencyEnd();
        sampleResult.sampleEnd();
        return sampleResult;
      } finally {
        waiters.forEach(ConditionWaiter::stop);
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

  private String buildResponseHeader(Position cursorPosition, boolean state) {
    return "Inhibited: " + state + "\n" +
        "Cursor position: row " + cursorPosition.getRow() + " column " + cursorPosition.getColumn();
  }

  private String buildRequestHeader(boolean state, Action action,
      List<CoordInput> coordInputs) {
    StringBuilder ret = new StringBuilder("Server: " + getServer() + "\n" +
        "Port: " + getPort() + "\n" +
        "Protocol: " + getProtocol().toString() + "\n" +
        "Terminal type: " + getTerminalType().toString() + "\n" +
        "Inhibited: " + state + "\n\n" +
        "Action: " + action.toString() + "\n" +
        "Inputs:\n\n");

    for (CoordInput c : coordInputs) {
      ret.append("Row ").append(c.getPosition().getRow()).append(" Column ")
          .append(c.getPosition().getColumn()).append(" = ").append(c.getInput()).append("\n");
    }
    return ret.toString();
  }

  private RteProtocolClient getClient()
      throws RteIOException, InterruptedException, TimeoutException {
    String clientId = buildConnectionId();
    Map<String, RteProtocolClient> clients = connections.get();

    if (clients.containsKey(clientId)) {
      return clients.get(clientId);
    }

    RteProtocolClient client = protocolFactory.apply(getProtocol());
    SSLData ssldata = new SSLData(getSSLType(),
        System.getProperty(JAVAX_NET_SSL_KEY_STORE_PASSWORD),
        System.getProperty(JAVAX_NET_SSL_KEY_STORE));
    client.connect(getServer(), getPort(), ssldata, getTerminalType(), getConnectionTimeout(),
        getStableTimeout());
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
      waiters.add(new SyncWaitCondition(getWaitSyncTimeoutValue(), getStableTimeout()));
    }
    if (getWaitCursor()) {
      waiters.add(buildCursorWaitCondition());
    }
    if (getWaitSilent()) {
      waiters.add(new SilentWaitCondition(getWaitSilentTimeoutValue(), getWaitSilentTimeValue()));
    }
    if (getWaitText()) {
      waiters.add(buildTextWaitCondition());
    }
    return waiters;
  }

  private CursorWaitCondition buildCursorWaitCondition() {
    return new CursorWaitCondition(
        new Position(getWaitCursorRowValue(), getWaitCursorColumnValue()),
        getWaitCursorTimeoutValue(), getStableTimeout());
  }

  private TextWaitCondition buildTextWaitCondition() {
    return new TextWaitCondition(
        JMeterUtils.getPattern(getWaitTextRegex()),
        JMeterUtils.getMatcher(),
        Area.fromTopLeftBottomRight(getWaitTextAreaTopValue(), getWaitTextAreaLeftValue(),
            getWaitTextAreaBottomValue(), getWaitTextAreaRightValue()),
        getWaitTextTimeoutValue(),
        getStableTimeout());
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
