package com.blazemeter.jmeter.rte.recorder.emulator;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.swing.timing.Pause.pause;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.blazemeter.jmeter.rte.core.AttentionKey;
import com.blazemeter.jmeter.rte.core.CharacterBasedProtocolClient;
import com.blazemeter.jmeter.rte.core.CoordInput;
import com.blazemeter.jmeter.rte.core.Input;
import com.blazemeter.jmeter.rte.core.LabelInput;
import com.blazemeter.jmeter.rte.core.NavigationInput;
import com.blazemeter.jmeter.rte.core.Position;
import com.blazemeter.jmeter.rte.core.RteProtocolClient;
import com.blazemeter.jmeter.rte.core.Screen;
import com.blazemeter.jmeter.rte.sampler.NavigationType;
import com.blazemeter.jmeter.rte.sampler.gui.ThemedIcon;
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
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.api.JUnitSoftAssertions;
import org.assertj.swing.core.KeyPressInfo;
import org.assertj.swing.driver.JComponentDriver;
import org.assertj.swing.exception.ComponentLookupException;
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
import org.mockito.internal.stubbing.answers.AnswersWithDelay;
import org.mockito.junit.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class Xtn5250TerminalEmulatorIT {

  public static final String GOODBYE_TEXT = "Thanks for use testing login";
  public static final String SAMPLE_NAME_FIELD = "sampleNameField";
  public static final String ASSERTION_TEST_LITERAL = "Assertion Test";
  public static final String CONNECTING_LITERAL = "CONNECTING";
  public static final String DEFAULT_SAMPLE_NAME_INPUT_VALUE = "DEFAULT_INPUT_VALUE";
  public static final String BLOCK_CURSOR_LABEL = "blockedCursor";
  public static final String BLOCKED_CURSOR_RESOURCE_NAME = "blocked-cursor.png";
  public static final String KEYBOARD_UNLOCKED_RESOURCE_NAME = "keyboard-unlocked.png";
  public static final String CHUNK_OF_SCREEN = "*****";
  public static final String DEVELOPER_ID = "TT";
  public static final String WORKDATE_LITERAL_VALUE = "00";
  private static final long PAUSE_TIMEOUT = 10000;
  private static final int COLUMNS = 80;
  private static final int ROWS = 24;
  private static final String COPY_BUTTON = "copyButton";
  private static final String PASTE_BUTTON = "pasteButton";
  private static final String INPUT_BY_LABEL_BUTTON = "labelButton";
  private static final String TEST_SCREEN_FILE = "test-screen.txt";
  private static final String TEST_SCREEN_PRESS_KEY_ON_FIELD_FILE = "test-screen-press-key-on"
      + "-field.txt";
  private static final String WAIT_FOR_TEXT_BUTTON = "waitForTextButton";
  private static final String ASSERTION_BUTTON = "assertionButton";
  private static final String KEYBOARD_LABEL = "keyboardLabel";
  private static final Position FIRST_VT_POS = new Position(11, 41);
  private static final Position SECOND_VT_POS = new Position(11, 42);

  @Rule
  public final JUnitSoftAssertions softly = new JUnitSoftAssertions();
  private Xtn5250TerminalEmulator xtn5250TerminalEmulator;
  private FrameFixture frame;
  private CharacterBasedEmulator characterBasedEmulator;
  private Screen screen;
  @Mock
  private RteProtocolClient protocolClient;

  @Mock
  private CharacterBasedProtocolClient characterBasedProtocolClient;

  @Mock
  private TerminalEmulatorListener listener;
  private String samplerName;

  private static Screen buildScreen(String text, boolean withFields) {
    Dimension screenSize = new Dimension(80, 24);
    Screen screen = new Screen(screenSize);
    int segmentPosition = 0;
    screen.addSegment(segmentPosition,
        completeLine("*****************************************", screenSize.width));
    segmentPosition += screenSize.width;
    if (withFields) {
      screen.addField(segmentPosition, completeLine(text, screenSize.width));
    } else {
      screen.addSegment(segmentPosition,
          completeLine(text, screenSize.width));
    }
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
    xtn5250TerminalEmulator = new Xtn5250TerminalEmulator(new FieldBasedEmulator());
    xtn5250TerminalEmulator.setSupportedAttentionKeys(buildSupportedAttentionKeys());
    xtn5250TerminalEmulator.setProtocolClient(protocolClient);
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
    xtn5250TerminalEmulator.setScreen(buildScreen("", false));
    assertThat(xtn5250TerminalEmulator.getScreen()).isEqualTo(getFileContent(TEST_SCREEN_FILE));
  }

  @Test
  public void shouldGetProperTextWhenPressKeyOnField() throws IOException {
    setScreen("");
    xtn5250TerminalEmulator.setKeyboardLock(false);
    sendKeyWithCursorUpdate(KeyEvent.VK_E, 0, 2, 1);
    awaitTextInScreen(getFileContent(TEST_SCREEN_PRESS_KEY_ON_FIELD_FILE));
  }

  @Test
  public void shouldGetProperTextWhenPressKeyOnFieldAndKeyboardIsLocked() throws IOException {
    setScreen("");
    xtn5250TerminalEmulator.setKeyboardLock(true);
    sendKeyWithCursorUpdate(KeyEvent.VK_E, 0, 2, 1);
    awaitTextInScreen(getFileContent(TEST_SCREEN_FILE));
  }

  @Test
  public void shouldDeleteTextWhenPressBackspaceKey() throws IOException {
    setScreen("E");
    xtn5250TerminalEmulator.setKeyboardLock(false);
    sendKeyWithCursorUpdate(KeyEvent.VK_BACK_SPACE, 0, 2, 2);
    awaitTextInScreen(getFileContent(TEST_SCREEN_FILE));
  }

  @Test
  public void shouldGetProperTextWhenPressKeyOutOfField() throws IOException {
    setScreen("");
    xtn5250TerminalEmulator.setKeyboardLock(false);
    sendKeyWithCursorUpdate(KeyEvent.VK_E, 0, 1, 1);
    awaitTextInScreen(getFileContent(TEST_SCREEN_FILE));
  }

  private void setScreen(String text) {
    setScreen(text, "");
  }

  private void setScreen(String text, String sampleName, boolean withFields) {
    xtn5250TerminalEmulator.setScreenSize(COLUMNS, ROWS);
    xtn5250TerminalEmulator.setScreen(buildScreen(text, withFields));
    xtn5250TerminalEmulator.setScreenName(sampleName);
    frame = new FrameFixture(xtn5250TerminalEmulator);
    frame.show();
  }

  private void setScreen(String text, String sampleName) {
    setScreen(text, sampleName, true);
  }

  private void sendKeyWithCursorUpdate(int key, int modifiers, int row, int column) {
    Component focusedComponent = frame.robot().finder().findByName("Terminal", true);
    JComponentDriver driver = new JComponentDriver(frame.robot());
    updateCursorPos(row, column);
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
    sendKeyWithCursorUpdate(KeyEvent.VK_F1, 0, 2, 2);
    verify(listener, timeout(PAUSE_TIMEOUT)).onAttentionKey(AttentionKey.F1,
        Collections.singletonList(new CoordInput(new Position(2,2), "")),
        "");
  }

  @Test
  public void shouldCallTheListenerWhenPressControlAttentionKey() {
    setScreen("");
    xtn5250TerminalEmulator.addTerminalEmulatorListener(listener);
    sendKeyWithCursorUpdate(KeyEvent.VK_CONTROL, KeyEvent.CTRL_MASK, 2, 2);
    verify(listener, timeout(PAUSE_TIMEOUT))
        .onAttentionKey(AttentionKey.RESET,
            Collections.singletonList(new CoordInput(new Position(2, 2), "")), "");
  }

  @Test
  public void shouldCopyTextWhenClickCopyButton() throws IOException, UnsupportedFlavorException {
    setScreen("");
    xtn5250TerminalEmulator.setSelectedArea(new Rectangle(0, 0, 5, 1));
    clickButton(COPY_BUTTON);
    assertTextIsInClipboard(CHUNK_OF_SCREEN);
  }

  @Test
  public void shouldCopyTextWhenClickCopyButtonUsingCharacterBasedEmulator() throws IOException,
      UnsupportedFlavorException {
    updateCharacterBasedWelcomeScreen();
    setupInteractiveCharacterEmulator();
    xtn5250TerminalEmulator.setSelectedArea(new Rectangle(30, 0, 18, 1));
    clickButton(COPY_BUTTON);
    assertTextIsInClipboard("**W E L C O M E **");
  }

  @Test
  public void shouldCopyTextWhenPressShortcut()
      throws IOException, UnsupportedFlavorException {
    setScreen("");
    xtn5250TerminalEmulator.setSelectedArea(new Rectangle(0, 0, 5, 1));
    sendKeyWithCursorUpdate(KeyEvent.VK_C, getMenuShortcutKeyMask(), 0, 0);
    assertTextIsInClipboard(CHUNK_OF_SCREEN);
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
    updateCursorPos(2, 1);
    addTextToClipboard("e");
    clickButton(PASTE_BUTTON);
    awaitTextInScreen(getFileContent(TEST_SCREEN_PRESS_KEY_ON_FIELD_FILE));
  }

  @Test
  public void shouldPasteTextWhenPressShortcut() throws IOException {
    setScreen("");
    updateCursorPos(2, 1);
    addTextToClipboard("e");
    sendKeyWithCursorUpdate(KeyEvent.VK_V, getMenuShortcutKeyMask(), 2, 1);
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
    sendKeyWithCursorUpdate(KeyEvent.VK_ESCAPE, 0, 0, 0);
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
    sendKeyWithCursorUpdate(KeyEvent.VK_T, 0, 2, 1);
    sendKeyWithCursorUpdate(KeyEvent.VK_ENTER, 0, 2, 5);
    List<Input> inputs = new ArrayList<>();
    inputs.add(new LabelInput(CHUNK_OF_SCREEN, input));
    inputs.add(new CoordInput(new Position(2, 5), ""));
    verify(listener).onAttentionKey(AttentionKey.ENTER, inputs, "");
  }

  @Test
  public void shouldNotifyListenerOfMultipleInputByLabel() {
    setScreenWithUserNameAndPasswordFields();
    xtn5250TerminalEmulator.addTerminalEmulatorListener(listener);
    xtn5250TerminalEmulator.setSelectedArea(new Rectangle(0, 0, 11, 1));
    clickButton(INPUT_BY_LABEL_BUTTON);
    sendKeyWithCursorUpdate(KeyEvent.VK_T, 0, 1, 14);
    xtn5250TerminalEmulator.setSelectedArea(new Rectangle(0, 1, 15, 1));
    clickButton(INPUT_BY_LABEL_BUTTON);
    sendKeyWithCursorUpdate(KeyEvent.VK_Y, 0, 2, 18);
    sendKeyWithCursorUpdate(KeyEvent.VK_ENTER, 0, 2, 19);
    verify(listener)
        .onAttentionKey(AttentionKey.ENTER, buildExpectedInputListForMultipleInputsByLabel(), "");
  }

  private List<Input> buildExpectedInputListForMultipleInputsByLabel() {
    LabelInput userName = new LabelInput("Insert Name", "t");
    LabelInput password = new LabelInput("Insert Password", "y");
    return Arrays.asList(userName, password);
  }

  private void setScreenWithUserNameAndPasswordFields() {
    xtn5250TerminalEmulator.setScreen(buildLoginScreenWithUserNameAndPasswordFields());
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
        .onWaitForText(CHUNK_OF_SCREEN + "\n " + "    \n" + "EXTO \n" + "EXTO ");
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
    sendKeyWithCursorUpdate(KeyEvent.VK_ENTER, 0, 2, 1);
    verify(listener).onAttentionKey(AttentionKey.ENTER,
        Collections.singletonList(new CoordInput(new Position(2, 1), "")),
        CONNECTING_LITERAL);
  }

  private void setSampleName(String name) {
    JTextComponentFixture field = frame.textBox(SAMPLE_NAME_FIELD);
    // target needed in order to effectively iterations with `listener` mock.
    field.target().setText(name);
  }

  @Test
  public void shouldSetDefaultValueInFieldWhenOnAttentionKey() {
    xtn5250TerminalEmulator.setKeyboardLock(true);
    setScreen("", DEFAULT_SAMPLE_NAME_INPUT_VALUE);
    xtn5250TerminalEmulator.addTerminalEmulatorListener(listener);
    xtn5250TerminalEmulator.setKeyboardLock(false);
    sendKeyWithCursorUpdate(KeyEvent.VK_ENTER, 0, 2, 1);
    assertThat(frame.textBox(SAMPLE_NAME_FIELD).text()).isEqualTo(DEFAULT_SAMPLE_NAME_INPUT_VALUE);
  }

  @Test
  public void shouldSwitchCredentialVisibilityIconWhenClickIcon() {
    setScreen("");
    frame.label("showCredentials").click();
    Icon actual = frame.label("showCredentials").target().getIcon();
    ImageIcon expected = new ImageIcon("/light-theme/visible-credentials.png");
    assertThat(((ImageIcon) actual).getImage().equals(expected.getImage()));
  }

  @Test
  public void shouldProperBuildInputsWithTabsWhenSendCredentialsUsingVT() {
    updateCharacterBasedWelcomeScreen();
    setupInteractiveCharacterEmulator();
    updateCharacterBasedWelcomeScreen("T ");
    setCurrentCursorPositionAndScreen(FIRST_VT_POS);
    when(characterBasedProtocolClient.getCursorPosition())
        .thenReturn(Optional.of(SECOND_VT_POS));
    sendKeyInCurrentPosition(KeyEvent.VK_T, 11, 42);

    updateCharacterBasedWelcomeScreen(DEVELOPER_ID);
    setCurrentCursorPositionAndScreen(new Position(11, 43));
    sendKeyInCurrentPosition(KeyEvent.VK_T, 11, 43);

    setCurrentCursorPositionAndScreen(new Position(13, 41));
    sendKeyInCurrentPosition(KeyEvent.VK_TAB, 13, 41);

    updateCharacterBasedWelcomeScreen(DEVELOPER_ID, "0 ");
    setCurrentCursorPositionAndScreen(new Position(13, 42));
    sendKeyInCurrentPosition(KeyEvent.VK_0, 13, 42);

    updateCharacterBasedWelcomeScreen(DEVELOPER_ID, WORKDATE_LITERAL_VALUE);
    setCurrentCursorPositionAndScreen(new Position(13, 43));
    sendKeyInCurrentPosition(KeyEvent.VK_0, 13, 43);

    assertThat(xtn5250TerminalEmulator.getInputs())
        .isEqualTo(buildExpectedTabulatorInput());

  }

  private List<Input> buildExpectedTabulatorInput() {
    return Arrays
        .asList(new NavigationInput(0, NavigationType.TAB, "tt"), new NavigationInput(1,
            NavigationType.TAB, WORKDATE_LITERAL_VALUE));
  }

  public void setCurrentCursorPositionAndScreen(Position cursor) {
    when(characterBasedProtocolClient.getScreen()).thenReturn(screen);
    when(characterBasedProtocolClient.getCursorPosition())
        .thenReturn(Optional.of(cursor));
  }

  private void updateCursorPos(int i, int i2) {
    xtn5250TerminalEmulator.setCursor(i, i2);
  }

  private void sendKeyInCurrentPosition(int key, int r, int c) {
    Component focusedComponent = frame.robot().finder().find(Component::isFocusOwner);
    JComponentDriver driver = new JComponentDriver(frame.robot());
    driver.pressAndReleaseKey(focusedComponent, KeyPressInfo.keyCode(key).modifiers(0));
    xtn5250TerminalEmulator.setCursor(r, c);
    xtn5250TerminalEmulator.setScreen(screen);
    characterBasedEmulator.screenChanged(screen.getText());
  }

  private void setupInteractiveCharacterEmulator() {
    setupCharacterEmulator(true);
  }

  private void setupCharacterEmulator(boolean interactive) {
    characterBasedEmulator = new CharacterBasedEmulator();
    xtn5250TerminalEmulator = new Xtn5250TerminalEmulator(characterBasedEmulator);
    xtn5250TerminalEmulator.setSupportedAttentionKeys(buildSupportedAttentionKeys());
    xtn5250TerminalEmulator.setProtocolClient(characterBasedProtocolClient);
    xtn5250TerminalEmulator.addTerminalEmulatorListener(listener);
    xtn5250TerminalEmulator.setScreenSize(COLUMNS, ROWS);
    xtn5250TerminalEmulator.setScreen(screen);
    xtn5250TerminalEmulator.setScreenName(samplerName);
    characterBasedEmulator.setKeyboardStatus(true);
    characterBasedEmulator.screenChanged(screen.getText());
    if (interactive) {
      setScreenChangeEventWhenSendingInputs();
    }
    frame = new FrameFixture(xtn5250TerminalEmulator);
    frame.show();
  }

  private void setScreenChangeEventWhenSendingInputs() {
    doAnswer(invocation -> {
      characterBasedEmulator.screenChanged(screen.getText());
      return null;
    }).when(characterBasedProtocolClient).send(anyString());
  }

  private void updateCharacterBasedWelcomeScreen(String... inputs) {
    updateCharacterBasedScreen(getWelcomeScreenText(inputs));

  }

  private void updateCharacterBasedScreen(List<String> screenText) {
    screen = new Screen(new Dimension(80, 24));
    AtomicInteger linearPosition = new AtomicInteger();
    screenText.forEach(l -> screen.addSegment(linearPosition.getAndAdd(80), l));
  }

  private List<String> getWelcomeScreenText(String... strings) {
    return Arrays.asList(
        "                              **W E L C O M E **                                ",
        "                                                                                ",
        "                                    WARNING                                     ",
        "                                                                                ",
        "                    THIS MATERIAL IT IS JUST FOR TESTING PROPOUSES              ",
        "                                                                                ",
        "                                                                                ",
        "                                                                                ",
        "                              TESTER ID: 001                                    ",
        "                                                                                ",
        "                           DEVELOPER ID:" + (strings.length > 0 ? strings[0] : "  ")
            + "                                       ",
        "                                                                                ",
        "                               WORKDATE:" + (strings.length > 1 ? strings[1] : "  ")
            + "                                      ",
        "                                                                                ",
        "                               PASSWORD:                                        ",
        "                                                                                ",
        "                                                                                ",
        "                                                                                ",
        "                                                                                ",
        "                                                                                ",
        "                                ENTER     CANCEL                                ",
        "                                                                                ",
        "                                                                                ",
        "                                                                                ");

  }

  @Test
  public void shouldNotCreateInputsWithInvalidCharacterWhenTypingInVT() {
    updateCharacterBasedWelcomeScreen();
    setupInteractiveCharacterEmulator();
    setCurrentCursorPositionAndScreen(SECOND_VT_POS);
    sendKeyInCurrentPosition(KeyEvent.VK_T, 11, 42);
    assertThat(xtn5250TerminalEmulator.getInputs().isEmpty());
  }

  @Test
  public void shouldLockAndUnlockKeyboardWhenSendInputAndScreenChanges() {
    updateCharacterBasedWelcomeScreen();
    setupInteractiveCharacterEmulator();

    updateCharacterBasedWelcomeScreen("T ");
    setCurrentCursorPositionAndScreen(FIRST_VT_POS);
    startSingleEventGenerator(buildScreenChangeListener());
    sendKeyWithCursorUpdate(KeyEvent.VK_T, 0, 11, 42);
    xtn5250TerminalEmulator.setScreen(screen);
    awaitKeyboardToBeUnlocked();
  }

  private Runnable buildScreenChangeListener() {
    return () -> characterBasedEmulator.screenChanged("");
  }

  private void startSingleEventGenerator(Runnable eventGenerator) {
    ScheduledExecutorService eventGeneratorExecutor = Executors.newSingleThreadScheduledExecutor();
    eventGeneratorExecutor.schedule(eventGenerator, (long) 500, TimeUnit.MILLISECONDS);
  }

  private void awaitKeyboardToBeUnlocked() {
    pause(new Condition("Keyboard to be unlocked") {
      @Override
      public boolean test() {
        return frame.label(KEYBOARD_LABEL).target().getIcon().equals(ThemedIcon
            .fromResourceName(KEYBOARD_UNLOCKED_RESOURCE_NAME));
      }
    }, PAUSE_TIMEOUT);
  }

  @Test
  public void shouldShowBlockedMouseIconWhenUsingVT() {
    updateCharacterBasedWelcomeScreen();
    setupInteractiveCharacterEmulator();
    Icon actual = frame.label(BLOCK_CURSOR_LABEL).target().getIcon();
    assertThat(actual.equals(ThemedIcon
        .fromResourceName(BLOCKED_CURSOR_RESOURCE_NAME)));
  }

  @Test
  public void shouldBlockedCursorStartBlinkingWhenClickingScreen() {
    updateCharacterBasedWelcomeScreen();
    setupInteractiveCharacterEmulator();
    frame.robot().click(xtn5250TerminalEmulator);
    awaitBlockedCursorToBlink();
  }

  private void awaitBlockedCursorToBlink() {
    pause(new Condition("Blocked cursor icon has blinked.") {
      @Override
      public boolean test() {
        return frame.label(BLOCK_CURSOR_LABEL).target().getIcon().equals(StatusPanelIT.CURSOR_ICON);
      }
    }, PAUSE_TIMEOUT);
  }

  @Test(expected = ComponentLookupException.class)
  public void shouldNotAppearBlockedCursorWhenUsingFiledBasedEmulator() {
    setScreen("");
    //will not find this component therefore, ComponentLookupException is thrown.
    frame.label(BLOCK_CURSOR_LABEL).isEnabled();
  }

  @Test
  public void shouldNotSendAnyCharacterWhenKeyboardLock() {
    updateCharacterBasedWelcomeScreen();
    setupInteractiveCharacterEmulator();
    xtn5250TerminalEmulator.setKeyboardLock(true);
    sendKeyWithCursorUpdate(KeyEvent.VK_Y, 0, 11, 42);
    verify(characterBasedProtocolClient, never()).send(anyString());
  }

  @Test
  public void shouldSendAttentionKeyWhenKeyboardLock() {
    updateCharacterBasedWelcomeScreen();
    setupInteractiveCharacterEmulator();
    xtn5250TerminalEmulator.setKeyboardLock(true);
    sendKeyWithCursorUpdate(KeyEvent.VK_F1, 0, 11, 42);
    verify(listener).onAttentionKey(any(AttentionKey.class), anyList(),
        anyString());
  }

  @Test
  public void shouldBuildExpectedInputsWhenUsingPaste() {
    updateCharacterBasedWelcomeScreen();
    setupInteractiveCharacterEmulator();
    setCursorPositionMock(11, 41);
    updateCharacterBasedWelcomeScreen("T ");
    addTextToClipboard(DEVELOPER_ID.toLowerCase());

    clickButton(PASTE_BUTTON);
    awaitForPasteAvailability();
    updateCharacterBasedWelcomeScreen(DEVELOPER_ID);
    xtn5250TerminalEmulator.setScreen(screen);
    setCursorPositionMock(12, 41);
    updateCharacterBasedWelcomeScreen("0 ");
    addTextToClipboard(WORKDATE_LITERAL_VALUE);

    clickButton(PASTE_BUTTON);
    awaitForPasteAvailability();
    updateCharacterBasedWelcomeScreen(DEVELOPER_ID, WORKDATE_LITERAL_VALUE);
    xtn5250TerminalEmulator.setScreen(screen);
    assertThat(xtn5250TerminalEmulator.getInputs())
        .isEqualTo(buildExpectedTabulatorInputsForCharacterBased());
  }

  private void setCursorPositionMock(int row, int column) {
    when(characterBasedProtocolClient.getCursorPosition()).thenReturn(Optional.of(new Position(row,
        column))).thenReturn(Optional.of(new Position(row,
        column + 1))).thenReturn(Optional.of(new Position(row,
        column + 1))).thenReturn(Optional.of(new Position(row,
        column + 2)));
  }

  private void awaitForPasteAvailability() {
    pause(new Condition(" waiting for paste button to be unlocked") {
      @Override
      public boolean test() {
        return frame.button(PASTE_BUTTON).isEnabled();
      }
    }, PAUSE_TIMEOUT);
  }

  private List<Input> buildExpectedTabulatorInputsForCharacterBased() {
    return Arrays.asList(new NavigationInput(0, NavigationType.TAB, DEVELOPER_ID.toLowerCase()),
        new NavigationInput(0, NavigationType.TAB, WORKDATE_LITERAL_VALUE));
  }

  @Test
  public void shouldCloseWindowWhenWaitingForServerAnswer() {
    updateCharacterBasedWelcomeScreen();
    setupManualCharacterEmulator();
    setLockWhenInputSent();
    AtomicBoolean isClosed = new AtomicBoolean();
    setupWindowListener(isClosed);
    sendKeyWithCursorUpdate(KeyEvent.VK_0, 0, 1, 1);
    sendKeyWithCursorUpdate(KeyEvent.VK_F, 0, 1, 1);
    verify(characterBasedProtocolClient, times(1)).send(anyString());
    frame.close();
    assertThat(isClosed);
  }

  private void setupWindowListener(AtomicBoolean isClosed) {
    frame.target().addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosed(WindowEvent e) {
        isClosed.set(true);
        super.windowClosed(e);
      }
    });
  }

  private void setLockWhenInputSent() {
    doAnswer(new AnswersWithDelay(PAUSE_TIMEOUT, null))
        .when(characterBasedProtocolClient).send(anyString());
  }

  private void setupManualCharacterEmulator() {
    setupCharacterEmulator(false);
  }

  @Test
  public void shouldNotSetVisibleInputByLabelButtonWhenUsingCharacterBasedEmulator() {
    updateCharacterBasedWelcomeScreen();
    setupManualCharacterEmulator();
    assertThat(Arrays.stream(frame.target().getComponents())
        .filter(c -> c instanceof JButton)
        .anyMatch(c -> c.getName().equals("labelButton")));
  }

  @Test
  public void shouldBuildDownNavigationInputWhenSendingDownArrow() {
    updateArrowNavigationScreen();
    setupInteractiveCharacterEmulator();
    xtn5250TerminalEmulator.setCursor(1, 54);
    when(characterBasedProtocolClient.getCursorPosition())
        .thenReturn(Optional.of(new Position(1, 54)))
        .thenReturn(Optional.of(new Position(2, 47)))
        .thenReturn(Optional.of(new Position(2, 48)));
    updateArrowNavigationScreen(ArrowInput.ZERO);
    sendKeyInCurrentPosition(KeyEvent.VK_DOWN, 2, 47);
    sendKeyInCurrentPosition(KeyEvent.VK_0, 2, 26);
    assertThat(xtn5250TerminalEmulator.getInputs()).isEqualTo(ArrowInput.ZERO.inputs);
  }

  private void updateArrowNavigationScreen(ArrowInput... values) {
    String[] texts =
        Arrays.stream(values)
            .map(ArrowInput::toString)
            .collect(Collectors.toList())
            .toArray(new String[values.length]);
    updateCharacterBasedScreen(buildArrowScreenText(texts));
  }

  private List<String> buildArrowScreenText(String... text) {
    return Arrays.asList(
        "              USER NAME: " + getIndex(text, 2) +
            "     PERMISSIONS REQUIRED: " + getIndex(text, 3) + "                          ",
        "              USER TYPE: " + getIndex(text, 1)
            + "     INTERNAL CODE: " + getIndex(text, 0) + "                                 ",
        "                                                                                ",
        "                                                                                ",
        "                                                                                ",
        "                                                                                ",
        "                                                                                ",
        "                                                                                ",
        "                                                                                ",
        "                                                                                ",
        "                                                                                ",
        "                                                                                ",
        "                                                                                ",
        "                                                                                ",
        "                                                                                ",
        "                                                                                ",
        "                                                                                ",
        "                                                                                ",
        "                                                                                ",
        "                                                                                ",
        "                                                                                ",
        "                                                                                ",
        "                                                                                ",
        "                                                                                ");
  }

  private String getIndex(String[] strings, int i) {
    return strings.length > i ? strings[i] : " ";
  }

  @Test
  public void shouldBuildLeftNavigationInputWhenSendingLeftArrow() {
    updateArrowNavigationScreen(ArrowInput.ZERO);
    setupInteractiveCharacterEmulator();
    xtn5250TerminalEmulator.setCursor(2, 48);
    when(characterBasedProtocolClient.getCursorPosition())
        .thenReturn(Optional.of(new Position(2, 48)))
        .thenReturn(Optional.of(new Position(2, 46)))
        .thenReturn(Optional.of(new Position(2, 46)))
        .thenReturn(Optional.of(new Position(2, 26)))
        .thenReturn(Optional.of(new Position(2, 26)))
        .thenReturn(Optional.of(new Position(2, 27)));
    sendKeyInCurrentPosition(KeyEvent.VK_LEFT, 2, 46);
    sendKeyInCurrentPosition(KeyEvent.VK_LEFT, 2, 26);
    sendKeyInCurrentPosition(KeyEvent.VK_1, 2, 27);
    updateArrowNavigationScreen(ArrowInput.ZERO, ArrowInput.ONE);
    assertThat(xtn5250TerminalEmulator.getInputs()).isEqualTo(ArrowInput.ONE.inputs);
  }

  @Test
  public void shouldBuildUpAndLeftNavigationInputWhenSendingUpAndLeftArrow() {
    updateArrowNavigationScreen(ArrowInput.ZERO, ArrowInput.ONE);
    setupInteractiveCharacterEmulator();
    xtn5250TerminalEmulator.setCursor(2, 27);
    when(characterBasedProtocolClient.getCursorPosition())
        .thenReturn(Optional.of(new Position(2, 27)))
        .thenReturn(Optional.of(new Position(1, 27)))
        .thenReturn(Optional.of(new Position(1, 27)))
        .thenReturn(Optional.of(new Position(1, 26)))
        .thenReturn(Optional.of(new Position(1, 26)))
        .thenReturn(Optional.of(new Position(1, 27)));
    sendKeyInCurrentPosition(KeyEvent.VK_UP, 1, 27);
    sendKeyInCurrentPosition(KeyEvent.VK_LEFT, 1, 26);
    updateArrowNavigationScreen(ArrowInput.ZERO, ArrowInput.ONE, ArrowInput.TWO);
    sendKeyInCurrentPosition(KeyEvent.VK_2, 1, 27);
    assertThat(xtn5250TerminalEmulator.getInputs()).isEqualTo(ArrowInput.TWO.inputs);
  }

  @Test
  public void shouldBuildRightNavigationInputWhenSendingRightArrow() {
    updateArrowNavigationScreen(ArrowInput.ZERO, ArrowInput.ONE, ArrowInput.TWO);
    setupInteractiveCharacterEmulator();
    xtn5250TerminalEmulator.setCursor(1, 27);
    when(characterBasedProtocolClient.getCursorPosition())
        .thenReturn(Optional.of(new Position(1, 27)))
        .thenReturn(Optional.of(new Position(1, 54)))
        .thenReturn(Optional.of(new Position(1, 54)))
        .thenReturn(Optional.of(new Position(1, 55)));
    sendKeyInCurrentPosition(KeyEvent.VK_RIGHT, 1, 54);
    updateArrowNavigationScreen(ArrowInput.ZERO, ArrowInput.ONE, ArrowInput.TWO, ArrowInput.THREE);
    sendKeyInCurrentPosition(KeyEvent.VK_3, 1, 55);
    assertThat(xtn5250TerminalEmulator.getInputs()).isEqualTo(ArrowInput.THREE.inputs);
  }

  @Test
  public void shouldProperBuildMixedInputsWhenFirstPositionIsMiddleField() throws Exception {
    setScreenWithUserNameAndPasswordFields();
    xtn5250TerminalEmulator.setCursor(2, 18);
    sendNavigationKey(KeyEvent.VK_TAB, 0);
    sendKeyWithCursorUpdate(KeyEvent.VK_T, 0, 1, 14);
    xtn5250TerminalEmulator.setSelectedArea(new Rectangle(0, 0, 11, 1));
    clickButton(INPUT_BY_LABEL_BUTTON);
    sendKeyWithCursorUpdate(KeyEvent.VK_Y, 0, 2, 18);
    assertThat(xtn5250TerminalEmulator.getInputs()).isEqualTo(Arrays.asList(new NavigationInput(0
            , NavigationType.TAB, "y"), new LabelInput("Insert Name", "t"),
        new CoordInput(new Position(1, 14), "")));

  }

  private void sendNavigationKey(int key, int modifiers) {
    Component focusedComponent = frame.robot().finder().find(Component::isFocusOwner);
    JComponentDriver driver = new JComponentDriver(frame.robot());
    driver.pressAndReleaseKey(focusedComponent, KeyPressInfo.keyCode(key).modifiers(modifiers));
  }

  public enum ArrowInput {
    ZERO("0", Collections.singletonList(new NavigationInput(1, NavigationType.DOWN, "0"))),
    ONE("1", Collections.singletonList(new NavigationInput(2, NavigationType.LEFT, "1"))),
    TWO("2", Arrays.asList(new NavigationInput(1, NavigationType.UP, ""),
        new NavigationInput(1, NavigationType.LEFT, "2"))),
    THREE("3", Collections.singletonList(new NavigationInput(1, NavigationType.RIGHT, "3")));

    public String value;
    public List<NavigationInput> inputs;

    ArrowInput(String value, List<NavigationInput> inputs) {
      this.value = value;
      this.inputs = inputs;
    }

    @Override
    public String toString() {
      return value;
    }
  }

  @Test
  public void shouldRecoverFocusOnScreenWhenClickingBackFromSampleNameField() throws Exception {
    updateCharacterBasedWelcomeScreen();
    setupInteractiveCharacterEmulator();
    frame.textBox(SAMPLE_NAME_FIELD).click();
    frame.robot().click(characterBasedEmulator);
    assertTrue(characterBasedEmulator.isFocusOwner());
  }

  @Test
  public void shouldKeepSampleNameWhenSendingInputs() throws Exception {
    updateCharacterBasedWelcomeScreen();
    setupInteractiveCharacterEmulator();
    frame.textBox(SAMPLE_NAME_FIELD).setText(CONNECTING_LITERAL);
    frame.robot().click(characterBasedEmulator);
    sendKeyInCurrentPosition(KeyEvent.VK_T, 11, 42);
    updateCharacterBasedWelcomeScreen("T ");
    setCurrentCursorPositionAndScreen(FIRST_VT_POS);
    xtn5250TerminalEmulator.setScreen(screen);
    assertEquals(CONNECTING_LITERAL, frame.textBox(SAMPLE_NAME_FIELD).text());
  }

  @Test
  public void shouldSendAttentionKeyWhenScreenNotConstitutedByFields() {
    setScreen("Press Enter", "sample-name", false);
    xtn5250TerminalEmulator.addTerminalEmulatorListener(listener);
    sendKeyWithCursorUpdate(KeyEvent.VK_ENTER, 0, 2, 1);
    verify(listener).onAttentionKey(AttentionKey.ENTER,
        Collections.singletonList(new CoordInput(new Position(2, 1), "")),
        "sample-name");
  }

  @Test
  public void shouldSendAttentionKeyAtDesiredPositionWhenSendingAttentionKeyAndScreenWithoutFields() {
    setScreen("Press Enter here:", "", false);
    xtn5250TerminalEmulator.addTerminalEmulatorListener(listener);
    sendKeyWithCursorUpdate(KeyEvent.VK_ENTER, 0, 2, 18);
    verify(listener).onAttentionKey(AttentionKey.ENTER,
        Collections.singletonList(new CoordInput(new Position(2, 18), "")), "");
  }
  
}
