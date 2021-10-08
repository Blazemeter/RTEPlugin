package com.blazemeter.jmeter.rte.core;

import com.blazemeter.jmeter.rte.core.Screen.Segment.SegmentBuilder;
import com.blazemeter.jmeter.rte.extractor.PositionRange;
import java.awt.Dimension;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class Screen {

  private List<Segment> segments = new ArrayList<>();
  private Dimension size;

  // Provided for proper deserialization of sample results
  public Screen() {
  }

  public Screen(Dimension size) {
    this.size = size;
  }

  public Screen(Screen other) {
    if (other != null) {
      segments = new ArrayList<>(other.segments);
      size = other.size;
    }
  }

  public static Screen valueOf(String screen) {
    int width = screen.indexOf('\n');
    int height = screen.length() / (width + 1);
    Screen ret = new Screen(new Dimension(width, height));
    int pos = 0;
    for (String part : screen.split("\n")) {
      ret.addSegment(pos, part);
      pos += width;
    }
    return ret;
  }

  public static Screen buildScreenFromText(String screenText, Dimension screenSize) {
    Screen scr = new Screen(screenSize);
    scr.addSegment(0, screenText.replace("\n", ""));
    return scr;
  }

  public static Screen fromHtml(String html) {
    try {
      Document doc = parseHtmlDocument(html);
      Element root = doc.getDocumentElement();
      Element head = (Element) root.getElementsByTagName("head").item(0);
      Element meta = (Element) head.getElementsByTagName("meta").item(0);
      String sizeStr = meta.getAttribute("content");
      int separatorIndex = sizeStr.indexOf('x');
      Dimension screenSize = new Dimension(Integer.parseInt(sizeStr.substring(separatorIndex + 1)),
          Integer.parseInt(sizeStr.substring(0, separatorIndex)));
      Screen ret = new Screen(screenSize);
      NodeList pres = root.getElementsByTagName("pre");
      int linealPosition = 0;
      for (int i = 0; i < pres.getLength(); i++) {
        Element pre = (Element) pres.item(i);
        String segmentText = pre.getTextContent().replace("\n", "");
        if ("true".equals(pre.getAttribute("contenteditable"))) {
          if ("true".equals(pre.getAttribute("secretcontent"))) {
            ret.addSecretField(linealPosition, segmentText);
          } else {
            ret.addField(linealPosition, segmentText);
          }
        } else {
          ret.addSegment(linealPosition, segmentText);
        }
        linealPosition += segmentText.length();
      }
      return ret;
    } catch (ParserConfigurationException | SAXException | IOException e) {
      throw new RuntimeException(e);
    }
  }

  private static Document parseHtmlDocument(String html)
      throws SAXException, IOException, ParserConfigurationException {
    InputSource is = new InputSource();
    is.setCharacterStream(new StringReader(html));
    return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(is);
  }

  private static Position buildPositionFromLinearPosition(int linealPosition, int width) {
    return new Position(linealPosition / width + 1, linealPosition % width + 1);
  }

  public Dimension getSize() {
    return size;
  }

  public List<Segment> getSegments() {
    return segments;
  }

  public void addSegment(int linealPosition, String text) {
    segments.add(
        getSegmentBuilder(linealPosition, text)
            .build(size)
    );
  }

  private SegmentBuilder getSegmentBuilder(int linealPosition, String text) {
    return new SegmentBuilder()
        .withLinealPosition(linealPosition)
        .withText(text);
  }
  
  public void addField(int linealPosition, String text) {
    segments.add(
        getSegmentBuilder(linealPosition, text)
            .withEditable()
            .build(size)
    );
  }

  public void addSecretField(int linealPosition, String text) {
    segments.add(
        getSegmentBuilder(linealPosition, text)
            .withEditable()
            .withSecret()
            .build(size)
    );
  }

  public String getText() {
    StringBuilder screen = new StringBuilder();
    int nextScreenPosition = 0;
    for (Segment segment : segments) {
      int segmentPosition = buildLinealPosition(segment.getStartPosition(), size.width);
      if (segmentPosition != nextScreenPosition) {
        Segment fillSegment = buildBlankSegmentForRange(nextScreenPosition, segmentPosition);
        screen.append(fillSegment.getWrappedText(size.width));
        nextScreenPosition += fillSegment.getText().length();

      }
      screen.append(segment.getWrappedText(size.width));
      nextScreenPosition += segment.text.length();
    }
    int lastScreenPosition = size.width * size.height;
    if (nextScreenPosition < lastScreenPosition) {
      screen.append(buildBlankSegmentForRange(nextScreenPosition, lastScreenPosition)
          .getWrappedText(size.width));
    }

    return screen.toString();
  }

  private Segment buildBlankSegmentForRange(int firstPosition, int lastPosition) {
    return getSegmentBuilder(firstPosition, buildBlankString(lastPosition - firstPosition))
        .build(size);
  }

  private String buildBlankString(int length) {
    return StringUtils.repeat(' ', length);
  }

  private static int buildLinealPosition(Position position, int width) {
    return width * (position.getRow() - 1) + position.getColumn() - 1;
  }

  public Screen withInvisibleCharsToSpaces() {
    Screen ret = new Screen(size);
    for (Segment s : segments) {
      ret.segments.add(s.withInvisibleCharsToSpaces());
    }
    return ret;
  }

  public String getHtml() {
    return buildHtmlDocumentString(buildHtmlDocument());
  }

  private Document buildHtmlDocument() {
    try {
      Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
      Element root = doc.createElement("html");
      doc.appendChild(root);
      appendHtmlHead(doc, root);
      appendHtmlBody(doc, root);
      return doc;
    } catch (ParserConfigurationException e) {
      throw new RuntimeException(e);
    }
  }

  private void appendHtmlHead(Document doc, Element root) {
    Element head = appendHtmlChild("head", root, doc);
    Element meta = appendHtmlChild("meta", head, doc);
    meta.setAttribute("name", "screen-size");
    meta.setAttribute("content", size.height + "x" + size.width);
    Element style = appendHtmlChild("style", head, doc);
    style.setTextContent("pre { display: inline; background: black; color: green; }");
  }

  private Element appendHtmlChild(String childName, Element parent, Document doc) {
    Element element = doc.createElement(childName);
    parent.appendChild(element);
    return element;
  }

  private void appendHtmlBody(Document doc, Element root) {
    Element body = appendHtmlChild("body", root, doc);
    for (Segment segment : segments) {
      Element pre = appendHtmlChild("pre", body, doc);
      if (segment.isEditable()) {
        pre.setAttribute("contenteditable", "true");
      }
      if (segment.isSecret()) {
        pre.setAttribute("secretcontent", "true");
      }
      pre.setTextContent(segment.getWrappedText(size.width));
    }
  }

  private String buildHtmlDocumentString(Document doc) {
    try {
      DOMImplementationLS impl = (DOMImplementationLS) DOMImplementationRegistry.newInstance()
          .getDOMImplementation("LS");
      LSSerializer serializer = impl.createLSSerializer();
      serializer.getDomConfig().setParameter("xml-declaration", false);
      return serializer.writeToString(doc);
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }

  public static String replaceTrailingSpacesByNull(String str) {
    StringBuilder nulls = new StringBuilder();
    int i = str.length() - 1;
    while (i >= 0 && str.charAt(i) == ' ') {
      nulls.append('\u0000');
      i--;
    }
    return str.substring(0, i + 1) + nulls.toString();
  }

  @Override
  public String toString() {
    return getHtml();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Screen screen = (Screen) o;
    return segments.equals(screen.segments) &&
        size.equals(screen.size);
  }

  @Override
  public int hashCode() {
    return Objects.hash(segments, size);
  }

  public static class Segment {

    private final boolean editable;
    private final boolean secret;
    private final String text;
    private final PositionRange positionRange;

    private Segment(PositionRange positionRange, String text, boolean editable, boolean secret) {
      this.positionRange = positionRange;
      this.text = text;
      this.editable = editable;
      this.secret = secret;
    }

    public Position getStartPosition() {
      return positionRange.getStart();
    }

    public String getText() {
      return text;
    }

    public PositionRange getPositionRange() {
      return positionRange;
    }

    public boolean isEditable() {
      return editable;
    }

    public Position getEndPosition() {
      return positionRange.getEnd();
    }

    public boolean isSecret() {
      return secret;
    }

    private String getWrappedText(int width) {
      int offset =
          (positionRange.getStart().getColumn() > 0 ? positionRange.getStart().getColumn() : width)
              - 1;
      int pos = 0;
      StringBuilder ret = new StringBuilder();
      while (offset + text.length() - pos >= width) {
        ret.append(text, pos, pos + width - offset);
        ret.append("\n");
        pos += width - offset;
        offset = 0;
      }
      if (pos < text.length()) {
        ret.append(text, pos, text.length());
      }
      // in tn5250 and potentially other protocols, the screen contains non visible characters which
      // are used as markers of no data or additional info. We replace them with spaces for better
      // visualization in text representation.
      return convertInvisibleCharsToSpaces(ret.toString());
    }

    private String convertInvisibleCharsToSpaces(String str) {
      return str.replace('\u0000', ' ');
    }

    private Segment withInvisibleCharsToSpaces() {
      return new Segment(positionRange, convertInvisibleCharsToSpaces(text), editable, secret);
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }

      Segment segment = (Segment) o;
      return positionRange.getStart().equals(((Segment) o).positionRange.getStart()) &&
          text.equals(segment.text) &&
          editable == segment.editable &&
          secret == segment.secret;
    }

    @Override
    public int hashCode() {
      return Objects.hash(positionRange.getStart(), text);
    }

    @Override
    public String toString() {
      return "Segment{" +
          "Start position=" + positionRange.getStart().toString() +
          "End position=" + positionRange.getEnd() +
          ", text='" + text + '\'' +
          ", editable=" + editable +
          ", secret=" + secret +
          '}';
    }

    public static class SegmentBuilder {

      private boolean editable = false;
      private boolean secret = false;
      private String text;
      private int firstLinealPosition;
      private Position startPosition;

      public SegmentBuilder withEditable() {
        editable = true;
        return this;
      }

      public SegmentBuilder withSecret() {
        secret = true;
        return this;
      }

      public SegmentBuilder withText(String text) {
        this.text = text;
        return this;
      }

      private SegmentBuilder withLinealPosition(int linealPosition) {
        this.firstLinealPosition = linealPosition;
        return this;
      }

      public SegmentBuilder withPosition(int row, int col) {
        this.startPosition = new Position(row, col);
        return this;
      }

      private Position calculateEndPosition(Dimension screenSize, Position startPosition) {
        int startLinealPosition = buildLinealPosition(startPosition, screenSize.width);
        int endLinealPosition = startLinealPosition + text.length();
        int maxLinealPos = screenSize.width * screenSize.height;
        //circular field use case
        if (maxLinealPos < endLinealPosition) {
          return buildPositionFromLinearPosition(Math.abs(endLinealPosition - maxLinealPos),
              screenSize.width);
        }
        endLinealPosition = startPosition.getColumn() + text.length();
        return new Position(startPosition.getRow() + (endLinealPosition - 1) / screenSize.width,
            (endLinealPosition - 1) % screenSize.width + 1);
      }

      public Segment build(Dimension screenSize) {
        startPosition = startPosition == null ? buildPositionFromLinearPosition(firstLinealPosition,
            screenSize.width) : startPosition;
        PositionRange positionRange = new PositionRange(startPosition,
            calculateEndPosition(screenSize, startPosition));
        return new Segment(positionRange, text, editable, secret);
      }
    }
  }

}
