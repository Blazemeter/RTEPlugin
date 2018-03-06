package com.blazemeter.jmeter.rte.protocols.tn5250;

import com.blazemeter.jmeter.rte.core.CoordInput;
import com.blazemeter.jmeter.rte.core.RteIOException;
import com.blazemeter.jmeter.rte.core.RteProtocolClient;
import com.blazemeter.jmeter.rte.core.TerminalType;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeoutException;
import net.infordata.em.crt5250.XI5250Field;

public class Tn5250Client implements RteProtocolClient {

  private final ConfigurablePortEmulator em = new ConfigurablePortEmulator();
  private ScheduledExecutorService stableTimeoutExecutor;

  @Override
  public boolean isConnected() {
    return em.isActive();
  }

  @Override
  public void connect(String server, int port, SSLData sslData,
                      TerminalType terminalType, long timeoutMillis,
                      long stableTimeoutMillis)
      throws RteIOException, InterruptedException, TimeoutException {
    stableTimeoutExecutor = Executors.newSingleThreadScheduledExecutor();
    em.setHost(server);
    em.setPort(port);
    em.setTerminalType(terminalType.getType());
    em.setSslData(sslData);
    UnlockListener unlock = new UnlockListener(timeoutMillis, stableTimeoutMillis,
        stableTimeoutExecutor);
    em.addEmulatorListener(unlock);
    try {
      em.setActive(true);
      em.throwAnyPendingError();
      unlock.await();
    } finally {
      em.throwAnyPendingError();
      em.removeEmulatorListener(unlock);
    }
  }

  @Override
  public String send(List<CoordInput> input) throws InterruptedException {
    input.forEach(s -> {
      /*
      The values for row and column in getFieldFromPos are zero-indexed so we need to translate the
      core input values which are one-indexed.
       */
      XI5250Field field = em.getFieldFromPos(s.getPosition().getColumn() - 1,
          s.getPosition().getRow() - 1);
      if (field == null) {
        throw new IllegalArgumentException(
            "No field at row " + s.getPosition().getRow() + " and column " + s.getPosition()
                .getColumn());
      }
      field.setString(s.getInput());
    });
    sendSpecialKey(KeyEvent.VK_ENTER);
    //TODO: Replace with waiters
    Thread.sleep(3000); //Doing this "wait" to avoid getting empty screen.
    em.throwAnyPendingError();
    return getScreen();
  }

  private String getScreen() {
    int height = em.getCrtSize().height;
    int width = em.getCrtSize().width;
    StringBuilder screen = new StringBuilder();
    for (int i = 0; i < height; i++) {
      screen.append(em.getString(0, i, width));
      screen.append("\n");
    }
    return screen.toString();
  }

  private void sendSpecialKey(int specialKey) {
    em.processRawKeyEvent(
        new KeyEvent(em, KeyEvent.KEY_PRESSED, 0, 0, specialKey,
            KeyEvent.CHAR_UNDEFINED));
  }

  @Override
  public void disconnect() {
    stableTimeoutExecutor.shutdown();
    em.setActive(false);
    em.throwAnyPendingError();
  }

}
