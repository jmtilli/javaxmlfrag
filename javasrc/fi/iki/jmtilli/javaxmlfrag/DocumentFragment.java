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
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.io.OutputStream;
import java.io.Writer;
import java.io.StringWriter;
import org.w3c.dom.Node;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.StreamResult;
/**
   Fragment of an XML document.
  
   Document fragments can either be text fragments (have textual content,
   can't have children or attributes) or tags (can have both children or
   attributes but don't contain text themselves -- but can contain text
   fragment children).

   There are different families of getters and setters:
   <ul>
   <li>getAttr* and setAttr* get and set the attribute values</li>
   <li>getThis* and setThis* get and set the textual contents of this element</li>
   <li>get* and set* get and set the textual contents of a named subelement</li>
   <li>get*Object and set*Object use null to represent missing data</li>
   <li>get*NotNull throw a XMLValueMissingException if data is missing</li>
   </ul>

   Note that the getThis* family and the get* family of methods are slightly
   different: the getThis* family handles empty string as null, while the
   get* family handles a missing element as null and an empty string as an
   empty string. Two methods, getThisStringObjectEmptyIsNull and
   getThisStringObjectEmptyIsEmpty are provided for strings but for data
   types other than strings, the getThis* family handles empty string as null.
 */
public class DocumentFragment {
  private final String tag;
  private final String text;
  private final ArrayList<DocumentFragment> children;
  private final HashMap<String, String> attributes;

