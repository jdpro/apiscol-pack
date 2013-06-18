package fr.ac_versailles.crdp.apiscol.packageRepresentation;

import java.io.File;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import fr.ac_versailles.crdp.apiscol.UsedNamespaces;
import fr.ac_versailles.crdp.apiscol.pack.DAO;
import fr.ac_versailles.crdp.apiscol.pack.MongoDAO;
import fr.ac_versailles.crdp.apiscol.pack.PackApi;
import fr.ac_versailles.crdp.apiscol.pack.PackageFile;
import fr.ac_versailles.crdp.apiscol.utils.LogUtility;
import fr.ac_versailles.crdp.apiscol.utils.TimeUtils;
import fr.ac_versailles.crdp.apiscol.utils.XMLUtils;

public class PackageRepresentationBuilder {

	public static final String APISCOL_NAMESPACE_URI = "http://www.crdp.ac-versailles.fr/2012/apiscol";
	public static final String ATOM_NAMESPACE_URI = "http://www.w3.org/2005/Atom";
	private static Logger logger;
	private PackageFile packageFile;
	private MongoDAO dao;

	public PackageRepresentationBuilder() {
		if (logger == null)
			logger = LogUtility.createLogger(PackageFile.class
					.getCanonicalName());
		logger.info("creation du logger depuis la classe pakageRepresentationBuilder.");
	}

	public PackageRepresentationBuilder(PackageFile packageFile, MongoDAO dao) {
		if (logger == null)
			logger = LogUtility.createLogger(PackageFile.class
					.getCanonicalName());
		logger.info("creation du logger depuis la classe pakageRepresentationBuilder.");
		this.packageFile = packageFile;
		this.dao = dao;
	}

	public PackageRepresentationBuilder(MongoDAO dao) {
		if (logger == null)
			logger = LogUtility.createLogger(PackageFile.class
					.getCanonicalName());
		logger.info("creation du logger depuis la classe pakageRepresentationBuilder.");
		this.dao = dao;
	}

	public Object getFileTransferedRespresentation(String org,
			String[] metaData, String[] res, PackageFile packageFile,
			UriInfo uriInfo) {

		/* avec jwc.dom */
		String packId = packageFile.getFileValidatedName();
		String packIdh=packageFile.getFileNameHierarchy();
		logger.info("On va representer le package de packId="+packId);
		// Je suis en train de faire une representation...;
		Document pack = XMLUtils.createXMLDocument();
		Element rootElement = pack.createElement("entry");// packageReprsentation-->entry

		Element updated = pack.createElement("updated");
		updated.setTextContent(fileInfo(packageFile.getFileValidated()));
		rootElement.appendChild(updated);

		Element id = pack.createElement("id");

		id.setTextContent(String.format("urn:apiscol:%s:pack:%s",
				PackApi.getApiscolInstanceName(), packId));

		rootElement.appendChild(id);

		Element author = pack.createElement("author");
		rootElement.appendChild(author);
		Element name = pack.createElement("name");
		author.appendChild(name);

		Element title = pack.createElement("title");
		title.setTextContent(org);
		rootElement.appendChild(title);

		Element content = pack.createElement("content"); // organization-->content
		rootElement.appendChild(content);
		content.setAttribute("type", "application/xml");

		Element resources = pack.createElement("apiscol:resources");
		content.appendChild(resources);

		for (int i = 0; i < res.length; i++) {
			Element resource = pack.createElement("apiscol:resource");
			resources.appendChild(resource);
			resource.setAttribute("href", res[i]);
		}
		
		Element link = pack.createElement("link");
		rootElement.appendChild(link);
		link.setAttribute("href", uriInfo.getAbsolutePath().toString()
				+ "manifest/" + packId);
		link.setAttribute("rel", "self");
		link.setAttribute("type", "text/html");

		Element link2 = pack.createElement("link");
		rootElement.appendChild(link2);
		link2.setAttribute("href", uriInfo.getAbsolutePath().toString()
				+ "manifest/" + packId + "?format=xml");
		link2.setAttribute("rel", "self");
		link2.setAttribute("type", "application/atom+xml");

		Element link3 = pack.createElement("link");
		rootElement.appendChild(link3);
		link3.setAttribute("href", uriInfo.getAbsolutePath().toString()
				+ "manifests" + packIdh+".xml");
		link3.setAttribute("rel", "alternate");
		link3.setAttribute("type", "application/atom+xml");

		Element link4 = pack.createElement("link");
		rootElement.appendChild(link4);
		link4.setAttribute("href", uriInfo.getAbsolutePath().toString()
				+ "manifest/" + packId + ".js");
		link4.setAttribute("rel", "alternate");
		link4.setAttribute("type", "application/javascript");

		for (int i = 0; i < metaData.length; i++) {
			Element linkmeta = pack.createElement("link");
			rootElement.appendChild(linkmeta);
			linkmeta.setAttribute("href", metaData[i]);
			linkmeta.setAttribute("rel", "describedby");
			linkmeta.setAttribute("type", "application/atom+xml");

			Element linkmetaURLRest = pack.createElement("link");
			rootElement.appendChild(linkmetaURLRest);
			linkmetaURLRest.setAttribute("href",
					PackageFile.extractMetadataRestURL(metaData[i]));
			linkmetaURLRest.setAttribute("rel", "describedby");
			linkmetaURLRest.setAttribute("type", "application/lom+xml");
		}

		/*
		 * plus utile pour le moment Element pId = pack.createElement("packId");
		 * pId.setTextContent(packId); rootElement.appendChild(pId);
		 */

		pack.appendChild(rootElement);
		XMLUtils.addNameSpaces(pack, UsedNamespaces.ATOM);

		return pack;
	}

