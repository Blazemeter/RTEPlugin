package com.blazemeter.jmeter.rte.sampler.gui;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.swing.fixture.Containers.showInFrame;
import static org.assertj.swing.timing.Pause.pause;
import static org.assertj.swing.timing.Timeout.timeout;

import com.blazemeter.jmeter.rte.core.Action;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import kg.apc.emulators.TestJMeterUtils;
import org.apache.commons.io.FileUtils;
import org.assertj.swing.fixture.AbstractJComponentFixture;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.fixture.JCheckBoxFixture;
import org.assertj.swing.timing.Condition;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class RTESamplerPanelTest {

  private FrameFixture frame;

  @BeforeClass
  public static void setupClass() {
    TestJMeterUtils.createJmeterEnv();
  }

  @Before
  public void setup() {
    RTESamplerPanel panel = new RTESamplerPanel();
    frame = showInFrame(panel);
  }

  @Test
  public void shouldDisableRequestFieldsWhenJustConnectIsChecked() {
    JCheckBoxFixture check = frame.checkBox("justConnect");
    List<AbstractJComponentFixture> expected = new ArrayList<>();
    //set false previously to assure that the event is triggered
    check.check(false);
    check.check(true);
    expected.add(frame.table("table"));
    expected.add(frame.button("addButton"));
    expected.add(frame.button("addFromClipboardButton"));
    for (Action a : Action.values()) {
      expected.add(frame.radioButton(a.name()));
    }
    validateEnabled(false, expected);
  }

  @Test
  public void shouldEnableRequestFieldsWhenJustConnectIsNotChecked() {
    JCheckBoxFixture check = frame.checkBox("justConnect");
    List<AbstractJComponentFixture> expected = new ArrayList<>();
    //set true previously to assure that the event is triggered
    check.check(true);
    check.check(false);
    expected.add(frame.table("table"));
    expected.add(frame.button("addButton"));
    expected.add(frame.button("addFromClipboardButton"));
    for (Action a : Action.values()) {
      expected.add(frame.radioButton(a.name()));
    }
    validateEnabled(true, expected);
  }

  @Test
  public void shouldDisableSyncWaitFieldsWhenIsNotChecked() {
    JCheckBoxFixture check = frame.checkBox("waitSync");
    List<AbstractJComponentFixture> expected = new ArrayList<>();
    //set true previously to assure that the event is triggered
    check.check(true);
    check.check(false);
    expected.add(frame.textBox("waitSyncTimeout"));
    validateEnabled(false, expected);
  }

  @Test
  public void shouldEnableSyncWaitFieldsWhenIsChecked() {
    JCheckBoxFixture check = frame.checkBox("waitSync");
    List<AbstractJComponentFixture> expected = new ArrayList<>();
    //set false previously to assure that the event is triggered
    check.check(false);
    check.check(true);
    expected.add(frame.textBox("waitSyncTimeout"));
    validateEnabled(true, expected);
  }

  @Test
  public void shouldEnableCursorWaitFieldsWhenIsChecked() {
    JCheckBoxFixture check = frame.checkBox("waitCursor");
    List<AbstractJComponentFixture> expected = new ArrayList<>();
    //set false previously to assure that the event is triggered
    check.check(false);
    check.check(true);
    expected.add(frame.textBox("waitCursorRow"));
    expected.add(frame.textBox("waitCursorColumn"));
    expected.add(frame.textBox("waitCursorTimeout"));
    validateEnabled(true, expected);
  }

  @Test
  public void shouldDisableCursorWaitFieldsWhenIsNotChecked() {
    JCheckBoxFixture check = frame.checkBox("waitCursor");
    List<AbstractJComponentFixture> expected = new ArrayList<>();
    //set true previously to assure that the event is triggered
    check.check(true);
    check.check(false);
    expected.add(frame.textBox("waitCursorRow"));
    expected.add(frame.textBox("waitCursorColumn"));
    expected.add(frame.textBox("waitCursorTimeout"));
    validateEnabled(false, expected);
  }

  @Test
  public void shouldEnableSilentWaitFieldsWhenIsChecked() {
    JCheckBoxFixture check = frame.checkBox("waitSilent");
    List<AbstractJComponentFixture> expected = new ArrayList<>();
    //set false previously to assure that the event is triggered
    check.check(false);
    check.check(true);
    expected.add(frame.textBox("waitSilentTime"));
    expected.add(frame.textBox("waitSilentTimeout"));
    validateEnabled(true, expected);
  }

  @Test
  public void shouldDisableSilentWaitFieldsWhenIsNotChecked() {
    JCheckBoxFixture check = frame.checkBox("waitSilent");
    List<AbstractJComponentFixture> expected = new ArrayList<>();
    //set true previously to assure that the event is triggered
    check.check(true);
    check.check(false);
    expected.add(frame.textBox("waitSilentTime"));
    expected.add(frame.textBox("waitSilentTimeout"));
    validateEnabled(false, expected);
  }

  @Test
  public void shouldEnableTextWaitFieldWhenIsChecked() {
    JCheckBoxFixture check = frame.checkBox("waitText");
    List<AbstractJComponentFixture> expected = new ArrayList<>();
    //set false previously to assure that the event is triggered
    check.check(false);
    check.check(true);
    expected.add(frame.textBox("waitTextRegex"));
    expected.add(frame.textBox("waitTextTimeout"));
    expected.add(frame.textBox("waitTextAreaLeft"));
    expected.add(frame.textBox("waitTextAreaTop"));
    expected.add(frame.textBox("waitTextAreaBottom"));
    expected.add(frame.textBox("waitTextAreaRight"));
    validateEnabled(true, expected);
  }

  @Test
  public void shouldDisableTextWaitFieldsWhenIsNotChecked() {
    JCheckBoxFixture check = frame.checkBox("waitText");
    List<AbstractJComponentFixture> expected = new ArrayList<>();
    //set true previously to assure that the event is triggered
    check.check(true);
    check.check(false);
    expected.add(frame.textBox("waitTextRegex"));
    expected.add(frame.textBox("waitTextTimeout"));
    expected.add(frame.textBox("waitTextAreaLeft"));
    expected.add(frame.textBox("waitTextAreaTop"));
    expected.add(frame.textBox("waitTextAreaBottom"));
    expected.add(frame.textBox("waitTextAreaRight"));
    validateEnabled(false, expected);
  }

  @After
  public void tearDown() {
    frame.cleanUp();
  }

  private void validateEnabled(boolean enable, List<AbstractJComponentFixture> components) {

    pause(new Condition("All componentes are " + enable) {
      @Override
      public boolean test() {
        List<String> result = new ArrayList<>();
        List<String> expected = new ArrayList<>();
        for (AbstractJComponentFixture c : components) {
          if (c.isEnabled() == enable) {
            result.add(c.target().getName());
          }
          expected.add(c.target().getName());
        }
        return result.containsAll(expected) && expected.containsAll(result);
      }
    }, timeout(10000));
  }
}
