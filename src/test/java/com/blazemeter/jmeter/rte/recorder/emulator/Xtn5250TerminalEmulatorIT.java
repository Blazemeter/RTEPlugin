package com.blazemeter.jmeter.rte.recorder.emulator;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.swing.timing.Pause.pause;

import com.blazemeter.jmeter.rte.core.AttentionKey;
import com.blazemeter.jmeter.rte.core.Input;
import com.blazemeter.jmeter.rte.core.Screen;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.net.URL;
import java.util.List;
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
  public void shouldGetProperTextWhenPressKeyOnField() throws IOException {
    xtn5250TerminalEmulator.start(COLUMNS, ROWS);
    xtn5250TerminalEmulator.setScreen(printHome());
    FrameFixture frame = new FrameFixture(xtn5250TerminalEmulator.getFrame());
    frame.show();
    try {
      Component focusedComponent = frame.robot().finder().find(Component::isFocusOwner);
      JComponentDriver driver = new JComponentDriver(frame.robot());
      xtn5250TerminalEmulator.setCursor(2, 1);
      driver.pressAndReleaseKey(focusedComponent, KeyPressInfo.keyCode(KeyEvent.VK_E));
      pause(new Condition("Screen with text") {
        @Override
        public boolean test() {
          try {
            return xtn5250TerminalEmulator.getScreen()
                .equals(getFileContent("test-screen-press-key-on-field.txt"));
          } catch (IOException e) {
            e.printStackTrace();
            return false;
          }
        }
      }, PAUSE_TIMEOUT);
    } finally {
      frame.cleanUp();
    }
  }

  @Test
  public void shouldGetProperTextWhenPressKeyOnFieldAndKeyboardIsLocked() throws IOException {
    xtn5250TerminalEmulator.start(COLUMNS, ROWS);
    xtn5250TerminalEmulator.setScreen(printHome());
    FrameFixture frame = new FrameFixture(xtn5250TerminalEmulator.getFrame());
    frame.show();
    try {
      Component focusedComponent = frame.robot().finder().find(Component::isFocusOwner);
      JComponentDriver driver = new JComponentDriver(frame.robot());
      xtn5250TerminalEmulator.setKeyboardLock(true);
      xtn5250TerminalEmulator.setCursor(2, 1);
      driver.pressAndReleaseKey(focusedComponent, KeyPressInfo.keyCode(KeyEvent.VK_E));
      pause(new Condition("Screen with text") {
        @Override
        public boolean test() {
          try {
            return xtn5250TerminalEmulator.getScreen()
                .equals(getFileContent("test-screen.txt"));
          } catch (IOException e) {
            e.printStackTrace();
            return false;
          }
        }
      }, PAUSE_TIMEOUT);
    } finally {
      frame.cleanUp();
    }
  }

  @Test
  public void shouldDeleteTextWhenPressBackspaceKey() {
    xtn5250TerminalEmulator.start(COLUMNS, ROWS);
    xtn5250TerminalEmulator.setScreen(printHomeWithText());
    FrameFixture frame = new FrameFixture(xtn5250TerminalEmulator.getFrame());
    frame.show();
    try {
      Component focusedComponent = frame.robot().finder().find(Component::isFocusOwner);
      JComponentDriver driver = new JComponentDriver(frame.robot());
      xtn5250TerminalEmulator.setCursor(2, 2);
      driver.pressAndReleaseKey(focusedComponent, KeyPressInfo.keyCode(KeyEvent.VK_BACK_SPACE));
      pause(new Condition("Screen with text") {
        @Override
        public boolean test() {
          try {
            return xtn5250TerminalEmulator.getScreen()
                .equals(getFileContent("test-screen.txt"));
          } catch (IOException e) {
            e.printStackTrace();
            return false;
          }
        }
      }, PAUSE_TIMEOUT);
    } finally {
      frame.cleanUp();
    }
  }

  @Test
  public void shouldCallTheListenerWhenPressAttentionKey() {
    xtn5250TerminalEmulator.start(COLUMNS, ROWS);
    xtn5250TerminalEmulator.setScreen(printHome());
    TestTerminalEmulatorListener terminalEmulatorListener = new TestTerminalEmulatorListener();
    xtn5250TerminalEmulator.addTerminalEmulatorListener(terminalEmulatorListener);
    FrameFixture frame = new FrameFixture(xtn5250TerminalEmulator.getFrame());
    frame.show();
    try {
      Component focusedComponent = frame.robot().finder().find(Component::isFocusOwner);
      JComponentDriver driver = new JComponentDriver(frame.robot());
      xtn5250TerminalEmulator.setCursor(2, 2);
      driver.pressAndReleaseKey(focusedComponent, KeyPressInfo.keyCode(KeyEvent.VK_F1));
      pause(new Condition("Listener is called") {
        @Override
        public boolean test() {
          return terminalEmulatorListener.getAttentionKey().equals(AttentionKey.F1);
        }
      }, PAUSE_TIMEOUT);
    } finally {
      frame.cleanUp();
    }
  }

  @Test
  public void shouldGetProperTextWhenPressKeyOutOfField() {
    xtn5250TerminalEmulator.start(COLUMNS, ROWS);
    xtn5250TerminalEmulator.setScreen(printHome());
    FrameFixture frame = new FrameFixture(xtn5250TerminalEmulator.getFrame());
    frame.show();
    try {
      Component focusedComponent = frame.robot().finder().find(Component::isFocusOwner);
      JComponentDriver driver = new JComponentDriver(frame.robot());
      xtn5250TerminalEmulator.setCursor(1, 1);
      driver.pressAndReleaseKey(focusedComponent, KeyPressInfo.keyCode(KeyEvent.VK_E));
      pause(new Condition("Screen with text") {
        @Override
        public boolean test() {
          try {
            return xtn5250TerminalEmulator.getScreen()
                .equals(getFileContent("test-screen.txt"));
          } catch (IOException e) {
            e.printStackTrace();
            return false;
          }
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

  private static Screen printHomeWithText() {
    Screen screen = new Screen();
    screen.addSegment(1, 1, "*****************************************");
    screen.addField(2, 1, "E                                        ");
    screen.addSegment(3, 1, "TEXTO DE PRUEBA 1");
    screen.addSegment(4, 1, "TEXTO DE PRUEBA 2");
    screen.addSegment(5, 1, "TEXTO DE PRUEBA 3");
    screen.addSegment(6, 1, "*****************************************");
    return screen;
  }

  private static class TestTerminalEmulatorListener implements TerminalEmulatorListener {

    AttentionKey attentionKey = null;

    @Override
    public void onCloseTerminal() {
    }

    @Override
    public void onAttentionKey(AttentionKey attentionKey, List<Input> inputs) {
      this.attentionKey = AttentionKey.F1;
    }

    public AttentionKey getAttentionKey() {
      return attentionKey;
    }
  }
}
