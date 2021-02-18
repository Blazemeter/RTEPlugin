package com.blazemeter.jmeter.rte.protocols.tn3270;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.blazemeter.jmeter.rte.core.AttentionKey;
import com.blazemeter.jmeter.rte.core.CoordInput;
import com.blazemeter.jmeter.rte.core.Input;
import com.blazemeter.jmeter.rte.core.LabelInput;
import com.blazemeter.jmeter.rte.core.NavigationInput;
import com.blazemeter.jmeter.rte.core.Position;
import com.blazemeter.jmeter.rte.core.Screen;
import com.blazemeter.jmeter.rte.core.Screen.Segment;
import com.blazemeter.jmeter.rte.core.TerminalType;
import com.blazemeter.jmeter.rte.core.exceptions.InvalidFieldLabelException;
import com.blazemeter.jmeter.rte.core.exceptions.InvalidFieldPositionException;
import com.blazemeter.jmeter.rte.core.exceptions.RteIOException;
import com.blazemeter.jmeter.rte.core.listener.TerminalStateListener;
import com.blazemeter.jmeter.rte.core.ssl.SSLContextFactory;
import com.blazemeter.jmeter.rte.core.ssl.SSLType;
import com.blazemeter.jmeter.rte.core.wait.Area;
import com.blazemeter.jmeter.rte.core.wait.CursorWaitCondition;
import com.blazemeter.jmeter.rte.core.wait.SilentWaitCondition;
import com.blazemeter.jmeter.rte.core.wait.SyncWaitCondition;
import com.blazemeter.jmeter.rte.core.wait.TextWaitCondition;
import com.blazemeter.jmeter.rte.core.wait.WaitCondition;
import com.blazemeter.jmeter.rte.protocols.RteProtocolClientIT;
import com.blazemeter.jmeter.rte.sampler.NavigationType;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.util.JMeterUtils;
import org.junit.Test;

public class Tn3270ClientIT extends RteProtocolClientIT<Tn3270Client> {

  private static final String USERNAME = "testusr";

  @Override
  protected Tn3270Client buildClient() {
    return new Tn3270Client();
  }

  @Override
  protected TerminalType getDefaultTerminalType() {
    return new Tn3270Client().getDefaultTerminalType();
  }

  @Override
  protected List<Segment> buildExpectedFields() {
    return Arrays.asList(
        new Segment(new Position(8, 20), buildBlankStr(8), true, true, SCREEN_SIZE),
        new Segment(new Position(8, 71), buildBlankStr(8), true, true, SCREEN_SIZE),
        new Segment(new Position(10, 20), "PROC394 ", true, false, SCREEN_SIZE),
        new Segment(new Position(10, 71), buildBlankStr(8), true, false, SCREEN_SIZE),
        new Segment(new Position(12, 20), "1000000" + buildBlankStr(33), true, false, SCREEN_SIZE),
        new Segment(new Position(14, 20), "4096" + buildBlankStr(3), true, false, SCREEN_SIZE),
        new Segment(new Position(16, 20), buildBlankStr(3), true, false, SCREEN_SIZE),
        new Segment(new Position(18, 20), buildBlankStr(80), true, false, SCREEN_SIZE),
        new Segment(new Position(21, 11), " ", true, false, SCREEN_SIZE),
        new Segment(new Position(21, 27), " ", true, false, SCREEN_SIZE),
        new Segment(new Position(21, 44), " ", true, false, SCREEN_SIZE),
        new Segment(new Position(21, 62), " ", true, false, SCREEN_SIZE)
    );

  }

  private String buildBlankStr(int spaces) {
    return StringUtils.repeat(' ', spaces);
  }

  @Test
  public void shouldGetWelcomeScreenWhenConnect() throws Exception {
    loadLoginFlow();
    connectToVirtualService();
    assertThat(client.getScreen().withInvisibleCharsToSpaces())
        .isEqualTo(buildExpectedWelcomeScreen());
  }

  private Screen buildExpectedWelcomeScreen() throws IOException {
    return buildScreenFromHtmlFile("login-welcome-screen.html");
  }

  private void loadLoginFlow() throws FileNotFoundException {
    loadFlow("login-immediate-responses.yml");
  }

