package com.blazemeter.jmeter.rte.protocols.vt420.listeners;

import static org.mockito.Mockito.when;

import com.blazemeter.jmeter.rte.core.Position;
import com.blazemeter.jmeter.rte.core.wait.CursorWaitCondition;
import java.util.Optional;
import java.util.concurrent.TimeoutException;
import nl.lxtreme.jvt220.terminal.ScreenChangeListener;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class VisibleCursorListenerIT extends Vt420ConditionWaiterIT {

  private static final Position EXPECTED_CURSOR_POSITION = new Position(12, 42);
  private static final Position DEFAULT_CURSOR_POSITION = new Position(1, 1);

  @Override
  protected Vt420ConditionWaiter<?> buildConditionWaiter() {
    return new VisibleCursorListener(
        new CursorWaitCondition(EXPECTED_CURSOR_POSITION, TIMEOUT_MILLIS, STABLE_MILLIS),
        client,
        stableTimeoutExecutor,
        exceptionHandler);
  }

  @Override
  public void setup() throws Exception {
    when(client.getCursorPosition()).thenReturn(Optional.of(DEFAULT_CURSOR_POSITION));
    super.setup();
  }

  @Test
  public void shouldUnblockWhenCursorAlreadyInExpectedPosition() throws Exception {
    setupExpectedCursorPosition();
    super.setup();
    listener.await();
  }

  private void setupExpectedCursorPosition() {
    when(client.getCursorPosition()).thenReturn(Optional.of(EXPECTED_CURSOR_POSITION));
  }

  private Runnable buildStateChangeGenerator() {
    return () -> ((ScreenChangeListener) listener)
        .screenChanged("event");
  }

  @Test(expected = TimeoutException.class)
  public void shouldThrowTimeoutExceptionWhenCursorIsNotInExpectedPosition()
      throws Exception {
    buildStateChangeGenerator().run();
    listener.await();
  }

  @Test
  public void shouldUnlockWhenCursorChangePositionThroughNewEventArrived()
      throws Exception {
    setupExpectedCursorPosition();
    startSingleEventGenerator(Long.divideUnsigned(STABLE_MILLIS, 4), buildStateChangeGenerator());
    listener.await();
  }

  @Test(expected = TimeoutException.class)
  public void shouldThrowTimeoutExceptionWhenEventsArriveInPeriodsLowerThanStablePeriod()
      throws Exception {
    setupCursorRepositioningEmulator();
    startPeriodicEventGenerator(buildStateChangeGenerator());
    listener.await();
  }

  private void setupCursorRepositioningEmulator() {
    when(client.getCursorPosition()).thenAnswer(new Answer<Position>() {
      private boolean mockToExpectedPos = true;

      @Override
      public Position answer(InvocationOnMock invocation) {
        mockToExpectedPos = !mockToExpectedPos;
        return mockToExpectedPos
            ? EXPECTED_CURSOR_POSITION : DEFAULT_CURSOR_POSITION;
      }
    });
  }

}
