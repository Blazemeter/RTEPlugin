package com.blazemeter.jmeter.rte.sampler;

import com.blazemeter.jmeter.rte.core.CoordInput;
import com.blazemeter.jmeter.rte.core.Position;
import com.blazemeter.jmeter.rte.core.Protocol;
import com.blazemeter.jmeter.rte.core.RteIOException;
import com.blazemeter.jmeter.rte.core.RteProtocolClient;
import com.blazemeter.jmeter.rte.core.SSLType;
import com.blazemeter.jmeter.rte.core.TerminalType;
import com.blazemeter.jmeter.rte.core.Trigger;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.apache.jmeter.samplers.AbstractSampler;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.ThreadListener;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.testelement.property.TestElementProperty;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

public class RTESampler extends AbstractSampler implements ThreadListener {

  public static final String CONFIG_PORT = "RTEConnectionConfig.Port";
  public static final String CONFIG_SERVER = "RTEConnectionConfig.Server";
  public static final String CONFIG_USER = "RTEConnectionConfig.User";
  public static final String CONFIG_PASS = "RTEConnectionConfig.Pass";
  public static final String CONFIG_PROTOCOL = "RTEConnectionConfig.Protocol";
  public static final String CONFIG_SSL_TYPE = "RTEConnectionConfig.SSL_Type";
  public static final String CONFIG_TIMEOUT = "RTEConnectionConfig.Timeout";
  public static final String CONFIG_TERMINAL_TYPE = "RTEConnectionConfig.Terminal_Type";
  public static final String TYPING_STYLE_FAST = "Fast";
  public static final String TYPING_STYLE_HUMAN = "Human";
  public static final Trigger DEFAULT_TRIGGER = Trigger.ENTER;
  public static final Protocol DEFAULT_PROTOCOL = Protocol.TN5250;
  public static final TerminalType DEFAULT_TERMINAL_TYPE = TerminalType.IBM_3179_2;
  public static final SSLType DEFAULT_SSLTYPE = SSLType.NONE;

  private static final Logger LOG = LoggingManager.getLoggerForClass();
  private static final int DEFAULT_CONNECTION_TIMEOUT = 20000; // 20 sec
  private static final int DEFAULT_RESPONSE_TIMEOUT = 20000; // 20 sec
  private static final int UNSPECIFIED_PORT = 0;
  private static final String UNSPECIFIED_PORT_AS_STRING = "0";
  private static final int DEFAULT_RTE_PORT = 23;
  private static ThreadLocal<Map<String, RteProtocolClient>> connections = ThreadLocal
      .withInitial(HashMap::new);
  private final Function<Protocol, RteProtocolClient> protocolFactory;
  private SampleResult sampleResult;

  public RTESampler() {
    this(p -> p.createProtocolClient());
  }

  public RTESampler(Function<Protocol, RteProtocolClient> protocolFactory) {
    super();
    setName("RTE");
    this.protocolFactory = protocolFactory;
  }

  @Override
  public void setName(String name) {
    if (name != null) {
      setProperty(TestElement.NAME, name);
    }
  }

  @Override
  public String getName() {
    return getPropertyAsString(TestElement.NAME);
  }

  private String buildConnectionId() {
    return getServer() + ": " + getPort();
  }

  private RteProtocolClient getClient() throws RteIOException, InterruptedException {

    String clientId = buildConnectionId();

    Map<String, RteProtocolClient> clients = connections.get();

    if (clients.containsKey(clientId)) {
      return clients.get(clientId);
    }

    RteProtocolClient client = protocolFactory.apply(getProtocol());
    clients.put(clientId, client);
    client.connect(getServer(), getPort(), getTerminal());

    //TODO Remove when the RTEClient supports waiting until the connection is finished
    Thread.sleep(1000);

    return client;
  }

  private void closeConnections() {
    connections.get().values().forEach(c -> {
      c.disconnect();
    });
    connections.get().clear();
  }

  private void errorResult(String message, Throwable e) {
    StringWriter sw = new StringWriter();
    e.printStackTrace(new PrintWriter(sw));
    sampleResult.setDataType(SampleResult.TEXT);
    sampleResult.setResponseCode(e.getClass().getName());
    sampleResult.setResponseMessage(e.getMessage());
    sampleResult.setResponseData(sw.toString(), SampleResult.DEFAULT_HTTP_ENCODING);
    sampleResult.setSuccessful(false);
    sampleResult.sampleEnd();
    LOG.error(message, e);
  }

  @Override
  public SampleResult sample(Entry entry) {

    sampleResult = new SampleResult();
    sampleResult.setSampleLabel(getName());
    sampleResult.sampleStart();
    RteProtocolClient client = null;

    try {
      client = getClient();
      sampleResult.connectEnd();
    } catch (RteIOException e) {
      errorResult("Error while establishing the connection", e);
      return sampleResult;
    } catch (InterruptedException e) {
      errorResult("Error while establishing the connection", e);
      closeConnections();
      Thread.currentThread().interrupt();
      return sampleResult;
    }

    List<CoordInput> inputs = getCoordInputs();

    String screen = "";

    try {
      screen = client.send(inputs);
    } catch (InterruptedException e) {
      errorResult("Error while sending a message", e);
      closeConnections();
      Thread.currentThread().interrupt();
      return sampleResult;
    }

    sampleResult.setSuccessful(true);
    sampleResult.setResponseData(screen, "utf-8");
    sampleResult.sampleEnd();

    return sampleResult;
  }

