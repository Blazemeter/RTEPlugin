package com.blazemeter.jmeter.rte.recorder.emulator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.swing.fixture.Containers.showInFrame;
import static org.assertj.swing.timing.Pause.pause;

import java.awt.Frame;
import java.awt.event.MouseEvent;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import org.assertj.core.api.JUnitSoftAssertions;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.timing.Condition;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class StatusPanelIT {

  private static final long HELP_FRAME_VISIBLE_TIMEOUT_MILLIS = 1000;
  private FrameFixture frame;
  private StatusPanel statusPanel;

  @Rule
  public final JUnitSoftAssertions softly = new JUnitSoftAssertions();

  private static final String POSITION_LABEL = "positionLabel";
  private static final String MESSAGE_LABEL = "messageLabel";
  private static final String KEYBOARD_LABEL = "keyboardLabel";
  private static final String HELP_LABEL = "helpLabel";
  private static final String HELP_FRAME = "helpFrame";
  private static final String EXPECTED_POSITION_TEXT = "row: 66 / column: 66";
  private static final ImageIcon KEYBOARD_LOCKED_ICON = new ImageIcon(
      StatusPanel.class.getResource("/keyboard-locked.png"));
  private static final ImageIcon KEYBOARD_UNLOCKED_ICON = new ImageIcon(
      StatusPanel.class.getResource("/keyboard-unlocked.png"));


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
    statusPanel.updateStatusBarCursorPosition(66, 66);
    assertThat(frame.label(POSITION_LABEL).text()).isEqualTo(EXPECTED_POSITION_TEXT);
  }

  @Test
  public void shouldShowKeyboardLockedIconWhenKeyboardIsLocked() {
    statusPanel.setKeyboardStatus(true);
    JLabel keyboardLabel = frame.label(KEYBOARD_LABEL).target();
    assertThat(((ImageIcon) keyboardLabel.getIcon()).getImage())
        .isEqualTo(KEYBOARD_LOCKED_ICON.getImage());
  }

  @Test
  public void shouldShowKeyboardUnlockedIconWhenKeyboardIsUnlocked() {
    statusPanel.setKeyboardStatus(false);
    JLabel keyboardLabel = frame.label(KEYBOARD_LABEL).target();
    assertThat(((ImageIcon) keyboardLabel.getIcon()).getImage())
        .isEqualTo(KEYBOARD_UNLOCKED_ICON.getImage());
  }

  @Test
  public void shouldShowHelpFrameWhenClickInHelpIcon() {
    JLabel helpLabel = frame.label(HELP_LABEL).target();
    frame.label(HELP_LABEL).click();
    pause(new Condition("frame is visible") {

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

  @Test
  public void shouldShowMessageSetWhenMessageIsSet() {
    String message = "Test Message";
    statusPanel.setStatusMessage(message);
    assertThat(frame.label(MESSAGE_LABEL).text()).isEqualTo(message);
  }
}
