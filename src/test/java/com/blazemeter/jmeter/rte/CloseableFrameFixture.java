package com.blazemeter.jmeter.rte;


import java.awt.Frame;
import java.io.Closeable;
import java.io.IOException;
import javax.annotation.Nonnull;
import org.assertj.swing.core.Robot;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.fixture.JButtonFixture;

public class CloseableFrameFixture implements Closeable {

  private FrameFixture frame;

  public CloseableFrameFixture(@Nonnull Frame target) {
    frame = new FrameFixture(target);
    frame.show();
  }

  public Robot robot() {
    return frame.robot();
  }

  @Override
  public void close() throws IOException {
    frame.cleanUp();
  }

  public JButtonFixture button(String name) {
    return frame.button(name);
  }
}
