package com.blazemeter.jmeter.rte.protocols.vt420;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.blazemeter.jmeter.rte.JMeterTestUtils;
import com.blazemeter.jmeter.rte.core.AttentionKey;
import com.blazemeter.jmeter.rte.core.Input;
import com.blazemeter.jmeter.rte.core.NavigationInput;
import com.blazemeter.jmeter.rte.core.Position;
import com.blazemeter.jmeter.rte.core.Screen.Segment;
import com.blazemeter.jmeter.rte.core.TerminalType;
import com.blazemeter.jmeter.rte.core.exceptions.RteIOException;
import com.blazemeter.jmeter.rte.core.listener.ExceptionHandler;
import com.blazemeter.jmeter.rte.core.ssl.SSLContextFactory;
import com.blazemeter.jmeter.rte.core.ssl.SSLType;
import com.blazemeter.jmeter.rte.core.wait.Area;
import com.blazemeter.jmeter.rte.core.wait.CursorWaitCondition;
import com.blazemeter.jmeter.rte.core.wait.SyncWaitCondition;
import com.blazemeter.jmeter.rte.core.wait.TextWaitCondition;
import com.blazemeter.jmeter.rte.core.wait.WaitCondition;
import com.blazemeter.jmeter.rte.protocols.RteProtocolClientIT;
import com.blazemeter.jmeter.rte.sampler.NavigationType;
import com.blazemeter.jmeter.rte.sampler.RTESampler;
import java.awt.Dimension;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeoutException;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class Vt420ClientIT extends RteProtocolClientIT<Vt420Client> {

  private static final String USER_ID = "tt";
  private static final String DATA = "123456";
  private static final String PASSWORD = "passwd";
  private static final NavigationInput USER_ID_INPUT = new NavigationInput(0, NavigationType.TAB,
      USER_ID);
  private static final NavigationInput DATA_INPUT = new NavigationInput(0, NavigationType.TAB,
      DATA);
  private static final NavigationInput USER_PASSWORD_INPUT = new NavigationInput(0,
      NavigationType.TAB, PASSWORD);
  private static final Position USER_ID_CURSOR_POSITION = new Position(12, 42);
  private static final Position WELCOME_SCREEN_CURSOR_POSITION = new Position(12, 27);
  private static final String LOGIN_SUCCESS_SCREEN_HTML = "login-success-screen.html";
  private static final String ARROW_NAVIGATION_SCREEN_HTML = "arrow-navigation-screen.html";
  @Rule
  public final JUnitSoftAssertions softly = new JUnitSoftAssertions();

  @BeforeClass
  public static void setupClass() {
    JMeterTestUtils.setupJmeterEnv();
  }

  @Override
  protected Vt420Client buildClient() {
    return new Vt420Client();
  }

  @Override
  protected TerminalType getDefaultTerminalType() {
    return new TerminalType("VT420-7", new Dimension(80, 24));
  }

  @Override
  public void setup() throws Exception {
    super.setup();
    RTESampler.setCharacterTimeout(5000);
  }

  @Override
  protected List<Segment> buildExpectedFields() {
    return null;
  }

  private void loadLoginFlow() throws FileNotFoundException {
    loadFlow("login.yml");
  }

  @Test
  public void shouldGetWelcomeScreenWhenConnect() throws Exception {
    loadLoginFlow();
    connectToVirtualService();
    assertThat(client.getScreen())
        .isEqualTo(buildScreenFromHtmlFile("user-welcome-screen.html"));
  }

  @Test(expected = RteIOException.class)
  public void shouldThrowRteIOExceptionWhenConnectWithInvalidPort() throws Exception {
    client.connect(VIRTUAL_SERVER_HOST, 1, SSLType.NONE, getDefaultTerminalType(), TIMEOUT_MILLIS);
  }

  private void waitForCursorPosition(Position position)
      throws InterruptedException, TimeoutException, RteIOException {
    client.await(Collections.singletonList(
        new CursorWaitCondition(position, TIMEOUT_MILLIS, STABLE_TIMEOUT_MILLIS)));
  }

  @Test
  public void shouldGetUserMenuScreenWhenSendUsername() throws Exception {
    loadLoginFlow();
    connectToVirtualService();
    waitForCursorPosition(WELCOME_SCREEN_CURSOR_POSITION);
    sendEnterAttentionKey();
    waitForCursorPosition(USER_ID_CURSOR_POSITION);
    assertThat(client.getScreen().withInvisibleCharsToSpaces())
        .isEqualTo(buildScreenFromHtmlFile("user-menu-screen.html"));
  }

  private void sendEnterAttentionKey() throws RteIOException {
    client.send(Collections.emptyList(), AttentionKey.ENTER, TIMEOUT_MILLIS);
  }

  @Test
  public void shouldGetArrownavigationScreenWhenSendCorrectCredentialsByTabulator()
      throws Exception {
    loadLoginFlow();
    connectToVirtualService();
    waitForCursorPosition(WELCOME_SCREEN_CURSOR_POSITION);
    sendEnterAttentionKey();
    waitForCursorPosition(USER_ID_CURSOR_POSITION);
    sendCredentialsByTabulatorWithSilent();
    assertThat(client.getScreen().withInvisibleCharsToSpaces())
        .isEqualTo(buildScreenFromHtmlFile(ARROW_NAVIGATION_SCREEN_HTML));

  }

  private void sendCredentialsByTabulatorWithSilent()
      throws RteIOException, TimeoutException, InterruptedException {
    client.send(Arrays.asList(USER_ID_INPUT, DATA_INPUT, USER_PASSWORD_INPUT), AttentionKey.ENTER,
        TIMEOUT_MILLIS);
    client.await(Collections.singletonList(new SyncWaitCondition(3000, STABLE_TIMEOUT_MILLIS)));
  }

  @Test(expected = RteIOException.class)
  public void shouldThrowRteIOExceptionWhenSendAndServerDown() throws Exception {
    loadLoginFlow();
    connectToVirtualService();
    server.stop(SERVER_STOP_TIMEOUT);
    sendEnterAttentionKey();
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
  public void shouldThrowTimeoutExceptionWhenCursorWaitAndNotExpectedCursorPosition()
      throws Exception {
    Position failingCursorPosition = new Position(1, 1);
    loadLoginFlow();
    connectToVirtualService();
    waitForCursorPosition(WELCOME_SCREEN_CURSOR_POSITION);
    sendEnterAttentionKey();
    waitForCursorPosition(failingCursorPosition);
  }

  @Test(expected = TimeoutException.class)
  public void shouldThrowTimeoutExceptionWhenTextWaitWithNoMatchingRegex()
      throws Exception {
    loadLoginFlow();
    connectToVirtualService();
    waitForCursorPosition(WELCOME_SCREEN_CURSOR_POSITION);
    sendEnterAttentionKey();
    client.await(Collections
        .singletonList(new TextWaitCondition(new Perl5Compiler().compile("NOT-IN-SCREEN"),
            new Perl5Matcher(),
            Area.fromTopLeftBottomRight(1, 1, Position.UNSPECIFIED_INDEX,
                Position.UNSPECIFIED_INDEX),
            TIMEOUT_MILLIS,
            STABLE_TIMEOUT_MILLIS)));
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
    loadLoginFlow();
    connectToVirtualService();
    client.send(Collections.emptyList(), AttentionKey.RESET, 0);
  }

  @Test(expected = RteIOException.class)
  public void shouldThrowTimeoutExceptionWhenNoScreenChangesAndSendText() throws Exception {
    loadLoginFlow();
    connectToVirtualService();
    ExceptionHandler exceptionHandler = new ExceptionHandler("");
    client.setExceptionHandler(exceptionHandler);
    waitForCursorPosition(WELCOME_SCREEN_CURSOR_POSITION);
    client.send("E");
    exceptionHandler.throwAnyPendingError();
  }


  @Test
  public void shouldGetSuccessScreenWhenSendingInputsByNavigation() throws Exception {
    loadLoginFlow();
    connectToVirtualService();
    waitForCursorPosition(WELCOME_SCREEN_CURSOR_POSITION);
    sendEnterAttentionKey();
    waitForCursorPosition(USER_ID_CURSOR_POSITION);
    sendCredentialsByTabulatorWithSilent();
    waitForCursorPosition(new Position(1, 54));
    sendArrowMovementsScreenDataWithSilentWaitCondition();
    assertThat(client.getScreen().withInvisibleCharsToSpaces())
        .isEqualTo(buildScreenFromHtmlFile("login-success-screen.html"));
  }

  private void sendArrowMovementsScreenDataWithSilentWaitCondition()
      throws RteIOException, TimeoutException, InterruptedException {
    client.send(Arrays.asList(
        new NavigationInput(1, NavigationType.DOWN, "00"),
        new NavigationInput(2, NavigationType.LEFT, "dev"),
        new NavigationInput(1, NavigationType.UP, "test"),
        new NavigationInput(1, NavigationType.RIGHT, "root")),
        AttentionKey.ENTER,
        TIMEOUT_MILLIS);
    awaitSync();
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
        .isEqualTo(buildScreenFromHtmlFile("user-welcome-screen.html"));
  }

  private void awaitSync() throws InterruptedException, TimeoutException, RteIOException {
    client.await(Collections.singletonList(new SyncWaitCondition(3000, STABLE_TIMEOUT_MILLIS)));
  }
}
