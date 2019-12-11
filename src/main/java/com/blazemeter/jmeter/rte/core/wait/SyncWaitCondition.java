package com.blazemeter.jmeter.rte.core.wait;

/**
 * {@link WaitCondition} to wait for the terminal to get unlocked.
 */
public class SyncWaitCondition extends WaitCondition {

  public static final String NO_INPUT_INHIBITED_LOG_MESSAGE =
      "Start stable period since input is no longer inhibited";
  public static final String INPUT_INHIBITED_LOG_MESSAGE =
      "Cancel stable period since input has been inhibited";

  public SyncWaitCondition(long timeoutMillis, long stableTimeoutMillis) {
    super(timeoutMillis, stableTimeoutMillis);
  }

  @Override
  public String getDescription() {
    return "emulator to be unlocked";
  }

  @Override
  public String toString() {
    return "SyncWaitCondition{" +
        "timeoutMillis=" + timeoutMillis +
        ", stableTimeoutMillis=" + stableTimeoutMillis +
        '}';
  }

}
