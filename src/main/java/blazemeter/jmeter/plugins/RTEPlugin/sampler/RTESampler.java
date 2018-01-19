package blazemeter.jmeter.plugins.RTEPlugin.sampler;

import org.apache.jmeter.protocol.http.util.HTTPConstants;
import org.apache.jmeter.samplers.AbstractSampler;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestStateListener;
import org.apache.jmeter.testelement.ThreadListener;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

public class RTESampler extends AbstractSampler implements TestStateListener, ThreadListener  {
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -8230648949935454790L;

	protected static final Logger log = LoggingManager.getLoggerForClass();
    
    protected static final int DEFAULT_CONNECTION_TIMEOUT = 20000; //20 sec
    protected static final int DEFAULT_RESPONSE_TIMEOUT = 20000; //20 sec
    
    protected static final int UNSPECIFIED_PORT = 0;
    protected static final String UNSPECIFIED_PORT_AS_STRING = "0"; 
    
    protected static final int DEFAULT_RTE_PORT = 80; 
    
    public static final String TYPING_STYLE_FAST = "Fast";
    public static final String TYPING_STYLE_HUMAN = "Human";
    
    public static final String TYPE_FILL_FIELD = "Fill Field";
    public static final String TYPE_SEND_KEY = "Send Key";
    
    public static final String SSLTYPE_NONE = "NONE";
    public static final String SSLTYPE_SSLV2 = "SSLv2";
    public static final String SSLTYPE_SSLV3 = "SSLv3";
    public static final String SSLTYPE_TLS = "TLS";
    
    public static final String PROTOCOL_TN5250 = "TN5250";
    public static final String PROTOCOL_TN3270 = "TN3270";
      
    public static final String CONFIG_PORT = "RTEConnectionConfig.Port";
    public static final String CONFIG_SERVER = "RTEConnectionConfig.Server";
    public static final String CONFIG_USER = "RTEConnectionConfig.User";
    public static final String CONFIG_PASS = "RTEConnectionConfig.Pass";
    public static final String CONFIG_PROTOCOL = "RTEConnectionConfig.Protocol";
    public static final String CONFIG_SSL_TYPE = "RTEConnectionConfig.SSL_Type"; 
    public static final String CONFIG_TIMEOUT = "RTEConnectionConfig.Timeout"; 

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
  
    
    @Override
    public SampleResult sample(Entry entry) {
    	
    	SampleResult sampleResult = new SampleResult();
    	sampleResult.setSampleLabel(getName());
    	return sampleResult;
	}

    public String getServer(){
		return getPropertyAsString(CONFIG_SERVER);
	}
    
    public String getUser(){
		return getPropertyAsString(CONFIG_USER);
	}
    
    public String getPass(){
		return getPropertyAsString(CONFIG_PASS);
	}
    
    public String getProtocol(){
		return getPropertyAsString(CONFIG_PROTOCOL);
	}
    
    public String getSSLType(){
		return getPropertyAsString(CONFIG_SSL_TYPE);
	}
    
    public int getTimeout(){
		return getPropertyAsInt(CONFIG_TIMEOUT);
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
	
	public String getPayload() {
		return getPropertyAsString("Payload");
	}

	public void setPayload(String payload) {
		setProperty("Payload", payload);
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
	
	public String getTextToWait (){
		return getPropertyAsString("TextToWait");
	}
	
	public void setTextToWait (String textToWait){
		setProperty("TextToWait", textToWait);
	}
	
	public String getCoordXToWait (){
		return getPropertyAsString("CoordXToWait");
	}
	
	public void setCoordXToWait (String coordXToWait){
		setProperty("CoordXToWait", coordXToWait);
	}
	
	public String getCoordYToWait (){
		return getPropertyAsString("CoordYToWait");
	}
	
	public void setCoordYToWait (String coordYToWait){
		setProperty("CoordYToWait", coordYToWait);
	}
	
	public String getWaitTimeout() {
		return getPropertyAsString("WaitTimeout");
	}

	public void setWaitTimeout(String waitTimeout) {
		setProperty("WaitTimeout", waitTimeout);
	}
	
	public boolean getDisconnect() {
		return getPropertyAsBoolean("Disconnect");
	}

	public void setDisconnect(boolean disconnect) {
		setProperty("Disconnect", disconnect);
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
		// TODO Auto-generated method stub
		
	}

	@Override
	public void testStarted(String arg0) {
		// TODO Auto-generated method stub
		
	}
	
}

