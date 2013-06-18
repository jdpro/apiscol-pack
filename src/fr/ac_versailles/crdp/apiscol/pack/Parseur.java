/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ac_versailles.crdp.apiscol.pack;

import java.io.ByteArrayInputStream;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

/**
 *
 * @author bernada
 */
public class Parseur {

    static final String JAXP_SCHEMA_LANGUAGE = "http://java.sun.com/xml/jaxp/properties/schemaLanguage";
    static final String JAXP_SCHEMA_SOURCE = "http://java.sun.com/xml/jaxp/properties/schemaSource";
    static final String W3C_XML_SCHEMA = "http://www.w3.org/2001/XMLSchema";
    //static ResourceBundle rb = ResourceBundle.getBundle("props");
    
    String schemalocation;
    SAXParser saxParser;
    XMLReader xmlReader;
    boolean validated;
    
    public Parseur() {
        try {
            SAXParserFactory spf = SAXParserFactory.newInstance();
            spf.setNamespaceAware(true);
            spf.setValidating(true);
            saxParser = spf.newSAXParser();
            saxParser.setProperty(JAXP_SCHEMA_LANGUAGE, W3C_XML_SCHEMA);
            //schemalocation = rb.getString("schemalocation")+xsd;
            //saxParser.setProperty(JAXP_SCHEMA_SOURCE, new File(schemalocation));
            xmlReader = saxParser.getXMLReader();
            xmlReader.setErrorHandler(new SimpleErrorHandler());
        } catch (Exception e) {
            System.out.print("erreur creation parseur: "+e.getMessage());
            //e.printStackTrace();
        }
    }

    public boolean validate(String txt) {
        validated=true;
        try {
            xmlReader.parse(new InputSource(new ByteArrayInputStream(txt.getBytes())));
        } catch (Exception e) {
            System.out.print("erreur validation: "+e.getMessage());
            return false;
        }
        return validated;
    }
}
