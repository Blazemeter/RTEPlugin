package com.blazemeter.jmeter.rte.sampler.gui;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.blazemeter.jmeter.rte.core.Action;
import com.blazemeter.jmeter.rte.sampler.RTESampler;
import java.io.File;
import java.io.IOException;
import kg.apc.emulators.TestJMeterUtils;
import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class RTESamplerGuiTest {

  private RTESamplerGui samplerGui;
  private RTESampler testElement;

  @Mock
  private RTESamplerPanel panel;

  @Before
  public void setup() {
    samplerGui = new RTESamplerGui(panel);
    testElement = new RTESampler();
  }

  @BeforeClass
  public static void setupClass() {
    TestJMeterUtils.createJmeterEnv();
  }

  @Test
  public void shouldSetTheTestElementFromThePanel() {
    when(panel.getAction()).thenReturn(Action.ENTER);
    when(panel.getDisconnect()).thenReturn(true);
    when(panel.getJustConnect()).thenReturn(true);
    when(panel.getWaitSync()).thenReturn(true);
    when(panel.getWaitSyncTimeout()).thenReturn("1");
    when(panel.getWaitCursor()).thenReturn(true);
    when(panel.getWaitCursorColumn()).thenReturn("2");
    when(panel.getWaitCursorRow()).thenReturn("3");
    when(panel.getWaitCursorTimeout()).thenReturn("4");
    when(panel.getWaitSilent()).thenReturn(true);
    when(panel.getWaitSilentTimeout()).thenReturn("5");
    when(panel.getWaitSilentTime()).thenReturn("6");
    when(panel.getWaitText()).thenReturn(true);
    when(panel.getWaitTextAreaBottom()).thenReturn("7");
    when(panel.getWaitTextAreaLeft()).thenReturn("8");
    when(panel.getWaitTextAreaRight()).thenReturn("9");
    when(panel.getWaitTextAreaTop()).thenReturn("10");
    when(panel.getWaitTextRegex()).thenReturn("regExp");
    when(panel.getWaitTextTimeout()).thenReturn("11");
    when(panel.getPayload()).thenReturn(null);

    samplerGui.modifyTestElement(testElement);
    assertThat(testElement.getAction()).isEqualTo(Action.ENTER);
    assertThat(testElement.getDisconnect()).isEqualTo(true);
    assertThat(testElement.getJustConnect()).isEqualTo(true);
    assertThat(testElement.getWaitSync()).isEqualTo(true);
    assertThat(testElement.getWaitCursor()).isEqualTo(true);
    assertThat(testElement.getWaitSilent()).isEqualTo(true);
    assertThat(testElement.getWaitText()).isEqualTo(true);
    assertThat(testElement.getWaitSyncTimeout()).isEqualTo("1");
    assertThat(testElement.getWaitCursorColumn()).isEqualTo("2");
    assertThat(testElement.getWaitCursorRow()).isEqualTo("3");
    assertThat(testElement.getWaitCursorTimeout()).isEqualTo("4");
    assertThat(testElement.getWaitSilentTimeout()).isEqualTo("5");
    assertThat(testElement.getWaitSilentTime()).isEqualTo("6");
    assertThat(testElement.getWaitTextAreaBottom()).isEqualTo("7");
    assertThat(testElement.getWaitTextAreaLeft()).isEqualTo("8");
    assertThat(testElement.getWaitTextAreaRight()).isEqualTo("9");
    assertThat(testElement.getWaitTextAreaTop()).isEqualTo("10");
    assertThat(testElement.getWaitTextTimeout()).isEqualTo("11");
    assertThat(testElement.getWaitTextRegex()).isEqualTo("regExp");


  }

}
