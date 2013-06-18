/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.ac_versailles.crdp.apiscol.pack;

import org.xml.sax.*;
/**
 *
 * @author bernada
 */
public class SimpleErrorHandler implements ErrorHandler {
    
    //Parseur parseur;
    
    public SimpleErrorHandler() {
        //this.parseur=p;
    }
    
    @Override
    public void error(SAXParseException ex) {
        System.out.println("error line "+ex.getLineNumber()+": "+ex.getMessage());
        //parseur.validated=false;
    }
    @Override
    public void fatalError(SAXParseException ex) {
        System.out.println( "fatalError line "+ex.getLineNumber()+": "+ex.getMessage());
    }
    @Override
    public void warning(SAXParseException ex) {
        System.out.println("warning line "+ex.getLineNumber()+": "+ex.getMessage());
    }

}
