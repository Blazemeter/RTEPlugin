
package blazemeter.jmeter.plugins.rte.sampler.gui;

import java.awt.BorderLayout;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.config.gui.AbstractConfigGui;
import org.apache.jmeter.config.gui.ArgumentsPanel;
import org.apache.jmeter.protocol.http.gui.HTTPArgumentsPanel;
import org.apache.jmeter.samplers.gui.AbstractSamplerGui;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.TestElementProperty;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

import blazemeter.jmeter.plugins.rte.sampler.RTESampler;



public class RTEConfigGui extends AbstractConfigGui {

    private RTEConfigPanel rteConfigPanelConfigPanel;

	public RTEConfigGui() {
		super();
		init();
		initFields();

		setLayout(new BorderLayout(0, 5));
		setBorder(makeBorder());

		add(makeTitlePanel(), BorderLayout.NORTH);
		add(rteConfigPanelConfigPanel, BorderLayout.CENTER);
	}

    @Override
    public String getStaticLabel() {
        return "RTE Config";
    }
    
    @Override
    public String getLabelResource() {
        throw new IllegalStateException("This shouldn't be called"); //$NON-NLS-1$
    }

    @Override
    public void configure(TestElement element) {
        super.configure(element);
        if (element instanceof ConfigTestElement) {
        	ConfigTestElement configTestElement = (ConfigTestElement) element;
            rteConfigPanelConfigPanel.setServer(configTestElement.getPropertyAsString(RTESampler.CONFIG_SERVER));
            rteConfigPanelConfigPanel.setPort(configTestElement.getPropertyAsString(RTESampler.CONFIG_PORT));
            rteConfigPanelConfigPanel.setProtocol(configTestElement.getPropertyAsString(RTESampler.CONFIG_PROTOCOL));
            rteConfigPanelConfigPanel.setUser(configTestElement.getPropertyAsString(RTESampler.CONFIG_USER));
            rteConfigPanelConfigPanel.setPass(configTestElement.getPropertyAsString(RTESampler.CONFIG_PASS));
            rteConfigPanelConfigPanel.setSSLType(configTestElement.getPropertyAsString(RTESampler.CONFIG_SSL_TYPE));
            rteConfigPanelConfigPanel.setTimeout(configTestElement.getPropertyAsString(RTESampler.CONFIG_TIMEOUT));
           
        }
    }

    @Override
    public TestElement createTestElement() {
    	ConfigTestElement config = new ConfigTestElement();
    	config.setName(this.getName());
    	config.setProperty(TestElement.GUI_CLASS, this.getClass().getName());
    	config.setProperty(TestElement.TEST_CLASS, config.getClass().getName());
        modifyTestElement(config);
        return config;
    }

    @Override
    public void modifyTestElement(TestElement te) {
        configureTestElement(te);
        if (te instanceof ConfigTestElement) {
        	ConfigTestElement configTestElement = (ConfigTestElement) te;
        	configTestElement.setProperty(RTESampler.CONFIG_SERVER, rteConfigPanelConfigPanel.getServer());
        	configTestElement.setProperty(RTESampler.CONFIG_PORT, rteConfigPanelConfigPanel.getPort());
        	configTestElement.setProperty(RTESampler.CONFIG_USER, rteConfigPanelConfigPanel.getUser());
        	configTestElement.setProperty(RTESampler.CONFIG_PASS, rteConfigPanelConfigPanel.getPass());
        	configTestElement.setProperty(RTESampler.CONFIG_PROTOCOL, rteConfigPanelConfigPanel.getProtocol());
        	configTestElement.setProperty(RTESampler.CONFIG_SSL_TYPE, rteConfigPanelConfigPanel.getSSLType());
        	configTestElement.setProperty(RTESampler.CONFIG_TIMEOUT, rteConfigPanelConfigPanel.getTimeout());
        	
        }
    }

    @Override
    public void clearGui() {
        super.clearGui();
        initFields();
    }

    private void init() {
        rteConfigPanelConfigPanel = new RTEConfigPanel();
    }

    private void initFields() {
        rteConfigPanelConfigPanel.initFields();
    }
}
