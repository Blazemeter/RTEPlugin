package blazemeter.jmeter.plugins.rte.sampler;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.jmeter.samplers.AbstractSampler;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestStateListener;
import org.apache.jmeter.testelement.ThreadListener;
import org.apache.jmeter.testelement.property.TestElementProperty;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;
import org.tn5250j.Session5250;
import org.tn5250j.SessionConfig;
import org.tn5250j.framework.tn5250.Screen5250;
import org.tn5250j.TN5250jConstants;
import org.apache.jmeter.testelement.property.JMeterProperty;

public class RTESampler extends AbstractSampler implements TestStateListener, ThreadListener {

	private static final long serialVersionUID = -8230648949935454790L;

	public static final Trigger DEFAULT_TRIGGER = Trigger.ENTER;
	public static final Protocol DEFAULT_PROTOCOL = Protocol.TN5250;
	public static final TerminalType DEFAULT_TERMINAL_TYPE = TerminalType.IBM_3179_2;
	public static final SSLType DEFAULT_SSLTYPE = SSLType.NONE;
	
	protected static final Logger log = LoggingManager.getLoggerForClass();

	protected static final int DEFAULT_CONNECTION_TIMEOUT = 20000; // 20 sec
	protected static final int DEFAULT_RESPONSE_TIMEOUT = 20000; // 20 sec

	protected static final int UNSPECIFIED_PORT = 0;
	protected static final String UNSPECIFIED_PORT_AS_STRING = "0";

	protected static final int DEFAULT_RTE_PORT = 80;

	public static final String TYPING_STYLE_FAST = "Fast";
	public static final String TYPING_STYLE_HUMAN = "Human";

	public static final String CONFIG_PORT = "RTEConnectionConfig.Port";
	public static final String CONFIG_SERVER = "RTEConnectionConfig.Server";
	public static final String CONFIG_USER = "RTEConnectionConfig.User";
	public static final String CONFIG_PASS = "RTEConnectionConfig.Pass";
	public static final String CONFIG_PROTOCOL = "RTEConnectionConfig.Protocol";
	public static final String CONFIG_SSL_TYPE = "RTEConnectionConfig.SSL_Type";
	public static final String CONFIG_TIMEOUT = "RTEConnectionConfig.Timeout";
	public static final String CONFIG_TERMINAL_TYPE = "RTEConnectionConfig.Terminal_Type";

	protected static Map<String, Session5250> connectionList;

	private CountDownLatch openLatch = new CountDownLatch(1);

