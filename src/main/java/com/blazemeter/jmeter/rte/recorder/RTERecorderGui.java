package com.blazemeter.jmeter.rte.recorder;

import java.awt.BorderLayout;
import java.util.Collection;
import java.util.Collections;
import org.apache.jmeter.control.gui.LogicControllerGui;
import org.apache.jmeter.gui.JMeterGUIComponent;
import org.apache.jmeter.gui.UnsharedComponent;
import org.apache.jmeter.gui.util.MenuFactory;
import org.apache.jmeter.testelement.TestElement;

public class RTERecorderGui extends LogicControllerGui implements JMeterGUIComponent,
    UnsharedComponent, RecordingStateListener {

  private RTERecorderPanel recordingPanel;
  private RTERecorder recorder;

  public RTERecorderGui() {
    recordingPanel = new RTERecorderPanel(this);
    setLayout(new BorderLayout(0, 5));
    setBorder(makeBorder());
    add(makeTitlePanel(), BorderLayout.NORTH);
    add(recordingPanel, BorderLayout.CENTER);
  }

  @Override
  public String getStaticLabel() {
    return "bzm - RTE Recorder";
  }

  @Override
  public String getLabelResource() {
    throw new IllegalStateException("This shouldn't be called");
  }

  @Override
  public Collection<String> getMenuCategories() {
    return Collections.singletonList(MenuFactory.NON_TEST_ELEMENTS);
  }

  @Override
  public TestElement createTestElement() {
    RTERecorder rteRecorder = new RTERecorder();
    configureTestElement(rteRecorder);
    return rteRecorder;
  }

  @Override
  public void modifyTestElement(TestElement te) {
    configureTestElement(te);
    if (te instanceof RTERecorder) {
      recorder = (RTERecorder) te;
      recorder.setRecordingStateListener(recordingPanel);
      recorder.setServer(recordingPanel.getServer());
      recorder.setPort(recordingPanel.getPort());
      recorder.setProtocol(recordingPanel.getProtocol());
      recorder.setTerminalType(recordingPanel.getTerminalType());
      recorder.setSSLType(recordingPanel.getSSLType());
      recorder.setConnectionTimeout(recordingPanel.getConnectionTimeout());
    }
  }

  @Override
  public void configure(TestElement element) {
    super.configure(element);
    if (element instanceof RTERecorder) {
      RTERecorder recorder = (RTERecorder) element;
      recordingPanel.setServer(recorder.getServer());
      recordingPanel.setPort(String.valueOf(recorder.getPort()));
      recordingPanel.setProtocol(recorder.getProtocol());
      recordingPanel.setTerminalType(recorder.getTerminalType());
      recordingPanel.setSSLType(recorder.getSSLType());
      recordingPanel.setConnectionTimeout(String.valueOf(recorder.getConnectionTimeout()));
    }
  }

  @Override
  public void onRecordingStart() throws Exception {
    modifyTestElement(recorder);
    recorder.onRecordingStart();
  }

  @Override
  public void onRecordingStop() {
    recorder.onRecordingStop();
  }

}
