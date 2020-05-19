package com.blazemeter.jmeter.rte.recorder.emulator;

import com.blazemeter.jmeter.rte.core.Input;
import com.blazemeter.jmeter.rte.core.LabelInput;
import com.blazemeter.jmeter.rte.core.NavigationInput;
import com.blazemeter.jmeter.rte.core.Position;
import com.blazemeter.jmeter.rte.sampler.NavigationType;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.infordata.em.crt5250.XI5250Field;

public class FieldBasedEmulator extends XI5250CrtBase {

  private int initialColumn;
  private int initialRow;

  @Override
  public void setKeyboardLock(boolean lock) {
    locked = lock;
    statusPanel.setKeyboardStatus(lock);
  }

  @Override
  public void makePaste() {
    super.doPaste();
  }

  @Override
  public void teardown() {
    labelMap.clear();
  }

  @Override
  protected List<Input> getInputFields() {
    List<Input> fields = new ArrayList<>();
    XI5250Field fieldFromPos = getFieldFromPos(initialColumn, initialRow);
    if (fieldFromPos == null) {
      fieldFromPos = getNextFieldFromPos(initialColumn, initialRow);
    }
    int initialField = getFields().indexOf(fieldFromPos);
    Iterator<XI5250Field> it = getFields().listIterator(initialField);
    int index = 0;
    int offset = 1;
    while (it.hasNext() && index < getFields().size()) {
      XI5250Field f = it.next();
      if (f.isMDTOn()) {
        Position fieldPosition = new Position(f.getRow() + 1, f.getCol() + 1);
        String label = (String) labelMap.get(fieldPosition);
        String trimmedInput = trimNulls(f.getString());
        fields.add(label != null ? new LabelInput(label, trimmedInput)
            : new NavigationInput(f.equals(fieldFromPos) ? 0 : offset, NavigationType.TAB,
                trimmedInput));
        offset = 1;
      } else {
        offset++;
      }
      index++;
      if (!it.hasNext()) {
        it = getFields().iterator();
      }
    }
    labelMap.clear();
    return fields;
  }

  @Override
  public void setInitialCursorPos(int column, int row) {
    this.initialColumn = column;
    this.initialRow = row;
    this.setCursorPos(column, row);
  }

  private String trimNulls(String str) {
    if (str.isEmpty()) {
      return str;
    }
    int firstNotNull = 0;
    while (str.charAt(firstNotNull) == '\u0000') {
      firstNotNull++;
    }
    int lastNotNull = str.length() - 1;
    while (str.charAt(lastNotNull) == '\u0000') {
      lastNotNull--;
    }
    return str.substring(firstNotNull, lastNotNull + 1);
  }

  @Override
  protected void processMouseEvent(MouseEvent e) {
    super.processMouseEvent(e);
    statusPanel
        .updateStatusBarCursorPosition(this.getCursorRow() + 1, this.getCursorCol() + 1);
  }
}