	public MediaType getMediaType() {
		return MediaType.APPLICATION_XML_TYPE;
	}

	public Object packageRepresentation(String[] metaData, String res,
			String[] packId) {
		Document pack = XMLUtils.createXMLDocument();
		Element rootElement = pack.createElement("packageRepresentations");
		Element link = pack.createElement("link");
		link.setAttribute("rel", "self");
		link.setAttribute("href", res);
		rootElement.appendChild(link);
		for (int i = 0; i < packId.length; i++) {
			Element manifest = pack.createElement("manifest");
			rootElement.appendChild(manifest);

			Element pId = pack.createElement("packId");
			pId.setTextContent(packId[i]);
			manifest.appendChild(pId);

			Element resource = pack.createElement("resource");
			manifest.appendChild(resource);
			resource.setAttribute("href", res);
		}

		pack.appendChild(rootElement);
		XMLUtils.addNameSpaces(pack, UsedNamespaces.ATOM);

		return pack;

	}

	public Object packageRepresentation(String[] packId, int start, int rows,
			boolean desc, UriInfo uriInfo) { // desc= true pour tout afficher
		Document pack = XMLUtils.createXMLDocument();
		Element rootElement = pack.createElement("feed");

		Element cpt = pack.createElement("totalResults");
		cpt.setTextContent("" + packId.length);
		rootElement.appendChild(cpt);

		for (int i = start; i < Math.min(packId.length, start+rows); i++) {
			Element entryElement = pack.createElement("entry");
			String packIdsub =packId[i].substring(0, packId[i].length() - 4);
			String packIdh=PackageFile.retrieveFileHierarchy(packIdsub);
			
			Element updated = pack.createElement("updated");
			updated.setTextContent(dao.findUpdatedForPackage(packId[i]));
			entryElement.appendChild(updated);

			Element pId = pack.createElement("id");// packId
			// pId.setTextContent(packId[i]);
			pId.setTextContent(String.format("urn:apiscol:%s:pack:%s",
					PackApi.getApiscolInstanceName(), packIdsub));
			entryElement.appendChild(pId);
			Element auteur = pack.createElement("author");
			Element nom = pack.createElement("name");
			auteur.appendChild(nom);
			entryElement.appendChild(auteur);
			Element titre = pack.createElement("title");
			titre.setTextContent(dao.findTitleInPackage(packId[i]));
			entryElement.appendChild(titre);

			Element content = pack.createElement("content");
			entryElement.appendChild(content);
			content.setAttribute("type", "application/xml");

			if (desc){
				addResourcesInPackageRepresentation(pack, content, packId[i]);
			}
			// creation des link. Voir les /

			Element link1 = pack.createElement("link");
			entryElement.appendChild(link1);
			link1.setAttribute("href", uriInfo.getBaseUri().toString()
					+"manifest/"+packIdsub);
			link1.setAttribute("rel", "self");
			link1.setAttribute("type", "text/html");

			Element link2 = pack.createElement("link");
			entryElement.appendChild(link2);
			link2.setAttribute("href", uriInfo.getBaseUri().toString()
					+ "manifest/" + packIdsub + "?format=xml");
			link2.setAttribute("rel", "self");
			link2.setAttribute("type", "application/atom+xml");

			Element link3 = pack.createElement("link");
			entryElement.appendChild(link3);
			//link3.setAttribute("href", uriInfo.getBaseUri().toString()
			//      + "manifests/"+packId[i]);
			link3.setAttribute("href", uriInfo.getBaseUri().toString()
					+ "manifests"+packIdh+".xml");
			link3.setAttribute("rel", "alternate");
			link3.setAttribute("type", "application/xml");

			Element link4 = pack.createElement("link");
			entryElement.appendChild(link4);
			link4.setAttribute("href", uriInfo.getBaseUri().toString()
					+ "manifest/"+packIdsub + ".js");
			link4.setAttribute("rel", "alternate");
			link4.setAttribute("type", "application/javascript");
			String metaData = dao.findMetadataforPackage(packId[i]);

			Element linkmeta = pack.createElement("link");
			entryElement.appendChild(linkmeta);
			linkmeta.setAttribute("href", metaData);
			linkmeta.setAttribute("rel", "describedby");
			linkmeta.setAttribute("type", "application/atom+xml");

			Element linkmetaURLRest = pack.createElement("link");
			entryElement.appendChild(linkmetaURLRest);
			linkmetaURLRest.setAttribute("href",
					dao.findMetadataUrlRestForPackage(packId[i]));
			linkmetaURLRest.setAttribute("rel", "describedby");
			linkmetaURLRest.setAttribute("type", "application/lom+xml");
			rootElement.appendChild(entryElement);
		}

		pack.appendChild(rootElement);
		XMLUtils.addNameSpaces(pack, UsedNamespaces.ATOM);

		return pack;

	}