  private List<CoordInput> getCoordInputs() {
    List<CoordInput> inputs = new ArrayList<>();

    for (JMeterProperty p : getInputs()) {
      CoordInputRowGUI c = (CoordInputRowGUI) p.getObjectValue();
      inputs.add(new CoordInput(
          new Position(Integer.parseInt(c.getColumn()), Integer.parseInt(c.getRow())),
          c.getInput()));
    }
    return inputs;
  }

  private String getServer() {
    return getPropertyAsString(CONFIG_SERVER);
  }

  private String getUser() {
    return getPropertyAsString(CONFIG_USER);
  }

  private String getPass() {
    return getPropertyAsString(CONFIG_PASS);
  }

  private TerminalType getTerminal() {
    return TerminalType.valueOf(getPropertyAsString(CONFIG_TERMINAL_TYPE));
  }

  private Protocol getProtocol() {
    return Protocol.valueOf(getPropertyAsString(CONFIG_PROTOCOL));
  }

  private SSLType getSSLType() {
    return SSLType.valueOf(getPropertyAsString(CONFIG_SSL_TYPE));
  }

  private String getTimeout() {
    return getPropertyAsString(CONFIG_TIMEOUT);
  }

  private int getPort() {
    final int port = getPortIfSpecified();
    if (port == UNSPECIFIED_PORT) {
      return DEFAULT_RTE_PORT;
    }
    return port;
  }

  private int getPortIfSpecified() {
    String portS = getPropertyAsString(CONFIG_PORT, UNSPECIFIED_PORT_AS_STRING);
    try {
      return Integer.parseInt(portS.trim());
    } catch (NumberFormatException e) {
      return UNSPECIFIED_PORT;
    }
  }

  public String getTypingStyle() {
    return getPropertyAsString("TypingStyle");
  }

  public void setTypingStyle(String typingStyle) {
    setProperty("TypingStyle", typingStyle);
  }

  public Inputs getInputs() {
    return (Inputs) getProperty(Inputs.INPUTS).getObjectValue();
  }

  public void setPayload(Inputs payload) {
    setProperty(new TestElementProperty(Inputs.INPUTS, payload));
  }

  public boolean getWaitSync() {
    return getPropertyAsBoolean("WaitSync");
  }

  public void setWaitSync(boolean waitSync) {
    setProperty("WaitSync", waitSync);
  }

  public boolean getWaitCursor() {
    return getPropertyAsBoolean("WaitCursor");
  }

  public void setWaitCursor(boolean waitCursor) {
    setProperty("WaitCursor", waitCursor);
  }

  public boolean getWaitSilent() {
    return getPropertyAsBoolean("WaitSilent");
  }

  public void setWaitSilent(boolean waitSilent) {
    setProperty("WaitSilent", waitSilent);
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

  public String getWaitTimeoutSync() {
    return getPropertyAsString("WaitTimeoutSync");
  }

  public void setWaitTimeoutSync(String waitTimeoutSync) {
    setProperty("WaitTimeoutSync", waitTimeoutSync);
  }

  public String getWaitTimeoutCursor() {
    return getPropertyAsString("WaitTimeoutCursor");
  }

  public void setWaitTimeoutCursor(String waitTimeoutCursor) {
    setProperty("WaitTimeoutCursor", waitTimeoutCursor);
  }

  public String getWaitTimeoutSilent() {
    return getPropertyAsString("WaitTimeoutSilent");
  }

  public void setWaitTimeoutSilent(String waitTimeoutSilent) {
    setProperty("WaitTimeoutSilent", waitTimeoutSilent);
  }

  public String getWaitForSilent() {
    return getPropertyAsString("WaitForSilent");
  }

  public void setWaitForSilent(String waitForSilent) {
    setProperty("WaitForSilent", waitForSilent);
  }

  public String getWaitTimeoutText() {
    return getPropertyAsString("WaitTimeoutText");
  }

  public void setWaitTimeoutText(String waitTimeoutText) {
    setProperty("WaitTimeoutText", waitTimeoutText);
  }

  public boolean getDisconnect() {
    return getPropertyAsBoolean("Disconnect");
  }

  public void setDisconnect(boolean disconnect) {
    setProperty("Disconnect", disconnect);
  }

  public Trigger getTrigger() {
    if (getPropertyAsString("Trigger").isEmpty()) {
      return DEFAULT_TRIGGER;
    }
    return Trigger.valueOf(getPropertyAsString("Trigger"));
  }

  public void setTrigger(Trigger trigger) {
    setProperty("Trigger", trigger.name());
  }

  public String getConnectionId() {
    return getThreadName() + getServer() + getPort();
  }

  @Override
  public void threadFinished() {
    closeConnections();
  }

  @Override
  public void threadStarted() {
  }
}