  @Test
  public void shouldGetWelcomeScreenWhenConnectWithSsl() throws Exception {
    server.stop(SERVER_STOP_TIMEOUT);
    loadLoginFlow();
    SSLContextFactory.setKeyStore(findResource("/.keystore").getFile());
    SSLContextFactory.setKeyStorePassword("changeit");
    server.setSslEnabled(true);
    server.start();
    client.connect(VIRTUAL_SERVER_HOST, server.getPort(), SSLType.TLS, getDefaultTerminalType(),
        TIMEOUT_MILLIS);
    awaitSync();
    assertThat(client.getScreen().withInvisibleCharsToSpaces())
        .isEqualTo(buildExpectedWelcomeScreen());
  }

  private void awaitSync() throws InterruptedException, TimeoutException, RteIOException {
    client.await(
        Collections.singletonList(new SyncWaitCondition(TIMEOUT_MILLIS, STABLE_TIMEOUT_MILLIS)));
  }

  @Test
  public void shouldGetTrueSoundAlarmWhenServerSendTheSignal() throws Exception {
    loadLoginFlow();
    connectToVirtualService();
    sendUsernameWithSyncWait();
    assertThat(client.resetAlarm()).isTrue();
  }

  @Test
  public void shouldGetFalseSoundAlarmWhenServerDoNotSendTheSignal() throws Exception {
    loadLoginFlow();
    connectToVirtualService();
    assertThat(client.resetAlarm()).isFalse();
  }

  @Test(expected = RteIOException.class)
  public void shouldThrowRteIOExceptionWhenConnectWithInvalidPort() throws Exception {
    client.connect(VIRTUAL_SERVER_HOST, 1, SSLType.NONE, client.getDefaultTerminalType(),
        TIMEOUT_MILLIS);
  }

  @Test
  public void shouldGetUserMenuScreenWhenSendUsername() throws Exception {
    loadFlow("login.yml");
    connectToVirtualService();
    sendUsernameWithSyncWait();
    assertThat(client.getScreen().withInvisibleCharsToSpaces())
        .isEqualTo(buildScreenFromHtmlFile("user-menu-screen.html"));
  }

  private void sendUsernameWithSyncWait() throws Exception {
    sendEnterAttentionKey(buildUsernameField());
    awaitSync();
  }

  private Input buildUsernameField() {
    return new CoordInput(new Position(2, 1), USERNAME);
  }

  @Test
  public void shouldGetLoginSuccessScreenWhenSendCredsByLabel() throws Exception {
    loadFlow("login-immediate-responses.yml");
    connectToVirtualService();
    sendUsernameWithSyncWait();
    sendPasswordByLabelWithSyncWait();
    assertThat(client.getScreen().withInvisibleCharsToSpaces())
        .isEqualTo(buildLoginSuccessScreen());
  }

  private Screen buildLoginSuccessScreen() throws IOException {
    return buildScreenFromHtmlFile("login-success-screen.html");
  }

  private void sendPasswordByLabelWithSyncWait() throws Exception {
    client.send(buildPasswordByLabel(), AttentionKey.ENTER, 0);
    awaitSync();
  }

  private List<Input> buildPasswordByLabel() {
    return Collections.singletonList(
        new LabelInput("Password", "testpsw"));
  }

  @Test(expected = InvalidFieldLabelException.class)
  public void shouldThrowInvalidLabelExceptionWhenShowsIncorrectLabel()
      throws Exception {
    loadLoginFlow();
    connectToVirtualService();
    List<Input> input = Collections.singletonList(
        new LabelInput("Address", "address_Example_123"));
    client.send(input, AttentionKey.ENTER, 0);
    awaitSync();
  }

  @Test(expected = InvalidFieldPositionException.class)
  public void shouldThrowInvalidFieldPositionExceptionWhenSendIncorrectFieldPosition()
      throws Exception {
    loadLoginFlow();
    connectToVirtualService();
    List<Input> input = Collections.singletonList(
        new CoordInput(new Position(81, 1), "TEST"));
    client.send(input, AttentionKey.ENTER, 0);
  }

