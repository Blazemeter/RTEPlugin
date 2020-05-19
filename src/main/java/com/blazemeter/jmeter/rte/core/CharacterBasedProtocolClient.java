package com.blazemeter.jmeter.rte.core;

import nl.lxtreme.jvt220.terminal.ScreenChangeListener;

public interface CharacterBasedProtocolClient extends RteProtocolClient {

  void addScreenChangeListener(ScreenChangeListener listener);

  void removeScreenChangeListener(ScreenChangeListener listener);

  void send(String character);
}
