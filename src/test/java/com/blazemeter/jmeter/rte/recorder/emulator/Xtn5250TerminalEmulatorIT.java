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
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.assertj.swing.core.KeyPressInfo;
import org.assertj.swing.driver.JComponentDriver;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.fixture.JButtonFixture;
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
  private static final String COPY_BUTTON = "copyButton";
  private static final String PASTE_BUTTON = "pasteButton";
  public static final String TEST_SCREEN_FILE = "test-screen.txt";
  public static final String TEST_SCREEN_PRESS_KEY_ON_FIELD_FILE = "test-screen-press-key-on-field.txt";

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
    int expectedHeight = 731 + 31 + 43;
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
    xtn5250TerminalEmulator.setScreen(buildScreen(true));
    assertThat(xtn5250TerminalEmulator.getScreen()).isEqualTo(getFileContent(TEST_SCREEN_FILE));
  }

  @Test
  public void shouldGetProperTextWhenPressKeyOnField() throws IOException {
    sendKey(KeyEvent.VK_E, 2, 1, false,
        TEST_SCREEN_PRESS_KEY_ON_FIELD_FILE, true);
  }

  private void sendKey(int key, int row, int column, boolean keyboardLocked, String expectedScreen,
      boolean fieldIsEmpty)
      throws IOException {
    xtn5250TerminalEmulator.setScreenSize(COLUMNS, ROWS);
    xtn5250TerminalEmulator.setScreen(buildScreen(fieldIsEmpty));
    FrameFixture frame = new FrameFixture(xtn5250TerminalEmulator);
    frame.show();
    try {
      Component focusedComponent = frame.robot().finder().find(Component::isFocusOwner);
      JComponentDriver driver = new JComponentDriver(frame.robot());
      xtn5250TerminalEmulator.setKeyboardLock(keyboardLocked);
      xtn5250TerminalEmulator.setCursor(row, column);
      driver.pressAndReleaseKey(focusedComponent, KeyPressInfo.keyCode(key));
      awaitTextInScreen(getFileContent(expectedScreen));
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
        TEST_SCREEN_FILE, true);
  }

  @Test
  public void shouldDeleteTextWhenPressBackspaceKey() throws IOException {
    sendKey(KeyEvent.VK_BACK_SPACE, 2, 2, false,
        TEST_SCREEN_FILE, false);
  }

  @Test
  public void shouldCallTheListenerWhenPressAnyAttentionKey() {
    pressAttentionKey(KeyEvent.VK_F1, AttentionKey.F1);
  }

  @Test
  public void shouldCallTheListenerWhenPressControlAttentionKey() {
    pressAttentionKey(KeyEvent.VK_CONTROL, AttentionKey.RESET);
  }

  private void pressAttentionKey (int key, AttentionKey expected){
    xtn5250TerminalEmulator.setScreenSize(COLUMNS, ROWS);
    xtn5250TerminalEmulator.setScreen(buildScreen(true));
    TestTerminalEmulatorListener terminalEmulatorListener = new TestTerminalEmulatorListener();
    xtn5250TerminalEmulator.addTerminalEmulatorListener(terminalEmulatorListener);
    FrameFixture frame = new FrameFixture(xtn5250TerminalEmulator);
    frame.show();
    try {
      Component focusedComponent = frame.robot().finder().find(Component::isFocusOwner);
      JComponentDriver driver = new JComponentDriver(frame.robot());
      xtn5250TerminalEmulator.setCursor(2, 2);
      driver.pressAndReleaseKey(focusedComponent, KeyPressInfo.keyCode(key));
      pause(new Condition("Listener is called") {
        @Override
        public boolean test() {
          return terminalEmulatorListener.getAttentionKey() != null && terminalEmulatorListener
              .getAttentionKey().equals(expected);
        }
      }, PAUSE_TIMEOUT);
    } finally {
      frame.cleanUp();
    }
  }

  @Test
  public void shouldGetProperTextWhenPressKeyOutOfField() throws IOException {
    sendKey(KeyEvent.VK_E, 1, 1, false,
        TEST_SCREEN_FILE, true);
  }

  @Test
  public void shouldCopyTestWhenClickCopyButton() throws IOException, UnsupportedFlavorException {
    doCopy(null);
  }

  @Test
  public void shouldCopyTestWhenPressShortcut()
      throws IOException, UnsupportedFlavorException {
    doCopy(getMenuShortcutKeyMask());
  }

  private void doCopy(Integer keyMask) throws IOException, UnsupportedFlavorException {
    xtn5250TerminalEmulator.setScreenSize(COLUMNS, ROWS);
    xtn5250TerminalEmulator.setScreen(buildScreen(true));
    FrameFixture frame = new FrameFixture(xtn5250TerminalEmulator);
    frame.show();
    try {
      xtn5250TerminalEmulator.setSelectedArea(new Rectangle(0, 0, 5, 1));
      Component focusedComponent = frame.robot().finder().find(Component::isFocusOwner);
      if (keyMask == null) {
        JButtonFixture copyButton = frame.button(COPY_BUTTON);
        copyButton.click();
      } else {
        JComponentDriver driver = new JComponentDriver(frame.robot());
        driver.pressAndReleaseKey(focusedComponent, KeyEvent.VK_C, new int[]{keyMask});
      }
      Clipboard clipboard = focusedComponent.getToolkit().getSystemClipboard();
      String result = (String) clipboard.getContents(focusedComponent)
          .getTransferData(DataFlavor.stringFlavor);
      assertThat(result).isEqualTo("*****");
    } finally {
      frame.cleanUp();
    }
  }

  @Test
  public void shouldPasteTestWhenClickPasteButton() throws IOException {
    doPaste(null);
  }

  @Test
  public void shouldPasteTestWhenPressShortcut() throws IOException {
    doPaste(getMenuShortcutKeyMask());
  }

  private int getMenuShortcutKeyMask() {
    return Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
  }

  private void doPaste(Integer keyMask) throws IOException {
    xtn5250TerminalEmulator.setScreenSize(COLUMNS, ROWS);
    xtn5250TerminalEmulator.setScreen(buildScreen(true));
    xtn5250TerminalEmulator.setKeyboardLock(false);
    FrameFixture frame = new FrameFixture(xtn5250TerminalEmulator);
    frame.show();
    xtn5250TerminalEmulator.setCursor(2, 1);
    try {
      Component focusedComponent = frame.robot().finder().find(Component::isFocusOwner);
      Clipboard clipboard = focusedComponent.getToolkit().getSystemClipboard();
      StringSelection contents = new StringSelection("e");
      clipboard.setContents(contents, contents);
      if (keyMask == null) {
        JButtonFixture pasteButton = frame.button(PASTE_BUTTON);
        pasteButton.click();
      } else {
        JComponentDriver driver = new JComponentDriver(frame.robot());
        driver.pressAndReleaseKey(focusedComponent, KeyEvent.VK_V, new int[]{keyMask});
      }
      assertThat(xtn5250TerminalEmulator.getScreen())
          .isEqualTo(getFileContent(TEST_SCREEN_PRESS_KEY_ON_FIELD_FILE));
    } finally {
      frame.cleanUp();
    }
  }

  private String getFileContent(String file) throws IOException {
    return Resources.toString(getClass().getResource(file), Charsets.UTF_8);
  }

  private static Screen buildScreen(boolean fieldIsEmpty) {
    Dimension screenSize = new Dimension(80, 24);
    Screen screen = new Screen(screenSize);
    int segmentPosition = 0;
    screen.addSegment(segmentPosition,
        completeLine("*****************************************", screenSize.width));
    segmentPosition += screenSize.width;
    screen.addField(segmentPosition, completeLine((fieldIsEmpty ? " " : "E"), screenSize.width));
    segmentPosition += screenSize.width;
    for (String lineText : Arrays
        .asList("TEXTO DE PRUEBA 1", "TEXTO DE PRUEBA 2", "TEXTO DE PRUEBA 3",
            "*****************************************")) {
      screen.addSegment(segmentPosition, completeLine(lineText, screenSize.width));
      segmentPosition += screenSize.width;
    }
    return screen;
  }

  private static String completeLine(String baseLine, int width) {
    return baseLine + StringUtils.repeat(' ', width - baseLine.length());
  }

  private static class TestTerminalEmulatorListener implements TerminalEmulatorListener {

    private AttentionKey attentionKey = null;

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
