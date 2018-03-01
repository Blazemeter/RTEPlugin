package com.blazemeter.jmeter.rte.protocols.tn5250;

import static org.assertj.core.api.Assertions.assertThat;

import com.blazemeter.jmeter.rte.core.CoordInput;
import com.blazemeter.jmeter.rte.core.Position;
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
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Integration tests for {@link Tn5250Client}.
 */
public class Tn5250ClientIT {

  private static final int SERVER_PORT = 2323;

  private VirtualTcpService server;
  private Tn5250Client client = new Tn5250Client();

  @Before
  public void setup() throws Exception {
    server = new VirtualTcpService(SERVER_PORT);
    server.start();
  }

  @After
  public void teardown() throws Exception {
    server.stop(TimeUnit.SECONDS.toMillis(10));
  }

  @Test
  public void shouldGetInvalidCredentialsScreenWhenSendInvalidCreds() throws Exception {
    loadFlow("login-invalid-creds.yml");
    client.connect("localhost", SERVER_PORT, TerminalType.IBM_3477_FC);
    try {
      List<CoordInput> input = Arrays.asList(
          new CoordInput(new Position(53, 7), "TEST"),
          new CoordInput(new Position(53, 9), "PASS"));
      String screen = client.send(input);
      assertThat(screen)
          .isEqualTo(getFileContent("login-invalid-creds.txt"));
    } finally {
      client.disconnect();
    }
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

}
