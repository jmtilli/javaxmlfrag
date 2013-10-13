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
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Document;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import java.util.*;
/**
   Unit test for DocumentFragment.
 */
public class UnitTestDocumentFragment {
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
  private static <K,V> void assertMapEqual(Map<K, V> a, Map<K, V> b)
  {
    if (a.size() != b.size())
    {
      throw new RuntimeException("sizes differ");
    }
    for (Map.Entry<K, V> e: a.entrySet())
    {
      if (!b.containsKey(e.getKey()))
      {
        throw new RuntimeException("b doesn't have key " + e.getKey());
      }
      V v1 = e.getValue();
      V v2 = b.get(e.getKey());
      if (!equals(e.getValue(), b.get(e.getKey())))
      {
        throw new RuntimeException("values differ");
      }
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

  private static void testTextFragment()
  {
    DocumentFragment txt = DocumentFragment.newText("text");
    boolean thrown;

    assertEqual(txt.getTag(), null);
    assertEqual(txt.getText(), "text");
    assertTrue(txt.isTextElement());

    thrown = false;
    try {
      txt.getAttributes();
    }
    catch (IllegalStateException ex)
    {
      thrown = true;
    }
    assertTrue(thrown);

    thrown = false;
    try {
      txt.getChildren();
    }
    catch (IllegalStateException ex)
    {
      thrown = true;
    }
    assertTrue(thrown);

    thrown = false;
    try {
      txt.setAttrString("attr", "val");
    }
    catch (IllegalStateException ex)
    {
      thrown = true;
    }
    assertTrue(thrown);

    thrown = false;
    try {
      txt.add(new DocumentFragment("tag"));
    }
    catch (IllegalStateException ex)
    {
      thrown = true;
    }
    assertTrue(thrown);

    thrown = false;
    try {
      txt.addTextChild("text");
    }
    catch (IllegalStateException ex)
    {
      thrown = true;
    }
    assertTrue(thrown);

    thrown = false;
    try {
      txt.getStringObject("foo");
    }
    catch (IllegalStateException ex)
    {
      thrown = true;
    }
    assertTrue(thrown);

    thrown = false;
    try {
      txt.getFloat("foo", 1.0f);
    }
    catch (IllegalStateException ex)
    {
      thrown = true;
    }
    assertTrue(thrown);

    thrown = false;
    try {
      txt.getIntNotNull("foo");
    }
    catch (IllegalStateException ex)
    {
      thrown = true;
    }
    assertTrue(thrown);

    thrown = false;
    try {
      txt.getAttrShortObject("foo");
    }
    catch (IllegalStateException ex)
    {
      thrown = true;
    }
    assertTrue(thrown);

    thrown = false;
    try {
      txt.getAttrByte("foo", (byte)1);
    }
    catch (IllegalStateException ex)
    {
      thrown = true;
    }
    assertTrue(thrown);

    thrown = false;
    try {
      txt.getAttrLongNotNull("foo");
    }
    catch (IllegalStateException ex)
    {
      thrown = true;
    }
    assertTrue(thrown);

    assertEqual(txt.getTag(), null);
    assertEqual(txt.getText(), "text");
    assertTrue(txt.isTextElement());
  }

  private static void testTagFragment()
  {
    boolean thrown;
    DocumentFragment frag1 = new DocumentFragment("frag1");
    DocumentFragment frag2 = new DocumentFragment("frag2");
    DocumentFragment frag3 = new DocumentFragment("frag3");
    DocumentFragment frag4 = new DocumentFragment("frag4");
    DocumentFragment frag4_2 = new DocumentFragment("frag4");
    HashMap<String, String> attributes = new HashMap<String, String>();
    ArrayList<DocumentFragment> children = new ArrayList<DocumentFragment>();

    // These are coalesced to a single element per fragment.
    frag2.addTextChild("3");
    frag3.addTextChild("5");
    frag3.addTextChild(".");
    frag3.addTextChild("0");
    frag4.addTextChild("b");
    frag4.addTextChild("a");
    frag4.addTextChild("r");

    assertEqual(frag2.getTag(), "frag2");
    assertEqual(frag3.getTag(), "frag3");
    assertEqual(frag4.getTag(), "frag4");
    assertEqual(frag2.getText(), null);
    assertEqual(frag3.getText(), null);
    assertEqual(frag4.getText(), null);
    assertFalse(frag2.isTextElement());
    assertFalse(frag3.isTextElement());
    assertFalse(frag4.isTextElement());
    assertEqual(frag2.getChildren().size(), 1);
    assertEqual(frag3.getChildren().size(), 1);
    assertEqual(frag4.getChildren().size(), 1);
    assertEqual(frag2.getChildren().get(0).getTag(), null);
    assertEqual(frag3.getChildren().get(0).getTag(), null);
    assertEqual(frag4.getChildren().get(0).getTag(), null);
    assertEqual(frag2.getChildren().get(0).getText(), "3");
    assertEqual(frag3.getChildren().get(0).getText(), "5.0");
    assertEqual(frag4.getChildren().get(0).getText(), "bar");

    assertEqual(frag1.getTag(), "frag1");
    assertEqual(frag1.getText(), null);
    assertFalse(frag1.isTextElement());
    assertMapEqual(frag1.getAttributes(), attributes);
    assertListHasSameObjects(frag1.getChildren(), children);
    children.add(frag2);
    children.add(frag3);
    children.add(frag4);
    for (DocumentFragment child: children)
    {
      frag1.add(child);
    }
    assertMapEqual(frag1.getAttributes(), attributes);
    assertListHasSameObjects(frag1.getChildren(), children);
    attributes.put("attr1", "foo");
    attributes.put("attr2", "3");
    attributes.put("attr3", "5.0");
    for (Map.Entry<String, String> e: attributes.entrySet())
    {
      frag1.setAttrString(e.getKey(), e.getValue());
    }
    assertMapEqual(frag1.getAttributes(), attributes);
    assertListHasSameObjects(frag1.getChildren(), children);

    assertEqual(frag1.getAttrString("attr1", "bar"), "foo");
    assertEqual(frag1.getAttrString("attr2", "bar"), "3");
    assertEqual(frag1.getAttrString("attr3", "bar"), "5.0");
    assertEqual(frag1.getAttrString("attr4", "bar"), "bar");
    assertEqual(frag1.getAttrStringObject("attr1"), "foo");
    assertEqual(frag1.getAttrStringObject("attr2"), "3");
    assertEqual(frag1.getAttrStringObject("attr3"), "5.0");
    assertEqual(frag1.getAttrStringObject("attr4"), null);
    assertEqual(frag1.getAttrStringNotNull("attr1"), "foo");
    assertEqual(frag1.getAttrStringNotNull("attr2"), "3");
    assertEqual(frag1.getAttrStringNotNull("attr3"), "5.0");
    thrown = false;
    try {
      frag1.getAttrStringNotNull("attr4");
    }
    catch (XMLValueMissingException ex)
    {
      thrown = true;
    }
    assertTrue(thrown);
    thrown = false;
    try {
      assertEqual(frag1.getAttrDouble("attr1", 7.0), "foo");
    }
    catch (NumberFormatException ex)
    {
      thrown = true;
    }
    assertTrue(thrown);
    assertEqual(frag1.getAttrDouble("attr2", 7.0), 3.0);
    assertEqual(frag1.getAttrDouble("attr3", 7.0), 5.0);
    assertEqual(frag1.getAttrDouble("attr4", 7.0), 7.0);
    thrown = false;
    try {
      frag1.getAttrDoubleObject("attr1");
    }
    catch (NumberFormatException ex)
    {
      thrown = true;
    }
    assertTrue(thrown);
    assertEqual(frag1.getAttrDoubleObject("attr2"), 3.0);
    assertEqual(frag1.getAttrDoubleObject("attr3"), 5.0);
    assertEqual(frag1.getAttrDoubleObject("attr4"), null);
    thrown = false;
    try {
      frag1.getAttrDoubleNotNull("attr1");
    }
    catch (NumberFormatException ex)
    {
      thrown = true;
    }
    assertTrue(thrown);
    assertEqual(frag1.getAttrDoubleNotNull("attr2"), 3.0);
    assertEqual(frag1.getAttrDoubleNotNull("attr3"), 5.0);
    thrown = false;
    try {
      frag1.getAttrDoubleNotNull("attr4");
    }
    catch (XMLValueMissingException ex)
    {
      thrown = true;
    }
    assertTrue(thrown);

    assertEqual(frag1.getString("frag1", "foo"), "foo");
    assertEqual(frag1.getString("frag2", "foo"), "3");
    assertEqual(frag1.getString("frag3", "foo"), "5.0");
    assertEqual(frag1.getString("frag4", "foo"), "bar");
    assertEqual(frag1.getStringObject("frag1"), null);
    assertEqual(frag1.getStringObject("frag2"), "3");
    assertEqual(frag1.getStringObject("frag3"), "5.0");
    assertEqual(frag1.getStringObject("frag4"), "bar");
    thrown = false;
    try {
      frag1.getStringNotNull("frag1");
    }
    catch (XMLValueMissingException ex)
    {
      thrown = true;
    }
    assertTrue(thrown);
    assertEqual(frag1.getStringNotNull("frag2"), "3");
    assertEqual(frag1.getStringNotNull("frag3"), "5.0");
    assertEqual(frag1.getStringNotNull("frag4"), "bar");

    children.add(frag4_2);
    frag1.add(frag4_2);
    assertListHasSameObjects(frag1.getChildren(), children);
    assertEqual(frag1.getString("frag1", "foo"), "foo");
    thrown = false;
    try {
      frag1.getStringObject("frag4");
    }
    catch (XMLMultipleElementsException ex)
    {
      thrown = true;
    }
    thrown = false;
    try {
      frag1.get("frag4");
    }
    catch (XMLMultipleElementsException ex)
    {
      thrown = true;
    }
    assertEqual(frag1.getMulti("frag4").size(), 2);
    assertEqual(frag1.getMulti("frag1").size(), 0);
    assertEqual(frag1.get("frag1"), null);
  }

  private static void testConvertToDomNode() throws Throwable
  {
    DocumentFragment frag = new DocumentFragment("frag");
    Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
    Node n, first, second, one, two;
    NodeList children_n;
    NodeList children_first;
    NodeList children_second;
    frag.setAttrString("attr", "val");
    frag.setInt("first", 1);
    frag.setDouble("second", 2.0);
    doc.appendChild(n = frag.convertToDomNode(doc));
    assertEqual(n.getNodeType(), Node.ELEMENT_NODE);
    assertEqual(n.getNodeName(), "frag");
    children_n = n.getChildNodes();
    assertEqual(children_n.getLength(), 2);
    first = children_n.item(0);
    assertEqual(first.getNodeType(), Node.ELEMENT_NODE);
    assertEqual(first.getNodeName(), "first");
    second = children_n.item(1);
    assertEqual(second.getNodeType(), Node.ELEMENT_NODE);
    assertEqual(second.getNodeName(), "second");
    children_first = first.getChildNodes();
    children_second = second.getChildNodes();
    assertEqual(children_first.getLength(), 1);
    assertEqual(children_second.getLength(), 1);
    one = children_first.item(0);
    two = children_second.item(0);
    assertEqual(one.getNodeType(), Node.TEXT_NODE);
    assertEqual(two.getNodeType(), Node.TEXT_NODE);
    assertEqual(one.getNodeValue(), "1");
    assertEqual(two.getNodeValue(), "2.0");
  }

  private static void testRemove()
  {
    DocumentFragment frag = new DocumentFragment("frag");
    boolean thrown;
    assertEqual(frag.getChildren().size(), 0);
    frag.add("first");
    assertEqual(frag.getChildren().size(), 1);
    frag.add("second");
    assertEqual(frag.getChildren().size(), 2);
    frag.add("second");
    assertEqual(frag.getChildren().size(), 3);
    frag.add("third");
    assertEqual(frag.getChildren().size(), 4);
    frag.add("third");
    assertEqual(frag.getChildren().size(), 5);
    frag.add("third");
    assertEqual(frag.getChildren().size(), 6);
    assertEqual(frag.removeAll().size(), 6);
    assertEqual(frag.getChildren().size(), 0);
    frag.add("first");
    assertEqual(frag.getChildren().size(), 1);
    frag.add("second");
    assertEqual(frag.getChildren().size(), 2);
    frag.add("second");
    assertEqual(frag.getChildren().size(), 3);
    frag.add("third");
    assertEqual(frag.getChildren().size(), 4);
    frag.add("third");
    assertEqual(frag.getChildren().size(), 5);
    frag.add("third");
    assertEqual(frag.getChildren().size(), 6);
    assertTrue(frag.remove("first") != null);
    assertEqual(frag.getChildren().size(), 5);
    thrown = false;
    try {
      frag.remove("second");
    }
    catch (XMLMultipleElementsException e)
    {
      thrown = true;
    }
    assertTrue(thrown);
    assertEqual(frag.getChildren().size(), 5);
    thrown = false;
    try {
      frag.remove("third");
    }
    catch (XMLMultipleElementsException e)
    {
      thrown = true;
    }
    assertTrue(thrown);
    assertEqual(frag.getChildren().size(), 5);
    frag.add("first");
    assertEqual(frag.getChildren().size(), 6);
    assertEqual(frag.removeMulti("first").size(), 1);
    assertEqual(frag.getChildren().size(), 5);
    assertEqual(frag.removeMulti("second").size(), 2);
    assertEqual(frag.getChildren().size(), 3);
    assertEqual(frag.removeMulti("third").size(), 3);
    assertEqual(frag.getChildren().size(), 0);

  }

  private static void testGetNonTextChildren()
  {
    DocumentFragment frag1 = new DocumentFragment("frag1");
    assertEqual(frag1.getNonTextChildren().size(), 0);
    frag1.addTextChild("a");
    assertEqual(frag1.getNonTextChildren().size(), 0);
    frag1.add("b");
    assertEqual(frag1.getNonTextChildren().size(), 1);
    frag1.add("c");
    assertEqual(frag1.getNonTextChildren().size(), 2);
    frag1.addTextChild("d");
    assertEqual(frag1.getNonTextChildren().size(), 2);
  }

  private static void testRemoveAttr()
  {
    DocumentFragment frag = new DocumentFragment("frag");
    boolean thrown;
    thrown = false;
    try {
      frag.removeAttr(null);
    }
    catch(NullPointerException ex)
    {
      thrown = true;
    }
    frag.removeAttr("foo");
    frag.setAttrString("unrelated", "unrelated");
    frag.setAttrString("foo", "bar");
    assertEqual(frag.getAttrStringObject("unrelated"), "unrelated");
    assertEqual(frag.getAttrStringObject("foo"), "bar");
    frag.removeAttr("foo");
    assertEqual(frag.getAttrStringObject("unrelated"), "unrelated");
    assertEqual(frag.getAttrStringObject("foo"), null);
  }

  private static void testSetAttrTypeConv()
  {
    DocumentFragment frag = new DocumentFragment("frag");
    frag.setAttrString("first", "foo");
    frag.setAttrDouble("second", 1.7);
    frag.setAttrInt("third", -5);
    assertEqual(frag.getAttrStringObject("first"), "foo");
    assertEqual(frag.getAttrStringObject("second"), "1.7");
    assertEqual(frag.getAttrStringObject("third"), "-5");
    frag.setAttrStringObject("first", null);
    frag.setAttrDoubleObject("second", null);
    frag.setAttrIntObject("third", null);
    assertEqual(frag.getAttrStringObject("first"), null);
    assertEqual(frag.getAttrStringObject("second"), null);
    assertEqual(frag.getAttrStringObject("third"), null);
  }

  private static void testThisVsNonThisGetNullness()
  {
    DocumentFragment frag = new DocumentFragment("frag");
    boolean thrown;
    frag.setString("first", ""); // adds element with empty content
    frag.setStringObject("second", null); // removes nonexistent element
    assertEqual(frag.get("second"), null);
    assertEqual(frag.getIntObject("second"), null);
    thrown = false;
    try {
      frag.getIntObject("first");
    }
    catch(NumberFormatException ex)
    {
      thrown = true;
    }
    assertTrue(thrown);
    assertEqual(frag.get("first").getThisIntObject(), null);
  }

  private static void testSetAttrNull()
  {
    DocumentFragment frag = new DocumentFragment("frag");
    boolean thrown;
    thrown = false;
    try {
      frag.setAttrString(null, "foo");
    }
    catch(NullPointerException e)
    {
      thrown = true;
    }
    assertTrue(thrown);
    thrown = false;
    try {
      frag.setAttrString("foo", null);
    }
    catch(NullPointerException e)
    {
      thrown = true;
    }
    assertTrue(thrown);
    frag.setAttrStringObject("foo", null);
    assertEqual(frag.getAttributes().size(), 0);
  }

  private static void testAddSet()
  {
    DocumentFragment frag1 = new DocumentFragment("frag1");
    DocumentFragment a;
    boolean thrown;

    assertEqual(frag1.getNonTextChildren().size(), 0);
    a = frag1.add("a");
    assertEqual(frag1.getNonTextChildren().size(), 1);
    assertTrue(frag1.set("a") == a);
    assertEqual(frag1.getNonTextChildren().size(), 1);
    assertTrue(frag1.set("b") != a);
    assertEqual(frag1.getNonTextChildren().size(), 2);
    assertTrue(frag1.add("a") != a);
    assertEqual(frag1.getNonTextChildren().size(), 3);
    thrown = false;
    try {
      // throws because there are multiple "a" elements
      frag1.set("a");
    }
    catch(XMLMultipleElementsException e)
    {
      thrown = true;
    }
    assertTrue(thrown);
    // Test that "set" clears the element
    frag1.set("b").setInt("a", 5);
    frag1.set("b").setInt("b", 6);
    frag1.set("b").setInt("c", 7);
    assertEqual(frag1.get("b").getIntObject("a"), null);
    assertEqual(frag1.get("b").getIntObject("b"), null);
    assertEqual(frag1.get("b").getIntObject("c"), 7);
    frag1.getOrCreate("b").setInt("a", 5);
    frag1.getOrCreate("b").setInt("b", 6);
    frag1.getOrCreate("b").setInt("c", 7);
    assertEqual(frag1.get("b").getIntObject("a"), 5);
    assertEqual(frag1.get("b").getIntObject("b"), 6);
    assertEqual(frag1.get("b").getIntObject("c"), 7);
  }

  private static class Complex implements XMLRowable {
    public double re;
    public double im;
    public Complex(double re, double im)
    {
      this.re = re;
      this.im = im;
    }
    public void toXMLRow(DocumentFragment frag)
    {
      frag.setDouble("re", this.re);
      frag.setDouble("im", this.im);
    }
  };

  public static void testRowable()
  {
    DocumentFragment frag = new DocumentFragment("frag");
    boolean thrown = false;

    frag.setRow("complex", new Complex(2.0, -3.0));
    assertEqual(frag.getChildren().size(), 1);
    assertEqual(frag.get("complex").getChildren().size(), 2);
    assertEqual(frag.get("complex").getDoubleNotNull("re"), 2.0);
    assertEqual(frag.get("complex").getDoubleNotNull("im"), -3.0);

    thrown = false;
    try {
      frag.setRow("complex", null);
    }
    catch (NullPointerException e)
    {
      thrown = true;
    }
    assertTrue(thrown);
    assertEqual(frag.getChildren().size(), 1);
    assertEqual(frag.get("complex").getChildren().size(), 2);
    assertEqual(frag.get("complex").getDoubleNotNull("re"), 2.0);
    assertEqual(frag.get("complex").getDoubleNotNull("im"), -3.0);

    frag.set("complex").setThisRow(new Complex(2.0, -3.0));
    assertEqual(frag.getChildren().size(), 1);
    assertEqual(frag.get("complex").getChildren().size(), 2);
    assertEqual(frag.get("complex").getDoubleNotNull("re"), 2.0);
    assertEqual(frag.get("complex").getDoubleNotNull("im"), -3.0);

    thrown = false;
    try {
      frag.set("complex").setThisRow(null);
    }
    catch (NullPointerException e)
    {
      thrown = true;
    }
    assertTrue(thrown);
    assertEqual(frag.getChildren().size(), 1);
    assertEqual(frag.get("complex").getChildren().size(), 0);

    frag.set("complex").setThisRow(new Complex(2.0, -3.0));
    assertEqual(frag.getChildren().size(), 1);
    assertEqual(frag.get("complex").getChildren().size(), 2);
    assertEqual(frag.get("complex").getDoubleNotNull("re"), 2.0);
    assertEqual(frag.get("complex").getDoubleNotNull("im"), -3.0);
    frag.setRowObject("complex", null);
    assertEqual(frag.getChildren().size(), 0);

    frag.set("complex").setThisRow(new Complex(2.0, -3.0));
    assertEqual(frag.getChildren().size(), 1);
    assertEqual(frag.get("complex").getChildren().size(), 2);
    assertEqual(frag.get("complex").getDoubleNotNull("re"), 2.0);
    assertEqual(frag.get("complex").getDoubleNotNull("im"), -3.0);
    frag.set("complex").setThisRowObject(null);
    assertEqual(frag.getChildren().size(), 1);
    assertEqual(frag.get("complex").getChildren().size(), 0);
  }

  /**
     Run the unit test
   */
  public static void main(String[] args) throws Throwable
  {
    testTextFragment();
    testTagFragment();
    testConvertToDomNode();
    testGetNonTextChildren();
    testAddSet();
    testRemoveAttr();
    testSetAttrTypeConv();
    testThisVsNonThisGetNullness();
    testSetAttrNull();
    testRemove();
  }
};
