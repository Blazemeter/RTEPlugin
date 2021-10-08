package com.blazemeter.jmeter.rte.recorder.emulator;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.blazemeter.jmeter.rte.core.CoordInput;
import com.blazemeter.jmeter.rte.core.Input;
import com.blazemeter.jmeter.rte.core.Position;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import net.infordata.em.crt5250.XI5250Crt;
import net.infordata.em.crt5250.XI5250Field;
import org.junit.Before;
import org.junit.Test;

public class FieldBaseEmulatorTest {

  private FieldBaseEmulatorProxy emulator;

  @Before
  public void setup() {
    emulator = new FieldBaseEmulatorProxy();
  }

  @Test
  public void shouldDiscardModifiedFieldsConstitutedByNullsWhenGetInputFields() {
    ((XI5250FieldMDT) emulator.getFields().get(0)).setMDTOn();
    List<Input> inputFields = emulator.getInputFields();
    assertThat(inputFields).isEqualTo(Collections.singletonList(new CoordInput(new Position(1, 1),
        "")));
  }

  private static class FieldBaseEmulatorProxy extends FieldBasedEmulator {

    private final List<XI5250Field> fields = Arrays.asList(
        new XI5250FieldMDT(this, 1, 2, 6, -1),
        new XI5250FieldMDT(this, 1, 3, 6, -1),
        new XI5250FieldMDT(this, 1, 4, 6, -1),
        new XI5250FieldMDT(this, 1, 5, 6, -1)
    );

    private FieldBaseEmulatorProxy() {

      fields.forEach(this::addField);
    }

    @Override
    public List<XI5250Field> getFields() {
      return fields;
    }
  }

  private static class XI5250FieldMDT extends XI5250Field {

    public XI5250FieldMDT(XI5250Crt aCrt, int aCol, int aRow, int aLen, int aAttr) {
      super(aCrt, aCol, aRow, aLen, aAttr);
    }

    @Override
    public void setMDTOn() {
      super.setMDTOn();
    }
  }
}
