package fr.ac_versailles.crdp.apiscol.packageRepresentation;

import java.util.Collections;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import fr.ac_versailles.crdp.apiscol.utils.HTMLUtils;
import fr.ac_versailles.crdp.apiscol.utils.XMLUtils;

public class XHTMLConverter implements IConverter<String> {
	Document result;
	private String realPath;

	public XHTMLConverter(Document result, String realPath) {
		this.result = result;
		this.realPath = realPath;
	}

	@Override
	public String convert() {
		Node transform = XMLUtils.xsltTransform(
				new StringBuilder().append(realPath)
						.append("/xsl/manifestListXMLToHTMLTransformer.xsl")
						.toString(), result,
				Collections.<String, String> emptyMap());
		System.out.println(XMLUtils.XMLToString((Document) transform));
		return HTMLUtils.WrapInHTML5Headers((Document) transform);
	}
}
