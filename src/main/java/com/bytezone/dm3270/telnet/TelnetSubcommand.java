package com.bytezone.dm3270.telnet;

import com.bytezone.dm3270.buffers.AbstractTelnetCommand;
import com.bytezone.dm3270.streams.TelnetState;

/**
 * Class extracted from dm3270 source code but including START_TLS sub-command.
 */
public abstract class TelnetSubcommand extends AbstractTelnetCommand {

  // subcommands
  public static final byte BINARY = 0x00;
  public static final byte TERMINAL_TYPE = 0x18;
  public static final byte EOR = 0x19;
  public static final byte TN3270E = 0x28;
  public static final byte START_TLS = 0x2E;

  protected SubcommandType type;
  protected String value;

  public enum SubcommandType {
    SEND, IS, REPLY, NAME, DEVICE_TYPE, FUNCTIONS, REQUEST
  }

  public TelnetSubcommand(byte[] buffer, int offset, int length, TelnetState telnetState) {
    super(buffer, offset, length, telnetState);
  }

  public String getValue() {
    return value;
  }

  public String getName() {
    return toString();
  }

  @Override
  public String toString() {
    return String.format("Subcommand: %s %s", type, (value == null ? "" : value));
  }

}
