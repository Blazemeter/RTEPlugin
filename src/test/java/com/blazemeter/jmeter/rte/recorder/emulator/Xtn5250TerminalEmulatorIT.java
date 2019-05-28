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
import java.awt.event.KeyEvent;
import java.io.IOException;
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

  private static final long PAUSE_TIMEOUT = 15000;
  private static final int COLUMNS = 132;
  private static final int ROWS = 43;
  private Xtn5250TerminalEmulator xtn5250TerminalEmulator;

  @Before
  public void setup() {
    xtn5250TerminalEmulator = new Xtn5250TerminalEmulator();
  }

  @After
  public void teardown() {
    xtn5250TerminalEmulator.stop();
  }

  @Test
  public void shouldShowTerminalEmulatorFrameWithProperlySizeWhenStart() {
    xtn5250TerminalEmulator.setScreenSize(COLUMNS, ROWS);
    xtn5250TerminalEmulator.start();
    //xi5250Crt expected Height + StatusPanel expected height
    int expectedHeight = 731 + 31;
    int expectedWidth = 1056;
    //The title bar size is depending on the OS,
    // so a threshold is added to be able to test on different OS
    int threshold = 30;
    try {
      awaitFrameSizeIs(expectedWidth, expectedHeight, threshold);
    } finally {
      xtn5250TerminalEmulator.stop();
    }
  }

  private void awaitFrameSizeIs(int expectedWidth, int expectedHeight, int threshold) {
    pause(new Condition("frame size is correct") {
      @Override
      public boolean test() {
        Dimension size = xtn5250TerminalEmulator.getSize();
        return size.getHeight() >= expectedHeight
            && size.getHeight() <= expectedHeight + threshold
            && size.getWidth() >= expectedWidth && size.getWidth() <= expectedWidth + threshold;
      }
    }, PAUSE_TIMEOUT);
  }

  @Test
  public void shouldShowTheScreenExpectedWhenSetScreen() throws IOException {
    xtn5250TerminalEmulator.setScreenSize(COLUMNS, ROWS);
    xtn5250TerminalEmulator.setScreen(printHome(true));
    assertThat(xtn5250TerminalEmulator.getScreen()).isEqualTo(getFileContent("test-screen.txt"));
  }

  @Test
  public void shouldGetProperTextWhenPressKeyOnField() throws IOException {
    sendKey(KeyEvent.VK_E, 2, 1, false,
        "test-screen-press-key-on-field.txt", true);
  }

  private void sendKey(int key, int row, int column, boolean keyboardLocked, String expectedScreen,
      boolean fieldIsEmpty)
      throws IOException {
    xtn5250TerminalEmulator.setScreenSize(COLUMNS, ROWS);
    xtn5250TerminalEmulator.setScreen(printHome(fieldIsEmpty));
    FrameFixture frame = new FrameFixture(xtn5250TerminalEmulator);
    frame.show();
    try {
      Component focusedComponent = frame.robot().finder().find(Component::isFocusOwner);
      JComponentDriver driver = new JComponentDriver(frame.robot());
      xtn5250TerminalEmulator.setKeyboardLock(keyboardLocked);
      xtn5250TerminalEmulator.setCursor(row, column);
      driver.pressAndReleaseKey(focusedComponent, KeyPressInfo.keyCode(key));
      try {
        awaitTextInScreen(getFileContent(expectedScreen));
      } catch (IOException e) {
        throw e;
      }
    } finally {
      frame.cleanUp();
    }
  }

  private void awaitTextInScreen(String text) {
    pause(new Condition("Screen with text") {
      @Override
      public boolean test() {
        return xtn5250TerminalEmulator.getScreen().equals(text);
      }
    }, PAUSE_TIMEOUT);
  }

  @Test
  public void shouldGetProperTextWhenPressKeyOnFieldAndKeyboardIsLocked() throws IOException {
    sendKey(KeyEvent.VK_E, 2, 1, true,
        "test-screen.txt", true);
  }

  @Test
  public void shouldDeleteTextWhenPressBackspaceKey() throws IOException {
    sendKey(KeyEvent.VK_BACK_SPACE, 2, 2, false,
        "test-screen.txt", false);
  }

  @Test
  public void shouldCallTheListenerWhenPressAttentionKey() {
    xtn5250TerminalEmulator.setScreenSize(COLUMNS, ROWS);
    xtn5250TerminalEmulator.setScreen(printHome(true));
    TestTerminalEmulatorListener terminalEmulatorListener = new TestTerminalEmulatorListener();
    xtn5250TerminalEmulator.addTerminalEmulatorListener(terminalEmulatorListener);
    FrameFixture frame = new FrameFixture(xtn5250TerminalEmulator);
    frame.show();
    try {
      Component focusedComponent = frame.robot().finder().find(Component::isFocusOwner);
      JComponentDriver driver = new JComponentDriver(frame.robot());
      xtn5250TerminalEmulator.setCursor(2, 2);
      driver.pressAndReleaseKey(focusedComponent, KeyPressInfo.keyCode(KeyEvent.VK_F1));
      pause(new Condition("Listener is called") {
        @Override
        public boolean test() {
          return terminalEmulatorListener.getAttentionKey() != null ? terminalEmulatorListener
              .getAttentionKey().equals(AttentionKey.F1) : false;
        }
      }, PAUSE_TIMEOUT);
    } finally {
      frame.cleanUp();
    }
  }

  @Test
  public void shouldGetProperTextWhenPressKeyOutOfField() throws IOException {
    sendKey(KeyEvent.VK_E, 1, 1, false,
        "test-screen.txt", true);
  }


  private String getFileContent(String file) throws IOException {
    return Resources.toString(getClass().getResource(file), Charsets.UTF_8);
  }

  private static Screen printHome(boolean fieldIsEmpty) {
    Screen screen = new Screen();
    screen.addSegment(1, 1, "*****************************************");
    screen.addField(2, 1,
        "" + (fieldIsEmpty ? " " : "E") + "                                        ");
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
