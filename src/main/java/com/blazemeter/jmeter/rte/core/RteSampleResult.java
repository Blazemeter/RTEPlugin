package com.blazemeter.jmeter.rte.core;

import com.blazemeter.jmeter.rte.core.ssl.SSLType;

import org.apache.jmeter.samplers.SampleResult;

public class RteSampleResult extends SampleResult {
  
  private final String server;
  private final int port;
  private final Protocol protocol;
  private final TerminalType terminalType;
  private final SSLType sslType;
  private String label;
  
  private RteSampleResult(Builder builder) {
    server = builder.server;
    port = builder.port;
    protocol = builder.protocol;
    terminalType = builder.terminalType;
    sslType = builder.sslType;
    label = builder.label;
  }
  
  public String getServer() {
    return server;
  }

  public int getPort() {
    return port;
  }

  public Protocol getProtocol() {
    return protocol;
  }

  public TerminalType getTerminalType() {
    return terminalType;
  }

  public SSLType getSslType() {
    return sslType;
  }
  
  public String getLabel() {
    return label;
  }
  
  public String getRequestHeaders() {
    return "Server: " + getServer() + "\n" +
        "Port: " + getPort() + "\n" +
        "Protocol: " + getProtocol().toString() + "\n" +
        "Terminal-type: " + getTerminalType() + "\n" +
        "Security: " + getSslType() + "\n";
  }
  
  //TODO Must finish getRequestBody ASAP
  
  public String getRequestBody() {
    return "AttentionKey: " +
        //getAttentionKey() +
        "\n" +
        "Inputs:\n" +
        /* getInputs().stream()
            .map(Input::getCsv)
            .collect(Collectors.joining("\n")) + */ 
        "\n";
  }
  
  public static final class Builder {
    private String server;
    private int port;
    private Protocol protocol;
    private TerminalType terminalType;
    private SSLType sslType;
    private String label;

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

    public RteSampleResult build() {
      RteSampleResult ret  = new RteSampleResult(this);
      ret.setLabel("name");
      ret.sampleStart();
      return ret;
    }
  }

  public void setLabel(String label) {
    this.label = label;
  }
}
