package com.blazemeter.jmeter.rte.protocols.tn3270;

import static org.assertj.core.api.Assertions.assertThat;

import com.blazemeter.jmeter.rte.core.Action;
import com.blazemeter.jmeter.rte.core.CoordInput;
import com.blazemeter.jmeter.rte.core.InvalidFieldPositionException;
import com.blazemeter.jmeter.rte.core.Position;
import com.blazemeter.jmeter.rte.core.TerminalType;
import com.blazemeter.jmeter.rte.protocols.RteProtocolClientIT;
import java.io.FileNotFoundException;
import java.util.Collections;
import java.util.List;
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
    loadLoginAndStatsFlow();
    connectToVirtualService();
    assertThat(client.getScreen())
        .isEqualTo(getFileContent("login-welcome-screen.txt"));
  }

  private void loadLoginAndStatsFlow() throws FileNotFoundException {
    loadFlow("login-and-stats.yml");
  }

  @Test
  public void shouldGetUserMenuScreenWhenSendUsername() throws Exception {
    loadLoginAndStatsFlow();
    connectToVirtualService();
    sendUsernameWithSyncWait();
    assertThat(client.getScreen())
        .isEqualTo(getFileContent("user-menu-screen.txt"));
  }

  private void sendUsernameWithSyncWait() throws Exception {
    client.send(buildUsernameField(), Action.ENTER);
    //TODO: replace with actual client.await when the feature is implemented
    Thread.sleep(3000);
  }

  private List<CoordInput> buildUsernameField() {
    return Collections.singletonList(new CoordInput(new Position(2, 1), "testusr"));
  }

  @Test(expected = InvalidFieldPositionException.class)
  public void shouldThrowInvalidFieldPositionExceptionWhenSendIncorrectFieldPosition()
      throws Exception {
    loadLoginAndStatsFlow();
    connectToVirtualService();
    List<CoordInput> input = Collections.singletonList(
        new CoordInput(new Position(81, 1), "TEST"));
    client.send(input, Action.ENTER);
  }

  @Test
  public void shouldGetWelcomeScreenWhenConnectAfterDisconnectInvalidCreds() throws Exception {
    loadLoginAndStatsFlow();
    connectToVirtualService();
    sendUsernameWithSyncWait();
    client.disconnect();
    connectToVirtualService();
    assertThat(client.getScreen())
        .isEqualTo(getFileContent("login-welcome-screen.txt"));
  }

  @Test
  public void shouldNotThrowExceptionWhenDisconnectAndServerDown() throws Exception {
    loadLoginAndStatsFlow();
    connectToVirtualService();
    server.stop(SERVER_STOP_TIMEOUT);
    client.disconnect();
  }

}
