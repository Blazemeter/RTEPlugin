package com.blazemeter.jmeter.rte.recorder;

public interface RecordingStateListener {

  void onRecordingStart();

  void onRecordingStop();
  
  void onRecordingException(Exception e);

}
