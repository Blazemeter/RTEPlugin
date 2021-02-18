package com.blazemeter.jmeter.rte.sampler.gui;

import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BlazemeterLabsLogo extends ThemedIconLabel {

  private static final Logger LOG = LoggerFactory.getLogger(BlazemeterLabsLogo.class);

  public BlazemeterLabsLogo() {
    super("blazemeter-labs-logo.png");
    setBrowseOnClick("https://github.com/Blazemeter/RTEPlugin");
  }

  private void setBrowseOnClick(String url) {
    setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent mouseEvent) {
        if (Desktop.isDesktopSupported()) {
          try {
            Desktop.getDesktop().browse(new URI(url));
          } catch (IOException | URISyntaxException exception) {
            LOG.error("Problem when accessing repository", exception);
          }
        }
      }
    });
  }
}
