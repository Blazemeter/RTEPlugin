package com.blazemeter.jmeter.rte.core.listener;

import com.blazemeter.jmeter.rte.core.exceptions.RteIOException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExceptionHandler {

  private static final Logger LOG = LoggerFactory.getLogger(ExceptionHandler.class);

  private List<ExceptionListener> listeners = new ArrayList<>();
  private Throwable pendingError;
  private String server;
  
  public ExceptionHandler(String server) {
    this.server = server;
  }

  public synchronized void setPendingError(Throwable ex) {
    if (pendingError == null) {
      pendingError = ex;
      /*  Creating a copy of listeners keys to avoid concurrent modification exception 
       *  due to listeners potentially removing themselves on exception.
       */ 
      new ArrayList<>(listeners).forEach(l -> l.onException(ex));
    } else {
      LOG.error("Exception ignored in step result due to previously thrown exception", ex);
    }
  }

  public synchronized boolean hasPendingError() {
    return pendingError != null;
  }

  public synchronized void throwAnyPendingError() throws RteIOException {
    if (pendingError != null) {
      Throwable ret = pendingError;
      pendingError = null;
      throw new RteIOException(ret, server);
    }
  }

  public synchronized void removeListener(ExceptionListener listener) {
    listeners.remove(listener);
  }

  public synchronized void addListener(ExceptionListener listener) {
    listeners.add(listener);
  }

}
