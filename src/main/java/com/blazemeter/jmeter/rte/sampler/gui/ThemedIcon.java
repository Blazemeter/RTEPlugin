package com.blazemeter.jmeter.rte.sampler.gui;

import java.util.Map;
import java.util.WeakHashMap;
import javax.swing.ImageIcon;
import javax.swing.UIManager;

public class ThemedIcon {

  private static Map<String, ImageIcon> cachedIcons = new WeakHashMap<>();

  public static ImageIcon fromResourceName(String resourceName) {
    String resourcePath = getThemePath() + "/" + resourceName;
    return cachedIcons
        .computeIfAbsent(resourcePath, p -> new ImageIcon(ThemedIcon.class.getResource(p)));
  }

  private static String getThemePath() {
    return "Darcula".equals(UIManager.getLookAndFeel().getID()) ? "/dark-theme" : "/light-theme";
  }

  public static String getResourcePath() {
    // ThemedIcon.class.getResource use a relative to classLoader and can return
    // resources from others jars that match the pattern to find
    // The safe way is using the find of the resource of the class and get the jar path
    // to generate the correct path to the resource file
    String jarPath =
        ThemedIcon.class.getResource(
            '/' + ThemedIcon.class.getName().replace('.', '/') +
                ".class").toString().split("!")[0];
    return jarPath + "!" + getThemePath();
  }

}
