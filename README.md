# JavaXMLFrag: a powerful combined tree-based and event-based parser for XML

Typically, XML is either parsed by a tree-based parser or by an event-based parser. Event-based parsers are fast and have a low memory footprint, but a drawback is that it is cumbersome to write the required event handlers. Tree-based parsers make the code easier to write, to understand and to maintain but have a large memory footprint as a drawback. Often, XML is used for huge files such as database dumps that necessitate event-based parsing, or so it would appear at a glance, because a tree-based parser cannot hold the whole parse tree in memory at the same time.

## Example application: customers in a major bank

Let us consider an example application: a listing of a customers in a major bank that has 30 million customers. The test file is in the following format:

```
<allCustomers>
  <customer id="1">
    <name>Clark Henson</name>
    <accountCount>1</accountCount>
    <totalBalance>5085.96</totalBalance>
  </customer>
  <customer id="2">
    <name>Elnora Ericson</name>
    <accountCount>3</accountCount>
    <totalBalance>3910.11</totalBalance>
  </customer>
  ...
</allCustomers>
```
  
The example format requires about 130 bytes per customer plus customer name length. If we assume an average customer name is 15 characters long, the required storage is about 145 bytes per customer. For 30 million customers, this is 4 gigabytes. In the example, the file is read to the following structure:

```
public class Customer {
  public int customerId;
  public String name;
  public int accountCount;
  public double totalBalance;
};
```
  
## Parser with SAX

A SAX-based parser is implemented here:

```
import java.util.zip.GZIPInputStream;
import java.io.*;
import java.util.*;
import javax.xml.parsers.*;
public class ParseSAX {
  public static void main(String[] args) throws Throwable
  {
    final HashMap<Integer, Customer> customers =
      new HashMap<Integer, Customer>();
    SAXParser p = SAXParserFactory.newInstance().newSAXParser();
    String f = "customers.xml.gz";
    InputStream is = new GZIPInputStream(new FileInputStream(f));
    p.parse(is, new org.xml.sax.helpers.DefaultHandler() {
      StringBuilder txt = new StringBuilder();
      Customer c;
      public void startElement(String uri, String localName, String qName,
                               org.xml.sax.Attributes attributes) {
        txt.setLength(0);
        if (qName.equals("customer")) {
          c = new Customer();
          c.customerId = Integer.parseInt(attributes.getValue("id"));
          customers.put(c.customerId, c);
        }
      }
      public void characters(char[] ch, int start, int length) {
        txt.append(ch, start, length);
      }
      public void endElement(String uri, String localName, String qName) {
        if (qName.equals("name"))
          c.name = txt.toString();
        if (qName.equals("accountCount"))
          c.accountCount = Integer.parseInt(txt.toString());
        if (qName.equals("totalBalance"))
          c.totalBalance = Double.parseDouble(txt.toString());
      }
    });
  }
};
```
  
It can be seen that the parser is quite cumbersome and the code to construct a customer is scattered to two different places. Yet it is fast and has a low memory footprint.

## Parser with DOM

Here is a parser implemented with DOM:

```
import java.util.zip.GZIPInputStream;
import java.io.*;
import java.util.*;
import org.w3c.dom.*;
import javax.xml.parsers.*;
public class ParseDOM {
  public static void main(String[] args) throws Throwable
  {
    HashMap<Integer, Customer> customers = new HashMap<Integer, Customer>();
    DocumentBuilderFactory bf = DocumentBuilderFactory.newInstance();
    DocumentBuilder b = bf.newDocumentBuilder();
    String f = "customers.xml.gz";
    InputStream is = new GZIPInputStream(new FileInputStream(f));
    Element allCustomers = b.parse(is).getDocumentElement();
    if (!allCustomers.getTagName().equals("allCustomers"))
      throw new RuntimeException("toplevel tag not allCustomers");
    NodeList customerList = allCustomers.getElementsByTagName("customer");
    for (int i = 0; i < customerList.getLength(); i++)
    {
      Element customer = (Element)customerList.item(i);
      Customer c = new Customer();
      c.customerId = Integer.parseInt(customer.getAttribute("id"));
      NodeList ns = customer.getElementsByTagName("name");
      NodeList acs = customer.getElementsByTagName("accountCount");
      NodeList tbs = customer.getElementsByTagName("totalBalance");
      c.name = ns.item(0).getTextContent();
      c.accountCount = Integer.parseInt(acs.item(0).getTextContent());
      c.totalBalance = Double.parseDouble(tbs.item(0).getTextContent());
      customers.put(c.customerId, c);
    }
  }
};
```
  
