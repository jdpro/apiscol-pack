package fr.ac_versailles.crdp.apiscol.pack.exceptionMappers;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;

import fr.ac_versailles.crdp.apiscol.pack.IOErrorException;

public class IOErrorExceptionMapper implements
ExceptionMapper<IOErrorException>{
	@Override
	public Response toResponse(IOErrorException e) {
		return Response.status(Status.INTERNAL_SERVER_ERROR)
				.type(MediaType.APPLICATION_XML).entity(e.getXMLMessage())
				.build();
	}
}
