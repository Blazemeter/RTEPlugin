package com.blazemeter.jmeter.rte.recorder.emulator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.swing.fixture.Containers.showInFrame;
import static org.assertj.swing.timing.Pause.pause;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.timing.Condition;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class MessageLabelIT {

  private static final int TIMEOUT_MILLIS = 5500;
  private static final String TEST_MESSAGE = "Test Message";
  private FrameFixture frame;
  private MessageLabel messageLabel;
  @Before
  public void setup() {
    messageLabel = new MessageLabel();
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
  public void shouldAppearMessageForAPeriodOfTime() {
    List<Boolean> expected = Arrays
        .asList(false, true, false);
    List<Boolean> result = new ArrayList<>();
    result.add(messageLabel.isVisible());
    messageLabel.showMessage(TEST_MESSAGE);
    result.add(messageLabel.isVisible());
    awaitForTextToAppear();
    result.add(messageLabel.isVisible());
    assertThat(result).isEqualTo(expected);
  }

  private void awaitForTextToAppear() {
    pause(new Condition("text appear on time") {
      @Override
      public boolean test() {
        return !messageLabel.isVisible();
      }
    }, TIMEOUT_MILLIS);
  }

  @Test
  public void shouldStartNewMessageLabelWhenStart() throws InterruptedException {
    List<Boolean> expected = Arrays
        .asList(true, true);
    List<Boolean> result = new ArrayList<>();
    
    Callable<Void> firstShowMessage = () -> {
      messageLabel.showMessage(TEST_MESSAGE);
      result.add(messageLabel.getText().equals(TEST_MESSAGE));
      wait(100);
      return null;
    };
    
    Callable<Void> secondShowMessage = () -> {
      messageLabel.showMessage(TEST_MESSAGE + "1");
      result.add(messageLabel.getText().equals(TEST_MESSAGE + "1"));
      return null;
    };
    
    List<Callable<Void>> taskList = new ArrayList<>();
    taskList.add(firstShowMessage);
    taskList.add(secondShowMessage);
    ExecutorService executor = Executors.newSingleThreadExecutor();
    executor.invokeAll(taskList);
    
    assertThat(result).isEqualTo(expected);
  }
}