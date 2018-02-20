package com.blazemeter.jmeter.rte.protocols;

import com.blazemeter.jmeter.rte.core.CoordInput;
import com.blazemeter.jmeter.rte.core.RteProtocolClientListener;
import net.infordata.em.crt5250.XI5250Field;
import net.infordata.em.tn5250.XI5250Emulator;

import java.awt.event.KeyEvent;
import java.util.List;

public class Tn5250Client implements RteProtocolClientListener {

    private final XI5250Emulator em = new XI5250Emulator();

    public void connect(String server, int port) {
        em.setHost(server);
        em.setTerminalType("IBM-3179-2"); //This is the default option. To-do: support the other terminal-type option (IBM-3477-FC)
        em.setActive(true);
    }

    public String send(List<CoordInput> input) throws InterruptedException {
        for (int i = 0; i < input.size(); i++) {
            int fieldCol = input.get(i).getPosition().getColumn();
            int fieldRow = input.get(i).getPosition().getRow();
            //The values for row and column in getFieldFromPos are zero-indexed so we need to translate the core
            //input values which are one-indexed.
            XI5250Field field = em.getFieldFromPos(fieldCol - 1, fieldRow - 1);
            field.setString(input.get(i).getInput());
        }
        sendSpecialKey(KeyEvent.VK_ENTER);
        Thread.sleep(3000); //Doing this "wait" to avoid getting empty screen. To-do: Replace with waiters
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
