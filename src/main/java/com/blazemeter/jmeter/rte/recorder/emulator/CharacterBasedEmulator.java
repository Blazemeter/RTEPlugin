package com.blazemeter.jmeter.rte.recorder.emulator;

import com.blazemeter.jmeter.rte.core.AttentionKey;
import com.blazemeter.jmeter.rte.core.CharacterBasedProtocolClient;
import com.blazemeter.jmeter.rte.core.Input;
import com.blazemeter.jmeter.rte.core.NavigationInput.NavigationInputBuilder;
import com.blazemeter.jmeter.rte.core.Position;
import com.blazemeter.jmeter.rte.protocols.vt420.Vt420Client;
import com.blazemeter.jmeter.rte.sampler.NavigationType;
import com.blazemeter.jmeter.rte.sampler.RTESampler;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import nl.lxtreme.jvt220.terminal.ScreenChangeListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CharacterBasedEmulator extends
    XI5250CrtBase<CharacterBasedProtocolClient> implements ScreenChangeListener {

  private static final Logger LOG = LoggerFactory.getLogger(CharacterBasedEmulator.class);
  private Position lastCursorPosition;
  private StringBuilder inputBuffer = new StringBuilder();
  private final List<Input> inputs = new ArrayList<>();
  private int repetition;
  private boolean isAreaSelected;
  private SwingWorker<Object, Object> currentSwingWorker;
  private int pastingCharactersCount = 0;
  private int pastedCharactersCount = 0;
  private NavigationInputBuilder currentInput = new NavigationInputBuilder();

  @Override
  protected synchronized void processKeyEvent(KeyEvent e) {
    LOG.debug("Processing key: ({})", e);
    processCopyOrPaste(e);
    if (isAnyKeyPressedOrControlKeyReleasedAndNotCopy(e)) {
      AttentionKey attentionKey = KEY_EVENTS
          .get(new KeyEventMap(e.getModifiers(), e.getKeyCode()));
      if (attentionKey == null && !locked) {
        if (e.getKeyCode() == KeyEvent.VK_SHIFT
            || e.getKeyCode() == KeyEvent.VK_META
            || e.getKeyCode() == KeyEvent.KEY_LOCATION_UNKNOWN
            || copyPaste) {
          copyPaste = false;
          return;
        }
        lockEmulator(RTESampler.isWaitResponseCharSent());
        currentSwingWorker = buildSwingWorker(e);
        currentSwingWorker.execute();
      } else if (attentionKey != null) {
        if (isAttentionKeyValid(attentionKey)) {
          lockEmulator(true);
          lastCursorPosition = getCursorPosition();
          processAttentionKey(e, attentionKey);
        } else {
          showUserMessage(attentionKey + " not supported for current protocol");
          e.consume();
        }
      }
    }
  }

  @Override
  protected List<Input> getInputFields() {
    if (inputBuffer.length() > 0 || repetition != 0) {
      buildDefaultInputWhenNoNavigationType();
      insertCurrentInput();
    }
    List<Input> inputs = new ArrayList<>(this.inputs);
    this.inputs.clear();
    return inputs;
  }

  private SwingWorker<Object, Object> buildSwingWorker(KeyEvent e) {
    return new SwingWorker<Object, Object>() {
      @Override
      protected Object doInBackground() {
        recordInput(getKeyString(e));
        return null;
      }
    };
  }

  @Override
  public void setKeyboardLock(boolean lock) {
    locked = lock;
    statusPanel.setKeyboardStatus(lock);
  }

  @Override
  public synchronized void makePaste() {
    String value;
    try {
      value = getClipboardContent();
    } catch (IOException | UnsupportedFlavorException e) {
      LOG.warn("Error while trying to get clipboard content", e);
      return;
    }
    List<String> sequencesInClipboard = CharacterSequenceScaper.getSequencesIn(value);

    if (!sequencesInClipboard.isEmpty()) {
      String chunkAppearances = CharacterSequenceScaper.getSequenceChunkAppearancesIn(value);
      JOptionPane.showMessageDialog(this, "Clipboard content '" + String.join(", ",
              sequencesInClipboard) + "' is not "
              + "supported when pasting. \nAppearances of sequences near to: "
              + chunkAppearances, "Paste error",
          JOptionPane.INFORMATION_MESSAGE);
      LOG.error("Clipboard content contains unsupported ANSI sequence. RTE-Plugin may support "
              + "that/those sequence/s ({}) as an attention key or as a navigation input. "
              + "\nAppearances of sequences: {} ", sequencesInClipboard,
          chunkAppearances);
      LOG.info(
          "Check this page to understand what those characters means: https://en.wikipedia"
              + ".org/wiki/List_of_Unicode_characters#Control_codes");
      return;
    }
    pastingCharactersCount = value.length();
    pastedCharactersCount = 0;
    lockEmulator(true);
    currentSwingWorker = new SwingWorker<Object, Object>() {
      @Override
      protected Object doInBackground() {
        Arrays.stream(value.split(""))
            .forEach(c -> recordInput(c));
        return null;
      }
    };
    currentSwingWorker.execute();
  }

  private void lockEmulator(boolean isLock) {
    setKeyboardLock(isLock);
    setCursorVisible(!isLock);
    pasteConsumer.accept(!isLock);
  }

  private String getClipboardContent() throws IOException, UnsupportedFlavorException {
    Clipboard clipboard = this.getToolkit().getSystemClipboard();
    return (String) clipboard.getContents(this).getTransferData(DataFlavor.stringFlavor);
  }

  private void recordInput(String value) {
    terminalClient.send(Collections.singletonList(value), RTESampler.getCharacterTimeout());
    Optional<NavigationType> navigationKey = Arrays.stream(NavigationType.values())
        .filter(v -> Vt420Client.NAVIGATION_KEYS.get(v).equals(value))
        .findFirst();
    if (navigationKey.isPresent()) {
      buildNavigationInput(navigationKey.get());
    } else {
      if (lastCursorPosition == null || lastCursorPosition.isConsecutiveWith(getCursorPosition())
          || lastCursorPosition.equals(
          getCursorPosition())) {
        inputBuffer.append(value);
      } else {
        buildDefaultInputWhenNoNavigationType();
        insertCurrentInput();
        inputBuffer.append(value);
      }
      lastCursorPosition = new Position(getCursorPosition());
    }
  }

  private void buildNavigationInput(NavigationType type) {
    if (inputBuffer.length() > 0 || (repetition != 0 && !type
        .equals(currentInput.getNavigationType()))) {
      buildDefaultInputWhenNoNavigationType();
      insertCurrentInput();
    }
    if (currentInput.getNavigationType() == null) {
      currentInput.withNavigationType(type);
    }
    currentInput.withRepeat(++repetition);
  }

  private void buildDefaultInputWhenNoNavigationType() {
    if (currentInput.getNavigationType() == null) {
      currentInput = new NavigationInputBuilder()
          .withRepeat(repetition)
          .withNavigationType(NavigationType.TAB);
    }
  }

  private void insertCurrentInput() {
    inputs.add(currentInput.withInput(inputBuffer.toString()).build());
    currentInput = new NavigationInputBuilder();
    inputBuffer = new StringBuilder();
    repetition = 0;
    lastCursorPosition = null;
  }

  private Position getCursorPosition() {
    return terminalClient.getCursorPosition().orElse(Position.DEFAULT_POSITION);
  }

  private String getKeyString(KeyEvent e) {
    int keyCode = e.getKeyCode();
    switch (keyCode) {
      case KeyEvent.VK_TAB:
        return Vt420Client.NAVIGATION_KEYS.get(NavigationType.TAB);
      case KeyEvent.VK_LEFT:
        return Vt420Client.NAVIGATION_KEYS.get(NavigationType.LEFT);
      case KeyEvent.VK_RIGHT:
        return Vt420Client.NAVIGATION_KEYS.get(NavigationType.RIGHT);
      case KeyEvent.VK_UP:
        return Vt420Client.NAVIGATION_KEYS.get(NavigationType.UP);
      case KeyEvent.VK_DOWN:
        return Vt420Client.NAVIGATION_KEYS.get(NavigationType.DOWN);
      case KeyEvent.VK_SPACE:
        return " ";
      default:
        return String.valueOf(e.getKeyChar());
    }
  }

  @Override
  public synchronized void screenChanged(String s) {
    if (pastingCharactersCount == 0) {
      lockEmulator(false);
    } else if (++pastedCharactersCount == pastingCharactersCount) {
      lockEmulator(false);
      pastingCharactersCount = 0;
    }
  }

  public void setKeyboardStatus(boolean isLock) {
    locked = isLock;
    statusPanel.setKeyboardStatus(isLock);
  }

  @Override
  public synchronized void teardown() {
    terminalClient.removeScreenChangeListener(this);
    if (currentSwingWorker != null) {
      currentSwingWorker.cancel(true);
    }
  }

  @Override
  protected void processMouseEvent(MouseEvent e) {
    if (e.getID() == MouseEvent.MOUSE_CLICKED) {
      if (isAreaSelected) {
        super.setSelectedArea(null);
        isAreaSelected = false;
      } else {
        statusPanel.blinkBlockedCursor();
        this.requestFocus();
      }
    } else if (e.getID() == MouseEvent.MOUSE_PRESSED) {
      super.setIvMousePressed(true);
      super.setIvStartDragging(e);
    } else if (e.getID() == MouseEvent.MOUSE_RELEASED) {
      super.setIvMousePressed(false);
    }
  }

  @Override
  protected void processMouseMotionEvent(MouseEvent e) {
    if (e.getID() == MouseEvent.MOUSE_DRAGGED) {
      isAreaSelected = true;
    }
    super.processMouseMotionEvent(e);
  }

}
