package com.blazemeter.jmeter.rte.core;

import com.blazemeter.jmeter.rte.core.ssl.SSLType;
import com.blazemeter.jmeter.rte.sampler.Action;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.jmeter.samplers.SampleResult;

public class RteSampleResult extends SampleResult {
  
  private final String server;
  private final int port;
  private final Protocol protocol;
  private final TerminalType terminalType;
  private final SSLType sslType;
  private List<Input> inputs;
  private AttentionKey attentionKey;
  private String screen;
  private boolean inputInhibitedRequest;
  private boolean inputInhibitedResponse;
  private Position cursorPosition;
  private boolean soundedAlarm;
  private Action action;
  
  private RteSampleResult(Builder builder) {
    server = builder.server;
    port = builder.port;
    protocol = builder.protocol;
    terminalType = builder.terminalType;
    sslType = builder.sslType;
    action = builder.action;
  }
  
  public void setInputs(List<Input> inputs) {
    this.inputs = inputs;
  }

  public void setAttentionKey(AttentionKey attentionKey) {
    this.attentionKey = attentionKey;
  }

  public void setScreen(String screen) {
    this.screen = screen;
  }

  public void setInputInhibitedRequest(boolean inputInhibitedRequest) {
    this.inputInhibitedRequest = inputInhibitedRequest;
  }

  public void setInputInhibitedResponse(boolean inputInhibitedResponse) {
    this.inputInhibitedResponse = inputInhibitedResponse;
  }

  public void setCursorPosition(Position cursorPosition) {
    this.cursorPosition = cursorPosition;
  }

  public void setSoundedAlarm(boolean soundedAlarm) {
    this.soundedAlarm = soundedAlarm;
  }
  
  @Override
  public String getRequestHeaders() {
    return "Server: " + server + "\n" +
        "Port: " + port + "\n" +
        "Protocol: " + protocol + "\n" +
        "Terminal-type: " + terminalType + "\n" +
        "Security: " + sslType + "\n" +
        "Action: " + action + "\n" + 
        "Input-inhibited: " + inputInhibitedRequest;
  }

  @Override
  public String getSamplerData() {
    return new StringBuilder()
        .append("AttentionKey: ")
        .append(attentionKey)
        .append("\n")
        .append("Inputs:\n")
        .append(inputs.stream()
            .map(Input::getCsv)
            .collect(Collectors.joining("\n")))
        .append("\n")
        .toString();
  }
 
  @Override
  public String getResponseHeaders() {
    return "Input-inhibited: " + inputInhibitedResponse + "\n" +
        "Cursor-position: " + cursorPosition +
        (soundedAlarm ? "\nSound-Alarm: true" : "");
  }

  @Override
  public String getResponseDataAsString() {
    return "Screen: " + screen + "\n";
    
  }

  public static final class Builder {
    private String server;
    private int port;
    private Protocol protocol;
    private TerminalType terminalType;
    private SSLType sslType;
    private String label;
    private Action action;
    
    public Builder() {
    }

    public Builder withServer(String val) {
      server = val;
      return this;
    }

    public Builder withPort(int val) {
      port = val;
      return this;
    }

    public Builder withProtocol(Protocol val) {
      protocol = val;
      return this;
    }

    public Builder withTerminalType(TerminalType val) {
      terminalType = val;
      return this;
    }

    public Builder withSslType(SSLType val) {
      sslType = val;
      return this;
    }
    
    public Builder withLabel(String name) {
      label = name;
      return this;
    }

    public Builder withAction(Action val) {
      action = val;
      return this;
    }

    public RteSampleResult build() {
      RteSampleResult ret  = new RteSampleResult(this);
      ret.setSampleLabel(label);
      return ret;
    }
  }
  
}
