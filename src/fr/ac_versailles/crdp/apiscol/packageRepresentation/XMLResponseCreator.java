package fr.ac_versailles.crdp.apiscol.packageRepresentation;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import fr.ac_versailles.crdp.apiscol.utils.XMLUtils;

public class XMLResponseCreator {
	public XMLResponseCreator(){
		
	}
	public Object generateXMLResponse(String msg){
		Document response = XMLUtils.createXMLDocument();
		Element rootElement = response.createElement("entry");// packageReprsentation-->entry
		Element message = response.createElement("message");
		message.setTextContent(msg);
		rootElement.appendChild(message);
		response.appendChild(rootElement);
		return response;
		
		
	}
}
