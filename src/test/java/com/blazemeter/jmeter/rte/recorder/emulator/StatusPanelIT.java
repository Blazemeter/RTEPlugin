package com.blazemeter.jmeter.rte.recorder.emulator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.swing.fixture.Containers.showInFrame;
import static org.assertj.swing.timing.Pause.pause;

import com.blazemeter.jmeter.rte.sampler.gui.ThemedIcon;
import java.awt.Frame;
import java.awt.Image;
import java.awt.event.MouseEvent;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import org.assertj.core.api.JUnitSoftAssertions;
import org.assertj.swing.core.MouseButton;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.timing.Condition;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class StatusPanelIT {

  public static final ImageIcon KEYBOARD_UNLOCKED_ICON = ThemedIcon
      .fromResourceName("keyboard-unlocked.png");
  public static final ImageIcon CURSOR_ICON = ThemedIcon.fromResourceName(
      "cursor.png");
  private static final long HELP_FRAME_VISIBLE_TIMEOUT_MILLIS = 1000;
  private static final String POSITION_LABEL = "positionLabel";
  private static final String KEYBOARD_LABEL = "keyboardLabel";
  private static final String HELP_LABEL = "helpLabel";
  private static final String HELP_FRAME = "helpFrame";
  private static final String EXPECTED_POSITION_TEXT = "row: 66 / column: 66";
  private static final ImageIcon KEYBOARD_LOCKED_ICON = ThemedIcon
      .fromResourceName("keyboard-locked.png");
  @Rule
  public final JUnitSoftAssertions softly = new JUnitSoftAssertions();

  private FrameFixture frame;
  private StatusPanel statusPanel;

  @Before
  public void setup() {
    statusPanel = new StatusPanel();
    frame = showInFrame(statusPanel);
  }

  @After
  public void tearDown() {
    frame.cleanUp();
  }

  @Test
  public void shouldChangeTheValuesOfThePositionLabelWhenUpdateCursorPosition() {
    GuiActionRunner.execute(() -> statusPanel.updateStatusBarCursorPosition(66, 66));
    assertThat(frame.label(POSITION_LABEL).text()).isEqualTo(EXPECTED_POSITION_TEXT);
  }

  @Test
  public void shouldShowKeyboardLockedIconWhenKeyboardIsLocked() {
    GuiActionRunner.execute(() -> statusPanel.setKeyboardStatus(true));
    Image keyboardImage = GuiActionRunner.execute(
        () -> ((ImageIcon) frame.label(KEYBOARD_LABEL).target().getIcon()).getImage());
    assertThat(keyboardImage)
        .isEqualTo(KEYBOARD_LOCKED_ICON.getImage());
  }

  @Test
  public void shouldShowKeyboardUnlockedIconWhenKeyboardIsUnlocked() {
    GuiActionRunner.execute(() -> statusPanel.setKeyboardStatus(false));
    Image keyboardImage = GuiActionRunner.execute(
        () -> ((ImageIcon) frame.label(KEYBOARD_LABEL).target().getIcon()).getImage());
    assertThat(keyboardImage)
        .isEqualTo(KEYBOARD_UNLOCKED_ICON.getImage());
  }

  @Test
  public void shouldShowHelpFrameWhenClickInHelpIcon() {
    frame.label(HELP_LABEL).click(MouseButton.LEFT_BUTTON);
    pause(new Condition("for help frame to be visible") {

      @Override
      public boolean test() {
        Frame helpFrame = null;
        for (Frame f : Frame.getFrames()) {
          if (f.getName().equals(HELP_FRAME)) {
            helpFrame = f;
            break;
          }
        }
        return helpFrame != null && helpFrame.isVisible();
      }

    }, HELP_FRAME_VISIBLE_TIMEOUT_MILLIS);

  }

}