  @Test(expected = RteIOException.class)
  public void shouldThrowRteIOExceptionWhenSendAndServerDown() throws Exception {
    loadLoginFlow();
    connectToVirtualService();
    server.stop(SERVER_STOP_TIMEOUT);
    sendUsernameWithSyncWait();
  }

  @Test(expected = UnsupportedOperationException.class)
  public void shouldThrowUnsupportedOperationExceptionWhenAwaitWithUndefinedCondition()
      throws Exception {
    loadLoginFlow();
    connectToVirtualService();
    List<WaitCondition> conditions = Collections
        .singletonList(new WaitCondition(TIMEOUT_MILLIS, STABLE_TIMEOUT_MILLIS) {
          @Override
          public String getDescription() {
            return "test";
          }
        });
    client.await(conditions);
  }

  @Test(expected = TimeoutException.class)
  public void shouldThrowTimeoutExceptionWhenSyncWaitAndSlowResponse() throws Exception {
    loadFlow("slow-response.yml");
    connectToVirtualService();
    sendEnterAttentionKey(buildUsernameField());
    awaitSync();
  }

  private void sendEnterAttentionKey(Input... inputs) throws RteIOException {
    client.send(Arrays.asList(inputs), AttentionKey.ENTER, 0);
  }

  @Test(expected = TimeoutException.class)
  public void shouldThrowTimeoutExceptionWhenCursorWaitAndNotExpectedCursorPosition()
      throws Exception {
    loadLoginFlow();
    connectToVirtualService();
    sendEnterAttentionKey(buildUsernameField());
    client.await(Collections.singletonList(
        new CursorWaitCondition(new Position(1,
            50), TIMEOUT_MILLIS, STABLE_TIMEOUT_MILLIS)));
  }

  @Test(expected = TimeoutException.class)
  public void shouldThrowTimeoutExceptionWhenSilentWaitAndChattyServer() throws Exception {
    loadFlow("chatty-server.yml");
    connectToVirtualService();
    sendEnterAttentionKey(buildUsernameField());
    client.await(
        Collections.singletonList(new SilentWaitCondition(TIMEOUT_MILLIS, STABLE_TIMEOUT_MILLIS)));
  }

  @Test(expected = TimeoutException.class)
  public void shouldThrowTimeoutExceptionWhenTextWaitWithNoMatchingRegex()
      throws Exception {
    loadLoginFlow();
    connectToVirtualService();
    sendEnterAttentionKey(buildUsernameField());
    awaitText("testing-wait-text");
  }

  @Test
  public void shouldGetWelcomeScreenWhenConnectAfterDisconnectInvalidCreds() throws Exception {
    loadLoginFlow();
    connectToVirtualService();
    sendUsernameWithSyncWait();
    client.disconnect();
    connectToVirtualService();
    assertThat(client.getScreen().withInvisibleCharsToSpaces())
        .isEqualTo(buildExpectedWelcomeScreen());
  }

  @Test
  public void shouldNotThrowExceptionWhenDisconnectAndServerDown() throws Exception {
    loadLoginFlow();
    connectToVirtualService();
    server.stop(SERVER_STOP_TIMEOUT);
    client.disconnect();
  }

  @Test(expected = UnsupportedOperationException.class)
  public void shouldThrowUnsupportedOperationExceptionWhenSelectAttentionKeyUnsupported()
      throws Exception {
    loadFlow("login.yml");
    connectToVirtualService();
    client.send(Collections.singletonList(buildUsernameField()), AttentionKey.ROLL_UP, 0);
  }

  @Test
  public void shouldGetWelcomeScreenWhenSscpLuLogin() throws Exception {
    loadFlow("sscplu-login.yml");
    connectExtendedProtocolClientToVirtualService();
    assertThat(client.getScreen().withInvisibleCharsToSpaces())
        .isEqualTo(buildScreenFromHtmlFile("sscplu-welcome-screen.html"));
  }

  private void connectExtendedProtocolClientToVirtualService()
      throws RteIOException, InterruptedException, TimeoutException {
    client.connect(VIRTUAL_SERVER_HOST, server.getPort(), SSLType.NONE,
        client.getTerminalTypeById("IBM-3278-M2-E"), TIMEOUT_MILLIS);
    awaitSync();
  }

