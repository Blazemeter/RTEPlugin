package com.blazemeter.jmeter.rte.recorder.emulator;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.swing.timing.Pause.pause;
import static org.mockito.Mockito.verify;

import com.blazemeter.jmeter.rte.core.AttentionKey;
import com.blazemeter.jmeter.rte.core.Input;
import com.blazemeter.jmeter.rte.core.LabelInput;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.api.JUnitSoftAssertions;
import org.assertj.swing.core.KeyPressInfo;
import org.assertj.swing.driver.JComponentDriver;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.fixture.JButtonFixture;
import org.assertj.swing.timing.Condition;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class Xtn5250TerminalEmulatorIT {

  private static final long PAUSE_TIMEOUT = 10000;
  private static final int COLUMNS = 80;
  private static final int ROWS = 24;
  private static final String COPY_BUTTON = "copyButton";
  private static final String PASTE_BUTTON = "pasteButton";
  private static final String INPUT_BY_LABEL_BUTTON = "labelButton";
  private static final String TEST_SCREEN_FILE = "test-screen.txt";
  private static final String TEST_SCREEN_PRESS_KEY_ON_FIELD_FILE = "test-screen-press-key-on-field.txt";
  @Rule
  public final JUnitSoftAssertions softly = new JUnitSoftAssertions();
  private Xtn5250TerminalEmulator xtn5250TerminalEmulator;
  private FrameFixture frame;
  @Mock
  private TerminalEmulatorListener listener;

  private static Screen buildScreen(String text) {
    Dimension screenSize = new Dimension(80, 24);
    Screen screen = new Screen(screenSize);
    int segmentPosition = 0;
    screen.addSegment(segmentPosition,
        completeLine("*****************************************", screenSize.width));
    segmentPosition += screenSize.width;
    screen.addField(segmentPosition, completeLine(text, screenSize.width));
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

  private Set<AttentionKey> buildSupportedAttentionKeys() {
    return new HashSet<AttentionKey>() {{
      add(AttentionKey.ENTER);
      add(AttentionKey.F1);
      add(AttentionKey.CLEAR);
      add(AttentionKey.PA1);
      add(AttentionKey.RESET);
      add(AttentionKey.ROLL_UP);
    }};

  }

  @Before
  public void setup() {
    xtn5250TerminalEmulator = new Xtn5250TerminalEmulator();
    xtn5250TerminalEmulator.setSupportedAttentionKeys(buildSupportedAttentionKeys());

  }

  @After
  public void teardown() {
    xtn5250TerminalEmulator.stop();
    if (frame != null) {
      frame.cleanUp();
    }
  }

  @Test
  public void shouldShowTerminalEmulatorFrameWithProperlySizeWhenStart() {
    xtn5250TerminalEmulator.setScreenSize(132, 43);
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
    xtn5250TerminalEmulator.setScreen(buildScreen(""));
    assertThat(xtn5250TerminalEmulator.getScreen()).isEqualTo(getFileContent(TEST_SCREEN_FILE));
  }

  @Test
  public void shouldGetProperTextWhenPressKeyOnField() throws IOException {
    setScreen("");
    xtn5250TerminalEmulator.setKeyboardLock(false);
    sendKey(KeyEvent.VK_E, 0, 2, 1);
    awaitTextInScreen(getFileContent(TEST_SCREEN_PRESS_KEY_ON_FIELD_FILE));
  }

  @Test
  public void shouldGetProperTextWhenPressKeyOnFieldAndKeyboardIsLocked() throws IOException {
    setScreen("");
    xtn5250TerminalEmulator.setKeyboardLock(true);
    sendKey(KeyEvent.VK_E, 0, 2, 1);
    awaitTextInScreen(getFileContent(TEST_SCREEN_FILE));
  }

  @Test
  public void shouldDeleteTextWhenPressBackspaceKey() throws IOException {
    setScreen("E");
    xtn5250TerminalEmulator.setKeyboardLock(false);
    sendKey(KeyEvent.VK_BACK_SPACE, 0, 2, 2);
    awaitTextInScreen(getFileContent(TEST_SCREEN_FILE));
  }

  @Test
  public void shouldGetProperTextWhenPressKeyOutOfField() throws IOException {
    setScreen("");
    xtn5250TerminalEmulator.setKeyboardLock(false);
    sendKey(KeyEvent.VK_E, 0, 1, 1);
    awaitTextInScreen(getFileContent(TEST_SCREEN_FILE));
  }

  private void setScreen(String text) {
    xtn5250TerminalEmulator.setScreenSize(COLUMNS, ROWS);
    xtn5250TerminalEmulator.setScreen(buildScreen(text));
    frame = new FrameFixture(xtn5250TerminalEmulator);
    frame.show();
  }

  private void sendKey(int key, int modifiers, int row, int column) {
    Component focusedComponent = frame.robot().finder().find(Component::isFocusOwner);
    JComponentDriver driver = new JComponentDriver(frame.robot());
    xtn5250TerminalEmulator.setCursor(row, column);
    driver.pressAndReleaseKey(focusedComponent, KeyPressInfo.keyCode(key).modifiers(modifiers));
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
  public void shouldCallTheListenerWhenPressAnyAttentionKey() {
    setScreen("");
    TestTerminalEmulatorListener terminalEmulatorListener = new TestTerminalEmulatorListener();
    xtn5250TerminalEmulator.addTerminalEmulatorListener(terminalEmulatorListener);
    sendKey(KeyEvent.VK_F1, 0, 2, 2);
    awaitListenerIsCalled(AttentionKey.F1, terminalEmulatorListener);
  }

  @Test
  public void shouldCallTheListenerWhenPressControlAttentionKey() {
    setScreen("");
    TestTerminalEmulatorListener terminalEmulatorListener = new TestTerminalEmulatorListener();
    xtn5250TerminalEmulator.addTerminalEmulatorListener(terminalEmulatorListener);
    sendKey(KeyEvent.VK_CONTROL, KeyEvent.CTRL_MASK, 2, 2);
    awaitListenerIsCalled(AttentionKey.RESET, terminalEmulatorListener);

  }

  private void awaitListenerIsCalled(AttentionKey expected,
      TestTerminalEmulatorListener terminalEmulatorListener) {
    pause(new Condition("Listener is called") {
      @Override
      public boolean test() {
        return terminalEmulatorListener.getAttentionKey() != null && terminalEmulatorListener
            .getAttentionKey().equals(expected);
      }
    }, PAUSE_TIMEOUT);
  }

  @Test
  public void shouldCopyTextWhenClickCopyButton() throws IOException, UnsupportedFlavorException {
    setScreen("");
    xtn5250TerminalEmulator.setSelectedArea(new Rectangle(0, 0, 5, 1));
    clickButton(COPY_BUTTON);
    assertTextIsInClipboard("*****");
  }

  @Test
  public void shouldCopyTextWhenPressShortcut()
      throws IOException, UnsupportedFlavorException {
    setScreen("");
    xtn5250TerminalEmulator.setSelectedArea(new Rectangle(0, 0, 5, 1));
    sendKey(KeyEvent.VK_C, getMenuShortcutKeyMask(), 0, 0);
    assertTextIsInClipboard("*****");
  }

  private void clickButton(String name) {
    JButtonFixture button = frame.button(name);
    button.click();
  }

  private void assertTextIsInClipboard(String text) throws IOException, UnsupportedFlavorException {
    Component focusedComponent = frame.robot().finder().find(Component::isFocusOwner);
    Clipboard clipboard = focusedComponent.getToolkit().getSystemClipboard();
    String result = (String) clipboard.getContents(focusedComponent)
        .getTransferData(DataFlavor.stringFlavor);
    assertThat(result).isEqualTo(text);
  }

  @Test
  public void shouldPasteTextWhenClickPasteButton() throws IOException {
    setScreen("");
    xtn5250TerminalEmulator.setCursor(2, 1);
    addTextToClipboard("e");
    clickButton(PASTE_BUTTON);
    awaitTextInScreen(getFileContent(TEST_SCREEN_PRESS_KEY_ON_FIELD_FILE));
  }

  @Test
  public void shouldPasteTextWhenPressShortcut() throws IOException {
    setScreen("");
    xtn5250TerminalEmulator.setCursor(2, 1);
    addTextToClipboard("e");
    sendKey(KeyEvent.VK_V, getMenuShortcutKeyMask(), 2, 1);
    awaitTextInScreen(getFileContent(TEST_SCREEN_PRESS_KEY_ON_FIELD_FILE));
  }

  private void addTextToClipboard(String text) {
    Component focusedComponent = frame.robot().finder().find(Component::isFocusOwner);
    Clipboard clipboard = focusedComponent.getToolkit().getSystemClipboard();
    StringSelection contents = new StringSelection(text);
    clipboard.setContents(contents, contents);
  }

  private int getMenuShortcutKeyMask() {
    return Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
  }

  private String getFileContent(String file) throws IOException {
    return Resources.toString(getClass().getResource(file), Charsets.UTF_8);
  }

  @Test
  public void shouldNotifySetStatusMessageWhenUnsupportedAttentionKey() {
    setScreen("");
    sendKey(KeyEvent.VK_ESCAPE, 0, 0, 0);
    assertThat(xtn5250TerminalEmulator.getStatusMessage())
        .isEqualTo("ATTN not supported for this emulator protocol");
  }

  @Test
  public void shouldNotifySetStatusMessageWhenInputByLabelWithNonSelectedArea() {
    setScreen("");
    clickButton(INPUT_BY_LABEL_BUTTON);
    assertThat(xtn5250TerminalEmulator.getStatusMessage())
        .isEqualTo("ERROR: Please select a part of the screen");
  }

  @Test
  public void shouldNotifySetStatusMessageWhenInputByLabelWithTwoRows() {
    setScreen("");
    xtn5250TerminalEmulator.setSelectedArea(new Rectangle(0, 0, 5, 4));
    clickButton(INPUT_BY_LABEL_BUTTON);
    assertThat(xtn5250TerminalEmulator.getStatusMessage())
        .isEqualTo("ERROR: Please select only one row");
  }

  @Test
  public void shouldSendInputByLabelToListenerWhenInputByLabel() {
    setScreen("");
    xtn5250TerminalEmulator.addTerminalEmulatorListener(listener);
    xtn5250TerminalEmulator.setSelectedArea(new Rectangle(0, 0, 5, 1));
    clickButton(INPUT_BY_LABEL_BUTTON);
    String test = "t";
    String input = test + StringUtils.repeat(' ',  COLUMNS - test.length());
    sendKey(KeyEvent.VK_T, 0, 2, 1);
    sendKey(KeyEvent.VK_ENTER, 0, 2, 5);
    List<Input> inputs = new ArrayList<>();
    inputs.add(new LabelInput("*****", input));
    verify(listener).onAttentionKey(AttentionKey.ENTER, inputs);
  }

  private static class TestTerminalEmulatorListener implements TerminalEmulatorListener {

    private AttentionKey attentionKey = null;

    @Override
    public void onCloseTerminal() {
    }

    @Override
    public void onAttentionKey(AttentionKey attentionKey, List<Input> inputs) {
      this.attentionKey = attentionKey;
    }

    public AttentionKey getAttentionKey() {
      return attentionKey;
    }

  }
}
