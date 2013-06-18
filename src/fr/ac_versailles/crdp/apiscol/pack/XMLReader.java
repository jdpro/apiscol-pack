package fr.ac_versailles.crdp.apiscol.pack;

import java.io.File;
import java.util.Iterator;
import java.util.List;

import javax.ws.rs.core.UriInfo;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;

import fr.ac_versailles.crdp.apiscol.pack.PackApi.Mode;
import fr.ac_versailles.crdp.apiscol.packageRepresentation.PackageRepresentationBuilder;

public class XMLReader {
	private PackageFile packageFile;
	private DAO dao;
	private UriInfo uriInfo;
	private Document document;
	private Element racine;
	public XMLReader(PackageFile packageFile, DAO dao, UriInfo uriInfo){
		this.packageFile=packageFile;
		this.dao=dao;
		this.uriInfo=uriInfo;
	}
	public Object packageReaderAndDataBaseWriter() throws IOErrorException {
		Object response;
		SAXBuilder sxb = new SAXBuilder();
		try {
			document = sxb.build(packageFile.getFileValidated());
		} catch (Exception e) {
			e.printStackTrace();
		}
		racine = document.getRootElement();
		response = manifestReaderAndDataBaseWritter();
		return response;
	}
	
	public Object packageReader() throws IOErrorException {
		Object response;
		SAXBuilder sxb = new SAXBuilder();
		try {
			document = sxb.build(packageFile.getFileValidated());
		} catch (Exception e) {
			e.printStackTrace();
		}
		racine = document.getRootElement();
		response = manifestReader();
		return response;
	}
	
	public Object manifestReader() throws IOErrorException {
		String org = organizationsReader();
		String[] meta = metadataReader();
		String[] metaData = new String[meta.length];
		for (int i = 0; i < metaData.length; i++) {
			metaData[i] = PackageFile.extractMetadata(meta[i]);
		}
		String[] res = resourcesReader();
		String organizationTitle=organizationsReader();
		PackageRepresentationBuilder prb = new PackageRepresentationBuilder();
		Object response = prb.getFileTransferedRespresentation(org, metaData,
				res, packageFile,uriInfo);
		return response;
	}

	public Object manifestReaderAndDataBaseWritter() throws IOErrorException {
		String org = organizationsReader();
		String[] meta = metadataReader();
		String[] metaData = new String[meta.length];
		for (int i = 0; i < metaData.length; i++) {
			metaData[i] = PackageFile.extractMetadata(meta[i]);
		}
		String[] res = resourcesReader();
		String organizationTitle=organizationsReader();
		dao.writeData(org, metaData, res, packageFile
				.getFileValidated(),organizationTitle);
		PackageRepresentationBuilder prb = new PackageRepresentationBuilder();
		Object response = prb.getFileTransferedRespresentation(org, metaData,
				res, packageFile,uriInfo);
		return response;
	}

	public String organizationsReader() {
		List<Element> listOrganizations = racine.getChildren("organizations",
				racine.getNamespace());

		//System.out.println("taille organisation:" + listOrganizations.size());
		String organizationTitle = null;
		Iterator<Element> i = listOrganizations.iterator();
		String balise = "organization";
		if (PackApi.mode.equals(Mode.IMSLD)) {
			balise = "learning-design";
		}
		while (i.hasNext()) {
			// TODO ! j'ai ajouté expliciement imsld. A verif dans scorm au cas ou
			Element courant = (Element) i.next();
			organizationTitle = courant
					.getChild(balise, racine.getNamespace("imsld"))
					.getChild("title", racine.getNamespace("imsld")).getText();

		}
		return organizationTitle;
	}

	public String[] metadataReader() {

		List listMetadata = racine.getChildren("organizations",
				racine.getNamespace());

		//System.out.println("taille metadata:" + listMetadata.size());
		String[] metadata = new String[listMetadata.size()];

		Iterator ii = listMetadata.iterator();
		//System.out.println("valeur adlcp= " + racine.getNamespace("adlcp"));
		String balise = "organization";
		if (PackApi.mode.equals(Mode.IMSLD)) {
			balise = "learning-design";
		}
		int i = 0;
		while (ii.hasNext()) {

			Element courant = (Element) ii.next();
			// TODO ! j'ai ajouté expliciement imsld
			metadata[i] = courant
					.getChild(balise, racine.getNamespace("imsld"))
					.getChild("metadata", racine.getNamespace("imsld"))
					.getChild("location", racine.getNamespace("adlcp"))
					.getText();
			i++;
		}
		return metadata;
	}

	public String[] resourcesReader() {
		List<Element> listResources = racine.getChildren("resources",
				racine.getNamespace());
		Element e=listResources.get(0); //balise resources
		List<Element> l = e.getChildren("resource",
				racine.getNamespace());
		String[] resources = new String[l.size()];
		//System.out.println("taille ressource:" + l.size());
		Iterator iii = l.iterator();
		int i = 0;
		while (iii.hasNext()) {
			Element courant = (Element) iii.next();
			resources[i] = courant.getAttributeValue("identifier");
			//System.out.println("resource n°"+i+"="+resources[i]);
			i++;
		}
		return resources;
	}

}
