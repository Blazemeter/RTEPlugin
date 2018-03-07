package blazemeter.jmeter.plugins.RTEPlugin.test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import com.blazemeter.jmeter.rte.core.Protocol;
import com.blazemeter.jmeter.rte.core.RteProtocolClient;
import com.blazemeter.jmeter.rte.core.SSLType;
import com.blazemeter.jmeter.rte.core.TerminalType;
import com.blazemeter.jmeter.rte.sampler.CoordInputRowGUI;
import com.blazemeter.jmeter.rte.sampler.Inputs;
import com.blazemeter.jmeter.rte.sampler.RTESampler;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.concurrent.TimeoutException;
import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.samplers.SampleResult;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class RTESamplerTest {

  @Mock
  private RteProtocolClient rteProtocolClientMock;
  private RTESampler rteSampler;
  private ConfigTestElement configTestElement = new ConfigTestElement();

  @Before
  public void setup() throws Exception {
    rteSampler = new RTESampler(p -> rteProtocolClientMock);
  }

  @After
  public void teardown() throws Exception {
    rteSampler.threadFinished();
  }

  @Test
  public void shouldGetErrorSamplerResultWhenGetClientThrowTimeoutException() throws Exception {
    TimeoutException e = new TimeoutException();
    throwConnectException(e);
  }

  @Test
  public void shouldGetErrorSamplerResultWhenGetClientThrowInterruptedException() throws Exception {
    InterruptedException e = new InterruptedException();
    throwConnectException(e);
  }

  private void throwConnectException(Exception e) throws Exception{
    createRTEConfig("server", 23, RTESampler.DEFAULT_TERMINAL_TYPE, RTESampler.DEFAULT_PROTOCOL,
        "user", "pass", RTESampler.DEFAULT_SSLTYPE, "0");
    rteSampler.addTestElement(configTestElement);
    doThrow(e).when(rteProtocolClientMock)
        .connect(any(), anyInt(), any(), any(), anyLong(), anyLong());
    SampleResult result = rteSampler.sample(null);
    SampleResult expected = createExpectedErrorResult(e);

    assertThat(result)
        .isEqualToComparingOnlyGivenFields(expected, "sampleLabel", "dataType", "responseCode",
            "responseMessage", "responseData", "successful");
  }

  @Test
  public void shouldGetErrorSamplerResultWhenSendThrowIllegalArgumentException() throws Exception {
    IllegalArgumentException e = new IllegalArgumentException();
    throwSendException(e);
  }

  @Test
  public void shouldGetErrorSamplerResultWhenSendThrowInterruptedException() throws Exception {
    InterruptedException e = new InterruptedException();
    throwSendException(e);

  }

  private void throwSendException(Exception e) throws Exception{
    createRTEConfig("server", 23, RTESampler.DEFAULT_TERMINAL_TYPE, RTESampler.DEFAULT_PROTOCOL,
        "user", "pass", RTESampler.DEFAULT_SSLTYPE, "0");
    rteSampler.addTestElement(configTestElement);
    rteSampler.setPayload(createInputs(1));
    doThrow(e).when(rteProtocolClientMock)
        .send(any(), any(), any());
    SampleResult result = rteSampler.sample(null);
    SampleResult expected = createExpectedErrorResult(e);

    assertThat(result)
        .isEqualToComparingOnlyGivenFields(expected, "sampleLabel", "dataType", "responseCode",
            "responseMessage", "responseData", "successful");
  }

  @Test
  public void shouldGetSuccessfulSamplerResultWhenSend() throws Exception {
    createRTEConfig("server", 23, RTESampler.DEFAULT_TERMINAL_TYPE, RTESampler.DEFAULT_PROTOCOL,
        "user", "pass", RTESampler.DEFAULT_SSLTYPE, "0");
    rteSampler.addTestElement(configTestElement);
    rteSampler.setPayload(createInputs(1));
    String response = "Response";
    when(rteProtocolClientMock.getScreen()).thenReturn(response);
    SampleResult result = rteSampler.sample(null);
    SampleResult expected = createExpectedSuccessfulResult(response);
    assertThat(result)
        .isEqualToComparingOnlyGivenFields(expected, "sampleLabel", "dataType", "responseData", "successful");
  }

  private Inputs createInputs(int amount) {
    Inputs ret = new Inputs();
    for (int i = 0 ; i<amount; i++ ){
      CoordInputRowGUI c = new CoordInputRowGUI(i, i+1, "input_" + i);
      ret.addCoordInput(c);
    }
    return ret;
  }

  private void createRTEConfig(String server, int port, TerminalType terminalType,
      Protocol protocol,
      String user, String pass, SSLType sslType, String connectionTimeout) {
    configTestElement.setProperty(RTESampler.CONFIG_SERVER, server);
    configTestElement.setProperty(RTESampler.CONFIG_PORT, port);
    configTestElement
        .setProperty(RTESampler.CONFIG_TERMINAL_TYPE, terminalType.name());
    configTestElement.setProperty(RTESampler.CONFIG_PROTOCOL, protocol.name());
    configTestElement.setProperty(RTESampler.CONFIG_USER, user);
    configTestElement.setProperty(RTESampler.CONFIG_PASS, pass);
    configTestElement.setProperty(RTESampler.CONFIG_SSL_TYPE, sslType.name());
    configTestElement.setProperty(RTESampler.CONFIG_CONNECTION_TIMEOUT, connectionTimeout);
  }

  private SampleResult createExpectedErrorResult(Exception e) {
    SampleResult expected = new SampleResult();
    expected.setSampleLabel(rteSampler.getName());
    StringWriter sw = new StringWriter();
    e.printStackTrace(new PrintWriter(sw));
    expected.setDataType(SampleResult.TEXT);
    expected.setResponseCode(e.getClass().getName());
    expected.setResponseMessage(e.getMessage());
    expected.setResponseData(sw.toString(), SampleResult.DEFAULT_HTTP_ENCODING);
    expected.setSuccessful(false);
    return expected;
  }

  private SampleResult createExpectedSuccessfulResult(String responseData) {
    SampleResult expected = new SampleResult();
    expected.setSampleLabel(rteSampler.getName());
    expected.setDataType(SampleResult.TEXT);
    expected.setResponseData(responseData, "utf-8");
    expected.setSuccessful(true);
    return expected;
  }
}
