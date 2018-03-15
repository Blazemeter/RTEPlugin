package com.blazemeter.jmeter.rte.protocols.tn5250.listeners;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.blazemeter.jmeter.rte.core.Position;
import com.blazemeter.jmeter.rte.core.wait.CursorWaitCondition;
import com.google.common.base.Stopwatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class VisibleCursorListenerIT extends Tn5250ConditionWaiterIT {

  private static final Position DEFAULT_CURSOR_POSITION = new Position(7, 53);

  @Override
  protected Tn5250ConditionWaiter<?> buildConditionWaiter() {
    return new VisibleCursorListener(
        new CursorWaitCondition(DEFAULT_CURSOR_POSITION, TIMEOUT_MILLIS, STABLE_MILLIS),
        client,
        stableTimeoutExecutor);
  }

  @Before
  public void setup() throws Exception {
    super.setup();
    when(emulator.isCursorVisible()).thenReturn(true);
    when(emulator.getCursorRow()).thenReturn(DEFAULT_CURSOR_POSITION.getRow());
    when(emulator.getCursorCol()).thenReturn(DEFAULT_CURSOR_POSITION.getColumn());
  }

  @Test
  public void shouldUnblockAfterReceivingExpectedCursorPosition() throws Exception {
    long unlockDelayMillis = 500;
    Stopwatch waitTime = Stopwatch.createStarted();
    startSingleEventGenerator(unlockDelayMillis, buildStateChangeGenerator());
    listener.await();
    assertThat(waitTime.elapsed(TimeUnit.MILLISECONDS)).isGreaterThanOrEqualTo(unlockDelayMillis);
  }

  @Test(expected = TimeoutException.class)
  public void shouldThrowTimeoutExceptionWhenReceivedUnexpectedCursorPosition() throws Exception {
    when(emulator.getCursorRow()).thenReturn(DEFAULT_CURSOR_POSITION.getRow() + 1);
    startSingleEventGenerator(0, buildStateChangeGenerator());
    listener.await();
  }

  @Test(expected = TimeoutException.class)
  public void shouldThrowTimeoutExceptionWhenNoVisibleCursorPosition() throws Exception {
    when(emulator.isCursorVisible()).thenReturn(false);
    startSingleEventGenerator(0, buildStateChangeGenerator());
    listener.await();
  }

  @Test(expected = TimeoutException.class)
  public void shouldThrowTimeoutExceptionWhenCursorMovesWhileStablePeriod() throws Exception {
    setupCursorRepositioningEmulator();
    startPeriodicEventGenerator(buildStateChangeGenerator());
    listener.await();
  }

  private void setupCursorRepositioningEmulator() {
    when(emulator.getCursorRow()).thenAnswer(new Answer<Integer>() {
      private boolean returnDefaultRow = true;

      @Override
      public Integer answer(InvocationOnMock invocation) {
        returnDefaultRow = !returnDefaultRow;
        return returnDefaultRow
            ? DEFAULT_CURSOR_POSITION.getRow() : DEFAULT_CURSOR_POSITION.getRow() + 1;
      }
    });
  }
}
