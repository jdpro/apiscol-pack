package fr.ac_versailles.crdp.apiscol.pack.exceptionMappers;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import fr.ac_versailles.crdp.apiscol.pack.NoPackageFoundException;

@Provider
public class NoPackageFoundExceptionMapper implements
		ExceptionMapper<NoPackageFoundException> {
	@Override
	public Response toResponse(NoPackageFoundException e) {
		return Response.status(Status.NOT_FOUND)
				.type(MediaType.APPLICATION_XML).entity(e.getXMLMessage())
				.build();
	}
}
