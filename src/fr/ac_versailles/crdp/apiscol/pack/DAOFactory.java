package fr.ac_versailles.crdp.apiscol.pack;

import fr.ac_versailles.crdp.apiscol.database.DBAccessException;

public class DAOFactory {
	public static DAO getDAO(String DBname) throws DBAccessException{
		if (DBname.equals("MongoDB")){
			DAO dao=new MongoDAO();
			return dao;
		}
		return null; 
	}
}
