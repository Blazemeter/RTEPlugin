package com.blazemeter.jmeter.rte.recorder;

import org.apache.jmeter.testelement.TestElement;
import org.junit.Test;

public class RTERecorderGuiTest {


    @Test
    public void shouldSetTestElementWithConfiguredPanelWhenModifyTestElement(){

        /**
         RTERecorder recorder = (RTERecorder) element;
         recordingPanel.setServer(recorder.getServer());
         recordingPanel.setPort(String.valueOf(recorder.getPort()));
         recordingPanel.setProtocol(recorder.getProtocol());
         recordingPanel.setTerminalType(recorder.getTerminalType());
         recordingPanel.setSSLType(recorder.getSSLType());
         recordingPanel.setConnectionTimeout(String.valueOf(recorder.getConnectionTimeout()));
         * */

        TestElement element = new RTERecorder();
        element.setProperty("server", "127.0.0.123");

        RTERecorderGui oneRTERecorderGui = new RTERecorderGui();

        oneRTERecorderGui.configure(element);

    }

    @Test
    public void shouldConfigurePanelWithGivenTestElementWhenConfigure(){}

    @Test
    public void shouldNotifyRecorderOnRecordingStartWhenOnRecordingStart(){}

    @Test
    public void shouldNotifyRecorderWhenOnRecordingStop(){}


}
