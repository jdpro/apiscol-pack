package fr.ac_versailles.crdp.apiscol.pack;

import fr.ac_versailles.crdp.apiscol.ApiscolException;

public class IOErrorException extends ApiscolException{
private static final long serialVersionUID = 1L;
	
	public IOErrorException(String packId) {
		super(String.format("No file was found for the package id %s.", packId));
	}
}
