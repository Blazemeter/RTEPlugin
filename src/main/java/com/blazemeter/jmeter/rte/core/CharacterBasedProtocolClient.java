package com.blazemeter.jmeter.rte.core;

import java.util.List;
import nl.lxtreme.jvt220.terminal.ScreenChangeListener;

public interface CharacterBasedProtocolClient extends RteProtocolClient {

  void addScreenChangeListener(ScreenChangeListener listener);

  void removeScreenChangeListener(ScreenChangeListener listener);

  void send(List<String> input, long echoTimeoutMillis);

}