  @Test
  public void shouldGetWelcomeScreenWhenLoginWithoutFields() throws Exception {
    loadFlow("login-without-fields.yml");
    connectExtendedProtocolClientToVirtualService();
    client.send(Collections.singletonList(new CoordInput(new Position(20, 48), USERNAME)),
        AttentionKey.ENTER, 0);
    awaitSync();
    assertThat(client.getScreen().withInvisibleCharsToSpaces())
        .isEqualTo(buildScreenFromHtmlFile("login-without-fields-screen.html"));
  }

  @Test
  public void shouldNotifyAddedListenerWhenTerminalStateChanges() throws Exception {
    TerminalStateListener terminalEmulatorUpdater = mock(TerminalStateListener.class);
    loadLoginFlow();
    connectToVirtualService();

    client.addTerminalStateListener(terminalEmulatorUpdater);
    sendUsernameWithSyncWait();

    /*
     * When inputs are sent to client, 17 changes happens: the screen changes, the cursor moves
     * and also the keyboard changes.
     */
    verify(terminalEmulatorUpdater, times(17)).onTerminalStateChange();
  }

  @Test
  public void shouldNotNotifyRemovedListenerWhenTerminalStateChanges() throws Exception {
    TerminalStateListener terminalEmulatorUpdater = mock(TerminalStateListener.class);
    loadLoginFlow();
    connectToVirtualService();
    client.addTerminalStateListener(terminalEmulatorUpdater);
    client.removeTerminalStateListener(terminalEmulatorUpdater);

    sendUsernameWithSyncWait();

    verify(terminalEmulatorUpdater, never()).onTerminalStateChange();
  }

  @Test
  public void shouldValidateSecretFieldsOnScreenWhenBuildScreen() throws Exception {
    loadLoginFlow();
    connectToVirtualService();
    sendUsernameWithSyncWait();

    List<Segment> currentSegments = client.getScreen().getSegments().stream()
        .filter(Segment::isEditable)
        .collect(Collectors.toList());

    assertThat(currentSegments).isEqualTo(buildExpectedFields());
  }

  @Test(expected = TimeoutException.class)
  public void shouldThrowTimeoutExceptionWhenMatchedScreenChangedBeforeStablePeriod()
      throws Exception {
    loadFlow("login-with-multiple-flash-screen.yml");
    client.connect(VIRTUAL_SERVER_HOST, server.getPort(), SSLType.NONE, getDefaultTerminalType(),
        TIMEOUT_MILLIS);
    awaitText("AAAAA");
  }

  private void awaitText(String text)
      throws InterruptedException, TimeoutException, RteIOException {
    client.await(Collections.singletonList(new TextWaitCondition(JMeterUtils.getPattern(text),
        JMeterUtils.getMatcher(),
        Area.fromTopLeftBottomRight(1, 1, Position.UNSPECIFIED_INDEX, Position.UNSPECIFIED_INDEX),
        TIMEOUT_MILLIS, STABLE_TIMEOUT_MILLIS)));
  }

  @Test
  public void shouldGetUserMenuScreenWhenSendUserNameWithArrows() throws Exception {
    loadFlow("login.yml");
    connectToVirtualService();
    sendEnterAttentionKey(new NavigationInput(1, NavigationType.DOWN, ""),
        new NavigationInput(27, NavigationType.RIGHT, ""),
        new NavigationInput(2, NavigationType.UP, ""),
        new NavigationInput(1, NavigationType.LEFT, "testusr"));
    awaitSync();
    assertThat(client.getScreen().withInvisibleCharsToSpaces())
        .isEqualTo(buildScreenFromHtmlFile("user-menu-screen.html"));
  }

  @Test
  public void shouldValidateTextOnScreeWhenScreenBuiltFromPlainTextAndFields() throws Exception {
    loadFlow("login-mixed-fields-and-plain-text.yml");
    connectExtendedProtocolClientToVirtualService();
    sendEnterAttentionKey(new NavigationInput(0, NavigationType.TAB, "TESTUSR "),
        new NavigationInput(1, NavigationType.TAB,
            "TESTPSW" + IntStream.range(0, 35).mapToObj(i -> " ").collect(Collectors.joining())));
    awaitText("Ready");
  }
}
