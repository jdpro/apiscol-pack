package fr.ac_versailles.crdp.apiscol.packageRepresentation;

import javax.ws.rs.core.MediaType;

import org.w3c.dom.Document;

public class ConverterFactory {
	public static IConverter<?> getConverter(Document result, String mediaType,
			String realpath) {
		if (mediaType.equals(MediaType.APPLICATION_XHTML_XML)
				|| mediaType.equals(MediaType.TEXT_HTML)) {
			return new XHTMLConverter(result, realpath);
		}
		return null;
	}
}
