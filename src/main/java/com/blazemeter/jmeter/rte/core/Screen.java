package com.blazemeter.jmeter.rte.core;

import java.awt.Dimension;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
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

  public static Screen fromHtml(String html) {
    try {
      Document doc = parseHtmlDocument(html);
      Element root = doc.getDocumentElement();
      Element head = (Element) root.getElementsByTagName("head").item(0);
      Element meta = (Element) head.getElementsByTagName("meta").item(0);
      String sizeStr = meta.getAttribute("content");
      int separatorIndex = sizeStr.indexOf('x');
      Screen ret = new Screen(new Dimension(Integer.parseInt(sizeStr.substring(separatorIndex + 1)),
          Integer.parseInt(sizeStr.substring(0, separatorIndex))));
      NodeList pres = root.getElementsByTagName("pre");
      int linealPosition = 0;
      for (int i = 0; i < pres.getLength(); i++) {
        Element pre = (Element) pres.item(i);
        String segmentText = pre.getTextContent().replace("\n", "");
        if ("true".equals(pre.getAttribute("contenteditable"))) {
          ret.addField(linealPosition, segmentText);
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

  public List<Position> getFieldEndPositions(int width) {
    return this.segments.stream()
        .filter(Segment::isEditable)
        .map(l -> l.getEndPosition(width))
        .collect(Collectors.toList());
  }

  public Dimension getSize() {
    return size;
  }

  public List<Segment> getSegments() {
    return segments;
  }

  public void addSegment(int linealPosition, String text) {
    segments.add(new Segment(buildPositionFromLinearPosition(linealPosition), text, false));
  }

  private Position buildPositionFromLinearPosition(int linealPosition) {
    return new Position((linealPosition / size.width + 1), linealPosition % size.width + 1);
  }

  public void addField(int linealPosition, String text) {
    segments.add(new Segment(buildPositionFromLinearPosition(linealPosition), text, true));
  }

  public String getText() {
    StringBuilder screen = new StringBuilder();
    int nextScreenPosition = 0;
    for (Segment segment : segments) {
      int segmentPosition = buildLinealPosition(segment.getPosition());
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
    return new Segment(buildPositionFromLinearPosition(firstPosition),
        buildBlankString(lastPosition - firstPosition), false);
  }

  private String buildBlankString(int length) {
    return StringUtils.repeat(' ', length);
  }

  private int buildLinealPosition(Position position) {
    return size.width * (position.getRow() - 1) + position.getColumn() - 1;
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

    private final String text;
    private final boolean editable;
    private Position position;

    public Segment(Position position, String text, boolean editable) {
      this.text = text;
      this.position = position;
      this.editable = editable;
    }

    public Position getPosition() {
      return position;
    }

    public String getText() {
      return text;
    }

    public boolean isEditable() {
      return editable;
    }

    public Position getEndPosition(int width) {
      int relativeColumnPosition = position.getColumn() + text.length();
      int row = position.getRow();
      if (relativeColumnPosition <= width) {
        return new Position(position.getRow(), relativeColumnPosition);
      }

      while (relativeColumnPosition >= width) {
        if ((relativeColumnPosition % width) == 0) {
          row++;
        }
        relativeColumnPosition--;
      }

      return new Position(row, relativeColumnPosition);
    }

    private String getWrappedText(int width) {
      int offset = (position.getColumn() > 0 ? position.getColumn() : width) - 1;
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
      return new Segment(position, convertInvisibleCharsToSpaces(text), editable);
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
      return position.equals(((Segment) o).position) &&
          text.equals(segment.text) &&
          editable == segment.editable;
    }

    @Override
    public int hashCode() {
      return Objects.hash(position, text);
    }

    @Override
    public String toString() {
      return "Segment{" +
          "Position=" + position.toString() +
          ", text='" + text + '\'' +
          ", editable=" + editable +
          '}';
    }

  }

}
