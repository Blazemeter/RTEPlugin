package com.blazemeter.jmeter.rte.recorder;

import com.blazemeter.jmeter.rte.recorder.gui.RTERecorderGui;
import com.blazemeter.jmeter.rte.recorder.gui.RTERecorderPanel;
import kg.apc.emulators.TestJMeterUtils;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.fixture.JTableFixture;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.assertj.swing.fixture.Containers.showInFrame;


public class RecorderPanelTest {
  private FrameFixture frame;
  private RTERecorderPanel panel;
  private JTableFixture inputTable;
  private RTERecorderGui RecPanelGui;

  @BeforeClass
  public static void setupClass() {
    TestJMeterUtils.createJmeterEnv();
  }

  @Before
  @Test
  public void setup() throws InterruptedException {
    RecPanelGui = new RTERecorderGui();
    frame = showInFrame(RecPanelGui);
    inputTable = frame.table("table");
  wait(60000);
  }

  
}
