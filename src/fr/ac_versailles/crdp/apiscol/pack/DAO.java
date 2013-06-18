package fr.ac_versailles.crdp.apiscol.pack;

import java.io.File;

import javax.ws.rs.core.UriInfo;

public interface DAO {

	public Object readInDbResources(String resIdQuery);
	public Object readInDbResources(int start, int rows, boolean desc, UriInfo uriInfo);
	public boolean packageExists(String packId);
	public void removePackageInDataBase(String packId);

	void writeData(String organization, String[] metadata, String[] resources,
			File packId, String org);
 
	public void disconnect();
}
