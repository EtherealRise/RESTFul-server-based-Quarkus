package uk.ac.newcastle.enterprisemiddleware.travelagentbooking;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

import org.eclipse.microprofile.rest.client.ext.ResponseExceptionMapper;

import io.quarkus.arc.Priority;

@Provider
@Priority(1)
public class RestServiceResponseExceptionMapper implements ResponseExceptionMapper<RuntimeException> {

	@Override
	public NotFoundException toThrowable(Response response) {
		if (response.getStatus() == 404) {
			throw new NotFoundException("The remote service responded with HTTP 404");
		}
		return null;
	}
}