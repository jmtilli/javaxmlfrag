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
import javax.xml.parsers.*;
import org.xml.sax.*;
public abstract class DocumentFragmentHandler
  extends org.xml.sax.helpers.DefaultHandler
{
  private static class XMLStack {
    private final ArrayList<String> elements = new ArrayList<String>();
    public void push(String name) {
      elements.add(name);
    }
    public void pop(String name) {
      String last = elements.remove(elements.size()-1);
      if (!last.equals(name))
      {
        throw new Error("Expected element " + last +
                        " end, got element " + name + " end");
      }
    }
    public boolean is(String... names)
    {
      if (names.length != elements.size())
      {
        return false;
      }
      for (int i = 0; i < names.length; i++)
      {
        if (!names[i].equals(elements.get(i)))
        {
          return false;
        }
      }
      return true;
    }
  };
  private class ConvertToDocumentFragmentHandler {
    private final StringBuffer buf = new StringBuffer();
    private final ArrayList<DocumentFragment> frags =
      new ArrayList<DocumentFragment>();
    private DocumentFragment f = null;
    public final boolean ready()
    {
      return f != null && frags.isEmpty();
    }
    public final DocumentFragment getDocumentFragment()
    {
      return f;
    }
    public final void startElement(String uri, String localName, String qName,
                                   Attributes attributes)
    {
      HashMap<String, String> m = new HashMap<String, String>();
      for (int i = 0; i < attributes.getLength(); i++)
      {
        if (m.containsKey(attributes.getQName(i)))
        {
          throw new Error("duplicate attribute");
        }
        m.put(attributes.getQName(i), attributes.getValue(i));
      }
      DocumentFragment df = new DocumentFragment(qName, m);
      if (f == null)
      {
        f = df;
      }
      if (!frags.isEmpty())
      {
        if (buf.length() > 0)
        {
          frags.get(frags.size()-1).addTextChild(buf.toString());
        }
        frags.get(frags.size()-1).addChild(df);
      }
      buf.setLength(0);
      frags.add(df);
    }
    public final void endElement(String uri, String localName, String qName)
    {
      if (buf.length() > 0)
      {
        frags.get(frags.size()-1).addTextChild(buf.toString());
        buf.setLength(0);
      }
      frags.remove(frags.size()-1);
    }
    public final void characters(char[] ch, int start, int length)
    {
      buf.append(ch, start, length);
    }
  };
  private ConvertToDocumentFragmentHandler h;
  private boolean startXMLElementCallActive = false;
  private final XMLStack s = new XMLStack();
  /**
     Test the parsing context.

     For example, for a document &lt;doc&gt;&lt;elt&gt;&lt;/elt&gt;&lt;/doc&gt;
     the context is {"doc", "elt"} when parsing the inner "elt" element.
    
     @param tags The list of qualified tag names.
     @return Whether the parsing context is the tested one
   */
  public final boolean is(String... tags)
  {
    return s.is(tags);
  }
  /**
     Start fragment collection.

     Changes from sequential event-based parsing mode to a tree-based parsing
     mode. During fragment collection, the startXMLElement, endXMLElement and
     xmlCharacters calls are skipped until the whole fragment of the element
     the user requested has been collected, after which endXMLElement is called
     with the collected fragment as an argument.
            
     Can only be called from within startXMLElement.
   */
  public final void startFragmentCollection()
  {
    if (!startXMLElementCallActive)
    {
      throw new Error("can be called only within startXMLElement");
    }
    if (h != null)
    {
      throw new Error("fragment collection already started");
    }
    h = new ConvertToDocumentFragmentHandler();
  }
  /**
     Handler for element start.

     The derived class should implement this.

     This is not called during fragment collection. The user can start fragment
     collection by calling startFragmentCollection() from this method. Then the
     rest of the startXMLElement, endXMLElement and xmlCharacters are skipped
     until the whole fragment of the element the user requested has been
     collected, after which endXMLElement is called with the collected fragment
     as an argument.

     @param uri The namespace URI of the tag
     @param localName The local name of the tag
     @param qName The qualified name of the tag
     @param attributes The attributes of the element
   */
  public abstract void startXMLElement(String uri, String localName,
                                       String qName,
                                       org.xml.sax.Attributes attributes);
  /**
     Handler for character data.

     The derived class may choose to implement this.

     This is not called during fragment collection.

     @param ch The character array
     @param start Start position of the encountered characters witihin ch.
     @param length The number of characters encountered.
   */
  public void xmlCharacters(char[] ch, int start, int length)
  {
  }
  /**
     Handler for element end.

     The derived class should implement this.

     This is not called during fragment collection. After the fragment has been
     collected, this method is called with a non-null f argument. When fragment
     collection is not active, f is null.

     @param uri The namespace URI of the tag
     @param localName The local name of the tag
     @param qName The qualified name of the tag
     @param f The document fragment the user requested or null
   */
  public abstract void endXMLElement(String uri, String localName,
                                     String qName, DocumentFragment f);
  /**
     Handler for element start.

     May not be overridden by the derived class. The derived class should
     instead implement startXMLElement.

     @param uri The namespace URI of the tag
     @param localName The local name of the tag
     @param qName The qualified name of the tag
     @param attributes The attributes of the element
   */
  public final void startElement(String uri, String localName, String qName,
                                 org.xml.sax.Attributes attributes) {
    s.push(qName);
    if (h == null)
    {
      startXMLElementCallActive = true;
      try {
        startXMLElement(uri, localName, qName, attributes);
      }
      finally {
        startXMLElementCallActive = false;
      }
    }
    // Note: h may have changed here
    if (h != null)
    {
      h.startElement(uri, localName, qName, attributes);
    }
  }
  /**
     Handler for character data.

     May not be overridden by the derived class. The derived class should
     instead implement xmlCharacters.

     @param ch The character array
     @param start Start position of the encountered characters witihin ch.
     @param length The number of characters encountered.
   */
  public final void characters(char[] ch, int start, int length) {
    if (h != null)
    {
      h.characters(ch, start, length);
    }
    else
    {
      xmlCharacters(ch, start, length);
    }
  }
  /**
     Handler for element end.

     May not be overridden by the derived class. The derived class should
     instead implement endXMLElement.

     @param uri The namespace URI of the tag
     @param localName The local name of the tag
     @param qName The qualified name of the tag
   */
  public void endElement(String uri, String localName, String qName) {
    DocumentFragment df = null;
    if (h != null) {
      h.endElement(uri, localName, qName);
      if (h.ready()) {
        df = h.getDocumentFragment();
        h = null;
      }
    }
    // Note: h may have changed here
    if (h == null)
    {
      endXMLElement(uri, localName, qName, df);
    }
    s.pop(qName);
  }
  private static class WholeDocumentHandler extends DocumentFragmentHandler {
    private DocumentFragment f_global;
    public void startXMLElement(String uri, String localName, String qName,
                                org.xml.sax.Attributes attributes) {
      super.startFragmentCollection();
    }
    public void endXMLElement(String uri, String localName, String qName,
                              DocumentFragment f) {
      if (f_global != null)
      {
        throw new Error("multiple endXMLElement calls, expected only one");
      }
      f_global = f;
    }
  }
  /**
     Parse a whole document.

     @param fact A parser factory
     @param f File of the document
     @return A parse tree of the document
   */
  public static DocumentFragment parseWhole(SAXParserFactory fact, File f)
    throws ParserConfigurationException, SAXException, IOException
  {
    WholeDocumentHandler whole = new WholeDocumentHandler();
    fact.newSAXParser().parse(f, whole);
    return whole.f_global;
  }
  /**
     Parse a whole document.

     @param f File of the document
     @return A parse tree of the document
   */
  public static DocumentFragment parseWhole(File f)
    throws ParserConfigurationException, SAXException, IOException
  {
    return parseWhole(SAXParserFactory.newInstance(), f);
  }
  /**
     Parse a whole document.

     @param fact A parser factory
     @param is InputStream of the document
     @return A parse tree of the document
   */
  public static DocumentFragment parseWhole(SAXParserFactory fact,
                                            InputStream is)
    throws ParserConfigurationException, SAXException, IOException
  {
    WholeDocumentHandler whole = new WholeDocumentHandler();
    fact.newSAXParser().parse(is, whole);
    return whole.f_global;
  }
  /**
     Parse a whole document.

     @param is InputStream of the document
     @return A parse tree of the document
   */
  public static DocumentFragment parseWhole(InputStream is)
    throws ParserConfigurationException, SAXException, IOException
  {
    return parseWhole(SAXParserFactory.newInstance(), is);
  }
  /**
     Parse a whole document.

     @param fact A parser factory
     @param is InputSource of the document
     @return A parse tree of the document
   */
  public static DocumentFragment parseWhole(SAXParserFactory fact,
                                            InputSource is)
    throws ParserConfigurationException, SAXException, IOException
  {
    WholeDocumentHandler whole = new WholeDocumentHandler();
    fact.newSAXParser().parse(is, whole);
    return whole.f_global;
  }
  /**
     Parse a whole document.

     @param is InputSource of the document
     @return A parse tree of the document
   */
  public static DocumentFragment parseWhole(InputSource is)
    throws ParserConfigurationException, SAXException, IOException
  {
    return parseWhole(SAXParserFactory.newInstance(), is);
  }
  /**
     Parse a whole document.

     @param fact A parser factory
     @param uri URI of the document
     @return A parse tree of the document
   */
  public static DocumentFragment parseWhole(SAXParserFactory fact, String uri)
    throws ParserConfigurationException, SAXException, IOException
  {
    WholeDocumentHandler whole = new WholeDocumentHandler();
    fact.newSAXParser().parse(uri, whole);
    return whole.f_global;
  }
  /**
     Parse a whole document.

     @param uri URI of the document
     @return A parse tree of the document
   */
  public static DocumentFragment parseWhole(String uri)
    throws ParserConfigurationException, SAXException, IOException
  {
    return parseWhole(SAXParserFactory.newInstance(), uri);
  }
}
