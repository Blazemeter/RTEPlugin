package com.bytezone.dm3270.display;

import com.bytezone.dm3270.attributes.ColorAttribute;
import java.awt.Color;

/**
 * Class extracted from dm3270 source code but removing javafx dependencies, unnecessary code and
 * general refactor to comply with code style.
 */
public class ScreenContext {

  public final Color foregroundColor;
  public final Color backgroundColor;
  public final byte highlight;
  public final boolean highIntensity;

  public ScreenContext(Color foregroundColor, Color backgroundColor, byte highlight,
      boolean highIntensity) {
    this.foregroundColor = foregroundColor;
    this.backgroundColor = backgroundColor;
    this.highlight = highlight;
    this.highIntensity = highIntensity;
  }

  public boolean matches(ScreenContext other) {
    return foregroundColor == other.foregroundColor
        && backgroundColor == other.backgroundColor
        && highlight == other.highlight
        && highIntensity == other.highIntensity;
  }

  public boolean matches(Color foregroundColor, Color backgroundColor, byte highlight,
      boolean highIntensity) {
    return this.foregroundColor == foregroundColor
        && this.backgroundColor == backgroundColor
        && this.highlight == highlight
        && this.highIntensity == highIntensity;
  }

  @Override
  public String toString() {
    return String.format("[Fg:%-10s Bg:%-10s In:%s  Hl:%02X]",
        ColorAttribute.getName(foregroundColor),
        ColorAttribute.getName(backgroundColor),
        (highIntensity ? 'x' : ' '), highlight);
  }

}
