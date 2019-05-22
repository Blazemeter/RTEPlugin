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
    System.out.println("1");
    configureTestElement(te);
    System.out.println("2");
    if (te instanceof RTERecorder) {
      System.out.println("3");
      recorder = (RTERecorder) te;
      System.out.println("4");
      recorder.setRecordingStateListener(recordingPanel);
      System.out.println("5");
      recorder.setServer(recordingPanel.getServer());
      System.out.println("6");
      recorder.setPort(recordingPanel.getPort());
      System.out.println("7");
      recorder.setProtocol(recordingPanel.getProtocol());
      System.out.println("8");
      recorder.setTerminalType(recordingPanel.getTerminalType());
      System.out.println("9");
      recorder.setSSLType(recordingPanel.getSSLType());
      System.out.println("10");
      recorder.setConnectionTimeout(recordingPanel.getConnectionTimeout());
    }
    System.out.println("11");
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