	// ajout des ressources si desc = true...
	public void addResourcesInPackageRepresentation(Document pack,
			Element content, String packId) {
		String[] res = dao.findResourcesInPackage(packId);
		Element resources = pack.createElement("apiscol:resources");
		content.appendChild(resources);
		for (int i = 0; i < res.length; i++) {
			Element resource = pack.createElement("apiscol:resource");
			if (res[i] != null) {
				resource.setAttribute("href", res[i]);
			} else
				resource.setAttribute("href", "none");
			resources.appendChild(resource);
		}
	}

	public String fileInfo(File file) {

		long d = file.lastModified();
		String info = TimeUtils.toRFC3339(d);
		return info;

	}

	public Object packageRepresentation(String packId, boolean desc,
			UriInfo uriInfo) { // desc= true pour tout afficher
		//String p=PackageFile.retrieveFileHierarchy(packId);
		//logger.info("On va representer juste un manifest ("+p+") avec desc=" + desc);
		Document pack = XMLUtils.createXMLDocument();
		Element rootElement = pack.createElement("entry");
		
		String packIdsub = packId; // on conserve packId sans extension .xml
		String packIdh=PackageFile.retrieveFileHierarchy(packId);
		packId = packId + ".xml";
		//ces deux variables ne sont pas vraiment utilies si on utilise UriInfo
		Element updated = pack.createElement("updated");
		updated.setTextContent(dao.findUpdatedForPackage(packId));
		rootElement.appendChild(updated);

		Element pId = pack.createElement("id");// packId
		// pId.setTextContent(packId[i]);
		pId.setTextContent(String.format("urn:apiscol:%s:pack:%s",
				PackApi.getApiscolInstanceName(), packIdsub));
		rootElement.appendChild(pId);
		Element auteur = pack.createElement("author");
		Element nom = pack.createElement("name");
		auteur.appendChild(nom);
		rootElement.appendChild(auteur);
		Element titre = pack.createElement("title");
		titre.setTextContent(dao.findTitleInPackage(packId));
		rootElement.appendChild(titre);

		Element content = pack.createElement("content");
		rootElement.appendChild(content);
		content.setAttribute("type", "application/xml");

		if (desc){
			addResourcesInPackageRepresentation(pack, content, packId);
		}
		// creation des link

		Element link1 = pack.createElement("link");
		rootElement.appendChild(link1);
		link1.setAttribute("href", uriInfo.getAbsolutePath().toString());
		link1.setAttribute("rel", "self");
		link1.setAttribute("type", "text/html");

		Element link2 = pack.createElement("link");
		rootElement.appendChild(link2);
		link2.setAttribute("href", uriInfo.getAbsolutePath().toString() + "?format=xml");
		link2.setAttribute("rel", "self");
		link2.setAttribute("type", "application/atom+xml");

		Element link3 = pack.createElement("link");
		rootElement.appendChild(link3);
		link3.setAttribute("href", uriInfo.getBaseUri().toString()
				+ "manifests"+packIdh);
		link3.setAttribute("rel", "alternate");
		link3.setAttribute("type", "application/xml");

		Element link4 = pack.createElement("link");
		rootElement.appendChild(link4);
		link4.setAttribute("href", uriInfo.getAbsolutePath().toString()+ ".js");
		link4.setAttribute("rel", "alternate");
		link4.setAttribute("type", "application/javascript");
		String metaData = dao.findMetadataforPackage(packId);

		Element linkmeta = pack.createElement("link");
		rootElement.appendChild(linkmeta);
		linkmeta.setAttribute("href", metaData);
		linkmeta.setAttribute("rel", "describedby");
		linkmeta.setAttribute("type", "application/atom+xml");

		Element linkmetaURLRest = pack.createElement("link");
		rootElement.appendChild(linkmetaURLRest);
		linkmetaURLRest.setAttribute("href",
				dao.findMetadataUrlRestForPackage(packId));
		linkmetaURLRest.setAttribute("rel", "describedby");
		linkmetaURLRest.setAttribute("type", "application/lom+xml");

		pack.appendChild(rootElement);
		XMLUtils.addNameSpaces(pack, UsedNamespaces.ATOM);

		return pack;

	}
}
