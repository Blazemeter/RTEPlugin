package com.blazemeter.jmeter.rte;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.jmeter.samplers.SampleResult;

public class SampleResultAssertions {

  public static void assertSampleResult(SampleResult expected, SampleResult result) {
    assertThat(result)
        .isEqualToComparingOnlyGivenFields(expected, "sampleLabel", "requestHeaders", "samplerData",
            "successful", "responseCode", "responseMessage", "responseHeaders", "dataType",
            "responseDataAsString");
  }

}
