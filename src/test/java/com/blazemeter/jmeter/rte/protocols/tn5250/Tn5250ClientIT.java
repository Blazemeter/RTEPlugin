package com.blazemeter.jmeter.rte.protocols.tn5250;

import static org.assertj.core.api.Assertions.assertThat;

import com.blazemeter.jmeter.rte.core.CoordInput;
import com.blazemeter.jmeter.rte.core.Position;
import com.blazemeter.jmeter.rte.core.RteIOException;
import com.blazemeter.jmeter.rte.core.TerminalType;
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
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class Tn5250ClientIT {

  private static final String VIRTUAL_SERVER_HOST = "localhost";
  private static final int VIRTUAL_SERVER_PORT = 2323;
  private static final int CONNECTION_TIMEOUT_MILLIS = 5000;
  private static final int STABLE_TIMEOUT_MILLIS = 1000;

  private VirtualTcpService server;
  private Tn5250Client client = new Tn5250Client();

  @Before
  public void setup() throws Exception {
    server = new VirtualTcpService(VIRTUAL_SERVER_PORT);
    server.start();
  }

  @After
  public void teardown() throws Exception {
    client.disconnect();
    server.stop(TimeUnit.SECONDS.toMillis(10));
  }

  @Test
  public void shouldGetInvalidCredentialsScreenWhenSendInvalidCreds() throws Exception {
    loadFlow("login-invalid-creds.yml");
    connectToVirtualService();
    List<CoordInput> input = Arrays.asList(
        new CoordInput(new Position(7, 53), "TEST"),
        new CoordInput(new Position(9, 53), "PASS"));
    String screen = client.send(input);
    assertThat(screen)
        .isEqualTo(getFileContent("login-invalid-creds.txt"));
  }

  private void connectToVirtualService() throws InterruptedException, TimeoutException {
    client.connect(VIRTUAL_SERVER_HOST, VIRTUAL_SERVER_PORT, TerminalType.IBM_3477_FC,
        CONNECTION_TIMEOUT_MILLIS, STABLE_TIMEOUT_MILLIS);
  }

  private void loadFlow(String flowFile) throws FileNotFoundException {
    File file = new File(findResource(flowFile).getFile());
    server.setFlow(Flow.fromYml(file));
  }

  private URL findResource(String file) {
    return getClass().getResource(file);
  }

  private String getFileContent(String file) throws IOException {
    return Resources.toString(findResource(file), Charsets.UTF_8);
  }

  @Test(expected = RteIOException.class)
  public void shouldThrowRteIOExceptionWhenConnectWithInvalidPort() throws Exception {
    client.connect(VIRTUAL_SERVER_HOST, 2222, TerminalType.IBM_3477_FC, CONNECTION_TIMEOUT_MILLIS,
        STABLE_TIMEOUT_MILLIS);
  }

  @Test(expected = TimeoutException.class)
  public void shouldThrowTimeoutExceptionWhenConnectAndServerIsTooSlow() throws Exception {
    loadFlow("slow-response.yml");
    connectToVirtualService();
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldThrowIllegalArgumentExceptionWhenSendIncorrectFieldPosition() throws Exception {
    loadFlow("login-invalid-creds.yml");
    connectToVirtualService();
    List<CoordInput> input = Collections.singletonList(
        new CoordInput(new Position(7, 1), "TEST"));
    client.send(input);
  }

}
