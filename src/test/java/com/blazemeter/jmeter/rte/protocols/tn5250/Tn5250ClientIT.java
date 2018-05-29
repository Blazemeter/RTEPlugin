package com.blazemeter.jmeter.rte.protocols.tn5250;

import static org.assertj.core.api.Assertions.assertThat;

import com.blazemeter.jmeter.rte.core.AttentionKey;
import com.blazemeter.jmeter.rte.core.CoordInput;
import com.blazemeter.jmeter.rte.core.InvalidFieldPositionException;
import com.blazemeter.jmeter.rte.core.Position;
import com.blazemeter.jmeter.rte.core.RteIOException;
import com.blazemeter.jmeter.rte.core.TerminalType;
import com.blazemeter.jmeter.rte.core.ssl.SSLSocketFactory;
import com.blazemeter.jmeter.rte.core.ssl.SSLType;
import com.blazemeter.jmeter.rte.core.wait.Area;
import com.blazemeter.jmeter.rte.core.wait.CursorWaitCondition;
import com.blazemeter.jmeter.rte.core.wait.SilentWaitCondition;
import com.blazemeter.jmeter.rte.core.wait.SyncWaitCondition;
import com.blazemeter.jmeter.rte.core.wait.TextWaitCondition;
import com.blazemeter.jmeter.rte.core.wait.WaitCondition;
import com.blazemeter.jmeter.rte.protocols.RteProtocolClientIT;
import com.blazemeter.jmeter.rte.virtualservice.VirtualTcpService;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeoutException;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;
import org.junit.Test;

public class Tn5250ClientIT extends RteProtocolClientIT<Tn5250Client> {

  @Override
  protected Tn5250Client buildClient() {
    return new Tn5250Client();
  }

  @Override
  protected TerminalType getDefaultTerminalType() {
    return client.getTerminalTypeById("IBM-3477-FC");
  }

  @Test
  public void shouldGetWelcomeScreenWhenConnect() throws Exception {
    loadLoginFlow();
    connectToVirtualService();
    assertThat(client.getScreen())
        .isEqualTo(getFileContent("login-welcome-screen.txt"));
  }

  private void loadLoginFlow() throws FileNotFoundException {
    loadFlow("login.yml");
  }

  @Test
  public void shouldGetWelcomeScreenWhenConnectWithSsl() throws Exception {
    server.stop(SERVER_STOP_TIMEOUT);
    SSLSocketFactory.setKeyStore(findResource("/.keystore").getFile());
    SSLSocketFactory.setKeyStorePassword("changeit");
    server = new VirtualTcpService(SSLType.TLS);
    server.start();
    loadLoginFlow();
    client.connect(VIRTUAL_SERVER_HOST, server.getPort(), SSLType.TLS, getDefaultTerminalType(),
        TIMEOUT_MILLIS, STABLE_TIMEOUT_MILLIS);
    assertThat(client.getScreen())
        .isEqualTo(getFileContent("login-welcome-screen.txt"));
  }

  @Test(expected = RteIOException.class)
  public void shouldThrowRteIOExceptionWhenConnectWithInvalidPort() throws Exception {
    client.connect(VIRTUAL_SERVER_HOST, 0, SSLType.NONE, getDefaultTerminalType(), TIMEOUT_MILLIS,
        STABLE_TIMEOUT_MILLIS);
  }

  @Test(expected = TimeoutException.class)
  public void shouldThrowTimeoutExceptionWhenConnectAndServerIsTooSlow() throws Exception {
    loadFlow("slow-welcome-screen.yml");
    connectToVirtualService();
  }

  @Test
  public void shouldGetUserMenuScreenWhenSendCreds() throws Exception {
    loadLoginFlow();
    connectToVirtualService();
    sendCredsWithSyncWait();
    assertThat(client.getScreen())
        .isEqualTo(getFileContent("user-menu-screen.txt"));
  }

  private void sendCredsWithSyncWait() throws Exception {
    client.send(buildCredsFields(), AttentionKey.ENTER);
    client.await(
        Collections.singletonList(new SyncWaitCondition(TIMEOUT_MILLIS, STABLE_TIMEOUT_MILLIS)));
  }

  private List<CoordInput> buildCredsFields() {
    return Arrays.asList(
        new CoordInput(new Position(6, 53), "TESTUSR"),
        new CoordInput(new Position(7, 53), "TESTPSW"));
  }

  @Test(expected = InvalidFieldPositionException.class)
  public void shouldThrowInvalidFieldPositionExceptionWhenSendIncorrectFieldPosition()
      throws Exception {
    loadLoginFlow();
    connectToVirtualService();
    List<CoordInput> input = Collections.singletonList(
        new CoordInput(new Position(7, 1), "TESTUSR"));
    client.send(input, AttentionKey.ENTER);
  }

  @Test(expected = RteIOException.class)
  public void shouldThrowRteIOExceptionWhenSendAndServerDown() throws Exception {
    loadLoginFlow();
    connectToVirtualService();
    server.stop(SERVER_STOP_TIMEOUT);
    sendCredsWithSyncWait();
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
    client.send(buildCredsFields(), AttentionKey.ENTER);
    client.await(
        Collections.singletonList(new SyncWaitCondition(TIMEOUT_MILLIS, STABLE_TIMEOUT_MILLIS)));
  }

  @Test(expected = TimeoutException.class)
  public void shouldThrowTimeoutExceptionWhenCursorWaitAndNotExpectedCursorPosition()
      throws Exception {
    loadLoginFlow();
    connectToVirtualService();
    client.send(buildCredsFields(), AttentionKey.ENTER);
    client.await(Collections.singletonList(
        new CursorWaitCondition(new Position(1, 1), TIMEOUT_MILLIS, STABLE_TIMEOUT_MILLIS)));
  }

  @Test(expected = TimeoutException.class)
  public void shouldThrowTimeoutExceptionWhenSilentWaitAndChattyServer() throws Exception {
    loadFlow("chatty-server.yml");
    connectToVirtualService();
    client.send(buildCredsFields(), AttentionKey.ENTER);
    client.await(
        Collections.singletonList(new SilentWaitCondition(TIMEOUT_MILLIS, STABLE_TIMEOUT_MILLIS)));
  }

  @Test(expected = TimeoutException.class)
  public void shouldThrowTimeoutExceptionWhenTextWaitWithNoMatchingRegex()
      throws Exception {
    loadLoginFlow();
    connectToVirtualService();
    client.send(buildCredsFields(), AttentionKey.ENTER);
    client.await(Collections
        .singletonList(new TextWaitCondition(new Perl5Compiler().compile("testing-wait-text"),
            new Perl5Matcher(),
            Area.fromTopLeftBottomRight(1, 1, Position.UNSPECIFIED_INDEX,
                Position.UNSPECIFIED_INDEX),
            TIMEOUT_MILLIS,
            STABLE_TIMEOUT_MILLIS)));
  }

  @Test
  public void shouldGetWelcomeScreenWhenConnectAfterDisconnect() throws Exception {
    loadLoginFlow();
    connectToVirtualService();
    sendCredsWithSyncWait();
    client.disconnect();
    connectToVirtualService();
    assertThat(client.getScreen())
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
    loadLoginFlow();
    connectToVirtualService();
    client.send(buildCredsFields(), AttentionKey.PA1);
  }

}
