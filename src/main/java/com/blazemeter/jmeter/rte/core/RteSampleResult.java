package com.blazemeter.jmeter.rte.core;

import com.blazemeter.jmeter.rte.core.ssl.SSLType;
import com.blazemeter.jmeter.rte.sampler.Action;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.jmeter.samplers.SampleResult;

public class RteSampleResult extends SampleResult {

  private String server;
  private int port;
  private Protocol protocol;
  private TerminalType terminalType;
  private SSLType sslType;
  private Action action;
  private Boolean inputInhibitedRequest;
  private List<Input> inputs;
  private AttentionKey attentionKey;
  private boolean inputInhibitedResponse;
  private Position cursorPosition;
  private boolean soundedAlarm;
  private Screen screen;

  public void setServer(String server) {
    this.server = server;
  }

  public void setPort(int port) {
    this.port = port;
  }

  public void setProtocol(Protocol protocol) {
    this.protocol = protocol;
  }

  public void setTerminalType(TerminalType terminalType) {
    this.terminalType = terminalType;
  }

  public void setSslType(SSLType sslType) {
    this.sslType = sslType;
  }

  public void setAction(Action action) {
    this.action = action;
  }

  public void setInputInhibitedRequest(boolean inputInhibitedRequest) {
    this.inputInhibitedRequest = inputInhibitedRequest;
  }

  public void setInputs(List<Input> inputs) {
    this.inputs = inputs;
  }

  public void setAttentionKey(AttentionKey attentionKey) {
    this.attentionKey = attentionKey;
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

  public void setScreen(Screen screen) {
    this.screen = screen;
    setDataType(SampleResult.TEXT);
    setResponseData(screen.toString(), "utf-8");
  }

  @Override
  public String getRequestHeaders() {
    return "Server: " + server + "\n" +
        "Port: " + port + "\n" +
        "Protocol: " + protocol + "\n" +
        "Terminal-type: " + terminalType + "\n" +
        "Security: " + sslType + "\n" +
        "Action: " + action + "\n" +
        (inputInhibitedRequest != null ? "Input-inhibited: " + inputInhibitedRequest + "\n" : "");
  }

  @Override
  public String getSamplerData() {
    if (attentionKey == null) {
      return "";
    }
    return "AttentionKey: " + attentionKey + "\n"
        + "Inputs:\n"
        + inputs.stream()
        .map(Input::getCsv)
        .collect(Collectors.joining("\n"))
        + "\n";
  }

  @Override
  public String getResponseHeaders() {
    return "Input-inhibited: " + inputInhibitedResponse + "\n" +
        "Cursor-position: " + (cursorPosition != null ? cursorPosition.getRow() + ","
        + cursorPosition.getColumn() : "") +
        (soundedAlarm ? "\nSound-Alarm: true" : "");
  }

  @Override
  public String getResponseDataAsString() {
    return screen != null ? screen.toString() : "";
  }

}
