package uk.ac.newcastle.enterprisemiddleware.flight;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.hibernate.service.spi.ServiceException;
import org.jboss.resteasy.reactive.Cache;

import uk.ac.newcastle.enterprisemiddleware.util.RestServiceException;

@Path("/flights")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class FlightRestService {

	@Inject
	Logger log;

	@Inject
	FlightService flightService;

	@GET
	@Operation(summary = "Fetch all Flights", description = "Returns a JSON array of all stored Flight objects.")
	@APIResponse(responseCode = "200", description = "Flights would be return")
	public Response getAll() {
		return Response.ok(flightService.findAll()).build();
	}

	@GET
	@Cache
	@Path("/id/{id:[0-9]+}")
	@Operation(summary = "Fetch a Flight by id", description = "Returns a JSON representation of the Flight object with the provided id.")
	@APIResponse(responseCode = "200", description = "Flight found")
	@APIResponse(responseCode = "404", description = "Flight with id not found")
	public Response getById(
			@Parameter(description = "Id of Flight to be fetched") @Schema(minimum = "0", required = true) @PathParam("id") Integer id) {
		return flightService.findById(id).map(flight -> Response.ok(flight).build())
				.orElse(Response.status(Response.Status.NOT_FOUND).build());
	}

	@GET
	@Cache
	@Path("/number/{number:.+}")
	@Operation(summary = "Fetch a Flight by Number", description = "Returns a JSON representation of the Flight object with the provided number.")
	@APIResponse(responseCode = "200", description = "Flight found")
	@APIResponse(responseCode = "404", description = "Flight with number not found")
	public Response getByNumber(
			@Parameter(description = "Number of Flight to be fetched", required = true) @PathParam("number") String number) {
		return flightService.findByNumber(number).map(flight -> Response.ok(flight).build())
				.orElse(Response.status(Response.Status.NOT_FOUND).build());
	}

	@GET
	@Path("/booking/{id:.+}")
	@Operation(summary = "Fetch all bookings made by this flight", description = "Returns a JSON representation of the list of bookings.")
	@APIResponse(responseCode = "200", description = "Bookings would be return, empty if flight not found")
	public Response getBooking(
			@Parameter(description = "id of Flight to be fetched", required = true) @PathParam("id") Integer id) {
		return Response.ok(flightService.getBooking(id)).build();
	}

	@POST
	@Operation(description = "Add a new Flight to the database")
	@APIResponse(responseCode = "201", description = "Flight created successfully.")
	@APIResponse(responseCode = "400", description = "Invalid Flight supplied in request body")
	@APIResponse(responseCode = "409", description = "Flight supplied in request body conflicts with an existing Flight")
	@APIResponse(responseCode = "500", description = "An unexpected error occurred whilst processing the request")
	@Transactional
	public Response createFlight(
			@Parameter(description = "JSON representation of Flight object to be added to the database", required = true) Flight flight) {
		
        if (flight == null) {
            throw new RestServiceException("Bad Request", Response.Status.BAD_REQUEST);
        }

		try {
			
			flight.setId(null);
			flightService.validateFlight(flight);
			flightService.create(flight);
			
		} catch (ConstraintViolationException ce) {
			
			Map<String, String> responseObj = new HashMap<>();
			for (ConstraintViolation<?> violation : ce.getConstraintViolations()) {
				responseObj.put(violation.getPropertyPath().toString(), violation.getMessage());
			}
			
			throw new RestServiceException("Bad Request", responseObj, Response.Status.BAD_REQUEST, ce);
			
		} catch (IllegalArgumentException e) {
			
			Map<String, String> responseObj = new HashMap<>();
			responseObj.put("destination", "destination must not be same as departure");
			
			throw new RestServiceException("destination of this flight is same as its departure", responseObj,
					Response.Status.BAD_REQUEST, e);
			
		} catch (UniqueNumberException e) {
			
			Map<String, String> responseObj = new HashMap<>();
			responseObj.put("number", "please use a unique number");
			
			throw new RestServiceException("number details supplied in request body conflict with another Flight",
					responseObj, Response.Status.CONFLICT, e);
			
		}

		return Response.ok(flight).status(Response.Status.CREATED).build();
	}

	@PUT
	@Path("/id/{id:[0-9]+}")
	@Operation(description = "Update a Flight in the database")
	@APIResponse(responseCode = "200", description = "Flight updated successfully")
	@APIResponse(responseCode = "400", description = "Invalid Flight supplied in request body")
	@APIResponse(responseCode = "404", description = "Flight with id not found")
	@APIResponse(responseCode = "500", description = "An unexpected error occurred whilst processing the request")
	public Response updateFlight(
			@Parameter(description = "Id of flight to be updated", required = true) @Schema(minimum = "0") @PathParam("id") Integer id,
			@Parameter(description = "JSON representation of Flight object to be added to the database", required = true) Flight flight) {
		
        if (flight == null) {
            throw new RestServiceException("Bad Request", Response.Status.BAD_REQUEST);
        }
        
        if (flight.getId() != id) {
        	throw new RestServiceException("Bad Request", Response.Status.BAD_REQUEST);
        }

		try {
			
			flightService.validateFlight(flight);
			
		} catch (ConstraintViolationException ce) {
			// Handle bean validation issues
			Map<String, String> responseObj = new HashMap<>();
			for (ConstraintViolation<?> violation : ce.getConstraintViolations()) {
				responseObj.put(violation.getPropertyPath().toString(), violation.getMessage());
			}
			
			throw new RestServiceException("Bad Request", responseObj, Response.Status.BAD_REQUEST, ce);
			
		} catch (IllegalArgumentException e) {
			
			Map<String, String> responseObj = new HashMap<>();
			responseObj.put("destination", "destination must not be same as departure");
			
			throw new RestServiceException("destination of this flight is same as its departure", responseObj,
					Response.Status.BAD_REQUEST, e);
			
		} catch (UniqueNumberException e) {
			// we are updating an existence flight, so ignore this as expected
			
		}

		try {
			
			flightService.update(id, flight);
			
		} catch (ServiceException e) {
			Map<String, String> responseObj = new HashMap<>();
			responseObj.put("id", "please ensure the id is associated with this number");
			
			throw new RestServiceException("Bad Request", responseObj, Response.Status.NOT_FOUND, e);
			
		}

		return Response.ok(flight).build();
	}

	@DELETE
	@Path("/id/{id:[0-9]+}")
	@Operation(description = "Delete a Flight from the database")
	@APIResponse(responseCode = "200", description = "The Flight has been successfully deleted")
	@APIResponse(responseCode = "400", description = "Invalid Flight id supplied")
	@APIResponse(responseCode = "404", description = "Flight with id not found")
	@APIResponse(responseCode = "500", description = "An unexpected error occurred whilst processing the request")
	public Response deleteFlight(
			@Parameter(description = "Id of flight to be deleted", required = true) @Schema(minimum = "0") @PathParam("id") Integer id) {

		Flight flight = flightService.findById(id).orElseThrow(
				() -> new RestServiceException("The id " + id + " was not found", Response.Status.NOT_FOUND));
		flightService.delete(id);

		return Response.ok(flight).build();
	}
}