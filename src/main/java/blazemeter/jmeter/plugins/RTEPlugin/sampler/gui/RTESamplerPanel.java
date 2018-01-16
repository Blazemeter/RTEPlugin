
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
    
    private JCheckBox waitSync = new JCheckBox("Wait Sync");
    
    public RTESamplerPanel() {
        initComponents();
    }


    private void initComponents() {
  
        requestPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("RTE Message"));
        
        typingStyleComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { RTESampler.TYPING_STYLE_FAST, RTESampler.TYPING_STYLE_HUMAN}));
        
        group.add(fillField);
        group.add(sendKey);
        
        typingStyleLabel.setText("Typing Style: ");
        waitSync.setText("Wait Sync");
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
	                 .addComponent(waitSync)))
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
                        .addComponent(waitSync))
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
                    .addComponent(requestPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
	            .addComponent(requestPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE)
	            .addContainerGap())
        );
    }

    public void initFields() {
    	payloadContent.setText("");
    	field.setText("");
    	coordX.setText("");
    	coordY.setText("");
    	waitSync.setSelected(false);
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

	public boolean getWaitSync() {
		return this.waitSync.isSelected();
	}
	
	public void setWaitSync(boolean waitSync) {
		this.waitSync.setSelected(waitSync);
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
