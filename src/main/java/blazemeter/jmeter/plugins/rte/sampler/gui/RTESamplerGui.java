
package blazemeter.jmeter.plugins.rte.sampler.gui;

import java.awt.BorderLayout;
import org.apache.jmeter.samplers.gui.AbstractSamplerGui;
import org.apache.jmeter.testelement.TestElement;
import blazemeter.jmeter.plugins.rte.sampler.Inputs;
import blazemeter.jmeter.plugins.rte.sampler.RTESampler;

public class RTESamplerGui extends AbstractSamplerGui {

	private static final long serialVersionUID = 4024916662489960067L;
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
			RTESamplerPanel.setDisconnect(RTESamplerTestElement.getDisconnect());
			RTESamplerPanel.setWaitCursor(RTESamplerTestElement.getWaitCursor());
			RTESamplerPanel.setWaitSilent(RTESamplerTestElement.getWaitSilent());
			RTESamplerPanel.setWaitSync(RTESamplerTestElement.getWaitSync());
			RTESamplerPanel.setWaitText(RTESamplerTestElement.getWaitText());
			RTESamplerPanel.setWaitTimeoutSync(RTESamplerTestElement.getWaitTimeoutSync());
			RTESamplerPanel.setWaitTimeoutCursor(RTESamplerTestElement.getWaitTimeoutCursor());
			RTESamplerPanel.setWaitTimeoutSilent(RTESamplerTestElement.getWaitTimeoutSilent());
			RTESamplerPanel.setWaitTimeoutText(RTESamplerTestElement.getWaitTimeoutText());
			RTESamplerPanel.setCoordXWait(RTESamplerTestElement.getCoordXToWait());
			RTESamplerPanel.setCoordYWait(RTESamplerTestElement.getCoordYToWait());
			RTESamplerPanel.setTextWait(RTESamplerTestElement.getTextToWait());
			Inputs payload = RTESamplerTestElement.getPayload();
			if (payload != null) {
				RTESamplerPanel.getPayload().configure(payload);
			}
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
			RTESamplerTestElement.setDisconnect(RTESamplerPanel.getDisconnect());
			RTESamplerTestElement.setWaitCursor(RTESamplerPanel.getWaitCursor());
			RTESamplerTestElement.setWaitSilent(RTESamplerPanel.getWaitSilent());
			RTESamplerTestElement.setWaitSync(RTESamplerPanel.getWaitSync());
			RTESamplerTestElement.setWaitText(RTESamplerPanel.getWaitText());
			RTESamplerTestElement.setWaitTimeoutSync(RTESamplerPanel.getWaitTimeoutSync());
			RTESamplerTestElement.setWaitTimeoutCursor(RTESamplerPanel.getWaitTimeoutCursor());
			RTESamplerTestElement.setWaitTimeoutSilent(RTESamplerPanel.getWaitTimeoutSilent());
			RTESamplerTestElement.setWaitTimeoutText(RTESamplerPanel.getWaitTimeoutText());
			RTESamplerTestElement.setCoordXToWait(RTESamplerPanel.getCoordXWait());
			RTESamplerTestElement.setCoordYToWait(RTESamplerPanel.getCoordYWait());
			RTESamplerTestElement.setTextToWait(RTESamplerPanel.getTextWait());

			CoordInputPanel payload = RTESamplerPanel.getPayload();
			if (payload != null) {
				RTESamplerTestElement.setPayload((Inputs) payload.createTestElement());
			}
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