  /**
     Convert the XML to a string representation.
    
     @param document_type XMLDocumentType.WHOLE or FRAGMENT
     @param result The StreamResult
   */
  public void unparse(XMLDocumentType document_type, StreamResult result)
    throws ParserConfigurationException, TransformerConfigurationException,
           TransformerException
  {
    DocumentBuilderFactory bf;
    DocumentBuilder b;
    Document doc;
    TransformerFactory tf;
    Transformer t;

    bf = DocumentBuilderFactory.newInstance();
    b = bf.newDocumentBuilder();
    doc = b.newDocument();
    tf = TransformerFactory.newInstance();
    t = tf.newTransformer();
    t.setOutputProperty(OutputKeys.INDENT, "yes");
    t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION,
                        document_type.getOmitXmlDeclaration());
    doc.appendChild(convertToDomNode(doc));
    t.transform(new DOMSource(doc), result);
  }
  /**
     Convert the XML to a string representation.
    
     @param document_type XMLDocumentType.WHOLE or FRAGMENT
     @param os An output stream
   */
  public void unparse(XMLDocumentType document_type, OutputStream os)
    throws ParserConfigurationException, TransformerConfigurationException,
           TransformerException
  {
    unparse(document_type, new StreamResult(os));
  }
  /**
     Convert the XML to a string representation.
    
     @param document_type XMLDocumentType.WHOLE or FRAGMENT
     @param w A writer
   */
  public void unparse(XMLDocumentType document_type, Writer w)
    throws ParserConfigurationException, TransformerConfigurationException,
           TransformerException
  {
    unparse(document_type, new StreamResult(w));
  }
  /**
     Convert the XML to a string representation.
    
     @param document_type XMLDocumentType.WHOLE or FRAGMENT
     @return The string representation
   */
  public String unparseToString(XMLDocumentType document_type)
    throws ParserConfigurationException, TransformerConfigurationException,
           TransformerException
  {
    StringWriter w = new StringWriter();
    unparse(document_type, w);
    return w.toString();
  }
  /**
     Convert the XML to a DOM node.
    
     @param doc The DOM document
     @return The DOM node
   */
  public Node convertToDomNode(Document doc)
  {
    Element e;
    if (text != null)
    {
      return doc.createTextNode(text);
    }
    e = doc.createElement(tag);
    for (Map.Entry<String, String> entry: getAttributes().entrySet())
    {
      e.setAttribute(entry.getKey(), entry.getValue());
    }
    for (DocumentFragment child: getChildren())
    {
      e.appendChild(child.convertToDomNode(doc));
    }
    return e;
  }
  /**
     Get the attributes of the element.

     Note that text elements do not have attributes, so this will throw
     an exception if called for a text element.

     NB: the caller may modify the map of attributes, but may not add
     null keys or values to the map (this isn't enforced currently).
    
     @return modifiable attribute map
   */
  public Map<String, String> getAttributes()
  {
    if (isTextElement())
    {
      throw new IllegalStateException("text tags don't have attributes");
    }
    return attributes;
  }
  /**
     Get the children of the element.

     Note that text elements do not have children, so this will throw
     an exception if called for a text element.

     NB: the caller may modify the list of children, but may not add
     null elements to the list (this isn't enforced currently).
    
     @return modifiable children list
   */
  public List<DocumentFragment> getChildren()
  {
    if (isTextElement())
    {
      throw new IllegalStateException("text tags don't have children");
    }
    return children;
  }
  /**
     Get the non-text children of the element.

     Note that text elements do not have children, so this will throw
     an exception if called for a text element.

     NB: the returned list is an off-line copy; modifying it will have no
     effect.
    
     @return children list
   */
  public List<DocumentFragment> getNonTextChildren()
  {
    List<DocumentFragment> nonText = new ArrayList<DocumentFragment>();
    for (DocumentFragment frag: getChildren())
    {
      if (!frag.isTextElement())
      {
        nonText.add(frag);
      }
    }
    return nonText;
  }
  /**
     Convenience method for setting an attribute.

     Note that text elements do not have attributes, so this will throw
     an exception if called for a text element.

     @deprecated use setAttrString

     @param attr The attribute name
     @param val The attribute value
   */
  public void setAttr(String attr, String val)
  {
    setAttrString(attr, val);
  }
  /**
     Convenience method for setting an attribute.

     Note that text elements do not have attributes, so this will throw
     an exception if called for a text element.

     @param attr The attribute name
     @param val The attribute value
   */
  public void setAttrString(String attr, String val)
  {
    if (attr == null || val == null)
    {
      throw new NullPointerException();
    }
    getAttributes().put(attr, val);
  }
  /**
     Convenience method for removing an attribute.

     Note that text elements do not have attributes, so this will throw
     an exception if called for a text element.

     @param attr The attribute name
   */
  public void removeAttr(String attr)
  {
    if (attr == null)
    {
      throw new NullPointerException();
    }
    getAttributes().remove(attr);
  }
  /**
     Set a byte attribute.

     Note that text elements do not have attributes, so this will throw
     an exception if called for a text element.

     @param attr The attribute name
     @param val The attribute value
   */
  public void setAttrByte(String attr, byte val)
  {
    setAttrString(attr, ""+val);
  }
  /**
     Set a short attribute.

     Note that text elements do not have attributes, so this will throw
     an exception if called for a text element.

     @param attr The attribute name
     @param val The attribute value
   */
  public void setAttrShort(String attr, short val)
  {
    setAttrString(attr, ""+val);
  }
  /**
     Set an int attribute.

     Note that text elements do not have attributes, so this will throw
     an exception if called for a text element.

     @param attr The attribute name
     @param val The attribute value
   */
  public void setAttrInt(String attr, int val)
  {
    setAttrString(attr, ""+val);
  }
  /**
     Set a long attribute.

     Note that text elements do not have attributes, so this will throw
     an exception if called for a text element.

     @param attr The attribute name
     @param val The attribute value
   */
  public void setAttrLong(String attr, long val)
  {
    setAttrString(attr, ""+val);
  }
  /**
     Set a float attribute.

     Note that text elements do not have attributes, so this will throw
     an exception if called for a text element.

     @param attr The attribute name
     @param val The attribute value
   */
  public void setAttrFloat(String attr, float val)
  {
    setAttrString(attr, ""+val);
  }
  /**
     Set a double attribute.

     Note that text elements do not have attributes, so this will throw
     an exception if called for a text element.

     @param attr The attribute name
     @param val The attribute value
   */
  public void setAttrDouble(String attr, double val)
  {
    setAttrString(attr, ""+val);
  }
  /**
     Set a byte attribute or remove the attribute.

     Note that text elements do not have attributes, so this will throw
     an exception if called for a text element.

     @param attr The attribute name
     @param val The attribute value or null if the attribute is to be removed
   */
  public void setAttrByteObject(String attr, Byte val)
  {
    if (val == null)
    {
      removeAttr(attr);
      return;
    }
    setAttrByte(attr, val.byteValue());
  }
  /**
     Set a short attribute or remove the attribute.

     Note that text elements do not have attributes, so this will throw
     an exception if called for a text element.

     @param attr The attribute name
     @param val The attribute value or null if the attribute is to be removed
   */
  public void setAttrShortObject(String attr, Short val)
  {
    if (val == null)
    {
      removeAttr(attr);
      return;
    }
    setAttrShort(attr, val.shortValue());
  }
  /**
     Set a int attribute or remove the attribute.

     Note that text elements do not have attributes, so this will throw
     an exception if called for a text element.

     @param attr The attribute name
     @param val The attribute value or null if the attribute is to be removed
   */
  public void setAttrIntObject(String attr, Integer val)
  {
    if (val == null)
    {
      removeAttr(attr);
      return;
    }
    setAttrInt(attr, val.intValue());
  }
  /**
     Set a long attribute or remove the attribute.

     Note that text elements do not have attributes, so this will throw
     an exception if called for a text element.

     @param attr The attribute name
     @param val The attribute value or null if the attribute is to be removed
   */
  public void setAttrLongObject(String attr, Long val)
  {
    if (val == null)
    {
      removeAttr(attr);
      return;
    }
    setAttrLong(attr, val.longValue());
  }
  /**
     Set a float attribute or remove the attribute.

     Note that text elements do not have attributes, so this will throw
     an exception if called for a text element.

     @param attr The attribute name
     @param val The attribute value or null if the attribute is to be removed
   */
  public void setAttrFloatObject(String attr, Float val)
  {
    if (val == null)
    {
      removeAttr(attr);
      return;
    }
    setAttrFloat(attr, val.floatValue());
  }
  /**
     Set a double attribute or remove the attribute.

     Note that text elements do not have attributes, so this will throw
     an exception if called for a text element.

     @param attr The attribute name
     @param val The attribute value or null if the attribute is to be removed
   */
  public void setAttrDoubleObject(String attr, Double val)
  {
    if (val == null)
    {
      removeAttr(attr);
      return;
    }
    setAttrDouble(attr, val.doubleValue());
  }
  /**
     Set a string attribute or remove the attribute.

     Note that text elements do not have attributes, so this will throw
     an exception if called for a text element.

     @param attr The attribute name
     @param val The attribute value or null if the attribute is to be removed
   */
  public void setAttrStringObject(String attr, String val)
  {
    if (val == null)
    {
      removeAttr(attr);
      return;
    }
    setAttrString(attr, val);
  }
  /**
     Convenience method for adding a child.

     Note that text elements do not have children, so this will throw
     an exception if called for a text element.

     @deprecated Use add(child)
     @param child The child to be added
   */
  public void addChild(DocumentFragment child)
  {
    add(child);
  }
  /**
     Convenience method for adding a child.

     Note that text elements do not have children, so this will throw
     an exception if called for a text element.

     @param child The child to be added
   */
  public void add(DocumentFragment child)
  {
    if (child == null)
    {
      throw new NullPointerException();
    }
    getChildren().add(child);
  }
  /**
     Convenience method for adding a child by tag name.

     Note that text elements do not have children, so this will throw
     an exception if called for a text element.

     @param tag The tag name
     @return The added child
   */
  public DocumentFragment add(String tag)
  {
    DocumentFragment result;
    if (tag == null)
    {
      throw new NullPointerException();
    }
    result = new DocumentFragment(tag);
    add(result);
    return result;
  }
  /**
     Convenience method for adding a text child.

     Will coalesce multiple consecutive text children into one.

     Note that text elements do not have children, so this will throw
     an exception if called for a text element.

     @param text The text of the child to be added
   */
  public void addTextChild(String text)
  {
    List<DocumentFragment> children = getChildren();
    while (   !children.isEmpty()
           &&  children.get(children.size() - 1).isTextElement())
    {
      text = children.remove(children.size() - 1).getText() + text;
    }
    addChild(newText(text));
  }
  /**
     Test whether this element is a text element.
    
     @return Whether this is a text element
   */
  public boolean isTextElement()
  {
    return text != null;
  }
  /**
     Return the text of the element.
    
     Returns null if called for a non-text element.
    
     @return The text of the element or null.
   */
  public String getText()
  {
    return text;
  }
  /**
     Construct a text element.
    
     @param tag Placeholder, must be null.
     @param text The text of the text element.
   */
  private DocumentFragment(String tag, String text)
  {
    // tag is placeholder
    if (tag != null)
    {
      throw new Error("text fragments can't have tag");
    }
    if (text == null)
    {
      throw new NullPointerException();
    }
    this.tag = null;
    this.text = text;
    this.children = new ArrayList<DocumentFragment>();
    this.attributes = new HashMap<String, String>();
  }
  /**
     Construct a text element.
    
     @param text The text of the text element.
   */
  public static DocumentFragment newText(String text)
  {
    return new DocumentFragment(null, text);
  }
  /**
     Construct an element with no children and no attributes.
    
     @param tag The XML tag of the element.
   */
  public DocumentFragment(String tag)
  {
    if (tag == null)
    {
      throw new NullPointerException();
    }
    this.tag = tag;
    this.text = null;
    this.children = new ArrayList<DocumentFragment>();
    this.attributes = new HashMap<String, String>();
  }
  /**
     Construct an element with no children
    
     @param tag The XML tag of the element.
     @param attributes The attributes of the element.
   */
  public DocumentFragment(String tag,
                          Map<String, String> attributes)
  {
    if (tag == null)
    {
      throw new NullPointerException();
    }
    this.tag = tag;
    this.text = null;
    this.children = new ArrayList<DocumentFragment>();
    this.attributes = new HashMap<String, String>(attributes);
  }
  /**
     Construct an element with no attributes
    
     @param tag The XML tag of the element.
     @param children The children of the element.
   */
  public DocumentFragment(String tag,
                          List<DocumentFragment> children)
  {
    if (tag == null)
    {
      throw new NullPointerException();
    }
    this.tag = tag;
    this.text = null;
    this.children = new ArrayList<DocumentFragment>(children);
    this.attributes = new HashMap<String, String>();
  }
  /**
     Construct an element
    
     @param tag The XML tag of the element.
     @param children The children of the element.
     @param attributes The attributes of the element.
   */
  public DocumentFragment(String tag,
                          List<DocumentFragment> children,
                          Map<String, String> attributes)
  {
    if (tag == null)
    {
      throw new NullPointerException();
    }
    this.tag = tag;
    this.text = null;
    this.children = new ArrayList<DocumentFragment>(children);
    this.attributes = new HashMap<String, String>(attributes);
  }
  /**
     Get the tag name of the element.

     @return The tag name or null for text elements
   */
  public String getTag()
  {
    return tag;
  }
  /**
     Get a list of children with the specified tag

     @param tag The specified tag
     @return The list of children with the specified tag
   */
  public ArrayList<DocumentFragment> getMulti(String tag)
  {
    ArrayList<DocumentFragment> l = new ArrayList<DocumentFragment>();
    if (tag == null)
    {
      throw new NullPointerException();
    }
    for (DocumentFragment child: getChildren())
    {
      if (tag.equals(child.getTag()))
      {
        l.add(child);
      }
    }
    return l;
  }
  /**
     Get the only child with the specified tag.

     If there are multiple children with the specified tag, throws an
     exception.

     @param tag The specified tag
     @return The child with the specified tag or null if no such child exists
   */
  public DocumentFragment get(String tag)
  {
    DocumentFragment f = null;
    if (tag == null)
    {
      throw new NullPointerException();
    }
    for (DocumentFragment child: getChildren())
    {
      if (!tag.equals(child.getTag()))
      {
        continue;
      }
      if (f != null)
      {
        throw new XMLMultipleElementsException("multiple elements having" +
                                               " tag " + tag);
      }
      f = child;
    }
    return f;
  }
  /**
     Get the only child with the specified tag, asserting it is non-null.

     If there are multiple children with the specified tag, throws an
     exception.

     If there is no child with the specified tag, throws an exception.

     @param tag The specified tag
     @return The child with the specified tag
   */
  public DocumentFragment getNotNull(String tag)
  {
    DocumentFragment f = get(tag);
    if (f == null)
    {
      throw new XMLException("no such tag: " + tag);
    }
    return f;
  }
  /**
     Assert that the tag name is the specified.

     If the tag name differs, throws an exception.

     @param tag The specified tag name
   */
  public void assertTag(String tag)
  {
    if (!tag.equals(this.tag))
    {
      throw new XMLException("tag name expected: " + tag +
                             ", actual: " + this.tag);
    }
  }
  /**
     Remove and get a list of all children

     @return The list of children with the specified tag
   */
  public List<DocumentFragment> removeAll()
  {
    List<DocumentFragment> children = getChildren();
    List<DocumentFragment> removed = new ArrayList<DocumentFragment>(children);
    children.clear();
    return removed;
  }
  /**
     Remove and get a list of children with the specified tag

     @param tag The specified tag
     @return The list of children with the specified tag
   */
  public ArrayList<DocumentFragment> removeMulti(String tag)
  {
    ArrayList<DocumentFragment> new_children = new ArrayList<DocumentFragment>();
    ArrayList<DocumentFragment> removed = new ArrayList<DocumentFragment>();
    if (tag == null)
    {
      throw new NullPointerException();
    }
    for (DocumentFragment child: getChildren())
    {
      if (!tag.equals(child.getTag()))
      {
        new_children.add(child);
      }
      else
      {
        removed.add(child);
      }
    }
    this.children.clear();
    this.children.addAll(new_children);
    return removed;
  }
  /**
     Remove and get the only child with the specified tag.

     If there are multiple children with the specified tag, throws an
     exception and does not modify the list.

     @param tag The specified tag
     @return The child with the specified tag or null if no such child exists
   */
  public DocumentFragment remove(String tag)
  {
    List<DocumentFragment> children = getChildren();
    int saved_i = -1;
    int i = 0;
    final int num_children = children.size();
    Iterator<DocumentFragment> it = children.iterator();
    if (tag == null)
    {
      throw new NullPointerException();
    }
    while (it.hasNext())
    {
      DocumentFragment frag = it.next();
      if (tag.equals(frag.getTag()))
      {
        if (saved_i >= 0)
        {
          throw new XMLMultipleElementsException("multiple elements having" +
                                                 " tag " + tag);
        }
        saved_i = i;
      }
      i++;
    }
    if (saved_i < 0)
    {
      return null;
    }
    return children.remove(saved_i);
  }


  /**
     Get a byte attribute that must be non-null.

     Throws an exception if the attribute does not exist.

     @param attr The attribute name
     @return The value of the attribute as a byte
   */
  public byte getAttrByteNotNull(String attr)
  {
    return Byte.parseByte(getAttrStringNotNull(attr));
  }
  /**
     Get a byte attribute

     @param attr The attribute name
     @param default_value Default value if the attribute does not exist
     @return The value of the attribute as a byte
   */
  public byte getAttrByte(String attr, byte default_value)
  {
    try
    {
      return Byte.parseByte(getAttrStringNotNull(attr));
    }
    catch(XMLValueMissingException e)
    {
      return default_value;
    }
  }
  /**
     Get a byte attribute or null

     @param attr The attribute name
     @return The value of the attribute as a byte or null if nonexistent
   */
  public Byte getAttrByteObject(String attr)
  {
    try
    {
      return Byte.valueOf(getAttrStringNotNull(attr));
    }
    catch(XMLValueMissingException e)
    {
      return null;
    }
  }
  /**
     Get a short attribute that must be non-null.

     Throws an exception if the attribute does not exist.

     @param attr The attribute name
     @return The value of the attribute as a short
   */
  public short getAttrShortNotNull(String attr)
  {
    return Short.parseShort(getAttrStringNotNull(attr));
  }
  /**
     Get a short attribute

     @param attr The attribute name
     @param default_value Default value if the attribute does not exist
     @return The value of the attribute as a short
   */
  public short getAttrShort(String attr, short default_value)
  {
    try
    {
      return Short.parseShort(getAttrStringNotNull(attr));
    }
    catch(XMLValueMissingException e)
    {
      return default_value;
    }
  }
  /**
     Get a short attribute or null

     @param attr The attribute name
     @return The value of the attribute as a short or null if nonexistent
   */
  public Short getAttrShortObject(String attr)
  {
    try
    {
      return Short.valueOf(getAttrStringNotNull(attr));
    }
    catch(XMLValueMissingException e)
    {
      return null;
    }
  }
  /**
     Get an integer attribute that must be non-null.

     Throws an exception if the attribute does not exist.

     @param attr The attribute name
     @return The value of the attribute as an integer
   */
  public int getAttrIntNotNull(String attr)
  {
    return Integer.parseInt(getAttrStringNotNull(attr));
  }
  /**
     Get an integer attribute

     @param attr The attribute name
     @param default_value Default value if the attribute does not exist
     @return The value of the attribute as an integer
   */
  public int getAttrInt(String attr, int default_value)
  {
    try
    {
      return Integer.parseInt(getAttrStringNotNull(attr));
    }
    catch(XMLValueMissingException e)
    {
      return default_value;
    }
  }
  /**
     Get an integer attribute or null

     @param attr The attribute name
     @return The value of the attribute as an integer or null if nonexistent
   */
  public Integer getAttrIntObject(String attr)
  {
    try
    {
      return Integer.valueOf(getAttrStringNotNull(attr));
    }
    catch(XMLValueMissingException e)
    {
      return null;
    }
  }
  /**
     Get a long attribute that must be non-null.

     Throws an exception if the attribute does not exist.

     @param attr The attribute name
     @return The value of the attribute as a long
   */
  public long getAttrLongNotNull(String attr)
  {
    return Long.parseLong(getAttrStringNotNull(attr));
  }
  /**
     Get a long attribute

     @param attr The attribute name
     @param default_value Default value if the attribute does not exist
     @return The value of the attribute as a long
   */
  public long getAttrLong(String attr, long default_value)
  {
    try
    {
      return Long.parseLong(getAttrStringNotNull(attr));
    }
    catch(XMLValueMissingException e)
    {
      return default_value;
    }
  }
  /**
     Get a long attribute or null

     @param attr The attribute name
     @return The value of the attribute as a long or null if nonexistent
   */
  public Long getAttrLongObject(String attr)
  {
    try
    {
      return Long.valueOf(getAttrStringNotNull(attr));
    }
    catch(XMLValueMissingException e)
    {
      return null;
    }
  }
  /**
     Get a float attribute that must be non-null.

     Throws an exception if the attribute does not exist.

     @param attr The attribute name
     @return The value of the attribute as a float
   */
  public float getAttrFloatNotNull(String attr)
  {
    return Float.parseFloat(getAttrStringNotNull(attr));
  }
  /**
     Get a float attribute

     @param attr The attribute name
     @param default_value Default value if the attribute does not exist
     @return The value of the attribute as a float
   */
  public float getAttrFloat(String attr, float default_value)
  {
    try
    {
      return Float.parseFloat(getAttrStringNotNull(attr));
    }
    catch(XMLValueMissingException e)
    {
      return default_value;
    }
  }
  /**
     Get a float attribute or null

     @param attr The attribute name
     @return The value of the attribute as a float or null if nonexistent
   */
  public Float getAttrFloatObject(String attr)
  {
    try
    {
      return Float.valueOf(getAttrStringNotNull(attr));
    }
    catch(XMLValueMissingException e)
    {
      return null;
    }
  }
  /**
     Get a double attribute that must be non-null.

     Throws an exception if the attribute does not exist.

     @param attr The attribute name
     @return The value of the attribute as a double
   */
  public double getAttrDoubleNotNull(String attr)
  {
    return Double.parseDouble(getAttrStringNotNull(attr));
  }
  /**
     Get a double attribute

     @param attr The attribute name
     @param default_value Default value if the attribute does not exist
     @return The value of the attribute as a double
   */
  public double getAttrDouble(String attr, double default_value)
  {
    try
    {
      return Double.parseDouble(getAttrStringNotNull(attr));
    }
    catch(XMLValueMissingException e)
    {
      return default_value;
    }
  }
  /**
     Get a double attribute or null

     @param attr The attribute name
     @return The value of the attribute as a double or null if nonexistent
   */
  public Double getAttrDoubleObject(String attr)
  {
    try
    {
      return Double.valueOf(getAttrStringNotNull(attr));
    }
    catch(XMLValueMissingException e)
    {
      return null;
    }
  }
  /**
     Get a string attribute that must be non-null.

     Throws an exception if the attribute does not exist.

     @param attr The attribute name
     @return The value of the attribute as a string
   */
  public String getAttrStringNotNull(String attr)
  {
    String result = getAttrStringObject(attr);
    if (result == null)
    {
      throw new XMLValueMissingException();
    }
    return result;
  }
  /**
     Get a string attribute

     @param attr The attribute name
     @param default_value Default value if the attribute does not exist
     @return The value of the attribute as a string
   */
  public String getAttrString(String attr, String default_value)
  {
    String result = getAttrStringObject(attr);
    if (result == null)
    {
      return default_value;
    }
    return result;
  }
  /**
     Get a string attribute or null

     @param attr The attribute name
     @return The value of the attribute as a string or null if nonexistent
   */
  public String getAttrStringObject(String attr)
  {
    return getAttributes().get(attr);
  }


  /**
     Get a byte text that must be non-null in this element.
    
     @return The value of the text as a byte
   */
  public byte getThisByteNotNull()
  {
    return Byte.parseByte(getThisStringNotEmpty());
  }
  /**
     Get a byte text in this element.
    
     @param default_value Default value if the element does not exist
     @return The value of the text as a byte
   */
  public byte getThisByte(byte default_value)
  {
    try
    {
      return Byte.parseByte(getThisStringNotEmpty());
    }
    catch(XMLValueMissingException e)
    {
      return default_value;
    }
  }
  /**
     Get a byte text in this element or null.
    
     @return The value of the text as a byte
   */
  public Byte getThisByteObject()
  {
    try
    {
      return Byte.valueOf(getThisStringNotEmpty());
    }
    catch(XMLValueMissingException e)
    {
      return null;
    }
  }
  /**
     Get a short text that must be non-null in this element.
    
     @return The value of the text as a short
   */
  public short getThisShortNotNull()
  {
    return Short.parseShort(getThisStringNotEmpty());
  }
  /**
     Get a short text in this element.
    
     @param default_value Default value if the element does not exist
     @return The value of the text as a short
   */
  public short getThisShort(short default_value)
  {
    try
    {
      return Short.parseShort(getThisStringNotEmpty());
    }
    catch(XMLValueMissingException e)
    {
      return default_value;
    }
  }
  /**
     Get a short text in this element or null.
    
     @return The value of the text as a short
   */
  public Short getThisShortObject()
  {
    try
    {
      return Short.valueOf(getThisStringNotEmpty());
    }
    catch(XMLValueMissingException e)
    {
      return null;
    }
  }
  /**
     Get an integer text that must be non-null in this element.
    
     @return The value of the text as an integer
   */
  public int getThisIntNotNull()
  {
    return Integer.parseInt(getThisStringNotEmpty());
  }
  /**
     Get an integer text in this element.
    
     @param default_value Default value if the element does not exist
     @return The value of the text as an integer
   */
  public int getThisInt(int default_value)
  {
    try
    {
      return Integer.parseInt(getThisStringNotEmpty());
    }
    catch(XMLValueMissingException e)
    {
      return default_value;
    }
  }
  /**
     Get an integer text in this element or null.
    
     @return The value of the text as an integer
   */
  public Integer getThisIntObject()
  {
    try
    {
      return Integer.valueOf(getThisStringNotEmpty());
    }
    catch(XMLValueMissingException e)
    {
      return null;
    }
  }
  /**
     Get a long text that must be non-null in this element.
    
     @return The value of the text as a long
   */
  public long getThisLongNotNull()
  {
    return Long.parseLong(getThisStringNotEmpty());
  }
  /**
     Get a long text in this element.
    
     @param default_value Default value if the element does not exist
     @return The value of the text as a long
   */
  public long getThisLong(long default_value)
  {
    try
    {
      return Long.parseLong(getThisStringNotEmpty());
    }
    catch(XMLValueMissingException e)
    {
      return default_value;
    }
  }
  /**
     Get a long text in this element or null.
    
     @return The value of the text as a long
   */
  public Long getThisLongObject()
  {
    try
    {
      return Long.valueOf(getThisStringNotEmpty());
    }
    catch(XMLValueMissingException e)
    {
      return null;
    }
  }
  /**
     Get a float text that must be non-null in this element.
    
     @return The value of the text as a float
   */
  public float getThisFloatNotNull()
  {
    return Float.parseFloat(getThisStringNotEmpty());
  }
  /**
     Get a float text in this element.
    
     @param default_value Default value if the element does not exist
     @return The value of the text as a float
   */
  public float getThisFloat(float default_value)
  {
    try
    {
      return Float.parseFloat(getThisStringNotEmpty());
    }
    catch(XMLValueMissingException e)
    {
      return default_value;
    }
  }
  /**
     Get a float text in this element or null.
    
     @return The value of the text as a float
   */
  public Float getThisFloatObject()
  {
    try
    {
      return Float.valueOf(getThisStringNotEmpty());
    }
    catch(XMLValueMissingException e)
    {
      return null;
    }
  }
  /**
     Get a double text that must be non-null in this element.
    
     @return The value of the text as a double
   */
  public double getThisDoubleNotNull()
  {
    return Double.parseDouble(getThisStringNotEmpty());
  }
  /**
     Get a double text in this element.
    
     @param default_value Default value if the element does not exist
     @return The value of the text as a double
   */
  public double getThisDouble(double default_value)
  {
    try
    {
      return Double.parseDouble(getThisStringNotEmpty());
    }
    catch(XMLValueMissingException e)
    {
      return default_value;
    }
  }
  /**
     Get a double text in this element or null.
    
     @return The value of the text as a double
   */
  public Double getThisDoubleObject()
  {
    try
    {
      return Double.valueOf(getThisStringNotEmpty());
    }
    catch(XMLValueMissingException e)
    {
      return null;
    }
  }
  /**
     Get text in this element.
    
     @param default_value Default value if the text is empty
     @return The value of the text as a string
   */
  public String getThisString(String default_value)
  {
    String result = getThisStringObjectEmptyIsNull();
    if (result == null)
    {
      return default_value;
    }
    return result;
  }
  /**
     Get text in this element that must be non-empty.

     Throws XMLValueMissingException if the contents are empty.
    
     @return The value of the text as a string
   */
  public String getThisStringNotEmpty()
  {
    String result = getThisStringObjectEmptyIsNull();
    if (result == null)
    {
      throw new XMLValueMissingException();
    }
    return result;
  }
  /**
     Get text in this element or null.

     Treats empty string content as null.
    
     @return The value of the text as a string
   */
  public String getThisStringObjectEmptyIsNull()
  {
    String result = getThisStringObjectEmptyIsEmpty();
    if ("".equals(result))
    {
      return null;
    }
    return result;
  }
  /**
     Get text in this element.

     Returns "" if the content is empty.
    
     @return The value of the text as a string
   */
  public String getThisStringObjectEmptyIsEmpty()
  {
    String result = "";
    DocumentFragment e = this;
    for (DocumentFragment frag: e.getChildren())
    {
      if (frag.getText() == null)
      {
        throw new XMLException("element has a non-text child");
      }
      result = result + frag.getText();
    }
    return result;
  }


  /**
     Get a byte text that must be non-null in enclosing element.
    
     @param element The tag of the enclosing element
     @return The value of the text as a byte
   */
  public byte getByteNotNull(String element)
  {
    return Byte.parseByte(getStringNotNull(element));
  }
  /**
     Get a byte text in enclosing element.
    
     @param element The tag of the enclosing element
     @param default_value Default value if the element does not exist
     @return The value of the text as a byte
   */
  public byte getByte(String element, byte default_value)
  {
    try
    {
      return Byte.parseByte(getStringNotNull(element));
    }
    catch(XMLValueMissingException e)
    {
      return default_value;
    }
  }
  /**
     Get a byte text in enclosing element or null.
    
     @param element The tag of the enclosing element
     @return The value of the text as a byte
   */
  public Byte getByteObject(String element)
  {
    try
    {
      return Byte.valueOf(getStringNotNull(element));
    }
    catch(XMLValueMissingException e)
    {
      return null;
    }
  }
  /**
     Get a short text that must be non-null in enclosing element.
    
     @param element The tag of the enclosing element
     @return The value of the text as a short
   */
  public short getShortNotNull(String element)
  {
    return Short.parseShort(getStringNotNull(element));
  }
  /**
     Get a short text in enclosing element.
    
     @param element The tag of the enclosing element
     @param default_value Default value if the element does not exist
     @return The value of the text as a short
   */
  public short getShort(String element, short default_value)
  {
    try
    {
      return Short.parseShort(getStringNotNull(element));
    }
    catch(XMLValueMissingException e)
    {
      return default_value;
    }
  }
  /**
     Get a short text in enclosing element or null.
    
     @param element The tag of the enclosing element
     @return The value of the text as a short
   */
  public Short getShortObject(String element)
  {
    try
    {
      return Short.valueOf(getStringNotNull(element));
    }
    catch(XMLValueMissingException e)
    {
      return null;
    }
  }
  /**
     Get an integer text that must be non-null in enclosing element.
    
     @param element The tag of the enclosing element
     @return The value of the text as an integer
   */
  public int getIntNotNull(String element)
  {
    return Integer.parseInt(getStringNotNull(element));
  }
  /**
     Get an integer text in enclosing element.
    
     @param element The tag of the enclosing element
     @param default_value Default value if the element does not exist
     @return The value of the text as an integer
   */
  public int getInt(String element, int default_value)
  {
    try
    {
      return Integer.parseInt(getStringNotNull(element));
    }
    catch(XMLValueMissingException e)
    {
      return default_value;
    }
  }
  /**
     Get an integer text in enclosing element or null.
    
     @param element The tag of the enclosing element
     @return The value of the text as an integer
   */
  public Integer getIntObject(String element)
  {
    try
    {
      return Integer.valueOf(getStringNotNull(element));
    }
    catch(XMLValueMissingException e)
    {
      return null;
    }
  }
  /**
     Get a long text that must be non-null in enclosing element.
    
     @param element The tag of the enclosing element
     @return The value of the text as a long
   */
  public long getLongNotNull(String element)
  {
    return Long.parseLong(getStringNotNull(element));
  }
  /**
     Get a long text in enclosing element.
    
     @param element The tag of the enclosing element
     @param default_value Default value if the element does not exist
     @return The value of the text as a long
   */
  public long getLong(String element, long default_value)
  {
    try
    {
      return Long.parseLong(getStringNotNull(element));
    }
    catch(XMLValueMissingException e)
    {
      return default_value;
    }
  }
  /**
     Get a long text in enclosing element or null.
    
     @param element The tag of the enclosing element
     @return The value of the text as a long
   */
  public Long getLongObject(String element)
  {
    try
    {
      return Long.valueOf(getStringNotNull(element));
    }
    catch(XMLValueMissingException e)
    {
      return null;
    }
  }
  /**
     Get a float text that must be non-null in enclosing element.
    
     @param element The tag of the enclosing element
     @return The value of the text as a float
   */
  public float getFloatNotNull(String element)
  {
    return Float.parseFloat(getStringNotNull(element));
  }
  /**
     Get a float text in enclosing element.
    
     @param element The tag of the enclosing element
     @param default_value Default value if the element does not exist
     @return The value of the text as a float
   */
  public float getFloat(String element, float default_value)
  {
    try
    {
      return Float.parseFloat(getStringNotNull(element));
    }
    catch(XMLValueMissingException e)
    {
      return default_value;
    }
  }
  /**
     Get a float text in enclosing element or null.
    
     @param element The tag of the enclosing element
     @return The value of the text as a float
   */
  public Float getFloatObject(String element)
  {
    try
    {
      return Float.valueOf(getStringNotNull(element));
    }
    catch(XMLValueMissingException e)
    {
      return null;
    }
  }
  /**
     Get a double text that must be non-null in enclosing element.
    
     @param element The tag of the enclosing element
     @return The value of the text as a double
   */
  public double getDoubleNotNull(String element)
  {
    return Double.parseDouble(getStringNotNull(element));
  }
  /**
     Get a double text in enclosing element.
    
     @param element The tag of the enclosing element
     @param default_value Default value if the element does not exist
     @return The value of the text as a double
   */
  public double getDouble(String element, double default_value)
  {
    try
    {
      return Double.parseDouble(getStringNotNull(element));
    }
    catch(XMLValueMissingException e)
    {
      return default_value;
    }
  }
  /**
     Get a double text in enclosing element or null.
    
     @param element The tag of the enclosing element
     @return The value of the text as a double
   */
  public Double getDoubleObject(String element)
  {
    try
    {
      return Double.valueOf(getStringNotNull(element));
    }
    catch(XMLValueMissingException e)
    {
      return null;
    }
  }
  /**
     Get text that must be non-null in enclosing element.
    
     @param element The tag of the enclosing element
     @return The value of the text as a string
   */
  public String getStringNotNull(String element)
  {
    String result = getStringObject(element);
    if (result == null)
    {
      throw new XMLValueMissingException();
    }
    return result;
  }
  /**
     Get text in enclosing element.
    
     @param element The tag of the enclosing element
     @param default_value Default value if the element does not exist
     @return The value of the text as a string
   */
  public String getString(String element, String default_value)
  {
    String result = getStringObject(element);
    if (result == null)
    {
      return default_value;
    }
    return result;
  }
  /**
     Get text in enclosing element or null.
    
     @param element The tag of the enclosing element
     @return The value of the text as a string
   */
  public String getStringObject(String element)
  {
    String result = "";
    DocumentFragment e = get(element);
    if (e == null)
    {
      return null;
    }
    return e.getThisStringObjectEmptyIsEmpty();
  }
  /**
     Set byte text in enclosing element.
    
     @param element The tag of the enclosing element
     @param val The value that will be converted to text
   */
  public void setByte(String element, byte val)
  {
    setString(element, ""+val);
  }
  /**
     Set short text in enclosing element.
    
     @param element The tag of the enclosing element
     @param val The value that will be converted to text
   */
  public void setShort(String element, short val)
  {
    setString(element, ""+val);
  }
  /**
     Set int text in enclosing element.
    
     @param element The tag of the enclosing element
     @param val The value that will be converted to text
   */
  public void setInt(String element, int val)
  {
    setString(element, ""+val);
  }
  /**
     Set long text in enclosing element.
    
     @param element The tag of the enclosing element
     @param val The value that will be converted to text
   */
  public void setLong(String element, long val)
  {
    setString(element, ""+val);
  }
  /**
     Set float text in enclosing element.
    
     @param element The tag of the enclosing element
     @param val The value that will be converted to text
   */
  public void setFloat(String element, float val)
  {
    setString(element, ""+val);
  }
  /**
     Set double text in enclosing element.
    
     @param element The tag of the enclosing element
     @param val The value that will be converted to text
   */
  public void setDouble(String element, double val)
  {
    setString(element, ""+val);
  }
  /**
     Set a data structure in enclosing element.
    
     @param element The tag of the enclosing element
     @param rowable The value that will be serialized
   */
  public void setRow(String element, XMLRowable rowable)
  {
    if (rowable == null)
    {
      throw new NullPointerException();
    }
    rowable.toXMLRow(set(element));
  }
  /**
     Set byte text in enclosing element or remove the element.
    
     @param element The tag of the enclosing element
     @param object The value that will be converted to text or null if the
                   element is to be removed
   */
  public void setByteObject(String element, Byte object)
  {
    if (object == null)
    {
      remove(element);
      return;
    }
    setByte(element, object.byteValue());
  }
  /**
     Set short text in enclosing element or remove the element.
    
     @param element The tag of the enclosing element
     @param object The value that will be converted to text or null if the
                   element is to be removed
   */
  public void setShortObject(String element, Short object)
  {
    if (object == null)
    {
      remove(element);
      return;
    }
    setShort(element, object.shortValue());
  }
  /**
     Set int text in enclosing element or remove the element.
    
     @param element The tag of the enclosing element
     @param object The value that will be converted to text or null if the
                   element is to be removed
   */
  public void setIntObject(String element, Integer object)
  {
    if (object == null)
    {
      remove(element);
      return;
    }
    setInt(element, object.intValue());
  }
  /**
     Set long text in enclosing element or remove the element.
    
     @param element The tag of the enclosing element
     @param object The value that will be converted to text or null if the
                   element is to be removed
   */
  public void setLongObject(String element, Long object)
  {
    if (object == null)
    {
      remove(element);
      return;
    }
    setLong(element, object.longValue());
  }
  /**
     Set float text in enclosing element or remove the element.
    
     @param element The tag of the enclosing element
     @param object The value that will be converted to text or null if the
                   element is to be removed
   */
  public void setFloatObject(String element, Float object)
  {
    if (object == null)
    {
      remove(element);
      return;
    }
    setFloat(element, object.floatValue());
  }
  /**
     Set double text in enclosing element or remove the element.
    
     @param element The tag of the enclosing element
     @param object The value that will be converted to text or null if the
                   element is to be removed
   */
  public void setDoubleObject(String element, Double object)
  {
    if (object == null)
    {
      remove(element);
      return;
    }
    setDouble(element, object.doubleValue());
  }
  /**
     Set data structure in enclosing element or remove the element.
    
     @param element The tag of the enclosing element
     @param object The value that will be serialized or null if the
                   element is to be removed
   */
  public void setRowObject(String element, XMLRowable object)
  {
    if (object == null)
    {
      remove(element);
      return;
    }
    setRow(element, object);
  }

  /**
     Set byte text in this element.
    
     @param val The value that will be converted to text
   */
  public void setThisByte(byte val)
  {
    setThisString(""+val);
  }
  /**
     Set short text in this element.
    
     @param val The value that will be converted to text
   */
  public void setThisShort(short val)
  {
    setThisString(""+val);
  }
  /**
     Set int text in this element.
    
     @param val The value that will be converted to text
   */
  public void setThisInt(int val)
  {
    setThisString(""+val);
  }
  /**
     Set long text in this element.
    
     @param val The value that will be converted to text
   */
  public void setThisLong(long val)
  {
    setThisString(""+val);
  }
  /**
     Set float text in this element.
    
     @param val The value that will be converted to text
   */
  public void setThisFloat(float val)
  {
    setThisString(""+val);
  }
  /**
     Set double text in this element.
    
     @param val The value that will be converted to text
   */
  public void setThisDouble(double val)
  {
    setThisString(""+val);
  }
  /**
     Set data structure in this element.
    
     @param rowable The value that will be serialized
   */
  public void setThisRow(XMLRowable rowable)
  {
    if (rowable == null)
    {
      throw new NullPointerException();
    }
    removeAll();
    rowable.toXMLRow(this);
  }
  /**
     Set byte text in this element or remove the text contents.
    
     @param object The value that will be converted to text or null if the
                   text contents are to be removed
   */
  public void setThisByteObject(Byte object)
  {
    if (object == null)
    {
      removeAll();
      return;
    }
    setThisByte(object.byteValue());
  }
  /**
     Set short text in this element or remove the text contents.
    
     @param object The value that will be converted to text or null if the
                   text contents are to be removed
   */
  public void setThisShortObject(Short object)
  {
    if (object == null)
    {
      removeAll();
      return;
    }
    setThisShort(object.shortValue());
  }
  /**
     Set int text in this element or remove the text contents.
    
     @param object The value that will be converted to text or null if the
                   text contents are to be removed
   */
  public void setThisIntObject(Integer object)
  {
    if (object == null)
    {
      removeAll();
      return;
    }
    setThisInt(object.intValue());
  }
  /**
     Set long text in this element or remove the text contents.
    
     @param object The value that will be converted to text or null if the
                   text contents are to be removed
   */
  public void setThisLongObject(Long object)
  {
    if (object == null)
    {
      removeAll();
      return;
    }
    setThisLong(object.longValue());
  }
  /**
     Set float text in this element or remove the text contents.
    
     @param object The value that will be converted to text or null if the
                   text contents are to be removed
   */
  public void setThisFloatObject(Float object)
  {
    if (object == null)
    {
      removeAll();
      return;
    }
    setThisFloat(object.floatValue());
  }
  /**
     Set double text in this element or remove the text contents.
    
     @param object The value that will be converted to text or null if the
                   text contents are to be removed
   */
  public void setThisDoubleObject(Double object)
  {
    if (object == null)
    {
      removeAll();
      return;
    }
    setThisDouble(object.doubleValue());
  }
  /**
     Set data structure in this element or remove the contents.
    
     @param object The value that will be serialized or null if the
                   contents are to be removed
   */
  public void setThisRowObject(XMLRowable object)
  {
    if (object == null)
    {
      removeAll();
      return;
    }
    setThisRow(object);
  }
  /**
     Return the element with the tag name, creating it if nonexistent.
    
     @param element The tag of the element
     @return The element
   */
  public DocumentFragment getOrCreate(String element)
  {
    DocumentFragment e;
    if (element == null)
    {
      throw new NullPointerException();
    }
    e = get(element);
    if (e == null)
    {
      e = new DocumentFragment(element);
      add(e);
    }
    return e;
  }
  /**
     Return the element with the tag name, creating it if nonexistent and clear
     its contents if existent.
    
     @param element The tag of the element
     @return The element
   */
  public DocumentFragment set(String element)
  {
    DocumentFragment e = getOrCreate(element);
    e.removeAll();
    return e;
  }
  /**
     Set text in enclosing element
    
     @param element The tag of the enclosing element
     @param object The text in enclosing element
   */
  public void setString(String element, String object)
  {
    if (element == null || object == null)
    {
      throw new NullPointerException();
    }
    set(element).addTextChild(object);
  }
  /**
     Set text in enclosing element or remove the element.
    
     @param element The tag of the enclosing element
     @param object The text in enclosing element or null if the element is to be
                   removed
   */
  public void setStringObject(String element, String object)
  {
    if (element == null)
    {
      throw new NullPointerException();
    }
    if (object == null)
    {
      remove(element);
      return;
    }
    setString(element, object);
  }

  /**
     Set text in this element
    
     @param object The text in enclosing element or null if the element is to be
                   removed
   */
  public void setThisString(String object)
  {
    if (object == null)
    {
      throw new NullPointerException();
    }
    removeAll();
    addTextChild(object);
  }
  /**
     Set text in this element or remove the contents.
    
     @param object The text in enclosing element or null if the contents are to
                   be removed
   */
  public void setThisStringObject(String object)
  {
    if (object == null)
    {
      removeAll();
      return;
    }
    setThisString(object);
  }
};
