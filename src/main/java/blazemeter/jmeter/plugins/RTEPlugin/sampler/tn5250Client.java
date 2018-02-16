import net.infordata.em.crt5250.XI5250Field;
import net.infordata.em.tn5250.XI5250Emulator;

import java.awt.event.KeyEvent;
import java.util.List;

public class tn5250Client implements RteProtocolClientListener {

    private XI5250Emulator em = new XI5250Emulator();

    public void Connect(String server, int port, String username, String password, SecurityProtocol protocol){
        em.setHost(server);
        em.setTerminalType("IBM-3179-2"); //This is the default option. To-do: support the other terminal-type option (IBM-3477-FC)

        try {
            em.setActive(true);
            }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public String send(List<Coordinput> input, List<RteWaiter> waiters){
        String screen = "";
        try{
        for (int i=0;i<input.size();i++){
            int fieldCol = input.get(i).getPosition().getCol();
            int fieldRow = input.get(i).getPosition().getRow();
            //Warning: the values for row and column in getFieldMethod should be 1 row and 1 column less than
            // the real position of the field in the emulator screen. That's why there's a "-1" on each attribute.
            XI5250Field field = em.getFieldFromPos(fieldCol-1,fieldRow-1);
            field.setString(input.get(i).getInput());
        }

        //Send an "Enter" key to the server
        em.processRawKeyEvent(
                new KeyEvent(em, KeyEvent.KEY_PRESSED,0,0,KeyEvent.VK_ENTER, KeyEvent.CHAR_UNDEFINED));

        //To-do: add waiters

        Thread.sleep(3000);

        //Print the screen:
        int height = em.getCrtSize().height;
        int width = em.getCrtSize().width;

        for (int i=0;i<height;i++){
            screen += em.getString(0,i,width);
            screen += "\n";
        }

        }
        catch (Exception ex){
            ex.printStackTrace();
        }
        return screen;
    }

    public void disconnect(){

        try {
            em.setActive(false);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    public XI5250Emulator getEm(){
        return em;
    }

}
