package com.blazemeter.jmeter.rte.recorder.emulator;

import com.blazemeter.jmeter.rte.protocols.vt420.Vt420Client;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.CharUtils;

public class CharacterSequenceScaper {

  private static final int CHUNK_TEXT_SIZE = 5;
  private static final Map<String, String> SEQUENCES = buildCharacterSequences();

  private static Map<String, String> buildCharacterSequences() {
    ImmutableMapBuilder mapBuilder = new ImmutableMapBuilder()
        .put("\b")
        .put("\n")
        .put("\r")
        .put("\f")
        .put("\'")
        .put("\"")
        .put("\\");
    Vt420Client.NAVIGATION_KEYS.forEach((k, v) -> mapBuilder.put(v));
    Vt420Client.ATTENTION_KEYS.forEach((k, v) -> mapBuilder.put(v));
    return mapBuilder.build();
  }

  public static String getSequenceChunkAppearancesIn(String value) {
    if (value == null) {
      return null;
    }
    StringBuilder sequenceLocation = new StringBuilder();
    for (String sequence : SEQUENCES.keySet()) {
      if (value.contains(sequence)) {
        int index = value.indexOf(sequence);
        int chunkBegin = Math.max(index - CHUNK_TEXT_SIZE, 0);
        int chunkEnd = Math.min(index + CHUNK_TEXT_SIZE, value.length());
        String chunk = value.substring(chunkBegin, chunkEnd);
        sequenceLocation.append(chunkBegin == 0 ? "\n" : "\n...");
        sequenceLocation.append(chunk);
        sequenceLocation.append(chunkEnd == value.length() ? "" : "...");
      }
    }
    return sequenceLocation.toString();
  }

  public static List<String> getSequencesIn(String text) {
    List<String> sequencesAppeared = new ArrayList<>();
    SEQUENCES.forEach((k, v) -> {
      if (text.contains(k)) {
        sequencesAppeared.add(v);
      }
    });
    return sequencesAppeared;
  }

  public static class ImmutableMapBuilder {

    private Map<String, String> map = new HashMap<>();

    public ImmutableMapBuilder put(String key) {
      map.put(key, CharUtils.unicodeEscaped(key.charAt(0)));
      return this;
    }

    public Map<String, String> build() {
      return map;
    }

  }
}
