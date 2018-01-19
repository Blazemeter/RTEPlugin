
package blazemeter.jmeter.plugins.RTEPlugin.sampler.gui;

import java.awt.Component;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.config.gui.ArgumentsPanel;
import org.apache.jmeter.gui.util.JSyntaxTextArea;
import org.apache.jmeter.protocol.http.gui.HTTPArgumentsPanel;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

import blazemeter.jmeter.plugins.RTEPlugin.sampler.RTESampler;


public class RTESamplerPanel extends javax.swing.JPanel {
	
	private JPanel requestPanel = new JPanel();
	
	private JLabel typingStyleLabel = new JLabel();
	private JComboBox typingStyleComboBox = new JComboBox();
	
    private ButtonGroup group = new ButtonGroup();
    private JRadioButton fillField = new JRadioButton(RTESampler.TYPE_FILL_FIELD,true);
    private JRadioButton sendKey = new JRadioButton(RTESampler.TYPE_SEND_KEY);
    
    private JLabel fieldLabel = new JLabel();
    private JTextField field = new JTextField();
    
    private JLabel coordXLabel = new JLabel();
    private JTextField coordX = new JTextField();
    private JLabel coordYLabel = new JLabel();
    private JTextField coordY = new JTextField();
    
    private JEditorPane payloadContent = new JEditorPane();
	private JScrollPane bodyPanel = new JScrollPane();
    
    private JCheckBox disconnect = new JCheckBox("Disconnect?");
    
    private JPanel waitPanel = new JPanel();
    
    private JCheckBox waitSync = new JCheckBox("Sync?");
    private JCheckBox waitCursor = new JCheckBox("Cursor?");
    private JCheckBox waitSilent = new JCheckBox("Silent?");
    private JCheckBox waitText = new JCheckBox("Text?");
    
    private JLabel waitTimeoutLable = new JLabel();
    private JTextField waitTimeout = new JTextField();
    
    private JTextField textWait = new JTextField();
    private JTextField coordXWait = new JTextField();
    private JTextField coordYWait = new JTextField();
    
    
    public RTESamplerPanel() {
        initComponents();
    }


    private void initComponents() {
  
        requestPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("RTE Message"));
        
        typingStyleComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { RTESampler.TYPING_STYLE_FAST, RTESampler.TYPING_STYLE_HUMAN}));
        
        group.add(fillField);
        group.add(sendKey);
        
        typingStyleLabel.setText("Typing Style: ");
        disconnect.setText("Disconnect?");
        fieldLabel.setText("Field: ");
        coordXLabel.setText("Coord X: ");
        coordYLabel.setText("Coord Y: ");
        
        bodyPanel.setViewportView(payloadContent);

        javax.swing.GroupLayout requestPanelLayout = new javax.swing.GroupLayout(requestPanel);
        requestPanel.setLayout(requestPanelLayout);
		requestPanelLayout.setHorizontalGroup(
    		requestPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(requestPanelLayout.createSequentialGroup()
                .addGroup(requestPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
	                 .addGroup(requestPanelLayout.createSequentialGroup()
	                		.addComponent(typingStyleLabel)
	                		.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
	            			.addComponent(typingStyleComboBox))
	                 .addGroup(requestPanelLayout.createSequentialGroup()
	                 		.addComponent(fillField)
	                 		.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
	             			.addComponent(sendKey))
	                 .addGroup(requestPanelLayout.createSequentialGroup()
	                 		.addComponent(fieldLabel)
	                 		.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
	                 		.addComponent(field,javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
	                 		.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
	             			.addComponent(coordXLabel)
	             			.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
	                 		.addComponent(coordX,javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
	                      	.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
	                 		.addComponent(coordYLabel)
	             			.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
	                 		.addComponent(coordY,javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE))
	                 .addComponent(bodyPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
	                 .addComponent(disconnect)))
        );
		
		requestPanelLayout.setVerticalGroup(
			requestPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(requestPanelLayout.createSequentialGroup()
                .addGap(8, 8, 8)
                .addGroup(requestPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                		.addComponent(typingStyleLabel)
                		.addComponent(typingStyleComboBox))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(requestPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                		.addComponent(fillField)
                		.addComponent(sendKey))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(requestPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                	.addComponent(fieldLabel)
                	.addComponent(field, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(coordXLabel)
                    .addComponent(coordX, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(coordYLabel)
                    .addComponent(coordY, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(bodyPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 102, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(requestPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(disconnect))
                .addGap(8, 8, 8)
                .addContainerGap())
        );
		
		
		waitPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Wait for:"));
		waitTimeoutLable.setText("Timeout: ");
		
		
		javax.swing.GroupLayout waitPanelLayout = new javax.swing.GroupLayout(waitPanel);
		waitPanel.setLayout(waitPanelLayout);
        waitPanelLayout.setHorizontalGroup(
        		waitPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(waitPanelLayout.createSequentialGroup()
                .addGroup(waitPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            		.addComponent(waitSync)
            		.addComponent(waitCursor)
            		.addComponent(waitSilent)
            		.addGroup(waitPanelLayout.createSequentialGroup()
	                 		.addComponent(waitText)
	                 		.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
	                 		.addComponent(textWait,javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
	                 		.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
	             			.addComponent(coordXLabel)
	             			.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
	                 		.addComponent(coordXWait,javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
	                      	.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
	                 		.addComponent(coordYLabel)
	             			.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
	                 		.addComponent(coordYWait,javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE))
            		.addGroup(waitPanelLayout.createSequentialGroup()
	                 		.addComponent(waitTimeoutLable)
	                 		.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
	                 		.addComponent(waitTimeout,javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))))
        );
		
        waitPanelLayout.setVerticalGroup(
				waitPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(waitPanelLayout.createSequentialGroup()
                .addGap(8, 8, 8)
                .addComponent(waitSync)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(waitCursor)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(waitSilent)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(waitPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                		.addComponent(waitText)
                		.addComponent(textWait)
                		.addComponent(coordXLabel)
                		.addComponent(coordXWait)
                		.addComponent(coordYLabel)
                		.addComponent(coordYWait))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(waitPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                		.addComponent(waitTimeoutLable)
                		.addComponent(waitTimeout))
                .addGap(8, 8, 8)
                .addContainerGap())
        );
		
        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(requestPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(waitPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
	            .addComponent(requestPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE)
	            .addContainerGap()
	            .addComponent(waitPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE)
	            .addContainerGap())
        );
    }

    public void initFields() {
    	payloadContent.setText("");
    	field.setText("");
    	coordX.setText("");
    	coordY.setText("");
    	disconnect.setSelected(false);
    	waitCursor.setSelected(false);
    	waitSilent.setSelected(false);
    	waitSync.setSelected(false);
    	waitText.setSelected(false);
    	waitTimeout.setText("");
    	textWait.setText("");
    	coordXWait.setText("");
    	coordYWait.setText("");
    	typingStyleComboBox.setSelectedItem(RTESampler.TYPING_STYLE_FAST);
    	fillField.setSelected(true);
    }
    
	public String getPayloadContent() {
		return this.payloadContent.getText();
	}

	public void setPayloadContent(String payloadContent2) {
		this.payloadContent.setText(payloadContent2);
	}

	public String getField() {
		return this.field.getText();
	}

	public void setField(String field) {
		this.field.setText(field);
	}
	
	public String getCoordX() {
		return this.coordX.getText();
	}

	public void setCoordX(String coordX) {
		this.coordX.setText(coordX);
	}
	
	public String getCoordY() {
		return this.coordY.getText();
	}

	public void setCoordY(String coordY) {
		this.coordY.setText(coordY);
	}
	
	public String getCoordYWait() {
		return this.coordYWait.getText();
	}

	public void setCoordYWait(String coordY) {
		this.coordYWait.setText(coordY);
	}
	
	public String getCoordXWait() {
		return this.coordXWait.getText();
	}

	public void setCoordXWait(String coordX) {
		this.coordXWait.setText(coordX);
	}
	
	public String getTextWait() {
		return this.textWait.getText();
	}

	public void setTextWait(String textWait) {
		this.textWait.setText(textWait);
	}
	
	public boolean getDisconnect() {
		return this.disconnect.isSelected();
	}
	
	public void setDisconnect(boolean disconnect) {
		this.disconnect.setSelected(disconnect);
	}
    
	public boolean getWaitSync() {
		return this.waitSync.isSelected();
	}
	
	public void setWaitSync(boolean waitSync) {
		this.waitSync.setSelected(waitSync);
	}

	public boolean getWaitCursor() {
		return this.waitCursor.isSelected();
	}
	
	public void setWaitCursor(boolean waitCursor) {
		this.waitCursor.setSelected(waitCursor);
	}
	
	public boolean getWaitSilent() {
		return this.waitSilent.isSelected();
	}
	
	public void setWaitSilent(boolean waitSilent) {
		this.waitSilent.setSelected(waitSilent);
	}
	
	public boolean getWaitText() {
		return this.waitText.isSelected();
	}
	
	public void setWaitText(boolean waitText) {
		this.waitText.setSelected(waitText);
	}
	
	public String getWaitTimeout() {
		return this.waitTimeout.getText();
	}
	
	public void setWaitTimeout(String waitTimeout) {
		this.waitTimeout.setText(waitTimeout);
	}

	public void setTypingStyle(String typingStyle) {
        typingStyleComboBox.setSelectedItem(typingStyle);
    }
    
    public String getTypingStyle() {
    	return (String) typingStyleComboBox.getSelectedItem();
    }
	
    public void setType(String action) {
    	if (action.equals(RTESampler.TYPE_FILL_FIELD)){
    		fillField.setSelected(true);
    	}else if(action.equals(RTESampler.TYPE_SEND_KEY)){
    		sendKey.setSelected(true);
    	} 		
    }
    
    public String getType(){
    	if (fillField.isSelected()){
    		return RTESampler.TYPE_FILL_FIELD;
    	}else if(sendKey.isSelected()){
    		return RTESampler.TYPE_SEND_KEY;
	    }else {
			return RTESampler.TYPE_FILL_FIELD;
		}
    	
    }
}
