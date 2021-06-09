package com.blazemeter.jmeter.rte.recorder.emulator;

import com.blazemeter.jmeter.rte.core.AttentionKey;
import com.blazemeter.jmeter.rte.core.Position;
import com.blazemeter.jmeter.rte.core.RteProtocolClient;
import com.blazemeter.jmeter.rte.core.Screen;
import com.blazemeter.jmeter.rte.sampler.gui.SwingUtils;
import com.helger.commons.annotation.VisibleForTesting;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import net.infordata.em.crt5250.XI5250Crt;
import net.infordata.em.crt5250.XI5250Field;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.Perl5Compiler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Xtn5250TerminalEmulator extends JFrame implements TerminalEmulator {

  private static final String TITLE = "Recorder";
  private static final Color BACKGROUND = Color.black;
  private static final int DEFAULT_FONT_SIZE = 14;
  private static final Logger LOG = LoggerFactory.getLogger(Xtn5250TerminalEmulator.class);
  private final JButton waitForTextButton = SwingUtils.createIconButton("waitForTextButton",
      "waitForText.png");
  private final JButton copyButton = SwingUtils.createIconButton("copyButton", "copy.png");
  private final JButton pasteButton = SwingUtils.createIconButton("pasteButton", "paste.png");
  private final JButton labelButton = SwingUtils
      .createIconButton("labelButton", "inputByLabel.png");
  private final JButton assertionButton = SwingUtils
      .createIconButton("assertionButton", "assertion.png");
  private final JLabel sampleNameLabel = new JLabel("Sample name: ");
  private final JTextField sampleNameField = SwingUtils
      .createComponent("sampleNameField", new JTextField());
  private final List<TerminalEmulatorListener> terminalEmulatorListeners = new ArrayList<>();
  private boolean shownCredentials = false;
  private boolean stopping;
  private final StatusPanel statusPanel = new StatusPanel();
  private XI5250CrtBase xi5250Crt;

  public Xtn5250TerminalEmulator(XI5250CrtBase xi5250Crt) {
    this.xi5250Crt = xi5250Crt;
    xi5250Crt.setPasteEnableConsumer((isPaste) -> pasteButton.setEnabled((Boolean) isPaste));
    xi5250Crt.setName("Terminal");
    xi5250Crt.setDefBackground(BACKGROUND);
    xi5250Crt.setBlinkingCursor(true);
    xi5250Crt.setEnabled(true);
    setTitle(TITLE);
    setLayout(new BorderLayout());
    add(createToolsPanel(), BorderLayout.NORTH);
    add(xi5250Crt, BorderLayout.CENTER);
    add(statusPanel, BorderLayout.SOUTH);
    addWindowListener(new WindowAdapter() {
      @Override
      public void windowOpened(WindowEvent e) {
        Dimension testSize = calculateCrtDefaultSize();
        xi5250Crt.setSize(testSize.width, testSize.height);
        pack();
        xi5250Crt.requestFocus();
      }

      @Override
      public void windowClosed(WindowEvent e) {
        if (!stopping) {
          for (TerminalEmulatorListener g : terminalEmulatorListeners) {
            g.onCloseTerminal();
          }
        }
        statusPanel.dispose();
      }
    });
    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    statusPanel.getShowCredentials().addMouseListener(buildOnShowCredentials());
    setButtonListeners();
    modificationSamplerListener();
    xi5250Crt.setStatusPanel(statusPanel);
    validateComponentsForCharacterEmulator();
  }

  private void validateComponentsForCharacterEmulator() {
    if (xi5250Crt instanceof CharacterBasedEmulator) {
      statusPanel.setBlockedCursorVisible();
      labelButton.setVisible(false);
    }
  }

  private MouseListener buildOnShowCredentials() {
    return new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        shownCredentials = !shownCredentials;
        LOG.info("Credentials are now {}", shownCredentials ? "visible" : "hidden");
        statusPanel.updateShowCredentials(shownCredentials);
        updateFieldsVisibility();
      }
    };
  }

  private void updateFieldsVisibility() {
    for (XI5250Field field : xi5250Crt.getFields()) {
      int attr = !shownCredentials ? ((ScreenField) field).originalAttr
          : XI5250CrtBase.DEFAULT_ATTR;
      updateFieldAttribute(field.getRow(), field.getCol(), attr);
      field.init();
    }
  }

  private void updateFieldAttribute(int row, int column, int attr) {
    xi5250Crt.drawString("\u0001", column - 1, row, attr);
  }

  private JPanel createToolsPanel() {
    JPanel toolsPanel = new JPanel();
    GroupLayout layout = new GroupLayout(toolsPanel);
    layout.setAutoCreateContainerGaps(true);
    toolsPanel.setLayout(layout);
    JPanel sampleNamePanel = buildSampleNamePanel();
    JPanel fillPanel = new JPanel();

    copyButton.setToolTipText("Copy");
    pasteButton.setToolTipText("Paste");
    labelButton.setToolTipText("Input by label");
    waitForTextButton.setToolTipText("Text wait condition");
    assertionButton.setToolTipText("Assertion");
    layout.setHorizontalGroup(layout.createSequentialGroup()
        .addComponent(copyButton)
        .addComponent(pasteButton)
        .addComponent(labelButton)
        .addComponent(waitForTextButton)
        .addComponent(assertionButton)
        .addPreferredGap(ComponentPlacement.RELATED)
        .addComponent(fillPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
            Short.MAX_VALUE)
        .addGroup(layout.createParallelGroup()
            .addComponent(sampleNamePanel)
        )
    );
    layout.setVerticalGroup(layout.createParallelGroup(Alignment.BASELINE)
        .addComponent(sampleNamePanel, Alignment.CENTER)
        .addComponent(copyButton)
        .addComponent(pasteButton)
        .addComponent(labelButton)
        .addComponent(waitForTextButton)
        .addComponent(assertionButton)
        .addComponent(fillPanel)
    );
    return toolsPanel;
  }

  private JPanel buildSampleNamePanel() {
    JPanel sampleNamePanel = new JPanel();
    GroupLayout layout = new GroupLayout(sampleNamePanel);
    sampleNamePanel.setLayout(layout);
    sampleNameField.setFont(new Font("SansSerif", Font.PLAIN, 11));
    layout.setHorizontalGroup(layout.createSequentialGroup()
        .addComponent(sampleNameLabel)
        .addPreferredGap(ComponentPlacement.RELATED)
        .addComponent(sampleNameField, GroupLayout.PREFERRED_SIZE, 150,
            GroupLayout.PREFERRED_SIZE));
    layout.setVerticalGroup(layout.createParallelGroup(Alignment.BASELINE)
        .addComponent(sampleNameLabel)
        .addComponent(sampleNameField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
            GroupLayout.PREFERRED_SIZE));

    return sampleNamePanel;
  }

  @Override
  public void start() {
    setVisible(true);
  }

  @Override
  public void setScreenSize(int columns, int rows) {
    xi5250Crt.setCrtSize(columns, rows);
  }

  private Dimension calculateCrtDefaultSize() {
    FontMetrics fm = xi5250Crt.getFontMetrics(
        new Font(xi5250Crt.getFont().getName(), xi5250Crt.getFont().getStyle(),
            DEFAULT_FONT_SIZE));
    return new Dimension(fm.charWidth('W') * xi5250Crt.getCrtSize().width,
        fm.getHeight() * xi5250Crt.getCrtSize().height);
  }

  @Override
  public void stop() {
    if (!stopping) {
      stopping = true;
      xi5250Crt.teardown();
      dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
      xi5250Crt = null;
    }
  }

  @Override
  public void setCursor(int row, int col) {
    xi5250Crt.setInitialCursorPos(col - 1, row - 1);
    this.statusPanel.updateStatusBarCursorPosition(row, col);
  }

  @VisibleForTesting
  public String getScreen() {
    int height = xi5250Crt.getCrtSize().height;
    int width = xi5250Crt.getCrtSize().width;
    StringBuilder screen = new StringBuilder();
    for (int i = 0; i < height; i++) {
      screen.append(xi5250Crt.getString(0, i, width).replaceAll("[\\x00-\\x19]", " "));
      screen.append("\n");
    }
    return screen.toString();
  }

  @Override
  public void setScreen(Screen screen) {
    /*
    setScreen delegated to XI5250CrtBase in order to proper synchronize setScreen, 
    paintComponent and processKeyEvent methods.
    */
    xi5250Crt.setScreen(screen, shownCredentials);
  }

  @Override
  public void setScreenName(String screenName) {
    sampleNameField.setText(screenName);
  }

  @VisibleForTesting
  public void setSelectedArea(Rectangle rectangle) {
    xi5250Crt.setSelectedArea(rectangle);
  }

  @Override
  public void soundAlarm() {
    Toolkit.getDefaultToolkit().beep();
    statusPanel.soundAlarm();
  }

  @Override
  public void setKeyboardLock(boolean lock) {
    xi5250Crt.setKeyboardLock(lock);
  }

  @Override
  public void addTerminalEmulatorListener(TerminalEmulatorListener terminalEmulatorListener) {
    terminalEmulatorListeners.add(terminalEmulatorListener);
    xi5250Crt.setTerminalEmulatorListeners(terminalEmulatorListeners);
  }

  @Override
  public void setSupportedAttentionKeys(Set<AttentionKey> supportedAttentionKeys) {
    xi5250Crt.setSupportedAttentionKeys(supportedAttentionKeys);
  }

  @Override
  public void setProtocolClient(RteProtocolClient terminalClient) {
    xi5250Crt.setProtocolClient(terminalClient);
  }

  private void setButtonListeners() {
    copyButton.addActionListener(e -> {
      xi5250Crt.doCopy();
      xi5250Crt.requestFocus();
    });
    pasteButton.addActionListener(e -> {
      xi5250Crt.makePaste();
      xi5250Crt.requestFocus();
    });
    labelButton.addActionListener(e -> {
      String labelText = xi5250Crt.getStringSelectedArea();
      if (labelText == null) {
        warnUserOfNotScreenSelectedArea("input by label");
      } else if (labelText.contains("\n")) {
        showUserMessage("Please try again selecting one row");
        LOG.warn(
            "Input by label does not support multiple selected rows, "
                + "please select just one row.");
      } else if (labelText.trim().isEmpty()) {
        showUserMessage("Please select a non empty or blank text \nto be used as input by label");
        LOG.warn(
            "Selected text is composed only by spaces.");
      } else {
        XI5250Field field = xi5250Crt
            .getNextFieldFromPos(xi5250Crt.getSelectedArea().x, xi5250Crt.getSelectedArea().y);

        if (isFieldValid(field)) {
          xi5250Crt.saveLabelWithPosition(new Position(field.getRow() + 1, field.getCol() + 1),
              labelText.trim());
        } else {
          showUserMessage("No input fields found near to \"" + labelText + "\".");
          LOG.warn("No field was found after specified label {}", labelText);
        }

      }

      xi5250Crt.requestFocus();
      xi5250Crt.clearSelectedArea();
    });

    waitForTextButton.addActionListener(e -> {
      String selectedText = xi5250Crt.getStringSelectedArea();

      if (selectedText != null) {
        for (TerminalEmulatorListener listener : terminalEmulatorListeners) {
          listener.onWaitForText(selectedText);
        }
      } else {
        warnUserOfNotScreenSelectedArea("wait text condition");
      }
      xi5250Crt.requestFocus();
      xi5250Crt.clearSelectedArea();
    });

    assertionButton.addActionListener(e -> {
      String selectedText = xi5250Crt.getStringSelectedArea();
      if (selectedText != null) {
        String assertionName = requestAssertionName();
        if (assertionName != null) {
          Pattern pattern = JMeterUtils
              .getPattern(Perl5Compiler.quotemeta(selectedText).replace("\\\n", ".*\\n.*"));

          for (TerminalEmulatorListener listener : terminalEmulatorListeners) {
            listener
                .onAssertionScreen(assertionName, pattern.getPattern());
          }
        }
      } else {
        warnUserOfNotScreenSelectedArea("assertion");
      }
      xi5250Crt.requestFocus();
      xi5250Crt.clearSelectedArea();
    });
  }

  private boolean isFieldValid(XI5250Field field) {
    int width = xi5250Crt.getCrtSize().width;
    int linearLabelPosition = width * xi5250Crt.getSelectedArea().y + xi5250Crt
        .getSelectedArea().x;
    int linearFieldPosition = width * field.getRow() + field.getCol();
    return linearFieldPosition > linearLabelPosition;
  }

  private void warnUserOfNotScreenSelectedArea(String usage) {
    showUserMessage("Please select a part of the screen");
    LOG.warn(
        "The selection of a screen area is essential to "
            + "be used as {} later on.", usage);
  }

  private void showUserMessage(String msg) {
    JOptionPane.showMessageDialog(this, msg, "Info", JOptionPane.INFORMATION_MESSAGE);
  }

  private String requestAssertionName() {
    return JOptionPane.showInputDialog(this, "Insert name of assertion", "Response Assertion");
  }

  private void modificationSamplerListener() {
    sampleNameField.getDocument().addDocumentListener(
        new DocumentListener() {
          @Override
          public void insertUpdate(DocumentEvent e) {
            xi5250Crt.setSampleSame(sampleNameField.getText());
          }

          @Override
          public void removeUpdate(DocumentEvent e) {

            xi5250Crt.setSampleSame(sampleNameField.getText());
          }

          @Override
          public void changedUpdate(DocumentEvent e) {

            xi5250Crt.setSampleSame(sampleNameField.getText());
          }
        }
    );
  }

  @VisibleForTesting
  public List getInputs() {
    return xi5250Crt.getInputFields();
  }

  public static class ScreenField extends XI5250Field {

    private final int originalAttr;

    public ScreenField(XI5250Crt aCrt, int aCol, int aRow, int aLen, int aAttr) {
      super(aCrt, aCol, aRow, aLen, aAttr);
      originalAttr = aAttr;
    }

  }
}
