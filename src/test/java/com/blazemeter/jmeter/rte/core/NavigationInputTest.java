package com.blazemeter.jmeter.rte.core;

import com.blazemeter.jmeter.rte.sampler.NavigationType;
import java.awt.Dimension;
import java.util.Arrays;
import java.util.Collection;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class NavigationInputTest {

  private static final Dimension screenSize = new Dimension(80, 24);
  @Rule
  public JUnitSoftAssertions softly = new JUnitSoftAssertions();
  @Parameter
  public int currentRowPos;
  @Parameter(1)
  public int currentColumnPos;
  @Parameter(2)
  public int expectedRowPos;
  @Parameter(3)
  public int expectedColumnPos;
  @Parameter(4)
  public NavigationType navigationType;
  @Parameter(5)
  public int repeated;

  @Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(new Object[][]{
        {1, 1, 24, 80, NavigationType.LEFT, 1},
        {2, 1, 1, 80, NavigationType.LEFT, 1},
        {8, 54, 6, 53, NavigationType.LEFT, 161},
        {23, 80, 24, 1, NavigationType.RIGHT, 1},
        {24, 80, 1, 1, NavigationType.RIGHT, 1},
        {6, 53, 8, 54, NavigationType.RIGHT, 161},
        {1, 1, 24, 1, NavigationType.UP, 1},
        {5, 5, 1, 5, NavigationType.UP, 4},
        {1, 1, 2, 1, NavigationType.UP, 47},
        {1, 2, 2, 2, NavigationType.DOWN, 25},
        {24, 1, 1, 1, NavigationType.DOWN, 1},
        {2, 2, 6, 2, NavigationType.DOWN, 4}
    });
  }

  @Test
  public void shouldProperlyCalculateFinalPosition() {
    NavigationInput input = new NavigationInput(repeated, navigationType, "");
    Position resultedPos = input.calculateInputFinalPosition(new Position(currentRowPos,
        currentColumnPos), screenSize);
    softly.assertThat(resultedPos.getRow()).isEqualTo(expectedRowPos);
    softly.assertThat(resultedPos.getColumn()).isEqualTo(expectedColumnPos);
  }
}
