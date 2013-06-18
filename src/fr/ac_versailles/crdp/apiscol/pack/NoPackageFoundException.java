package fr.ac_versailles.crdp.apiscol.pack;


import fr.ac_versailles.crdp.apiscol.ApiscolException;

public class NoPackageFoundException extends ApiscolException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public NoPackageFoundException(String packId) {
		super(String.format("No file was found for the package id %s.", packId));
	}

	

}