	public RTESampler() {
		super();
		setName("RTE");
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

	private Session5250 getConnection(String connectionId) throws Exception {

		if (connectionList.containsKey(connectionId)) {
			Session5250 session = connectionList.get(connectionId);

			if (!session.isConnected()) {
				int connectionTimeout;

				try {
					connectionTimeout = Integer.parseInt(getTimeout());
				} catch (NumberFormatException ex) {
					log.warn("Connection timeout is not a number; using the default connection timeout of "
							+ DEFAULT_CONNECTION_TIMEOUT + "ms");
					connectionTimeout = DEFAULT_CONNECTION_TIMEOUT;
				}
				while (!session.isConnected() && connectionTimeout > 0) {
					Thread.sleep(100);
					connectionTimeout = connectionTimeout - 100;
				}
			}

			return session;

		}

		String server = getServer();
		int port = getPort();

		Session5250 session = null;
		Properties sesProp = new Properties();
		SessionConfig sesConfig = new SessionConfig(server, server);
		session = new Session5250(sesProp, server, server, sesConfig);
		sesProp.setProperty(TN5250jConstants.SSL_TYPE, TN5250jConstants.SSL_TYPE_NONE);
		sesProp.setProperty(TN5250jConstants.SESSION_HOST, server);

		session.connect();

		int connectionTimeout;

		try {
			connectionTimeout = Integer.parseInt(getTimeout());
		} catch (NumberFormatException ex) {
			log.warn("Connection timeout is not a number; using the default connection timeout of "
					+ DEFAULT_CONNECTION_TIMEOUT + "ms");
			connectionTimeout = DEFAULT_CONNECTION_TIMEOUT;
		}

		while (!session.isConnected() && connectionTimeout > 0) {
			Thread.sleep(100);
			connectionTimeout = connectionTimeout - 100;
		}

		connectionList.put(connectionId, session);
		return session;
	}

	private String screenToString(Screen5250 screen) {
		String showme = getScreenAsString(screen);
		String sb = "";

		for (int i = 0; i < showme.length(); i += screen.getColumns()) {
			sb += showme.substring(i, i + screen.getColumns());
			sb += "\n";
		}
		return sb;
	}

	private String getScreenAsString(Screen5250 screen) {
		char[] buffer = new char[screen.getScreenLength()];
		screen.GetScreen(buffer, screen.getScreenLength(), TN5250jConstants.PLANE_TEXT);
		return new String(buffer);
	}

	@Override
	public SampleResult sample(Entry entry) {

		SampleResult sampleResult = new SampleResult();
		sampleResult.setSampleLabel(getName());
		sampleResult.sampleStart();
		Session5250 session = null;

		try {
			session = getConnection(getConnectionId());
		} catch (Exception e) {
			sampleResult.setSuccessful(false);
			sampleResult.setResponseMessage(e.getMessage());
			sampleResult.setResponseData(e.getStackTrace().toString(), "utf-8");
			sampleResult.sampleEnd();
			e.printStackTrace();
			return sampleResult;
		}

		if (!session.isConnected()) {
			sampleResult.setSuccessful(false);
			sampleResult.setResponseMessage("Connection error");
			sampleResult.setResponseData("Connection error", "utf-8");
			sampleResult.sampleEnd();
		}

		int waitCursor;
		int waitSilent;
		int waitSync;
		int waiyText;

		try {
			waitCursor = Integer.parseInt(getWaitTimeoutCursor());
		} catch (NumberFormatException ex) {
			log.warn("Cursor timeout is not a number; using the default cursor timeout of " + DEFAULT_RESPONSE_TIMEOUT
					+ "ms");
			waitCursor = DEFAULT_RESPONSE_TIMEOUT;
		}
		try {
			waitSilent = Integer.parseInt(getWaitTimeoutSilent());
		} catch (NumberFormatException ex) {
			log.warn("Silent timeout is not a number; using the default Silent timeout of " + DEFAULT_RESPONSE_TIMEOUT
					+ "ms");
			waitSilent = DEFAULT_RESPONSE_TIMEOUT;
		}
		try {
			waitSync = Integer.parseInt(getWaitTimeoutSync());
		} catch (NumberFormatException ex) {
			log.warn("Sync timeout is not a number; using the default Sync timeout of " + DEFAULT_RESPONSE_TIMEOUT
					+ "ms");
			waitSync = DEFAULT_RESPONSE_TIMEOUT;
		}
		try {
			waiyText = Integer.parseInt(getWaitTimeoutText());
		} catch (NumberFormatException ex) {
			log.warn("Text timeout is not a number; using the default Text timeout of " + DEFAULT_RESPONSE_TIMEOUT
					+ "ms");
			waiyText = DEFAULT_RESPONSE_TIMEOUT;
		}

		for (JMeterProperty p : getPayload()){
			CoordInput i = (CoordInput) p.getObjectValue();
			int col=0;
			int row=0;
			try {
				col = Integer.parseInt(i.getColumn());
			} catch (NumberFormatException ex) {
				log.warn("Column is not a number; using the default column timeout of 0");
			}
			try {
				row = Integer.parseInt(i.getRow());
			} catch (NumberFormatException ex) {
				log.warn("Row is not a number; using the default Row timeout of 0");
			}
			session.getScreen().setCursor(col, row);
			session.getScreen().sendKeys(i.getInput());
		}
		
		session.getScreen().sendKeys("[enter]");

		sampleResult.setSuccessful(true);
		sampleResult.setResponseData(screenToString(session.getScreen()), "utf-8");
		sampleResult.sampleEnd();

		return sampleResult;
	}

	public boolean awaitOpen(int duration, TimeUnit unit) throws InterruptedException {
		boolean res = this.openLatch.await(duration, unit);
		return res;
	}

	public String getServer() {
		return getPropertyAsString(CONFIG_SERVER);
	}

	public String getUser() {
		return getPropertyAsString(CONFIG_USER);
	}

	public String getPass() {
		return getPropertyAsString(CONFIG_PASS);
	}

	public TerminalType getTerminal() {
		return TerminalType.valueOf(getPropertyAsString(CONFIG_TERMINAL_TYPE));
	}
	
	public Protocol getProtocol() {
		return Protocol.valueOf(getPropertyAsString(CONFIG_PROTOCOL));
	}

	public SSLType getSSLType() {
		return SSLType.valueOf(getPropertyAsString(CONFIG_SSL_TYPE));
	}

	public String getTimeout() {
		return getPropertyAsString(CONFIG_TIMEOUT);
	}

	public int getPort() {
		final int port = getPortIfSpecified();
		if (port == UNSPECIFIED_PORT) {
			return DEFAULT_RTE_PORT;
		}
		return port;
	}

	public int getPortIfSpecified() {
		String port_s = getPropertyAsString(CONFIG_PORT, UNSPECIFIED_PORT_AS_STRING);
		try {
			return Integer.parseInt(port_s.trim());
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

	public String getType() {
		return getPropertyAsString("Type");
	}

	public void setType(String type) {
		setProperty("Type", type);
	}

	public String getField() {
		return getPropertyAsString("Field");
	}

	public void setField(String field) {
		setProperty("Field", field);
	}

	public int getCoordX() {
		return getPropertyAsInt("CoordX");
	}

	public void setCoordX(int coordX) {
		setProperty("CoordX", coordX);
	}

	public int getCoordY() {
		return getPropertyAsInt("CoordY");
	}

	public void setCoordY(int coordY) {
		setProperty("CoordY", coordY);
	}

	public Inputs getPayload() {
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
		if (getPropertyAsString("Trigger").isEmpty())
			return DEFAULT_TRIGGER;
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
		// TODO Auto-generated method stub

	}

	@Override
	public void threadStarted() {
		// TODO Auto-generated method stub

	}

	@Override
	public void testEnded() {
		// TODO Auto-generated method stub

	}

	@Override
	public void testEnded(String arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void testStarted() {
		connectionList = new ConcurrentHashMap<String, Session5250>();

	}

	@Override
	public void testStarted(String arg0) {
		// TODO Auto-generated method stub

	}
}

