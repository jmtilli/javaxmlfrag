/*
  Copyright (C) 2013 Juha-Matti Tilli
  
  Permission is hereby granted, free of charge, to any person obtaining a copy
  of this software and associated documentation files (the "Software"), to deal
  in the Software without restriction, including without limitation the rights
  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  copies of the Software, and to permit persons to whom the Software is
  furnished to do so, subject to the following conditions:
  
  The above copyright notice and this permission notice shall be included in
  all copies or substantial portions of the Software.
  
  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  SOFTWARE.
 */
package fi.iki.jmtilli.javaxmlfrag;
import java.util.*;
import java.io.*;
/**
   Unit test for DocumentFragmentHandler.
 */
public class UnitTestDocumentFragmentHandler {
  public static boolean equals(Object a, Object b)
  {
    if (a == null)
    {
      return b == null;
    }
    return a.equals(b);
  }
  private static void assertEqual(Object a, Object b)
  {
    if (!equals(a, b))
    {
      throw new RuntimeException("inequal: " + a + ", " + b);
    }
  }
  private static void assertTrue(boolean b)
  {
    if (!b)
    {
      throw new RuntimeException("false");
    }
  }
  private static void assertFalse(boolean b)
  {
    if (b)
    {
      throw new RuntimeException("true");
    }
  }
  private static <K, V> boolean mapEquals(Map<K, V> a, Map<K, V> b)
  {
    if (a.size() != b.size())
    {
      return false;
    }
    for (Map.Entry<K, V> e: a.entrySet())
    {
      if (!b.containsKey(e.getKey()))
      {
        return false;
      }
      V v1 = e.getValue();
      V v2 = b.get(e.getKey());
      if (!equals(e.getValue(), b.get(e.getKey())))
      {
        return false;
      }
    }
    return true;
  }
  private static <K,V> void assertMapEqual(Map<K, V> a, Map<K, V> b)
  {
    if (!mapEquals(a, b))
    {
      throw new RuntimeException("maps not equal");
    }
  }
  private static <T> void assertListHasSameObjects(List<T> a, List<T> b)
  {
    Iterator<T> iter2 = b.iterator();
    if (a.size() != b.size())
    {
      throw new RuntimeException("sizes differ");
    }
    for (T it1: a)
    {
      if (it1 != iter2.next())
      {
        throw new RuntimeException("objects differ");
      }
    }
  }
  private static boolean docFragEquals(DocumentFragment a, DocumentFragment b)
  {
    if (a == null)
    {
      return b == null;
    }
    if (a.isTextElement() != b.isTextElement())
    {
      return false;
    }
    if (!equals(a.getTag(), b.getTag()))
    {
      return false;
    }
    if (!equals(a.getText(), b.getText()))
    {
      return false;
    }
    if (!a.isTextElement())
    {
      if (!mapEquals(a.getAttributes(), b.getAttributes()))
      {
        return false;
      }
      List<DocumentFragment> children1 = a.getChildren();
      List<DocumentFragment> children2 = b.getChildren();
      Iterator<DocumentFragment> iter2 = children2.iterator();
      if (children1.size() != children2.size())
      {
        return false;
      }
      for (DocumentFragment frag1: children1)
      {
        DocumentFragment frag2 = iter2.next();
        if (!docFragEquals(frag1, frag2))
        {
          return false;
        }
      }
    }
    return true;
  }

  private static class Event {
    public final List<String> is;
    public Event(List<String> is)
    {
      this.is = Collections.unmodifiableList(new ArrayList<String>(is));
    }
  }

