package fr.ac_versailles.crdp.apiscol.pack;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;

import javax.ws.rs.core.UriInfo;

import org.apache.log4j.Logger;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;

import fr.ac_versailles.crdp.apiscol.database.DBAccessException;
import fr.ac_versailles.crdp.apiscol.database.MongoUtils;
import fr.ac_versailles.crdp.apiscol.packageRepresentation.PackageRepresentationBuilder;
import fr.ac_versailles.crdp.apiscol.utils.LogUtility;
import fr.ac_versailles.crdp.apiscol.utils.TimeUtils;

public class MongoDAO implements DAO {

	private static final String DB_NAME = "apiscol";
	private static final String COLLECTION_RESOURCE_NAME = "resourcesPack";
	private static final String COLLECTION_METADATA_NAME = "metaDataPack";

	private static DBCollection resourcesCollection;
	private static DBCollection metaDataCollection;
	private static Mongo mongo;
	protected static Logger logger;

	public MongoDAO() throws DBAccessException {
		createLogger();
		dbConnect();
	}

	protected void dbConnect() throws DBAccessException {
		if (mongo != null) {
			return;
		}
		mongo = MongoUtils.getMongoConnection();
		resourcesCollection = MongoUtils.getCollection(DB_NAME,
				COLLECTION_RESOURCE_NAME, mongo);
		metaDataCollection = MongoUtils.getCollection(DB_NAME,
				COLLECTION_METADATA_NAME, mongo);

	}

	public void writeData(String organization, String[] metadata,
		String[] resources,File file , String org) {
		//String packId=file.getName();
		String packId=PackageFile.retrievePackId(file);
		// doc.put("organisation",organization);//inutile, on l'a dans metadata
		for (int i = 0; i < metadata.length; i++) {
			BasicDBObject doc = new BasicDBObject();
			doc.put("metadata", metadata[i]);
			doc.put("metadataUrlRest", PackageFile.extractMetadataRestURL(metadata[i]));
			doc.put("packId", packId);
			doc.put("updated", fileInfo(file));
			doc.put("title", org);
			metaDataCollection.insert(doc);
		}

		for (int i = 0; i < resources.length; i++) {
			BasicDBObject doc2 = new BasicDBObject();
			doc2.put("resource", resources[i]);
			doc2.put("packId", packId);
			doc2.put("updated", fileInfo(file));
			doc2.put("title", org);
			resourcesCollection.insert(doc2);
		}
	}

	public static void mongoResourcesDisplay() {
		DBCursor cursor = resourcesCollection.find();
		try {
			while (cursor.hasNext()) {
				System.out.println("RESOURCES: " + cursor.next());
			}
		} finally {
			cursor.close();
		}
	}

	public static void mongoMetaDataDisplay() {
		DBCursor cursor = metaDataCollection.find();
		try {
			while (cursor.hasNext()) {
				System.out.println("METADATA: " + cursor.next());
			}
		} finally {
			cursor.close();
		}
	}

	private void createLogger() {
		if (logger == null)
			logger = LogUtility
					.createLogger(this.getClass().getCanonicalName());
	}

	public Object readInDbResources(int start, int rows,boolean desc,UriInfo uriInfo) {
		DBCursor cursor = resourcesCollection.find();
		HashSet<String> set=new HashSet<String>();
		try {
			while (cursor.hasNext()) {
				DBObject obj = cursor.next();
				set.add(obj.get("packId").toString());
				// on cherche d'abord les packid dans ressources et ensuite avec
				// on recupere les metadata dans metadataCollection	
			}
		} finally {
			cursor.close();
		}
		String[] packid=set.toArray(new String[0]);
		PackageRepresentationBuilder prb = new PackageRepresentationBuilder(null,this);
		Object response = prb.packageRepresentation(packid, start, rows,desc,uriInfo);
		return response;
	}

	public Object readInDbResources(String resIdQuery) {
		BasicDBObject resQuery = new BasicDBObject();
		DBCursor cursor;
		if (resIdQuery != null) {
			resQuery.put("resource", resIdQuery);
			cursor = resourcesCollection.find(resQuery);
		} else {
			cursor = resourcesCollection.find(resQuery);

		}

		String[] packid = new String[cursor.count()];
		int i = 0;
		try {
			while (cursor.hasNext()) {
				DBObject obj = cursor.next();
				packid[i] = obj.get("packId").toString(); // on cherche d'abord
															// les packid dans
															// ressources et
															// ensuite avec on
															// recupere les
															// metadata dans
															// metadataCollection
				i++;
			}
		} finally {
			cursor.close();
		} // on peut resortir les champs ici et appeler le representation
			// builder là. Donc, cette methode ne renvoi pas un string mais
			// directement une Reponse builder
		//System.out.println("il y a " + packid.length + " pack");
		BasicDBObject metaQuery = new BasicDBObject();
		metaQuery.put("packId", packid);
		DBCursor cursor2 = metaDataCollection.find(metaQuery);
		String[] metadata = new String[cursor2.count()];
		i = 0;
		try {
			while (cursor2.hasNext()) {
				DBObject obj = cursor2.next();
				metadata[i] = obj.get("metadata").toString(); // on cherche
																// d'abord les
																// packid dans
																// ressources et
																// ensuite avec
																// on recupere
																// les metadata
																// dans
																// metadataCollection
				i++;
			}
		} finally {
			cursor2.close();
		}
		PackageRepresentationBuilder prb = new PackageRepresentationBuilder();
		Object response = prb.packageRepresentation(metadata, resIdQuery,
				packid);
		return response;
	}

