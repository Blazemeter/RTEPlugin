package com.blazemeter.jmeter.rte.protocols.tn3270;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.blazemeter.jmeter.rte.core.AttentionKey;
import com.blazemeter.jmeter.rte.core.CoordInput;
import com.blazemeter.jmeter.rte.core.Input;
import com.blazemeter.jmeter.rte.core.exceptions.InvalidFieldLabelException;
import com.blazemeter.jmeter.rte.core.exceptions.InvalidFieldPositionException;
import com.blazemeter.jmeter.rte.core.LabelInput;
import com.blazemeter.jmeter.rte.core.Position;
import com.blazemeter.jmeter.rte.core.exceptions.RteIOException;
import com.blazemeter.jmeter.rte.core.TerminalType;
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

import java.io.FileNotFoundException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeoutException;

import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;
import org.junit.Test;

public class Tn3270ClientIT extends RteProtocolClientIT<Tn3270Client> {

  @Override
  protected Tn3270Client buildClient() {
    return new Tn3270Client();
  }

  @Override
  protected TerminalType getDefaultTerminalType() {
    return new Tn3270Client().getDefaultTerminalType();
  }

  @Test
  public void shouldGetWelcomeScreenWhenConnect() throws Exception {
    loadLoginFlow();
    connectToVirtualService();
    assertThat(client.getScreen().toString())
            .isEqualTo(getFileContent("login-welcome-screen.txt"));
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
    client.await(
            Collections.singletonList(new SyncWaitCondition(TIMEOUT_MILLIS, STABLE_TIMEOUT_MILLIS)));
    assertThat(client.getScreen().toString())
            .isEqualTo(getFileContent("login-welcome-screen.txt"));
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

  @Test(expected = TimeoutException.class)
  public void shouldThrowTimeoutExceptionWhenConnectAndServerIsTooSlow() throws Exception {
    loadFlow("slow-welcome-screen.yml");
    connectToVirtualService();
  }

  @Test
  public void shouldGetUserMenuScreenWhenSendUsername() throws Exception {
    loadFlow("login.yml");
    connectToVirtualService();
    sendUsernameWithSyncWait();
    assertThat(client.getScreen().toString())
            .isEqualTo(getFileContent("user-menu-screen.txt"));
  }

  private void sendUsernameWithSyncWait() throws Exception {
    client.send(buildUsernameField(), AttentionKey.ENTER);
    client.await(
            Collections.singletonList(new SyncWaitCondition(TIMEOUT_MILLIS, STABLE_TIMEOUT_MILLIS)));
  }

  private List<Input> buildUsernameField() {
    return Collections.singletonList(new CoordInput(new Position(2, 1), "testusr"));
  }

  @Test
  public void shouldGetLoginSuccessScreenWhenSendCredsByLabel() throws Exception {
    loadFlow("login-immediate-responses.yml");
    connectToVirtualService();
    sendUsernameWithSyncWait();
    sendPasswordByLabelWithSyncWait();
    assertThat(client.getScreen().toString())
            .isEqualTo(getFileContent("login-success-screen.txt"));
  }

  private void sendPasswordByLabelWithSyncWait() throws Exception {
    client.send(buildPasswordByLabel(), AttentionKey.ENTER);
    client.await(
            Collections.singletonList(new SyncWaitCondition(TIMEOUT_MILLIS, STABLE_TIMEOUT_MILLIS)));
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
    client.send(input, AttentionKey.ENTER);
    client.await(
            Collections.singletonList(new SyncWaitCondition(TIMEOUT_MILLIS, STABLE_TIMEOUT_MILLIS)));
  }

  @Test(expected = InvalidFieldPositionException.class)
  public void shouldThrowInvalidFieldPositionExceptionWhenSendIncorrectFieldPosition()
          throws Exception {
    loadLoginFlow();
    connectToVirtualService();
    List<Input> input = Collections.singletonList(
            new CoordInput(new Position(81, 1), "TEST"));
    client.send(input, AttentionKey.ENTER);
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
    client.send(buildUsernameField(), AttentionKey.ENTER);
    client.await(
            Collections.singletonList(new SyncWaitCondition(TIMEOUT_MILLIS, STABLE_TIMEOUT_MILLIS)));
  }

  @Test(expected = TimeoutException.class)
  public void shouldThrowTimeoutExceptionWhenCursorWaitAndNotExpectedCursorPosition()
          throws Exception {
    loadLoginFlow();
    connectToVirtualService();
    client.send(buildUsernameField(), AttentionKey.ENTER);
    client.await(Collections.singletonList(
            new CursorWaitCondition(new Position(1,
                    1), TIMEOUT_MILLIS, STABLE_TIMEOUT_MILLIS)));
  }

  @Test(expected = TimeoutException.class)
  public void shouldThrowTimeoutExceptionWhenSilentWaitAndChattyServer() throws Exception {
    loadFlow("chatty-server.yml");
    connectToVirtualService();
    client.send(buildUsernameField(), AttentionKey.ENTER);
    client.await(
            Collections.singletonList(new SilentWaitCondition(TIMEOUT_MILLIS, STABLE_TIMEOUT_MILLIS)));
  }

  @Test(expected = TimeoutException.class)
  public void shouldThrowTimeoutExceptionWhenTextWaitWithNoMatchingRegex()
          throws Exception {
    loadLoginFlow();
    connectToVirtualService();
    client.send(buildUsernameField(), AttentionKey.ENTER);
    client.await(Collections
            .singletonList(new TextWaitCondition(new Perl5Compiler().compile("testing-wait-text"),
                    new Perl5Matcher(),
                    Area.fromTopLeftBottomRight(1, 1, Position.UNSPECIFIED_INDEX,
                            Position.UNSPECIFIED_INDEX),
                    TIMEOUT_MILLIS,
                    STABLE_TIMEOUT_MILLIS)));
  }

  @Test
  public void shouldGetWelcomeScreenWhenConnectAfterDisconnectInvalidCreds() throws Exception {
    loadLoginFlow();
    connectToVirtualService();
    sendUsernameWithSyncWait();
    client.disconnect();
    connectToVirtualService();
    assertThat(client.getScreen().toString())
            .isEqualTo(getFileContent("login-welcome-screen.txt"));
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
    client.send(buildUsernameField(), AttentionKey.ROLL_UP);
  }

  @Test
  public void shouldNotifyAddedListenerWhenTerminalStateChanges() throws Exception{

    TerminalStateListener terminalEmulatorUpdater = mock(TerminalStateListener.class);

    loadLoginFlow();
    connectToVirtualService();

    client.addTerminalStateListener(terminalEmulatorUpdater);
    sendInputsAndWait();

    /*
     * When inputs are sent to client, 17 changes happens: the screen changes, the cursor moves
     * and also the keyboard changes.
     * */

    verify(terminalEmulatorUpdater, times(17)).onTerminalStateChange();
  }

  private void sendInputsAndWait() throws InterruptedException, TimeoutException, RteIOException {
    List<Input> inputs = Collections.singletonList(new CoordInput(new Position(2, 1), "testusr"));
    client.send(inputs, AttentionKey.ENTER);
    client.await(
            Collections.singletonList(new SyncWaitCondition(TIMEOUT_MILLIS, STABLE_TIMEOUT_MILLIS)));
  }

  @Test
  public void shouldNotNotifyRemovedListenerWhenTerminalStateChanges() throws Exception{

    TerminalStateListener terminalEmulatorUpdater = mock(TerminalStateListener.class);

    loadLoginFlow();
    connectToVirtualService();

    client.addTerminalStateListener(terminalEmulatorUpdater);
    client.removeTerminalStateListener(terminalEmulatorUpdater);
    client.send(buildUsernameField(), AttentionKey.ENTER);
    client.await(
            Collections.singletonList(new SyncWaitCondition(TIMEOUT_MILLIS, STABLE_TIMEOUT_MILLIS)));

    verify(terminalEmulatorUpdater, never()).onTerminalStateChange();
  }

}
