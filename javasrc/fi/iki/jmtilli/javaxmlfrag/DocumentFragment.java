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
/**
   Fragment of an XML document.
  
   Document fragments can either be text fragments (have textual content,
   can't have children or attributes) or tags (can have both children or
   attributes but don't contain text themselves -- but can contain text
   fragment children).
 */
public class DocumentFragment {
  private final String tag;
  private final String text;
  private final ArrayList<DocumentFragment> children;
  private final HashMap<String, String> attributes;
  /**
     Get the attributes of the element.

     Note that text elements do not have attributes, so this will throw
     an exception if called for a text element.
    
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
     Convenience method for setting an attribute.

     Note that text elements do not have attributes, so this will throw
     an exception if called for a text element.

     @param attr The attribute name
     @param val The attribute value
   */
  public void setAttr(String attr, String val)
  {
    getAttributes().put(attr, val);
  }
  /**
     Convenience method for adding a child.

     Note that text elements do not have children, so this will throw
     an exception if called for a text element.

     @param child The child to be added
   */
  public void addChild(DocumentFragment child)
  {
    getChildren().add(child);
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
    this.tag = tag;
    this.text = null;
    this.children = new ArrayList<DocumentFragment>();
    this.attributes = new HashMap<String, String>(attributes);
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

     @return The list of children with the specified tag
   */
  public ArrayList<DocumentFragment> getMulti(String tag)
  {
    ArrayList<DocumentFragment> l = new ArrayList<DocumentFragment>();
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

     @return The child with the specified tag or null if no such child exists
   */
  public DocumentFragment get(String tag)
  {
    DocumentFragment f = null;
    for (DocumentFragment child: getChildren())
    {
      if (!tag.equals(child.getTag()))
      {
        continue;
      }
      if (f != null)
      {
        throw new XMLException("multiple elements having tag " + tag);
      }
      f = child;
    }
    return f;
  }


  /**
     Get a byte attribute that must be non-null.

     Throws an exception if the attribute does not exist.

     @param attr The attribute name
     @return The value of the attribute as a byte
   */
  public byte getAttrByteNotNull(String attr)
  {
    return Byte.parseByte(getAttrStringObject(attr));
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
      return Byte.parseByte(getAttrStringObject(attr));
    }
    catch(NullPointerException e)
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
      return Byte.valueOf(getAttrStringObject(attr));
    }
    catch(NullPointerException e)
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
    return Short.parseShort(getAttrStringObject(attr));
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
      return Short.parseShort(getAttrStringObject(attr));
    }
    catch(NullPointerException e)
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
      return Short.valueOf(getAttrStringObject(attr));
    }
    catch(NullPointerException e)
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
    return Integer.parseInt(getAttrStringObject(attr));
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
      return Integer.parseInt(getAttrStringObject(attr));
    }
    catch(NullPointerException e)
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
      return Integer.valueOf(getAttrStringObject(attr));
    }
    catch(NullPointerException e)
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
    return Long.parseLong(getAttrStringObject(attr));
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
      return Long.parseLong(getAttrStringObject(attr));
    }
    catch(NullPointerException e)
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
      return Long.valueOf(getAttrStringObject(attr));
    }
    catch(NullPointerException e)
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
    return Float.parseFloat(getAttrStringObject(attr));
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
      return Float.parseFloat(getAttrStringObject(attr));
    }
    catch(NullPointerException e)
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
      return Float.valueOf(getAttrStringObject(attr));
    }
    catch(NullPointerException e)
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
    return Double.parseDouble(getAttrStringObject(attr));
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
      return Double.parseDouble(getAttrStringObject(attr));
    }
    catch(NullPointerException e)
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
      return Double.valueOf(getAttrStringObject(attr));
    }
    catch(NullPointerException e)
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
      throw new NullPointerException();
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
     Get a byte text that must be non-null in enclosing element.
    
     @param element The tag of the enclosing element
     @return The value of the text as a byte
   */
  public byte getByteNotNull(String element)
  {
    return Byte.parseByte(getStringObject(element));
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
      return Byte.parseByte(getStringObject(element));
    }
    catch(NullPointerException e)
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
      return Byte.valueOf(getStringObject(element));
    }
    catch(NullPointerException e)
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
    return Short.parseShort(getStringObject(element));
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
      return Short.parseShort(getStringObject(element));
    }
    catch(NullPointerException e)
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
      return Short.valueOf(getStringObject(element));
    }
    catch(NullPointerException e)
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
    return Integer.parseInt(getStringObject(element));
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
      return Integer.parseInt(getStringObject(element));
    }
    catch(NullPointerException e)
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
      return Integer.valueOf(getStringObject(element));
    }
    catch(NullPointerException e)
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
    return Long.parseLong(getStringObject(element));
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
      return Long.parseLong(getStringObject(element));
    }
    catch(NullPointerException e)
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
      return Long.valueOf(getStringObject(element));
    }
    catch(NullPointerException e)
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
    return Float.parseFloat(getStringObject(element));
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
      return Float.parseFloat(getStringObject(element));
    }
    catch(NullPointerException e)
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
      return Float.valueOf(getStringObject(element));
    }
    catch(NullPointerException e)
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
    return Double.parseDouble(getStringObject(element));
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
      return Double.parseDouble(getStringObject(element));
    }
    catch(NullPointerException e)
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
      return Double.valueOf(getStringObject(element));
    }
    catch(NullPointerException e)
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
      throw new NullPointerException();
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
};
