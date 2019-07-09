package com.blazemeter.jmeter.rte.recorder.emulator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.swing.fixture.Containers.showInFrame;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import org.assertj.core.api.JUnitSoftAssertions;
import org.assertj.swing.fixture.FrameFixture;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class MessageLabelIT {

  private static final String TEST_MESSAGE = "Test Message";
  @Rule
  public final JUnitSoftAssertions softly = new JUnitSoftAssertions();
  private FrameFixture frame;
  private MessageLabel messageLabel;
  private ScheduledExecutorServiceTest executorService;
  @Mock
  private ScheduledFuture future;


  @Before
  public void setup() {
    executorService = new ScheduledExecutorServiceTest(future);
    messageLabel = new MessageLabel(executorService);
    frame = showInFrame(messageLabel);
  }

  @After
  public void teardown() {
    frame.cleanUp();
  }

  @Test
  public void shouldAppearMessage() {
    messageLabel.showMessage(TEST_MESSAGE);
    assertThat(messageLabel.getText()).isEqualTo(TEST_MESSAGE);
  }

  @Test
  public void shouldHideMessageAfterSomeTimeWhenShowMessage() {
    List<Boolean> expected = Arrays
        .asList(false, true, false);
    List<Boolean> result = new ArrayList<>();
    result.add(messageLabel.isVisible());
    messageLabel.showMessage(TEST_MESSAGE);
    result.add(messageLabel.isVisible());
    executorService.tick();
    result.add(messageLabel.isVisible());
    assertThat(result).isEqualTo(expected);
  }

  @Test
  public void shouldStartNewMessageLabelWhenStart() {
    List<Boolean> expected = Arrays
        .asList(false, true, false, true, false);
    List<Boolean> result = new ArrayList<>();
    result.add(messageLabel.isVisible());
    messageLabel.showMessage(TEST_MESSAGE + "1");
    result.add(messageLabel.isVisible());
    executorService.tick();
    result.add(messageLabel.isVisible());
    messageLabel.showMessage(TEST_MESSAGE);
    result.add(messageLabel.isVisible());
    executorService.tick();
    result.add(messageLabel.isVisible());
    softly.assertThat(result).isEqualTo(expected);
    softly.assertThat(messageLabel.getText()).isEqualTo(TEST_MESSAGE);
  }
}
