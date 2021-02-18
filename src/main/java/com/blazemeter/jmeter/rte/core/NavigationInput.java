package com.blazemeter.jmeter.rte.core;

import com.blazemeter.jmeter.rte.sampler.NavigationType;
import java.awt.Dimension;
import java.util.Objects;

public class NavigationInput extends Input {

  private int repeat;
  private NavigationType navigationType;

  public NavigationInput(int repeat, NavigationType navigationType, String value) {
    super(value);
    this.repeat = repeat;
    this.navigationType = navigationType;
  }

  private NavigationInput(NavigationInputBuilder builder) {
    input = builder.input;
    setRepeat(builder.repeat);
    setNavigationType(builder.navigationType);
  }

  public Position calculateInputFinalPosition(Position currentPos,
      Dimension screenSize) {
    int finalRowPos = currentPos.getRow();
    int finalColPos = currentPos.getColumn();
    switch (navigationType) {
      case DOWN:
        finalRowPos = Math.floorMod(finalRowPos + repeat, screenSize.height);
        break;
      case UP:
        finalRowPos = finalRowPos - repeat;
        if (finalRowPos < 1) {
          finalRowPos = finalRowPos == 0 ? screenSize.height
              : Math.floorMod(Math.abs(finalRowPos) > screenSize.height ? finalRowPos
                      : Math.abs(finalRowPos),
                  screenSize.height);
        }
        break;
      case LEFT:
        finalColPos = finalColPos - repeat;
        while (finalColPos < 1) {
          finalColPos = screenSize.width + finalColPos;
          finalRowPos = finalRowPos == 1 ? screenSize.height : --finalRowPos;
        }
        break;
      case RIGHT:
        finalColPos = finalColPos + repeat;
        while (finalColPos > screenSize.width) {
          finalColPos = finalColPos - screenSize.width;
          finalRowPos = finalRowPos == screenSize.height ? 1 : ++finalRowPos;
        }
        break;
      default:
        throw new UnsupportedOperationException(
            "Invalid arrow navigation type (" + navigationType + ")");
    }
    return new Position(finalRowPos, finalColPos);
  }

  @Override
  public String getCsv() {
    return "<" + navigationType + "*" + repeat + ">," + input;
  }

  public int getRepeat() {
    return repeat;
  }

  public void setRepeat(int repeat) {
    this.repeat = repeat;
  }

  public NavigationType getNavigationType() {
    return navigationType;
  }

  public void setNavigationType(NavigationType navigationType) {
    this.navigationType = navigationType;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    NavigationInput that = (NavigationInput) o;
    return repeat == that.repeat &&
        navigationType == that.navigationType;
  }

  @Override
  public int hashCode() {
    return Objects.hash(repeat, navigationType);
  }

  @Override
  public String toString() {
    return getCsv();
  }

  public static final class NavigationInputBuilder {

    private String input;
    private int repeat;
    private NavigationType navigationType;

    public NavigationInputBuilder() {
    }

    public NavigationInputBuilder withInput(String val) {
      input = val;
      return this;
    }

    public NavigationInputBuilder withRepeat(int val) {
      repeat = val;
      return this;
    }

    public NavigationInputBuilder withNavigationType(NavigationType val) {
      navigationType = val;
      return this;
    }

    public NavigationInput build() {
      return new NavigationInput(this);
    }

    public NavigationType getNavigationType() {
      return navigationType;
    }
  }
}
