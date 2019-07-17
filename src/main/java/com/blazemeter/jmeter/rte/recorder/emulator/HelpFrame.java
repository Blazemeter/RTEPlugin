package com.blazemeter.jmeter.rte.recorder.emulator;

import com.blazemeter.jmeter.rte.sampler.gui.ThemedIcon;
import java.awt.CardLayout;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.IOException;
import java.net.URISyntaxException;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HelpFrame extends JFrame {

  private static final Logger LOG = LoggerFactory.getLogger(HelpFrame.class);
  private static final String HELP_FRAME_TITLE = "Help";

  public HelpFrame() {
    setTitle(HELP_FRAME_TITLE);
    setName("helpFrame");
    setLayout(new CardLayout());
    setLayout(new CardLayout(10, 10));
    JTextPane textPane = new JTextPane();
    textPane.setContentType("text/html");
    textPane.setText(buildHelpHtml());
    textPane.setCaretPosition(0);
    textPane.setEditable(false);
    textPane.setOpaque(false);
    textPane.addHyperlinkListener(buildOpenBrowserLinkListener());
    JScrollPane scrollPane = new JScrollPane(textPane);
    add(scrollPane);
    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    pack();
  }

  private String buildHelpHtml() {
    try {
      String helpHtmlPath = "/recorder-help.html";
      String helpHtml = IOUtils
          .toString(HelpFrame.class.getResourceAsStream(helpHtmlPath), "UTF-8");
      return helpHtml.replace("{{resourcesPath}}", ThemedIcon.getResourcePath());
    } catch (IOException e) {
      LOG.error("Error when loading help panel", e);
      return "PROBLEM LOADING HELP!, check logs.";
    }
  }

  private HyperlinkListener buildOpenBrowserLinkListener() {
    return event -> {
      if (HyperlinkEvent.EventType.ACTIVATED.equals(event.getEventType())) {
        Desktop desktop = Desktop.getDesktop();
        try {
          desktop.browse(event.getURL().toURI());
        } catch (IOException | URISyntaxException exception) {
          LOG.error("Problem when accessing repository", exception);
        }
      }
    };
  }

  public void open() {
    Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
    setSize((int) screen.getWidth() / 3, (int) screen.getHeight() / 2);
    setVisible(true);
    requestFocus();
  }

  public void close() {
    dispose();
  }

}
