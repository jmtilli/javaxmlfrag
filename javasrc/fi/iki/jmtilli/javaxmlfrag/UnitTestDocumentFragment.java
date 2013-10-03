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
      txt.setAttr("attr", "val");
    }
    catch (IllegalStateException ex)
    {
      thrown = true;
    }
    assertTrue(thrown);

    thrown = false;
    try {
      txt.addChild(new DocumentFragment("tag"));
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
      frag1.addChild(child);
    }
    assertMapEqual(frag1.getAttributes(), attributes);
    assertListHasSameObjects(frag1.getChildren(), children);
    attributes.put("attr1", "foo");
    attributes.put("attr2", "3");
    attributes.put("attr3", "5.0");
    for (Map.Entry<String, String> e: attributes.entrySet())
    {
      frag1.setAttr(e.getKey(), e.getValue());
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
    catch (NullPointerException ex)
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
    catch (NullPointerException ex)
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
    catch (NullPointerException ex)
    {
      thrown = true;
    }
    assertTrue(thrown);
    assertEqual(frag1.getStringNotNull("frag2"), "3");
    assertEqual(frag1.getStringNotNull("frag3"), "5.0");
    assertEqual(frag1.getStringNotNull("frag4"), "bar");

    children.add(frag4_2);
    frag1.addChild(frag4_2);
    assertListHasSameObjects(frag1.getChildren(), children);
    assertEqual(frag1.getString("frag1", "foo"), "foo");
    thrown = false;
    try {
      frag1.getStringObject("frag4");
    }
    catch (XMLException ex)
    {
      thrown = true;
    }
    thrown = false;
    try {
      frag1.get("frag4");
    }
    catch (XMLException ex)
    {
      thrown = true;
    }
    assertEqual(frag1.getMulti("frag4").size(), 2);
    assertEqual(frag1.getMulti("frag1").size(), 0);
    assertEqual(frag1.get("frag1"), null);
  }

  /**
     Run the unit test
   */
  public static void main(String[] args)
  {
    testTextFragment();
    testTagFragment();
  }
};