	@Override
	public void disconnect() {
		MongoUtils.dbDisconnect(mongo);

	}

	public String[] findResourcesInPackage(String packId) {
		BasicDBObject resQuery = new BasicDBObject();
		resQuery.put("packId", packId);
		DBCursor cursor;
		cursor = resourcesCollection.find(resQuery);
		ArrayList<String> tab=new ArrayList<String>(); 
		
		try {
			while (cursor.hasNext()) {
				DBObject obj = cursor.next();
				if (obj.get("resource") != null) {
						tab.add(obj.get("resource").toString());
				} else tab.add(" ");
			}
		} finally {
			cursor.close();
		}
		String[] resource=new String[tab.size()];
		tab.toArray(resource);
		return resource;
		
	}
	
	public String findTitleInPackage(String packId) {
		BasicDBObject resQuery = new BasicDBObject();
		resQuery.put("packId", packId);
		DBCursor cursor;
		cursor = resourcesCollection.find(resQuery);
		HashSet<String> tab=new HashSet<String>(); 
		
		try {
			while (cursor.hasNext()) {
				DBObject obj = cursor.next();
				if (obj.get("title") != null) {
						tab.add(obj.get("title").toString());
				} else tab.add("none");
			}
		} finally {
			cursor.close();
		}
		if(tab.isEmpty()) tab.add("no title");
		String[] title=new String[tab.size()];
		tab.toArray(title);
		//title est tjs de longueur 1
		return title[0];
		
	}
	public String findMetadataforPackage(String packId){
		BasicDBObject resQuery = new BasicDBObject();
		resQuery.put("packId", packId);
		DBCursor cursor;
		cursor = metaDataCollection.find(resQuery);
		HashSet<String> tab=new HashSet<String>(); 
		
		try {
			while (cursor.hasNext()) {
				DBObject obj = cursor.next();
				if (obj.get("metadata") != null) {
						tab.add(obj.get("metadata").toString());
				} else tab.add("none");
			}
		} finally {
			cursor.close();
		}
		if(tab.isEmpty()) tab.add("no metadata");
		String[] metadata=new String[tab.size()];
		tab.toArray(metadata);
		return metadata[0];
			
	}
	
	public String findMetadataUrlRestForPackage(String packId){
		BasicDBObject resQuery = new BasicDBObject();
		resQuery.put("packId", packId);
		DBCursor cursor;
		cursor = metaDataCollection.find(resQuery);
		HashSet<String> tab=new HashSet<String>(); 
		
		try {
			while (cursor.hasNext()) {
				DBObject obj = cursor.next();
				if (obj.get("metadataUrlRest") != null) {
						tab.add(obj.get("metadataUrlRest").toString());
				} else tab.add("none");
			}
		} finally {
			cursor.close();
		}
		if(tab.isEmpty()) tab.add("no metadata URL rest");
		String[] metadata=new String[tab.size()];
		tab.toArray(metadata);
		return metadata[0];
	}
	
	public String findUpdatedForPackage(String packId){
		BasicDBObject resQuery = new BasicDBObject();
		resQuery.put("packId", packId);
		DBCursor cursor;
		cursor = resourcesCollection.find(resQuery);
		HashSet<String> tab=new HashSet<String>(); 
		
		try {
			while (cursor.hasNext()) {
				DBObject obj = cursor.next();
				if (obj.get("updated") != null) {
						tab.add(obj.get("updated").toString());
				} else tab.add("none");
			}
		} finally {
			cursor.close();
		}
		if (tab.isEmpty()) tab.add("none");
		String[] updated=new String[tab.size()];
		tab.toArray(updated);
		return updated[0];
	}
	
	public String fileInfo(File file) {

		long d = file.lastModified();
		String info = TimeUtils.toRFC3339(d);
		return info;

	}
	
	public boolean packageExists(String packId){
		BasicDBObject packQuery = new BasicDBObject();
		packQuery.put("packId", packId+".xml");
		DBCursor cursor;
		cursor = resourcesCollection.find(packQuery);
		if (cursor.count()>=1){
			cursor.close();
			return true;
		}
		cursor.close();
		return false ;
	}
	public void removePackageInDataBase(String packId){
		BasicDBObject packQuery=new BasicDBObject();
		packQuery.put("packId",packId+".xml");
		DBCursor cursorResources=resourcesCollection.find(packQuery);;
		DBCursor cursorMetadata=metaDataCollection.find(packQuery);
		logger.info("avant suppresion ds resources:"+cursorResources.count());
		while(cursorResources.hasNext()){
			logger.info("suppression de:"+cursorResources.toString());
			resourcesCollection.remove(cursorResources.next());
		}
		logger.info("apres suppression ds resources:"+cursorResources.count());
		logger.info("avant suppresion ds metadata:"+cursorMetadata.count());
		while(cursorMetadata.hasNext()){
			logger.info("suppression de:"+cursorMetadata.toString());
			metaDataCollection.remove(cursorMetadata.next());
		}
		logger.info("apres suppression ds metadata:"+cursorMetadata.count());
		cursorResources.close();
		cursorMetadata.close();
	}
}
