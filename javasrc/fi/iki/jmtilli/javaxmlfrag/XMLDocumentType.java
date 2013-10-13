package fi.iki.jmtilli.javaxmlfrag;
public enum XMLDocumentType {
  WHOLE("no"),
  FRAGMENT("yes");

  private final String omit_xml_declaration;

  private XMLDocumentType(String omit_xml_declaration)
  {
    this.omit_xml_declaration = omit_xml_declaration;
  }
  public String getOmitXmlDeclaration()
  {
    return omit_xml_declaration;
  }
};
