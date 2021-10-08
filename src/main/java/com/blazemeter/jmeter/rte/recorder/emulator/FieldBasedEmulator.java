package com.blazemeter.jmeter.rte.recorder.emulator;

import com.blazemeter.jmeter.rte.core.CoordInput;
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
    List<Input> inputs = new ArrayList<>();
    XI5250Field fieldFromPos = getFieldFromPos(initialColumn, initialRow);
    if (fieldFromPos == null) {
      fieldFromPos = getNextFieldFromPos(initialColumn, initialRow);
    }
    int initialField = getFields().indexOf(fieldFromPos);
    //means screen is not constituted by fields
    if (initialField == -1) {
      inputs.add(new CoordInput(buildOneBasedPosition(getCursorRow(), getCursorCol()), ""));
      labelMap.clear();
      return inputs;
    }
    List<XI5250Field> fields = new ArrayList<>(getFields());
    processCircularFieldIfNeeded(fields, inputs);

    Iterator<XI5250Field> it = fields.listIterator(initialField);
    int index = 0;
    int offset = 0;
    Position lastFieldPosition = null;
    while (it.hasNext() && index < getFields().size()) {
      XI5250Field f = it.next();
      if (f.isMDTOn() && !isConstitutedByNulls(f.getString())) {
        Position fieldPosition = buildOneBasedPosition(f.getRow(), f.getCol());
        lastFieldPosition = fieldPosition;
        String label = (String) labelMap.get(fieldPosition);
        String trimmedInput = trimNulls(f.getString());
        inputs.add(label != null ? new LabelInput(label, trimmedInput)
            : new NavigationInput(f.equals(fieldFromPos) ? 0 : offset, NavigationType.TAB,
                trimmedInput));
        offset = trimmedInput.trim().length() == f.getLength() ? 0 : 1;
      } else {
        offset++;
      }
      index++;
      if (!it.hasNext()) {
        it = getFields().iterator();
      }
    }

    if (isNeededToUpdateCursorPosition(lastFieldPosition)) {
      inputs.add(new CoordInput(buildOneBasedPosition(getCursorRow(), getCursorCol()), ""));
    }
    labelMap.clear();
    return inputs;
  }

  private void processCircularFieldIfNeeded(List<XI5250Field> fields, List<Input> inputs) {
    //This method is actually modifying fields and inputs by reference
    if (fields.isEmpty()) {
      return;
    }
    XI5250Field firstField = fields.get(0);
    if (!(firstField instanceof XI5250CrtBase.CircularPartField)) {
      return;
    }
    XI5250Field lastField = fields.get(fields.size() - 1);
    if (!firstField.isMDTOn() && !lastField.isMDTOn()) {
      return;
    }

    Position circularPositionBegin = buildOneBasedPosition(lastField.getRow(), lastField.getCol());
    StringBuilder input = new StringBuilder();

    if (lastField.isMDTOn() && firstField.isMDTOn()) {
      input.append(lastField.getString())
          .append(trimNulls(firstField.getString()));
    } else if (firstField.isMDTOn()) {
      input.append(trimNulls(firstField.getString()));
    } else {
      input.append(trimNulls(lastField.getString()));
    }
    String label = (String) labelMap.get(circularPositionBegin);
    inputs.add(label != null ? new LabelInput(label, input.toString())
        : new CoordInput(circularPositionBegin, input.toString()));
    // We don't want to consume this fields later on. Since are already processed. 
    fields.remove(firstField);
    fields.remove(lastField);
  }

  private Position buildOneBasedPosition(int row, int col) {
    return new Position(row + 1, col + 1);
  }

  private boolean isNeededToUpdateCursorPosition(
      Position lastFieldPosition) {
    if (lastFieldPosition == null) {
      return true;
    }
    XI5250Field lastModifiedField = getFieldFromPos(lastFieldPosition.getColumn() - 1,
        lastFieldPosition.getRow() - 1);
    int fieldWrittenTextLength = lastModifiedField.getTrimmedString().length();
    if (new Position(lastModifiedField.getRow(),
        lastModifiedField.getCol() + fieldWrittenTextLength)
        .equals(new Position(getCursorRow(), getCursorCol()))) {
      return false;
    }

    XI5250Field nextField = getNextFieldAfterField(lastModifiedField);
    Position currentCursorPosition = new Position(getCursorRow(), getCursorCol());
    //when reaching the maximum length of a field while typing, emulator jumps to next field
    return lastModifiedField.getLength() != fieldWrittenTextLength || !currentCursorPosition
        .equals(buildOneBasedPosition(nextField.getRow(), nextField.getCol()));
  }

  private XI5250Field getNextFieldAfterField(XI5250Field field) {
    int index = getFields().indexOf(field);
    if (++index < getFields().size()) {
      return getFields().get(index);
    } else {
      return getField(0);
    }
  }

  @Override
  public void setInitialCursorPos(int column, int row) {
    this.initialColumn = column;
    this.initialRow = row;
    this.setCursorPos(column, row);
  }

  private boolean isConstitutedByNulls(String fieldText) {
    return !fieldText.contains(" ") && fieldText.trim().isEmpty();
  }

  private String trimNulls(String str) {
    if (str.isEmpty()) {
      return str;
    } else if (str.trim().isEmpty()) {
      return "";
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
