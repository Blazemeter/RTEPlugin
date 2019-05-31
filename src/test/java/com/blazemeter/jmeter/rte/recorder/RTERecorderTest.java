package com.blazemeter.jmeter.rte.recorder;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

public class RTERecorderTest {

  /**
   * TODO:
   * Comments
   * RTERecorder
   * [ ] Create a constructor of RTERecorder that receives a Supplier<TerminalEmulator> and
   * is initialized by default to Xtn5250TerminalEmulator::new
   * [ ] Use it instead of the new of Xtn5250TerminalEmulator.
   *
   * This will allow you to provide a different supplier for tests which return mocked
   * instances to TerminalEmulator.
   *
   * RTERecorder Test
   * [ ] Provide a MOCKED RecordingTargetFinder in constructor which always returns
   * the same tree node, and allows to verify elements added to it.
   *
   * [ ] In (@Before) setup add the RTERecorder to the tree node and a
   * MOCKED TestStateListener and SampleListener as children of recorder
   * in tree model, so you can verify if it is called in later test
   * Provide a protocolFactory (as we do in RTESampler) to be able to provide
   * mocked instances of client for verifying connect and getting expected
   * logic (status of keyboard etc)
  **/

  @Mock
  private RecordingTargetFinder finder;

  private RTERecorder rteRecorder;

  @Before
  public void setup(){

  }

  @Test
  public void shouldAddRecorderAsEmulatorListenerWhenStart(){}

  @Test
  public void shouldAddRteConfigToTargetControllerNodeWhenStart(){}

  @Test
  public void shouldNotifyChildrenTestStateListenersWhenStart(){}

  @Test
  public void shouldLockEmulatorKeyboardWhenStart(){}

  @Test
  public void shouldStartEmulatorWhenStart(){}

  @Test
  public void shouldConnectTerminalClientWhenStart(){}

  @Test
  public void shouldAddAEmulatorUpdaterAsTerminalStateListenerWhenStart(){}

  @Test
  public void shouldAddARequestListenerAsTerminalStateListenerWhenStart(){}

  @Test
  public void shouldStopTerminalEmulatorWhenStartWithFailingTerminalClientConnect(){}

  @Test
  public void shouldDisconnectTerminalClientWhenStartWithFailingTerminalClientConnect(){}

  @Test
  public void shouldNotifyChildrenTestStateListenersWhenStop(){}

  @Test
  public void shouldRemoveEmulatorUpdaterWhenStop(){}

  @Test
  public void shouldStopEmulatorWhenStop(){}

  @Test
  public void shouldDisconnectTerminalClientWhenStop(){}

  @Test
  public void shouldNotifyChildrenTestStateListenersWhenCloseTerminal(){}

  @Test
  public void shouldRemoveEmulatorUpdaterWhenCloseTerminal(){}

  @Test
  public void shouldStopEmulatorWhenCloseTerminal(){}

  @Test
  public void shouldDisconnectTerminalClientWhenCloseTerminal(){}

  @Test
  public void shouldNotifyRecordingListenerWhenCloseTerminal(){}

  @Test
  public void shouldLockEmulatorKeyboardWhenAttentionKey(){}

  @Test
  public void shouldResetTerminalClientAlarmWhenAttentionKey(){}

  @Test
  public void shouldNotifySuccessfulSampleResultToChildrenSampleListenersWhenAttentionKeyAndSuccessfulResult(){}

  @Test
  public void shouldNotifyFailedSampleResultToChildrenSampleListenersWhenAttentionKeyAndFailingSendingToTerminalClient (){}
//(I think we have a bug here, an if is missing before RTESampler.updateSampleResultResponse)

  @Test
  public void shouldAddSamplerToTargetControllerNodeWhenAttentionKey(){}

  @Test
  public void shouldRegisterRequestListenerWhenAttentionKey(){}

  @Test
  public void shouldSendInputsAndAttentionKeyToTerminalClientWhenAttentionKey(){}

  @Test
  public void shouldSetEmulatorStatusMessageWhenAttentionKeyWithFailingTerminalClientSend(){}

}
