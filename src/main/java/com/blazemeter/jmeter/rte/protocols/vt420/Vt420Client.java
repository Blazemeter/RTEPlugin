package com.blazemeter.jmeter.rte.protocols.vt420;

import com.blazemeter.jmeter.rte.core.AttentionKey;
import com.blazemeter.jmeter.rte.core.BaseProtocolClient;
import com.blazemeter.jmeter.rte.core.CharacterBasedProtocolClient;
import com.blazemeter.jmeter.rte.core.Input;
import com.blazemeter.jmeter.rte.core.NavigationInput;
import com.blazemeter.jmeter.rte.core.Position;
import com.blazemeter.jmeter.rte.core.Screen;
import com.blazemeter.jmeter.rte.core.TerminalType;
import com.blazemeter.jmeter.rte.core.exceptions.ConnectionClosedException;
import com.blazemeter.jmeter.rte.core.exceptions.RteIOException;
import com.blazemeter.jmeter.rte.core.listener.ExceptionHandler;
import com.blazemeter.jmeter.rte.core.listener.TerminalStateListener;
import com.blazemeter.jmeter.rte.core.ssl.SSLType;
import com.blazemeter.jmeter.rte.core.wait.ConditionWaiter;
import com.blazemeter.jmeter.rte.core.wait.CursorWaitCondition;
import com.blazemeter.jmeter.rte.core.wait.SilentWaitCondition;
import com.blazemeter.jmeter.rte.core.wait.SyncWaitCondition;
import com.blazemeter.jmeter.rte.core.wait.TextWaitCondition;
import com.blazemeter.jmeter.rte.core.wait.WaitCondition;
import com.blazemeter.jmeter.rte.protocols.vt420.listeners.ScreenTextListener;
import com.blazemeter.jmeter.rte.protocols.vt420.listeners.SilenceListener;
import com.blazemeter.jmeter.rte.protocols.vt420.listeners.UnlockListener;
import com.blazemeter.jmeter.rte.protocols.vt420.listeners.VisibleCursorListener;
import com.blazemeter.jmeter.rte.protocols.vt420.listeners.Vt420TerminalStateListenerProxy;
import com.blazemeter.jmeter.rte.sampler.NavigationType;
import com.blazemeter.jmeter.rte.sampler.RTESampler;
import java.awt.Dimension;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import nl.lxtreme.jvt220.terminal.ExceptionListener;
import nl.lxtreme.jvt220.terminal.ScreenChangeListener;
import nl.lxtreme.jvt220.terminal.TerminalClient;
import org.apache.commons.net.telnet.InvalidTelnetOptionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Vt420Client extends BaseProtocolClient implements CharacterBasedProtocolClient {

  public static final Map<NavigationType, String> NAVIGATION_KEYS = buildNavigationKeysMapping();
  public static final Map<AttentionKey, String> ATTENTION_KEYS = buildAttKeysMapping();
  private static final Logger LOG = LoggerFactory.getLogger(Vt420Client.class);
  private TerminalClient client;
  private Map<TerminalStateListener, Vt420TerminalStateListenerProxy> listeners =
      new ConcurrentHashMap<>();

  private static EnumMap<NavigationType, String> buildNavigationKeysMapping() {
    return new EnumMap<NavigationType, String>(NavigationType.class) {
      {
        put(NavigationType.TAB, "\t");
        put(NavigationType.UP, "\033[A");
        put(NavigationType.DOWN, "\033[B");
        put(NavigationType.LEFT, "\033[D");
        put(NavigationType.RIGHT, "\033[C");
      }
    };
  }

  private static EnumMap<AttentionKey, String> buildAttKeysMapping() {
    return new EnumMap<AttentionKey, String>(AttentionKey.class) {
      {
        put(AttentionKey.ENTER, "\r");
        put(AttentionKey.F1, "\033[11~");
        put(AttentionKey.F2, "\033[12~");
        put(AttentionKey.F3, "\033[13~");
        put(AttentionKey.F4, "\033[14~");
        put(AttentionKey.F5, "\033[15~");
        put(AttentionKey.F6, "\033[17~");
        put(AttentionKey.F7, "\033[18~");
        put(AttentionKey.F8, "\033[19~");
        put(AttentionKey.F9, "\033[20~");
        put(AttentionKey.F10, "\033[21~");
        put(AttentionKey.F11, "\033[23~");
        put(AttentionKey.F12, "\033[24~");
        put(AttentionKey.F13, "\033[25~");
        put(AttentionKey.F14, "\033[26~");
        put(AttentionKey.F15, "\033[28~");
        put(AttentionKey.F16, "\033[29~");
        put(AttentionKey.F17, "\033[31~");
        put(AttentionKey.F18, "\033[32~");
        put(AttentionKey.F19, "\033[33~");
        put(AttentionKey.F20, "\033[34~");
        put(AttentionKey.F21, "\033[35~");
        put(AttentionKey.F22, "\033[36~");
        put(AttentionKey.F23, "\033[37~");
        put(AttentionKey.F24, "\033[38~");
        put(AttentionKey.ROLL_UP, "\033[5~");
        put(AttentionKey.ROLL_DN, "\033[6~");
      }
    };
  }

  @Override
  protected void setField(Input input, long echoTimeoutMillis) {
    if (input instanceof NavigationInput) {
      NavigationInput navigationInput = (NavigationInput) input;
      NavigationType navigationType = navigationInput.getNavigationType();
      if (NAVIGATION_KEYS.get(navigationType) != null) {
        processArrowKey(echoTimeoutMillis, navigationInput);
      } else {
        LOG.error("Navigation type {} not supported", navigationType);
        throw new IllegalArgumentException("Navigation type not supported");
      }
    } else {
      LOG.error("{} not supported for VT420 protocol. Use Navigation inputs instead",
          input.getClass().getSimpleName());
      throw new IllegalArgumentException("Not supported input: " + input.getClass());
    }
  }

  private List<String> textToList(String text) {
    return text.chars().mapToObj(c -> ((char) c)).collect(Collectors.toList()).stream()
        .map(String::valueOf)
        .collect(Collectors.toList());
  }

  private void sendCharacterByOneAtATime(List<String> text, long timeout) {
    Semaphore semaphore = new Semaphore(0);
    ScreenChangeListener listener = s -> semaphore.release();
    client.addScreenChangeListener(listener);
    try {
      for (String character : text) {
        client.sendTextByCurrentCursorPosition(character);
        if (!semaphore.tryAcquire(timeout, TimeUnit.MILLISECONDS)) {
          exceptionHandler.setPendingError(
              new TimeoutException(
                  "No server response after waiting '" + timeout + "' milliseconds when sending "
                      + "'" + character + "' character of '" + text + "'."));
          LOG.warn("If you consider that the character timeout is too low "
              + "you can change the value by adding the line `RTEConnectionConfig"
              + ".characterTimeoutMillis=<time_in_millis>` in the jmeter.properties file.");
        }
      }
    } catch (IOException e) {
      exceptionHandler.setPendingError(e);
    } catch (InterruptedException ex) {
      LOG.debug("Send of '{}' has been interrupted", text, ex);
      exceptionHandler.setPendingError(ex);
      Thread.currentThread().interrupt();
    }
    client.removeScreenChangeListener(listener);
  }

  private void processArrowKey(long echoTimeoutMillis, NavigationInput navigationInput) {
    List<String> input = new ArrayList<>();
    IntStream.range(0, navigationInput.getRepeat())
        .forEach(e -> input.add(NAVIGATION_KEYS.get(navigationInput.getNavigationType())));
    input.addAll(textToList(navigationInput.getInput()));
    sendCharacterByOneAtATime(input, echoTimeoutMillis);
  }

  protected void sendAttentionKey(AttentionKey attentionKey) {
    String input = ATTENTION_KEYS.get(attentionKey);
    if (input == null) {
      throw new UnsupportedOperationException(
          attentionKey.name() + " attentionKey is unsupported for protocol VT420.");
    }
    try {
      client.sendTextByCurrentCursorPosition(input);
    } catch (IOException e) {
      exceptionHandler.setPendingError(e);
    }
  }

  @Override
  protected ConditionWaiter<?> buildWaiter(WaitCondition waitCondition) {
    if (waitCondition instanceof SyncWaitCondition) {
      return new UnlockListener((SyncWaitCondition) waitCondition, this, stableTimeoutExecutor,
          exceptionHandler);
    } else if (waitCondition instanceof TextWaitCondition) {
      return new ScreenTextListener((TextWaitCondition) waitCondition, this,
          stableTimeoutExecutor,
          exceptionHandler);
    } else if (waitCondition instanceof SilentWaitCondition) {
      return new SilenceListener((SilentWaitCondition) waitCondition, this, stableTimeoutExecutor,
          exceptionHandler);
    } else if (waitCondition instanceof CursorWaitCondition) {
      return new VisibleCursorListener((CursorWaitCondition) waitCondition, this,
          stableTimeoutExecutor,
          exceptionHandler);
    } else {
      throw new UnsupportedOperationException("Wait condition not supported yet.");
    }
  }

  @Override
  protected void doDisconnect() {
    try {
      stableTimeoutExecutor.shutdownNow();
      stableTimeoutExecutor = null;
      client.disconnect();
    } catch (IOException e) {
      exceptionHandler.setPendingError(new RteIOException(e, ""));
    }
  }

  @Override
  public List<TerminalType> getSupportedTerminalTypes() {
    return Collections.singletonList(
        new TerminalType("VT420-7", new Dimension(80, 24)));
  }

  @Override
  public void connect(String server, int port, SSLType sslType, TerminalType terminalType,
      long timeoutMillis) throws RteIOException {
    client = new TerminalClient(terminalType.getScreenSize(), terminalType.getId());
    stableTimeoutExecutor = Executors.newSingleThreadScheduledExecutor(NAMED_THREAD_FACTORY);
    exceptionHandler = new ExceptionHandler(server);
    client.setSocketFactory(getSocketFactory(sslType, server));
    client.setExceptionListener(new ExceptionListener() {
      @Override
      public void onException(Throwable throwable) {
        exceptionHandler.setPendingError(throwable);
      }

      @Override
      public void onConnectionClosed() {
        exceptionHandler.setPendingError(new ConnectionClosedException());
      }
    });

    listeners.forEach((stateListener, listenerProxy) -> client
        .addScreenChangeListener(listeners.get(stateListener)));

    try {
      client.connect(server, port, (int) timeoutMillis);
    } catch (IOException | InvalidTelnetOptionException e) {
      LOG.error("Connection error: ", e);
      throw new RteIOException(new Throwable("Connection error"), server);
    }

    exceptionHandler.throwAnyPendingError();
  }

  @Override
  public void addTerminalStateListener(TerminalStateListener terminalStateListener) {
    Vt420TerminalStateListenerProxy listenerProxy = new Vt420TerminalStateListenerProxy(
        terminalStateListener);
    listeners.put(terminalStateListener, listenerProxy);
    if (client != null) {
      client.addScreenChangeListener(listenerProxy);
    }
  }

  @Override
  public void removeTerminalStateListener(TerminalStateListener terminalStateListener) {
    Vt420TerminalStateListenerProxy listenerProxy = listeners.remove(terminalStateListener);
    if (client != null) {
      client.removeScreenChangeListener(listenerProxy);
    }
  }

  @Override
  public Screen getScreen() {
    return Screen.buildScreenFromText(client.getScreen(), client.getScreenSize());
  }

  @Override
  public Optional<Boolean> isInputInhibited() {
    return Optional.empty();
  }

  @Override
  public Optional<Position> getCursorPosition() {
    return client.getCursorPosition().map(point -> new Position(point.y + 1, point.x + 1));
  }

  @Override
  public boolean isAlarmOn() {
    return false;
  }

  @Override
  public boolean resetAlarm() {
    return false;
  }

  @Override
  public Set<AttentionKey> getSupportedAttentionKeys() {
    return ATTENTION_KEYS.keySet();
  }

  @Override
  public void addScreenChangeListener(ScreenChangeListener listener) {
    client.addScreenChangeListener(listener);
  }

  @Override
  public void removeScreenChangeListener(ScreenChangeListener listener) {
    client.removeScreenChangeListener(listener);
  }

  @Override
  public void send(String character) {
    sendCharacterByOneAtATime(Collections.singletonList(character),
        RTESampler.getCharacterTimeout());
  }

  public void setExceptionHandler(ExceptionHandler exceptionHandler) {
    this.exceptionHandler = exceptionHandler;
  }
}
