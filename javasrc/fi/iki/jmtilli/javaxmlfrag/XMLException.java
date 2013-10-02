package fi.iki.jmtilli.javaxmlfrag;
/**
   An XML exception.
  
   Thrown when the document structure contains something that cannot be
   parsed.
 */
public class XMLException extends RuntimeException {
  public XMLException()
  {
    super();
  }
  public XMLException(String s)
  {
    super(s);
  }
}
