
package blazemeter.jmeter.plugins.RTEPlugin.sampler.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

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

import org.apache.jmeter.config.Argument;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.config.gui.ArgumentsPanel;
import org.apache.jmeter.gui.util.JSyntaxTextArea;
import org.apache.jmeter.protocol.http.gui.HTTPArgumentsPanel;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.gui.ObjectTableModel;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.jorphan.reflect.Functor;
import org.apache.log.Logger;

import blazemeter.jmeter.plugins.RTEPlugin.sampler.RTESampler;


public class RTESamplerPanel extends javax.swing.JPanel {
	
	private JPanel requestPanel = new JPanel();
	
	private JLabel typingStyleLabel = new JLabel();
	private JComboBox typingStyleComboBox = new JComboBox();
	
	private CoordInputPanel payloadPanel;
    
    private JCheckBox disconnect = new JCheckBox("Disconnect?");
    
    private JPanel waitPanel = new JPanel();
    
    private JCheckBox waitSync = new JCheckBox("Sync?");
    private JCheckBox waitCursor = new JCheckBox("Cursor?");
    private JCheckBox waitSilent = new JCheckBox("Silent?");
    private JCheckBox waitText = new JCheckBox("Text?");
    
    private JLabel waitTimeoutLableSync = new JLabel();
    private JLabel waitTimeoutLableCursor = new JLabel();
    private JLabel waitTimeoutLableSilent = new JLabel();
    private JLabel waitTimeoutLableText = new JLabel();
    private JTextField waitTimeoutSync = new JTextField();
    private JTextField waitTimeoutCursor = new JTextField();
    private JTextField waitTimeoutSilent = new JTextField();
    private JTextField waitTimeoutText = new JTextField();
    
    private JTextField textWait = new JTextField();
    private JLabel coordXWaitLabel = new JLabel();
    private JTextField coordXWait = new JTextField();
    private JLabel coordYWaitLabel = new JLabel();
    private JTextField coordYWait = new JTextField();
    
    
    public RTESamplerPanel() {
    	payloadPanel = new CoordInputPanel("Payload");
        initComponents();
    }


    private void initComponents() {
  
        requestPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("RTE Message"));
        
        typingStyleComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { RTESampler.TYPING_STYLE_FAST, RTESampler.TYPING_STYLE_HUMAN}));
        

        
        typingStyleLabel.setText("Typing Style: ");
        disconnect.setText("Disconnect?");
        
        waitSync.addItemListener(new ItemListener(){
            @Override
            public void itemStateChanged(ItemEvent e) {
                if(e.getStateChange() == ItemEvent.SELECTED){
                	waitTimeoutSync.setEnabled(true);
                }
                else if(e.getStateChange() == ItemEvent.DESELECTED){
                	waitTimeoutSync.setEnabled(false);
                }
                validate();
                repaint();
            }
        });
        
        waitCursor.addItemListener(new ItemListener(){
            @Override
            public void itemStateChanged(ItemEvent e) {
                if(e.getStateChange() == ItemEvent.SELECTED){
                	waitTimeoutCursor.setEnabled(true);
                }
                else if(e.getStateChange() == ItemEvent.DESELECTED){
                	waitTimeoutCursor.setEnabled(false);
                }
                validate();
                repaint();
            }
        });
        
        waitSilent.addItemListener(new ItemListener(){
            @Override
            public void itemStateChanged(ItemEvent e) {
                if(e.getStateChange() == ItemEvent.SELECTED){
                	waitTimeoutSilent.setEnabled(true);
                }
                else if(e.getStateChange() == ItemEvent.DESELECTED){
                	waitTimeoutSilent.setEnabled(false);
                }
                validate();
                repaint();
            }
        });
        
        waitText.addItemListener(new ItemListener(){
            @Override
            public void itemStateChanged(ItemEvent e) {
                if(e.getStateChange() == ItemEvent.SELECTED){
                	waitTimeoutText.setEnabled(true);
                	textWait.setEnabled(true);
                	coordXWait.setEnabled(true);
                	coordYWait.setEnabled(true);
                }
                else if(e.getStateChange() == ItemEvent.DESELECTED){
                	waitTimeoutText.setEnabled(false);
                	textWait.setEnabled(false);
                	coordXWait.setEnabled(false);
                	coordYWait.setEnabled(false);
                }
                validate();
                repaint();
            }
        });
        

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
	                 .addComponent(payloadPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
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
                .addComponent(payloadPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 102, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(requestPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(disconnect))
                .addGap(8, 8, 8)
                .addContainerGap())
        );
		
		
		waitPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Wait for:"));
		waitTimeoutLableSync.setText("Timeout: ");
		waitTimeoutLableCursor.setText("Timeout: ");
		waitTimeoutLableSilent.setText("Timeout: ");
		waitTimeoutLableText.setText("Timeout: ");
		coordXWaitLabel.setText("Coord X: ");
        coordYWaitLabel.setText("Coord Y: ");
		
		
		javax.swing.GroupLayout waitPanelLayout = new javax.swing.GroupLayout(waitPanel);
		waitPanel.setLayout(waitPanelLayout);
        waitPanelLayout.setHorizontalGroup(
        		waitPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(waitPanelLayout.createSequentialGroup()
                .addGroup(waitPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            		.addGroup(waitPanelLayout.createSequentialGroup()
                		.addComponent(waitSync)
                		.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                		.addComponent(waitTimeoutLableSync)
                 		.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 		.addComponent(waitTimeoutSync,javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
            		.addGroup(waitPanelLayout.createSequentialGroup()
        				.addComponent(waitCursor)
                		.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                		.addComponent(waitTimeoutLableCursor)
                 		.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 		.addComponent(waitTimeoutCursor,javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
            		.addGroup(waitPanelLayout.createSequentialGroup()
        				.addComponent(waitSilent)
                		.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                		.addComponent(waitTimeoutLableSilent)
                 		.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 		.addComponent(waitTimeoutSilent,javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
            		.addGroup(waitPanelLayout.createSequentialGroup()
                 		.addComponent(waitText)
                 		.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 		.addComponent(textWait,javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                 		.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
             			.addComponent(coordXWaitLabel)
             			.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 		.addComponent(coordXWait,javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                      	.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 		.addComponent(coordYWaitLabel)
             			.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 		.addComponent(coordYWait,javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                 		.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 		.addComponent(waitTimeoutLableText)
                 		.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 		.addComponent(waitTimeoutText,javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))))
            );
	
        waitPanelLayout.setVerticalGroup(
				waitPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(waitPanelLayout.createSequentialGroup()
                .addGap(8, 8, 8)
                .addGroup(waitPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                		.addComponent(waitSync)
                		.addComponent(waitTimeoutLableSync)
                		.addComponent(waitTimeoutSync))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(waitPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                		.addComponent(waitCursor)
                		.addComponent(waitTimeoutLableCursor)
                		.addComponent(waitTimeoutCursor))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(waitPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                		.addComponent(waitSilent)
                		.addComponent(waitTimeoutLableSilent)
                		.addComponent(waitTimeoutSilent))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(waitPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                		.addComponent(waitText)
                		.addComponent(textWait)
                		.addComponent(coordXWaitLabel)
                		.addComponent(coordXWait)
                		.addComponent(coordYWaitLabel)
                		.addComponent(coordYWait)
                		.addComponent(waitTimeoutLableText)
                		.addComponent(waitTimeoutText))
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
    	payloadPanel.clear();
    	disconnect.setSelected(false);
    	waitCursor.setSelected(false);
    	waitSilent.setSelected(false);
    	waitSync.setSelected(false);
    	waitText.setSelected(false);
    	waitTimeoutSync.setText("");
    	waitTimeoutCursor.setText("");
    	waitTimeoutSilent.setText("");
    	waitTimeoutText.setText("");
    	textWait.setText("");
    	coordXWait.setText("");
    	coordYWait.setText("");
    	waitTimeoutSync.setEnabled(false);
    	waitTimeoutCursor.setEnabled(false);
    	waitTimeoutSilent.setEnabled(false);
    	waitTimeoutText.setEnabled(false);
    	textWait.setEnabled(false);
    	coordXWait.setEnabled(false);
    	coordYWait.setEnabled(false);
    	typingStyleComboBox.setSelectedItem(RTESampler.TYPING_STYLE_FAST);
    }
    
	public CoordInputPanel getPayload() {
		return this.payloadPanel;
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
	
	public String getWaitTimeoutSync() {
		return this.waitTimeoutSync.getText();
	}
	
	public void setWaitTimeoutSync(String waitTimeoutSync) {
		this.waitTimeoutSync.setText(waitTimeoutSync);
	}
	
	public String getWaitTimeoutCursor() {
		return this.waitTimeoutCursor.getText();
	}
	
	public void setWaitTimeoutCursor(String waitTimeoutCursor) {
		this.waitTimeoutCursor.setText(waitTimeoutCursor);
	}
	
	public String getWaitTimeoutSilent() {
		return this.waitTimeoutSilent.getText();
	}
	
	public void setWaitTimeoutSilent(String waitTimeoutSilent) {
		this.waitTimeoutSilent.setText(waitTimeoutSilent);
	}
	
	public String getWaitTimeoutText() {
		return this.waitTimeoutText.getText();
	}
	
	public void setWaitTimeoutText(String waitTimeoutText) {
		this.waitTimeoutText.setText(waitTimeoutText);
	}

	public void setTypingStyle(String typingStyle) {
        typingStyleComboBox.setSelectedItem(typingStyle);
    }
    
    public String getTypingStyle() {
    	return (String) typingStyleComboBox.getSelectedItem();
    }
}