  private static class XmlStartEvent extends Event {
    public final String uri;
    public final String localName;
    public final String qName;
    public final org.xml.sax.Attributes attributes;
    public final boolean startFragmentCollection;
    public XmlStartEvent(String uri, String localName, String qName,
                         org.xml.sax.Attributes attributes,
                         List<String> is, boolean startFragmentCollection)
    {
      super(is);
      this.uri = uri;
      this.localName = localName;
      this.qName = qName;
      this.attributes = attributes;
      this.startFragmentCollection = startFragmentCollection;
    }
    public boolean equals(Object b)
    {
      try {
        XmlStartEvent that = (XmlStartEvent)b;
        if (this.uri != that.uri)
        {
          return false;
        }
        if (this.localName != that.localName)
        {
          return false;
        }
        if (this.qName != that.qName)
        {
          return false;
        }
        if (this.attributes != that.attributes)
        {
          return false;
        }
        return true;
      }
      catch (ClassCastException e)
      {
        return false;
      }
    }
  };
  private static class XmlEndEvent extends Event {
    public final String uri;
    public final String localName;
    public final String qName;
    public final DocumentFragment frag;
    public XmlEndEvent(String uri, String localName, String qName,
                       DocumentFragment frag, List<String> is)
    {
      super(is);
      this.uri = uri;
      this.localName = localName;
      this.qName = qName;
      this.frag = frag;
    }
    public boolean equals(Object b)
    {
      try {
        XmlEndEvent that = (XmlEndEvent)b;
        if (this.uri != that.uri)
        {
          return false;
        }
        if (this.localName != that.localName)
        {
          return false;
        }
        if (this.qName != that.qName)
        {
          return false;
        }
        if (!docFragEquals(this.frag, that.frag))
        {
          return false;
        }
        return true;
      }
      catch (ClassCastException e)
      {
        return false;
      }
    }
  };
  private static class XmlCharsEvent extends Event {
    public final char[] ch;
    public final int start;
    public final int length;
    public XmlCharsEvent(char[] ch, int start, int length, List<String> is)
    {
      super(is);
      this.ch = ch;
      this.start = start;
      this.length = length;
    }
    public boolean equals(Object b)
    {
      try {
        XmlCharsEvent that = (XmlCharsEvent)b;
        if (this.ch != that.ch)
        {
          return false;
        }
        if (this.start != that.start)
        {
          return false;
        }
        if (this.length != that.length)
        {
          return false;
        }
        return true;
      }
      catch (ClassCastException e)
      {
        return false;
      }
    }
  };
  private static Event nextEvent = null;

  private static class TestDocumentFragmentHandler
      extends DocumentFragmentHandler
  {
    public void startXMLElement(String uri, String localName, String qName,
                                org.xml.sax.Attributes attributes)
    {
      assertEqual(nextEvent, new XmlStartEvent(uri, localName, qName,
                                               attributes,
                                               new ArrayList<String>(), false));
      assertTrue(is(nextEvent.is.toArray(new String[0])));
      if (((XmlStartEvent)nextEvent).startFragmentCollection)
      {
        super.startFragmentCollection();
      }
      nextEvent = null;
    }
    public void endXMLElement(String uri, String localName, String qName,
                              DocumentFragment f)
    {
      assertEqual(nextEvent, new XmlEndEvent(uri, localName, qName, f,
                                             new ArrayList<String>()));
      assertTrue(is(nextEvent.is.toArray(new String[0])));
      nextEvent = null;
    }
    public void xmlCharacters(char[] ch, int start, int length)
    {
      assertEqual(nextEvent, new XmlCharsEvent(ch, start, length,
                                               new ArrayList<String>()));
      assertTrue(is(nextEvent.is.toArray(new String[0])));
      nextEvent = null;
    }
  };

  private static void testDocumentFragmentHandler()
  {
    TestDocumentFragmentHandler testh = new TestDocumentFragmentHandler();
    org.xml.sax.Attributes attrs = new org.xml.sax.helpers.AttributesImpl();
    ArrayList<String> is = new ArrayList<String>();
    String chars;
    DocumentFragment head;
    DocumentFragment title;

    is.add("html");
    nextEvent = new XmlStartEvent("", "", "html", attrs, is, false);
    testh.startElement("", "", "html", attrs);

    is.add("head");
    nextEvent = new XmlStartEvent("", "", "head", attrs, is, true);
    testh.startElement("", "", "head", attrs);

    nextEvent = null;

    testh.startElement("", "", "title", attrs);
    chars = "AfooB";
    testh.characters(chars.toCharArray(), 1, chars.length()-2);
    testh.endElement("", "", "title");

    head = new DocumentFragment("head");
    title = new DocumentFragment("title");
    head.add(title);
    title.addTextChild("foo");
    nextEvent = new XmlEndEvent("", "", "head", head, is);
    testh.endElement("", "", "head");

    is.remove(is.size()-1);
    is.add("body");
    nextEvent = new XmlStartEvent("", "", "body", attrs, is, false);
    testh.startElement("", "", "body", attrs);
    nextEvent = new XmlEndEvent("", "", "body", null, is);
    testh.endElement("", "", "body");
    is.remove(is.size()-1);
    nextEvent = new XmlEndEvent("", "", "html", null, is);
    testh.endElement("", "", "html");
  }

  private static void testParseWhole() throws Throwable
  {
    DocumentFragment tag = new DocumentFragment("tag");
    tag.setAttrString("attr1", "foo");
    tag.setAttrString("attr2", "bar");
    InputStream is = new ByteArrayInputStream(
        "<tag attr1='foo' attr2='bar'></tag>".getBytes("UTF-8"));
    DocumentFragment tag_parsed = DocumentFragmentHandler.parseWhole(is);
    assertTrue(docFragEquals(tag, tag_parsed));
  }

  /**
     Run the unit test
   */
  public static void main(String[] args) throws Throwable
  {
    testDocumentFragmentHandler();
    testParseWhole();
  }
};
