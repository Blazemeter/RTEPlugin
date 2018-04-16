package com.blazemeter.jmeter.rte;

import org.junit.runner.Description;
import org.junit.runner.notification.RunListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestLoggerListener extends RunListener {

  private static final Logger LOG = LoggerFactory.getLogger(TestLoggerListener.class);

  public void testStarted(Description description) {
    LOG.info("Starting test {}...", description.getMethodName());
  }

  public void testFinished(Description description) {
    LOG.info("Ended test {}!", description.getMethodName());
  }

}
