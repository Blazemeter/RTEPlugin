package com.blazemeter.jmeter.rte.sampler;

import com.blazemeter.jmeter.rte.core.Input;
import com.blazemeter.jmeter.rte.core.NavigationInput;
import java.util.Arrays;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NavigationInputRowGui extends InputTestElement {

  private static final String REPEATED_COLUMN = "NavigationInputRowGui.repeated";
  private static final String NAVIGATION_TYPE = "NavigationInputRowGui.type";

  public static NavigationInputRowGui parse(String text, String input)
      throws IllegalArgumentException {
    Optional<NavigationType> currentType = Arrays.stream(NavigationType.values())
        .filter(navT -> text.toUpperCase().contains(navT.toString()))
        .findFirst();

    if (currentType.isPresent()) {
      return parseNavigationInputFromText(text.toUpperCase(), input, currentType.get());
    }
    throw new IllegalArgumentException(
        "Given text does not match with any Navigation Input format type.");
  }

  private static NavigationInputRowGui parseNavigationInputFromText(String text, String input,
      NavigationType prefix) {

    Pattern pattern = Pattern.compile("<" + prefix + ">");
    Pattern extendedPattern = Pattern.compile("([" + pattern + "]+)");
    Pattern shortedPattern = Pattern.compile("<" + prefix + "\\*(\\d+)>");

    Matcher shortVersionMatcher = shortedPattern.matcher(text);
    Matcher extendedVersionMatcher = extendedPattern.matcher(text);

    NavigationInputRowGui navigationInputRowGui = new NavigationInputRowGui();
    navigationInputRowGui.setInput(input);
    navigationInputRowGui.setType(prefix.getLabel());
    if (shortVersionMatcher.matches()) {
      navigationInputRowGui.setRepeated(shortVersionMatcher.group(1));
      return navigationInputRowGui;
    } else if (extendedVersionMatcher.matches()) {
      Matcher helperNavMatcher = pattern.matcher(extendedVersionMatcher.group(1));
      int repeated = 0;
      while (helperNavMatcher.find()) {
        repeated++;
      }
      navigationInputRowGui.setRepeated(String.valueOf(repeated));
      return navigationInputRowGui;
    }
    throw new IllegalArgumentException(
        "Given text does not match the established pattern for navigation inputs");
  }

  @Override
  public Input toInput() {
    return new NavigationInput(Integer.parseInt(getRepeated()), getTypeNavigation(), getInput());
  }

  @Override
  public void copyOf(InputTestElement cellValue) {
    if (cellValue instanceof NavigationInputRowGui) {
      NavigationInputRowGui source = (NavigationInputRowGui) cellValue;
      setRepeated(source.getRepeated());
      setInput(source.getInput());
      setType(source.getTypeNavigation().getLabel());
    }
  }

  public String getRepeated() {
    return getPropertyAsString(REPEATED_COLUMN, "1");
  }

  public void setRepeated(String repeated) {
    setProperty(REPEATED_COLUMN, repeated);
  }

  public void setType(String type) {
    setProperty(NAVIGATION_TYPE, type);
  }

  public NavigationType getTypeNavigation() {
    return NavigationType.fromLabel(getPropertyAsString(NAVIGATION_TYPE,
        NavigationType.TAB.getLabel()));
  }
}
