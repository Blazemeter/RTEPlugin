
package blazemeter.jmeter.plugins.RTEPlugin.sampler.gui;

import java.awt.BorderLayout;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.config.gui.ArgumentsPanel;
import org.apache.jmeter.samplers.gui.AbstractSamplerGui;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

import blazemeter.jmeter.plugins.RTEPlugin.sampler.RTESampler;


public class RTESamplerGui extends AbstractSamplerGui {

    private RTESamplerPanel RTESamplerPanel;

    public RTESamplerGui() {
        super();
        init();
        initFields();

        setLayout(new BorderLayout(0, 5));
        setBorder(makeBorder());

        add(makeTitlePanel(), BorderLayout.NORTH);
        add(RTESamplerPanel, BorderLayout.CENTER);
    }

    @Override
    public String getStaticLabel() {
        return "RTE Sampler";
    }

    @Override
    public String getLabelResource() {
        throw new IllegalStateException("This shouldn't be called"); //$NON-NLS-1$
    }

    @Override
    public void configure(TestElement element) {
        super.configure(element);
        if (element instanceof RTESampler) {
            RTESampler RTESamplerTestElement = (RTESampler) element;   
            RTESamplerPanel.setTypingStyle(RTESamplerTestElement.getTypingStyle());
            RTESamplerPanel.setType(RTESamplerTestElement.getType());
            RTESamplerPanel.setField(RTESamplerTestElement.getField());
            RTESamplerPanel.setCoordX(Integer.toString(RTESamplerTestElement.getCoordX()));
            RTESamplerPanel.setCoordY(Integer.toString(RTESamplerTestElement.getCoordY()));
            RTESamplerPanel.setPayloadContent(RTESamplerTestElement.getPayload());
            RTESamplerPanel.setWaitSync(RTESamplerTestElement.getWaitSync());
        }
    }

    @Override
    public TestElement createTestElement() {
        RTESampler preproc = new RTESampler();
        configureTestElement(preproc);
        return preproc;
    }

    @Override
    public void modifyTestElement(TestElement te) {
        configureTestElement(te);
        if (te instanceof RTESampler) {
            RTESampler RTESamplerTestElement = (RTESampler) te;   
            RTESamplerTestElement.setTypingStyle(RTESamplerPanel.getTypingStyle());
            RTESamplerTestElement.setType(RTESamplerPanel.getType());
            RTESamplerTestElement.setField(RTESamplerPanel.getField());
            RTESamplerTestElement.setCoordX(Integer.parseInt(RTESamplerPanel.getCoordX()));
            RTESamplerTestElement.setCoordY(Integer.parseInt(RTESamplerPanel.getCoordY()));
            RTESamplerTestElement.setPayload(RTESamplerPanel.getPayloadContent());
            RTESamplerTestElement.setWaitSync(RTESamplerPanel.getWaitSync());
        }
    }

    @Override
    public void clearGui() {
        super.clearGui();
        initFields();
    }

    private void init() {
    	RTESamplerPanel = new RTESamplerPanel();
    }

    private void initFields() {
    	RTESamplerPanel.initFields();
    }
}
