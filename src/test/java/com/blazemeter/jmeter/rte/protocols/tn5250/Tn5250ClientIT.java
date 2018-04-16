package com.blazemeter.jmeter.rte.protocols.tn5250;

import static org.assertj.core.api.Assertions.assertThat;

import com.blazemeter.jmeter.rte.core.Action;
import com.blazemeter.jmeter.rte.core.CoordInput;
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
import com.blazemeter.jmeter.rte.virtualservice.Flow;
import com.blazemeter.jmeter.rte.virtualservice.VirtualTcpService;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Tn5250ClientIT {

  private static final Logger LOG = LoggerFactory.getLogger(Tn5250ClientIT.class);

  private static final String VIRTUAL_SERVER_HOST = "localhost";
  private static final int VIRTUAL_SERVER_PORT = 2323;
  private static final int TIMEOUT_MILLIS = 5000;
  private static final int STABLE_TIMEOUT_MILLIS = 1000;
  private static final long SERVER_STOP_TIMEOUT = TimeUnit.SECONDS.toMillis(10);

  private VirtualTcpService server;
  private Tn5250Client client = new Tn5250Client();

  @Before
  public void setup() throws Exception {
    server = new VirtualTcpService(VIRTUAL_SERVER_PORT);
    server.start();
  }

  @After
  public void teardown() throws Exception {
    try {
      client.disconnect();
    } catch (Exception e) {
      LOG.warn("Problem disconnecting client", e);
    } finally {
      server.stop(SERVER_STOP_TIMEOUT);
    }
  }

  @Test
  public void shouldGetWelcomeScreenWhenConnect() throws Exception {
    loadLoginInvalidCredsFlow();
    connectToVirtualService();
    assertThat(client.getScreen())
        .isEqualTo(getFileContent("login-welcome-screen.txt"));
  }

  private void loadLoginInvalidCredsFlow() throws FileNotFoundException {
    loadFlow("login-invalid-creds.yml");
  }

  private void loadFlow(String flowFile) throws FileNotFoundException {
    File file = new File(findResource(flowFile).getFile());
    server.setFlow(Flow.fromYml(file));
  }

  private URL findResource(String file) {
    return getClass().getResource(file);
  }

  private void connectToVirtualService() throws Exception {
    client.connect(VIRTUAL_SERVER_HOST, VIRTUAL_SERVER_PORT, SSLType.NONE, TerminalType.IBM_3477_FC,
        TIMEOUT_MILLIS, STABLE_TIMEOUT_MILLIS);
  }

  private String getFileContent(String file) throws IOException {
    return Resources.toString(findResource(file), Charsets.UTF_8);
  }

  @Test
  public void shouldGetWelcomeScreenWhenConnectWithSsl() throws Exception {
    server.stop(SERVER_STOP_TIMEOUT);
    SSLSocketFactory.setKeyStore(findResource("/.keystore").getFile());
    SSLSocketFactory.setKeyStorePassword("changeit");
    server = new VirtualTcpService(VIRTUAL_SERVER_PORT, SSLType.TLS);
    server.start();
    loadLoginInvalidCredsFlow();
    client.connect(VIRTUAL_SERVER_HOST, VIRTUAL_SERVER_PORT, SSLType.TLS, TerminalType.IBM_3477_FC,
        TIMEOUT_MILLIS, STABLE_TIMEOUT_MILLIS);
    assertThat(client.getScreen())
        .isEqualTo(getFileContent("login-welcome-screen.txt"));
  }

  @Test(expected = RteIOException.class)
  public void shouldThrowRteIOExceptionWhenConnectWithInvalidPort() throws Exception {
    client
        .connect(VIRTUAL_SERVER_HOST, 2222, SSLType.NONE, TerminalType.IBM_3477_FC, TIMEOUT_MILLIS,
            STABLE_TIMEOUT_MILLIS);
  }

  @Test(expected = TimeoutException.class)
  public void shouldThrowTimeoutExceptionWhenConnectAndServerIsTooSlow() throws Exception {
    loadFlow("slow-welcome-screen.yml");
    connectToVirtualService();
  }

  @Test
  public void shouldGetInvalidCredentialsScreenWhenSendInvalidCreds() throws Exception {
    loadLoginInvalidCredsFlow();
    connectToVirtualService();
    sendInvalidCredsWithSyncWait();
    assertThat(client.getScreen())
        .isEqualTo(getFileContent("login-invalid-creds.txt"));
  }

  private void sendInvalidCredsWithSyncWait() throws Exception {
    client.send(buildInvalidCredsFields(), Action.ENTER);
    client.await(
        Collections.singletonList(new SyncWaitCondition(TIMEOUT_MILLIS, STABLE_TIMEOUT_MILLIS)));
  }

  private List<CoordInput> buildInvalidCredsFields() {
    return Arrays.asList(
        new CoordInput(new Position(7, 53), "TEST"),
        new CoordInput(new Position(9, 53), "PASS"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldThrowIllegalArgumentExceptionWhenSendIncorrectFieldPosition() throws Exception {
    loadLoginInvalidCredsFlow();
    connectToVirtualService();
    List<CoordInput> input = Collections.singletonList(
        new CoordInput(new Position(7, 1), "TEST"));
    client.send(input, Action.ENTER);
  }

  @Test(expected = RteIOException.class)
  public void shouldThrowRteIOExceptionWhenSendAndServerDown() throws Exception {
    loadLoginInvalidCredsFlow();
    connectToVirtualService();
    server.stop(SERVER_STOP_TIMEOUT);
    sendInvalidCredsWithSyncWait();
  }

  @Test(expected = UnsupportedOperationException.class)
  public void shouldThrowUnsupportedOperationExceptionWhenAwaitWithUndefinedCondition()
      throws Exception {
    loadLoginInvalidCredsFlow();
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
    client.send(buildInvalidCredsFields(), Action.ENTER);
    client.await(
        Collections.singletonList(new SyncWaitCondition(TIMEOUT_MILLIS, STABLE_TIMEOUT_MILLIS)));
  }

  @Test(expected = TimeoutException.class)
  public void shouldThrowTimeoutExceptionWhenCursorWaitAndNotExpectedCursorPosition()
      throws Exception {
    loadLoginInvalidCredsFlow();
    connectToVirtualService();
    client.send(buildInvalidCredsFields(), Action.ENTER);
    client.await(Collections.singletonList(
        new CursorWaitCondition(new Position(1, 1), TIMEOUT_MILLIS, STABLE_TIMEOUT_MILLIS)));
  }

  @Test(expected = TimeoutException.class)
  public void shouldThrowTimeoutExceptionWhenSilentWaitAndChattyServer() throws Exception {
    loadFlow("chatty-server.yml");
    connectToVirtualService();
    client.send(buildInvalidCredsFields(), Action.ENTER);
    client.await(
        Collections.singletonList(new SilentWaitCondition(TIMEOUT_MILLIS, STABLE_TIMEOUT_MILLIS)));
  }

  @Test(expected = TimeoutException.class)
  public void shouldThrowTimeoutExceptionWhenTextWaitWithNoMatchingRegex()
      throws Exception {
    loadLoginInvalidCredsFlow();
    connectToVirtualService();
    client.send(buildInvalidCredsFields(), Action.ENTER);
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
    loadLoginInvalidCredsFlow();
    connectToVirtualService();
    sendInvalidCredsWithSyncWait();
    client.disconnect();
    connectToVirtualService();
    assertThat(client.getScreen())
        .isEqualTo(getFileContent("login-welcome-screen.txt"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldThrowIllegalArgumentExceptionWhenSendAfterDisconnected() throws Exception {
    loadLoginInvalidCredsFlow();
    connectToVirtualService();
    client.disconnect();
    sendInvalidCredsWithSyncWait();
  }

  @Test
  public void shouldNotThrowExceptionWhenDisconnectAndServerDown() throws Exception {
    loadLoginInvalidCredsFlow();
    connectToVirtualService();
    server.stop(SERVER_STOP_TIMEOUT);
    client.disconnect();
  }

}