The DOM-based parser is more satisfactory: it has the code to construct a customer object in only one place. Yet it is still a bit more complex than we would like to have. Additionally, the memory consumption of the DOM parser is too high to read the whole 4 gigabyte test file on most computers.

## Parser with the new library

What if we could combine the benefits of the SAX-based approach with the benefits of the DOM-based approach? A parse tree fragment for a single <customer> element is small enough to be kept in memory. This is what the new library is about. Here is the code to parse the customer file with the new library:

```
import java.util.zip.GZIPInputStream;
import java.io.*;
import java.util.*;
import javax.xml.parsers.*;
import fi.iki.jmtilli.javaxmlfrag.*;
public class ParseCombo {
  public static void main(String[] args) throws Throwable
  {
    final HashMap<Integer, Customer> customers =
      new HashMap<Integer, Customer>();
    SAXParser p = SAXParserFactory.newInstance().newSAXParser();
    String f = "customers.xml.gz";
    InputStream is = new GZIPInputStream(new FileInputStream(f));
    p.parse(is, new DocumentFragmentHandler() {
      public void startXMLElement(String uri, String localName, String qName,
                                  org.xml.sax.Attributes attributes) {
        if (is("allCustomers", "customer"))
          super.startFragmentCollection();
      }
      public void endXMLElement(String uri, String localName, String qName,
                                DocumentFragment f) {
        if (is("allCustomers", "customer")) {
          Customer c = new Customer();
          c.customerId = f.getAttrIntNotNull("id");
          c.name = f.getStringNotNull("name");
          c.accountCount = f.getIntNotNull("accountCount");
          c.totalBalance = f.getDoubleNotNull("totalBalance");
          customers.put(c.customerId, c);
        }
      }
    });
  }
};
```
  
Note how the code is significantly more simple than for either the DOM-based approach or the SAX-based approach. Performance is close to the SAX-based approach, and memory consumption is essentially the same as for SAX.

Of course, the new library supports getting the whole parse tree in memory:

```
import java.util.zip.GZIPInputStream;
import java.io.*;
import java.util.*;
import fi.iki.jmtilli.javaxmlfrag.*;
public class ParseWhole {
  public static void main(String[] args) throws Throwable {
    final HashMap<Integer, Customer> customers =
      new HashMap<Integer, Customer>();
    String f = "customers.xml.gz";
    InputStream is = new GZIPInputStream(new FileInputStream(f));
    DocumentFragment frag = DocumentFragmentHandler.parseWhole(is);
    for (DocumentFragment cf: frag.getMulti("customer")) {
      Customer c = new Customer();
      c.customerId = cf.getAttrIntNotNull("id");
      c.name = cf.getStringNotNull("name");
      c.accountCount = cf.getIntNotNull("accountCount");
      c.totalBalance = cf.getDoubleNotNull("totalBalance");
      customers.put(c.customerId, c);
    }
  }
};
```
  
## License

All of the material related to JavaXMLFrag is licensed under the following MIT license:

Copyright (C) 2013 Juha-Matti Tilli

Permission is hereby granted, free of charge, to any person obtaining a copy of
this software and associated documentation files (the "Software"), to deal in
the Software without restriction, including without limitation the rights to
use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
of the Software, and to permit persons to whom the Software is furnished to do
so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
