package com.blazemeter.jmeter.rte.protocols.tn5250;

import com.blazemeter.jmeter.rte.core.CoordInput;
import com.blazemeter.jmeter.rte.core.RteProtocolClient;
import java.awt.event.KeyEvent;
import java.util.List;
import net.infordata.em.crt5250.XI5250Field;

public class Tn5250Client implements RteProtocolClient {

  private final ConfigurablePortEmulator em = new ConfigurablePortEmulator();

  public void connect(String server, int port) {
    em.setHost(server);
    em.setPort(port);
    //TODO To-do: support the other terminal-type option (IBM-3477-FC)
    em.setTerminalType("IBM-3179-2"); //This is the default option.
    em.setActive(true);
  }

  public String send(List<CoordInput> input) throws InterruptedException {
    input.forEach(s -> {
      /*
      The values for row and column in getFieldFromPos are zero-indexed so we need to translate the
      core input values which are one-indexed.
       */
      XI5250Field field = em
          .getFieldFromPos(s.getPosition().getColumn() - 1, s.getPosition().getRow() - 1);
      field.setString(s.getInput());
    });
    sendSpecialKey(KeyEvent.VK_ENTER);
    //TODO: Replace with waiters
    Thread.sleep(3000); //Doing this "wait" to avoid getting empty screen.
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
        new KeyEvent(em, KeyEvent.KEY_PRESSED, 0, 0, specialKey, KeyEvent.CHAR_UNDEFINED));
  }

  public void disconnect() {
    em.setActive(false);
  }

}
