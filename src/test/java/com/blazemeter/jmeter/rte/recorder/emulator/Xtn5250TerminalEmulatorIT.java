package com.blazemeter.jmeter.rte.recorder.emulator;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.swing.timing.Pause.pause;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
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
import org.assertj.swing.finder.JOptionPaneFinder;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.fixture.JButtonFixture;
import org.assertj.swing.fixture.JOptionPaneFixture;
import org.assertj.swing.fixture.JTextComponentFixture;
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

  public static final String GOODBYE_TEXT = "Thanks for use testing login";
  public static final String SAMPLE_NAME_FIELD = "sampleNameField";
  public static final String ASSERTION_TEST_LITERAL = "Assertion Test";
  public static final String CONNECTING_LITERAL = "CONNECTING";
  private static final long PAUSE_TIMEOUT = 10000;
  private static final int COLUMNS = 80;
  private static final int ROWS = 24;
  private static final String COPY_BUTTON = "copyButton";
  private static final String PASTE_BUTTON = "pasteButton";
  private static final String INPUT_BY_LABEL_BUTTON = "labelButton";
  private static final String TEST_SCREEN_FILE = "test-screen.txt";
  private static final String TEST_SCREEN_PRESS_KEY_ON_FIELD_FILE = "test-screen-press-key-on-field.txt";
  private static final String WAIT_FOR_TEXT_BUTTON = "waitForTextButton";
  private static final String ASSERTION_BUTTON = "assertionButton";
  public static final String DEFAULT_SAMPLE_NAME_INPUT_VALUE = "DEFAULT_INPUT_VALUE";
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

  private Screen buildLoginScreenWithUserNameAndPasswordFields() {
    Dimension screenSize = new Dimension(COLUMNS, ROWS);
    Screen screen = new Screen(screenSize);
    String name = "Insert Name: ";
    String password = "Insert Password: ";
    int fieldLength = 1;
    int linearPosition = 0;
    linearPosition = addScreenSegment(screen, linearPosition, name);
    linearPosition = addScreenField(screen, linearPosition, fieldLength);

    linearPosition = addScreenSegment(screen, linearPosition,
        completeLine("", COLUMNS - linearPosition));

    linearPosition = addScreenSegment(screen, linearPosition, password);
    linearPosition = addScreenField(screen, linearPosition, fieldLength);

    linearPosition = addScreenSegment(screen, linearPosition,
        completeLine("", COLUMNS - password.length() - fieldLength));

    addScreenSegment(screen, linearPosition, completeLine(GOODBYE_TEXT, COLUMNS));

    return screen;
  }

  private int addScreenSegment(Screen screen, int linearPosition, String name) {
    screen.addSegment(linearPosition, name);
    return linearPosition + name.length();
  }

  private int addScreenField(Screen screen, int linearPosition, int fieldLenght) {
    screen.addField(linearPosition, StringUtils.repeat(' ', fieldLenght));
    return linearPosition + fieldLenght;
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
    xtn5250TerminalEmulator.setScreen(buildScreen(""), "");
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
    setScreen(text, "");
  }

  private void setScreen(String text, String sampleName) {
    xtn5250TerminalEmulator.setScreenSize(COLUMNS, ROWS);
    xtn5250TerminalEmulator.setScreen(buildScreen(text), sampleName);
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
    xtn5250TerminalEmulator.addTerminalEmulatorListener(listener);
    sendKey(KeyEvent.VK_F1, 0, 2, 2);
    verify(listener, timeout(PAUSE_TIMEOUT)).onAttentionKey(AttentionKey.F1, new ArrayList<>(), "");
  }

  @Test
  public void shouldCallTheListenerWhenPressControlAttentionKey() {
    setScreen("");
    xtn5250TerminalEmulator.addTerminalEmulatorListener(listener);
    sendKey(KeyEvent.VK_CONTROL, KeyEvent.CTRL_MASK, 2, 2);
    verify(listener, timeout(PAUSE_TIMEOUT))
        .onAttentionKey(AttentionKey.RESET, new ArrayList<>(), "");
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
  public void shouldShowUserMessageWhenUnsupportedAttentionKey() {
    setScreen("");
    sendKey(KeyEvent.VK_ESCAPE, 0, 0, 0);
    findOptionPane().requireMessage("ATTN not supported for current protocol");

  }

  private JOptionPaneFixture findOptionPane() {
    return JOptionPaneFinder.findOptionPane().using(frame.robot());
  }

  @Test
  public void shouldShowUserMessageWhenInputByLabelWithNonSelectedArea() {
    setScreen("");
    clickButton(INPUT_BY_LABEL_BUTTON);
    findOptionPane().requireMessage("Please select a part of the screen");

  }

  @Test
  public void shouldSendInputByLabelThroughListenerWhenInputByLabel() {
    setScreen("");
    xtn5250TerminalEmulator.addTerminalEmulatorListener(listener);
    xtn5250TerminalEmulator.setSelectedArea(new Rectangle(0, 0, 5, 1));
    clickButton(INPUT_BY_LABEL_BUTTON);
    String test = "t";
    String input = test + StringUtils.repeat(' ', COLUMNS - test.length());
    sendKey(KeyEvent.VK_T, 0, 2, 1);
    sendKey(KeyEvent.VK_ENTER, 0, 2, 5);
    List<Input> inputs = new ArrayList<>();
    inputs.add(new LabelInput("*****", input));
    verify(listener).onAttentionKey(AttentionKey.ENTER, inputs, "");
  }

  @Test
  public void shouldNotifyListenerOfMultipleInputByLabel() {
    setScreenWithUserNameAndPasswordFields();
    xtn5250TerminalEmulator.addTerminalEmulatorListener(listener);
    xtn5250TerminalEmulator.setSelectedArea(new Rectangle(0, 0, 11, 1));
    clickButton(INPUT_BY_LABEL_BUTTON);
    sendKey(KeyEvent.VK_T, 0, 1, 14);
    xtn5250TerminalEmulator.setSelectedArea(new Rectangle(0, 1, 15, 1));
    clickButton(INPUT_BY_LABEL_BUTTON);
    sendKey(KeyEvent.VK_Y, 0, 2, 18);
    sendKey(KeyEvent.VK_ENTER, 0, 2, 19);

    verify(listener)
        .onAttentionKey(AttentionKey.ENTER, buildExpectedInputListForMultipleInputsByLabel(), "");
  }

  private List<Input> buildExpectedInputListForMultipleInputsByLabel() {
    LabelInput userName = new LabelInput("Insert Name", "t");
    LabelInput password = new LabelInput("Insert Password", "y");
    return Arrays.asList(userName, password);
  }

  private void setScreenWithUserNameAndPasswordFields() {
    xtn5250TerminalEmulator.setScreen(buildLoginScreenWithUserNameAndPasswordFields(), "");
    xtn5250TerminalEmulator.setScreenSize(COLUMNS, ROWS);
    frame = new FrameFixture(xtn5250TerminalEmulator);
    frame.show();
  }

  @Test
  public void shouldShowUserMessageWhenInputByLabelAndNoFieldAfterCurrentLabel() {
    setScreenWithUserNameAndPasswordFields();
    xtn5250TerminalEmulator.setSelectedArea(new Rectangle(0, 2, 28, 1));
    clickButton(INPUT_BY_LABEL_BUTTON);
    findOptionPane().requireMessage("No input fields found near to \"" + GOODBYE_TEXT + "\".");
  }

  @Test
  public void shouldShowUserMessageWhenInputByLabelAndBlankSelectedArea() {
    setScreenWithUserNameAndPasswordFields();
    xtn5250TerminalEmulator.setSelectedArea(new Rectangle(19, 3, 15, 1));
    clickButton(INPUT_BY_LABEL_BUTTON);
    findOptionPane().requireMessage("Please select a non empty or blank text \n"
        + "to be used as input by label");
  }

  @Test
  public void shouldShowUserMessageWhenInputByLabelAndMultipleRowsWereSelected() {
    setScreenWithUserNameAndPasswordFields();
    xtn5250TerminalEmulator.setSelectedArea(new Rectangle(19, 3, 15, 2));
    clickButton(INPUT_BY_LABEL_BUTTON);
    findOptionPane().requireMessage("Please try again selecting one row");
  }

  @Test
  public void shouldCallTheListenerWhenPressWaitForTextButton() {
    setScreen("");
    xtn5250TerminalEmulator.addTerminalEmulatorListener(listener);
    xtn5250TerminalEmulator.setSelectedArea(new Rectangle(1, 0, 5, 4));
    clickButton(WAIT_FOR_TEXT_BUTTON);
    verify(listener, timeout(PAUSE_TIMEOUT))
        .onWaitForText("*****\n " + "    \n" + "EXTO \n" + "EXTO ");
  }

  @Test
  public void shouldCallTheListenerWhenAssertionScreen() {
    setScreen("TEST");
    xtn5250TerminalEmulator.addTerminalEmulatorListener(listener);
    xtn5250TerminalEmulator.setSelectedArea(new Rectangle(0, 1, 4, 1));
    clickButton(ASSERTION_BUTTON);
    JOptionPaneFixture popup = findOptionPane();
    popup.textBox().setText(ASSERTION_TEST_LITERAL);
    popup.okButton().click();
    verify(listener, timeout(PAUSE_TIMEOUT)).onAssertionScreen(ASSERTION_TEST_LITERAL, "TEST");
  }

  @Test
  public void shouldShowUserMessageWhenAssertionButtonWhitNonSelectedArea() {
    setScreen("");
    clickButton(ASSERTION_BUTTON);
    findOptionPane().requireMessage("Please select a part of the screen");
  }

  @Test
  public void shouldNotNotifyListenerWhenAssertionScreenAndCancelButtonPressed() {
    setScreen("");
    xtn5250TerminalEmulator.addTerminalEmulatorListener(listener);
    xtn5250TerminalEmulator.setSelectedArea(new Rectangle(0, 1, 4, 1));
    clickButton(ASSERTION_BUTTON);
    findOptionPane().textBox().setText(ASSERTION_TEST_LITERAL);
    findOptionPane().cancelButton().click();
    verify(listener, never()).onAssertionScreen(ASSERTION_TEST_LITERAL, "TEST");
  }

  @Test
  public void shouldNotifyListenerWhenInputInhibitedOnSampleName() {
    xtn5250TerminalEmulator.setKeyboardLock(true);
    setScreen("");
    xtn5250TerminalEmulator.addTerminalEmulatorListener(listener);
    xtn5250TerminalEmulator.setKeyboardLock(false);
    setSampleName(CONNECTING_LITERAL);
    sendKey(KeyEvent.VK_ENTER, 0, 2, 1);

    verify(listener).onAttentionKey(AttentionKey.ENTER, new ArrayList<>(), CONNECTING_LITERAL);
  }

  private void setSampleName(String name) {
    JTextComponentFixture field = frame.textBox(SAMPLE_NAME_FIELD);
    field.target().setText(name);
  }

  @Test
  public void shouldSetDefaultValueInFieldWhenOnAttentionKey() {
    xtn5250TerminalEmulator.setKeyboardLock(true);
    setScreen("", DEFAULT_SAMPLE_NAME_INPUT_VALUE);
    xtn5250TerminalEmulator.addTerminalEmulatorListener(listener);
    xtn5250TerminalEmulator.setKeyboardLock(false);
    sendKey(KeyEvent.VK_ENTER, 0, 2, 1);
    assertThat(frame.textBox(SAMPLE_NAME_FIELD).text()).isEqualTo(DEFAULT_SAMPLE_NAME_INPUT_VALUE);
  }
}
