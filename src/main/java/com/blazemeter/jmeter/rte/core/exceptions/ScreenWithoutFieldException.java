package com.blazemeter.jmeter.rte.core.exceptions;

import java.util.NoSuchElementException;

public class ScreenWithoutFieldException extends NoSuchElementException {

  public ScreenWithoutFieldException() {
    super("Screen not composed by fields. None found.");
  }
}
