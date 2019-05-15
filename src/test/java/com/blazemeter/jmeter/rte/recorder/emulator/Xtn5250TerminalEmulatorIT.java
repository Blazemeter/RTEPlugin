package com.blazemeter.jmeter.rte.recorder.emulator;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.swing.timing.Pause.pause;

import com.blazemeter.jmeter.rte.core.Screen;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.net.URL;
import org.apache.commons.lang3.StringUtils;
import org.assertj.swing.core.KeyPressInfo;
import org.assertj.swing.driver.JComponentDriver;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.timing.Condition;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class Xtn5250TerminalEmulatorIT {

  private static final long PAUSE_TIMEOUT = 10000;
  private static final int COLUMNS = 132;
  private static final int ROWS = 43;
  private Xtn5250TerminalEmulator xtn5250TerminalEmulator;

  @Before
  public void setup() {
    xtn5250TerminalEmulator = new Xtn5250TerminalEmulator();
  }

  @After
  public void teardown() {
  }

  @Test
  public void shouldShowTerminalEmulatorFrameWithProperlySizeWhenStart() {
    xtn5250TerminalEmulator.start(COLUMNS, ROWS);
    Frame frame = xtn5250TerminalEmulator.getFrame();
    int expectedHeight = 750;
    int expectedWidth = 1056;
    Dimension expectedSize = new Dimension(expectedWidth, expectedHeight);
    pause(new Condition("frame size is correct") {
      @Override
      public boolean test() {
        Dimension size = frame.getSize();
        return size.getHeight() >= expectedHeight && size.getHeight() <= expectedHeight + 30
            && size.getWidth() >= expectedWidth && size.getWidth() <= expectedWidth + 20;
      }

    }, PAUSE_TIMEOUT);
  }

  @Test
  public void shouldShowTheScreenExpectedWhenSetScreen() throws IOException {
    xtn5250TerminalEmulator.start(COLUMNS, ROWS);
    xtn5250TerminalEmulator.setScreen(printHome());
    assertThat(xtn5250TerminalEmulator.getScreen()).isEqualTo(getFileContent("test-screen.txt"));
  }

  @Test
  public void shouldGetProperTextWhenSetScreen() throws Exception {
    xtn5250TerminalEmulator.start(80, 24);
    Screen screen = new Screen(new Dimension(80, 24));
    screen.addField(1, 1, StringUtils.repeat(" ", 80 * 24));
    xtn5250TerminalEmulator.setScreen(screen);
    FrameFixture frame = new FrameFixture(xtn5250TerminalEmulator.getFrame());
    frame.show();
    try {
      Component focusedComponent = frame.robot().finder().find(Component::isFocusOwner);
      JComponentDriver driver = new JComponentDriver(frame.robot());
      driver.pressAndReleaseKey(focusedComponent, KeyPressInfo.keyCode(KeyEvent.VK_E));
      pause(new Condition("Screen with text") {
        @Override
        public boolean test() {
          return !xtn5250TerminalEmulator.getScreen().trim().isEmpty();
        }
      }, PAUSE_TIMEOUT);
    } finally {
      frame.cleanUp();
    }

  }

  private String getFileContent(String file) throws IOException {
    return Resources.toString(findResource(file), Charsets.UTF_8);
  }

  private URL findResource(String file) {
    return getClass().getResource(file);
  }

  private static Screen printHome() {
    Screen screen = new Screen();
    screen.addSegment(1, 1, "*****************************************");
    screen.addField(2, 1, "                                         ");
    screen.addSegment(3, 1, "TEXTO DE PRUEBA 1");
    screen.addSegment(4, 1, "TEXTO DE PRUEBA 2");
    screen.addSegment(5, 1, "TEXTO DE PRUEBA 3");
    screen.addSegment(6, 1, "*****************************************");
    return screen;
  }
}
