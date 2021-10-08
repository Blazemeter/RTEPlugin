package com.blazemeter.jmeter.rte.recorder.emulator;

import com.blazemeter.jmeter.rte.core.AttentionKey;
import com.blazemeter.jmeter.rte.core.Input;
import com.blazemeter.jmeter.rte.core.Position;
import com.blazemeter.jmeter.rte.core.RteProtocolClient;
import com.blazemeter.jmeter.rte.core.Screen;
import com.blazemeter.jmeter.rte.core.Screen.Segment;
import com.blazemeter.jmeter.rte.recorder.emulator.Xtn5250TerminalEmulator.ScreenField;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import javax.swing.JOptionPane;
import net.infordata.em.crt5250.XI5250Crt;
import net.infordata.em.crt5250.XI5250Field;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class XI5250CrtBase<T extends RteProtocolClient> extends XI5250Crt {

  public static final int DEFAULT_ATTR = 32;
  protected static final Map<KeyEventMap, AttentionKey> KEY_EVENTS =
      new HashMap<KeyEventMap, AttentionKey>() {
        {
          put(new KeyEventMap(0, KeyEvent.VK_F1), AttentionKey.F1);
          put(new KeyEventMap(0, KeyEvent.VK_F2), AttentionKey.F2);
          put(new KeyEventMap(0, KeyEvent.VK_F3), AttentionKey.F3);
          put(new KeyEventMap(0, KeyEvent.VK_F4), AttentionKey.F4);
          put(new KeyEventMap(0, KeyEvent.VK_F5), AttentionKey.F5);
          put(new KeyEventMap(0, KeyEvent.VK_F6), AttentionKey.F6);
          put(new KeyEventMap(0, KeyEvent.VK_F7), AttentionKey.F7);
          put(new KeyEventMap(0, KeyEvent.VK_F8), AttentionKey.F8);
          put(new KeyEventMap(0, KeyEvent.VK_F9), AttentionKey.F9);
          put(new KeyEventMap(0, KeyEvent.VK_F10), AttentionKey.F10);
          put(new KeyEventMap(0, KeyEvent.VK_F11), AttentionKey.F11);
          put(new KeyEventMap(0, KeyEvent.VK_F12), AttentionKey.F12);
          put(new KeyEventMap(KeyEvent.SHIFT_MASK, KeyEvent.VK_F1), AttentionKey.F13);
          put(new KeyEventMap(KeyEvent.SHIFT_MASK, KeyEvent.VK_F2), AttentionKey.F14);
          put(new KeyEventMap(KeyEvent.SHIFT_MASK, KeyEvent.VK_F3), AttentionKey.F15);
          put(new KeyEventMap(KeyEvent.SHIFT_MASK, KeyEvent.VK_F4), AttentionKey.F16);
          put(new KeyEventMap(KeyEvent.SHIFT_MASK, KeyEvent.VK_F5), AttentionKey.F17);
          put(new KeyEventMap(KeyEvent.SHIFT_MASK, KeyEvent.VK_F6), AttentionKey.F18);
          put(new KeyEventMap(KeyEvent.SHIFT_MASK, KeyEvent.VK_F7), AttentionKey.F19);
          put(new KeyEventMap(KeyEvent.SHIFT_MASK, KeyEvent.VK_F8), AttentionKey.F20);
          put(new KeyEventMap(KeyEvent.SHIFT_MASK, KeyEvent.VK_F9), AttentionKey.F21);
          put(new KeyEventMap(KeyEvent.SHIFT_MASK, KeyEvent.VK_F10), AttentionKey.F22);
          put(new KeyEventMap(KeyEvent.SHIFT_MASK, KeyEvent.VK_F11), AttentionKey.F23);
          put(new KeyEventMap(KeyEvent.SHIFT_MASK, KeyEvent.VK_F12), AttentionKey.F24);
          put(new KeyEventMap(0, KeyEvent.VK_ENTER), AttentionKey.ENTER);
          put(new KeyEventMap(0, KeyEvent.VK_ESCAPE), AttentionKey.ATTN);
          put(new KeyEventMap(KeyEvent.META_MASK, KeyEvent.VK_F4), AttentionKey.CLEAR);
          put(new KeyEventMap(0, KeyEvent.VK_PAUSE), AttentionKey.CLEAR);
          put(new KeyEventMap(KeyEvent.SHIFT_MASK, KeyEvent.VK_ESCAPE), AttentionKey.SYSRQ);
          put(new KeyEventMap(0, KeyEvent.VK_CONTROL), AttentionKey.RESET);
          put(new KeyEventMap(KeyEvent.CTRL_MASK, KeyEvent.VK_CONTROL), AttentionKey.RESET);
          put(new KeyEventMap(0, KeyEvent.VK_PAGE_DOWN), AttentionKey.ROLL_UP);
          put(new KeyEventMap(0, KeyEvent.VK_PAGE_UP), AttentionKey.ROLL_DN);
          put(new KeyEventMap(KeyEvent.META_MASK, KeyEvent.VK_F1), AttentionKey.PA1);
          put(new KeyEventMap(KeyEvent.META_MASK, KeyEvent.VK_F2), AttentionKey.PA2);
          put(new KeyEventMap(KeyEvent.META_MASK, KeyEvent.VK_F3), AttentionKey.PA3);
          put(new KeyEventMap(KeyEvent.ALT_MASK, KeyEvent.VK_INSERT), AttentionKey.PA1);
          put(new KeyEventMap(KeyEvent.ALT_MASK, KeyEvent.VK_HOME), AttentionKey.PA2);
          put(new KeyEventMap(KeyEvent.SHIFT_MASK, KeyEvent.VK_PAGE_UP), AttentionKey.PA3);
        }
      };
  private static final int SECRET_CREDENTIAL_ATTR = 39;
  private static final Logger LOG = LoggerFactory.getLogger(XI5250CrtBase.class);
  protected Map<Position, String> labelMap = new HashMap<>();
  protected T terminalClient;
  protected StatusPanel statusPanel;
  protected boolean locked = false;
  protected boolean copyPaste = false;
  protected Consumer<Boolean> pasteConsumer;
  private List<TerminalEmulatorListener> terminalEmulatorListeners;
  private String sampleName = "";
  private Set<AttentionKey> supportedAttentionKeys;

  protected abstract List<Input> getInputFields();

  @Override
  protected synchronized void processKeyEvent(KeyEvent e) {
    LOG.debug("Processing key: ({})", e);
    AttentionKey attentionKey = null;
    processCopyOrPaste(e);
    if (isAnyKeyPressedOrControlKeyReleasedAndNotCopy(e)) {
      attentionKey = KEY_EVENTS
          .get(new KeyEventMap(e.getModifiers(), e.getKeyCode()));
      if (attentionKey != null) {
        processAttentionKey(e, attentionKey);
      }
    }
    processBackSpace(e, attentionKey);
  }

  private void processBackSpace(KeyEvent e, AttentionKey attentionKey) {
    if ((!locked && !e.isConsumed()) || attentionKey != null) {
      /*
        By default XI5250Crt only move the cursor when the backspace key is 
        pressed and delete when shift mask is enabled, in this way always delete
       */
      if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
        super.processKeyEvent(
            new KeyEvent(e.getComponent(), e.getID(), e.getWhen(), KeyEvent.SHIFT_MASK,
                e.getKeyCode(), e.getKeyChar(), e.getKeyLocation()));
      } else {
        super.processKeyEvent(e);
      }
      statusPanel
          .updateStatusBarCursorPosition(this.getCursorRow() + 1, this.getCursorCol() + 1);
    }
  }

  protected void processAttentionKey(KeyEvent e, AttentionKey attentionKey) {
    if (isAttentionKeyValid(attentionKey)) {
      List<Input> fields = getInputFields();
      for (TerminalEmulatorListener listener : terminalEmulatorListeners) {
        setKeyboardLock(true);
        listener.onAttentionKey(attentionKey, fields, sampleName);
      }
    } else {
      showUserMessage(attentionKey + " not supported for current protocol");
      e.consume();
    }

  }

  protected void processCopyOrPaste(KeyEvent e) {
    if (e.getID() == KeyEvent.KEY_PRESSED && e.getKeyCode() == KeyEvent.VK_C
        && e.getModifiers() == getMenuShortcutKeyMask()) {
      doCopy();
      e.consume();
      copyPaste = true;
    } else if (e.getID() == KeyEvent.KEY_PRESSED && e.getKeyCode() == KeyEvent.VK_V
        && e.getModifiers() == getMenuShortcutKeyMask() && !locked) {
      makePaste();
      e.consume();
      copyPaste = true;
    } else if (e.getID() == KeyEvent.KEY_RELEASED &&
        e.getKeyCode() == getKeyCodeFromKeyMask(getMenuShortcutKeyMask()) && copyPaste) {
      copyPaste = false;
    }
  }

  private int getMenuShortcutKeyMask() {
    return Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
  }

  private int getKeyCodeFromKeyMask(int keyMask) {
    return keyMask == KeyEvent.META_MASK ? KeyEvent.VK_META : KeyEvent.VK_CONTROL;
  }

  /*
  This is implemented in this way because ctrl key is used in a shortcut to
  copy/paste and the attention key should be triggered only if have not
  executed a copy paste before.
  */

  protected boolean isAnyKeyPressedOrControlKeyReleasedAndNotCopy(KeyEvent e) {
    return (e.getID() == KeyEvent.KEY_RELEASED
        && e.getKeyCode() == getKeyCodeFromKeyMask(getMenuShortcutKeyMask()) && !copyPaste)
        || (e.getID() == KeyEvent.KEY_PRESSED && e.getKeyCode() != getKeyCodeFromKeyMask(
        getMenuShortcutKeyMask()));
  }

  protected void showUserMessage(String msg) {
    JOptionPane.showMessageDialog(this, msg, "Info", JOptionPane.INFORMATION_MESSAGE);
  }

  @Override
  public synchronized void paintComponent(Graphics g) {
    super.paintComponent(g);
  }

  public abstract void setKeyboardLock(boolean lock);

  protected boolean isAttentionKeyValid(AttentionKey attentionKey) {
    return supportedAttentionKeys.contains(attentionKey);
  }

  public void setSupportedAttentionKeys(Set<AttentionKey> supportedAttentionKeys) {
    this.supportedAttentionKeys = supportedAttentionKeys;
  }

  public <V extends T> void setProtocolClient(V terminalClient) {
    this.terminalClient = terminalClient;
  }

  public void setTerminalEmulatorListeners(
      List<TerminalEmulatorListener> terminalEmulatorListeners) {
    this.terminalEmulatorListeners = terminalEmulatorListeners;
  }

  public void setSampleSame(String sampleName) {
    this.sampleName = sampleName;
  }

  public void saveLabelWithPosition(Position position, String label) {
    labelMap.put(position, label);
  }

  public void setStatusPanel(StatusPanel statusPanel) {
    this.statusPanel = statusPanel;
  }

  //super.doCopy() has protected visibility therefore is override
  @Override
  public synchronized void doCopy() {
    super.doCopy();
  }

  //super.doPaste() has protected visibility therefore is override
  @Override
  public synchronized void doPaste() {
    super.doPaste();
  }

  public abstract void makePaste();

  public abstract void teardown();

  public void setInitialCursorPos(int column, int row) {
    this.setCursorPos(column, row);
  }

  public synchronized void setScreen(Screen screen, boolean isShowCredential) {
    Dimension screenSize = screen.getSize();
    setCrtSize(screenSize.width, screenSize.height);
    clear();
    removeFields();
    for (Segment s : screen.getSegments()) {
      int row = s.getStartPosition().getRow() - 1;
      int column = s.getStartPosition().getColumn() - 1;
      if (s.isEditable()) {
        int attr =
            !isShowCredential && s.isSecret() ? SECRET_CREDENTIAL_ATTR : DEFAULT_ATTR;
        drawString("\u0001", column - 1, row, attr);
        if (isCircularSegment(s)) {
          processCircularField(s, screenSize, attr);
          continue;
        }
        XI5250Field xi5250Field = new ScreenField(this, column,
            row, s.getText().length(), attr);

        xi5250Field.setString(s.getText());
        xi5250Field.resetMDT();

        addField(xi5250Field);
      } else {
        drawString(s.getText(), column,
            row, DEFAULT_ATTR);
      }
    }
    this.initAllFields();
  }

  private boolean isCircularSegment(Segment s) {
    return s.getStartPosition().compare(s.getEndPosition()) > 0;
  }

  private void processCircularField(Segment s, Dimension screenSize, int attr) {
    int beginLinealPosition = s.getPositionRange().getStartLinealPosition(screenSize.width);
    int endScreenFieldTextLength = screenSize.height * screenSize.width - beginLinealPosition;
    String endScreenFieldText = s.getText().substring(0, endScreenFieldTextLength);
    String startScreenFieldText = s.getText().substring(endScreenFieldTextLength);
    int endScreenFieldRow = s.getStartPosition().getRow() - 1;
    int endScreenFieldColumn = s.getStartPosition().getColumn() - 1;

    XI5250Field endScreenField = new CircularPartField(this, endScreenFieldColumn,
        endScreenFieldRow, endScreenFieldText.length(), attr);
    endScreenField.setString(endScreenFieldText);
    endScreenField.resetMDT();

    XI5250Field startScreenField = new CircularPartField(this, 0,
        0, startScreenFieldText.length(), attr);
    startScreenField.setString(startScreenFieldText);
    startScreenField.resetMDT();

    addField(endScreenField);
    addField(startScreenField);
  }

  protected static class KeyEventMap {

    private final int modifier;

    private final int specialKey;

    KeyEventMap(int modifier, int specialKey) {
      this.modifier = modifier;
      this.specialKey = specialKey;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      KeyEventMap that = (KeyEventMap) o;
      return modifier == that.modifier &&
          specialKey == that.specialKey;
    }

    @Override
    public int hashCode() {
      return Objects.hash(modifier, specialKey);
    }

  }

  public void setPasteEnableConsumer(Consumer<Boolean> pasteConsumer) {
    this.pasteConsumer = pasteConsumer;
  }

  public static class CircularPartField extends ScreenField {

    private CircularPartField(XI5250Crt aCrt, int aCol, int aRow, int aLen, int aAttr) {
      super(aCrt, aCol, aRow, aLen, aAttr);
    }

  }
}
