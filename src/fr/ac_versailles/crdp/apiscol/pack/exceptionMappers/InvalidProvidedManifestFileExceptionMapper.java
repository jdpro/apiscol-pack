package fr.ac_versailles.crdp.apiscol.pack.exceptionMappers;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;

import fr.ac_versailles.crdp.apiscol.pack.InvalidProvidedManifestFileException;

public class InvalidProvidedManifestFileExceptionMapper implements
ExceptionMapper<InvalidProvidedManifestFileException>{
	@Override
	public Response toResponse(InvalidProvidedManifestFileException e) {
		return Response.status(Status.BAD_REQUEST)
				.type(MediaType.APPLICATION_XML).entity(e.getXMLMessage())
				.build();
	}
}
