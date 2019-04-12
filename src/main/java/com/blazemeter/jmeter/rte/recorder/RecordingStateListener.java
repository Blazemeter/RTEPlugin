package com.blazemeter.jmeter.rte.recorder;

public interface RecordingStateListener {

  void onRecordingStart() throws Exception;

  void onRecordingStop();

}
