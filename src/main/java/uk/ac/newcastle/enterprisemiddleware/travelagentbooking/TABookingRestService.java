package uk.ac.newcastle.enterprisemiddleware.travelagentbooking;

import java.util.Date;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.ClientWebApplicationException;

import uk.ac.newcastle.enterprisemiddleware.booking.Booking;
import uk.ac.newcastle.enterprisemiddleware.customer.Customer;
import uk.ac.newcastle.enterprisemiddleware.util.RestServiceException;

@Path("/tabookings")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@ApplicationScoped

// I won't provide update API since that is not required, and I agree with it since 
// it would be as same as directly call into specific application's update API.

public class TABookingRestService {

	@Inject
	Logger log;

	@Inject
	TABookingService tabookingService;

	@Inject
	@RestClient
	FlightService flightService;

	@Inject
	@RestClient
	TaxiService taxiService;

	@Inject
	@RestClient
	HotelService hotelService;

	private static Customer travelagent = new Customer();

	@GET
	@Operation(summary = "Fetch all Travel Agent booking", description = "Returns a JSON array of all stored Travel Agent tabooking.")
	@APIResponse(responseCode = "200", description = "TravelAgents would be return")
	public Response getAll() {
		return Response.ok(tabookingService.findAll()).build();
	}

	@GET
	@Path("/{id:[0-9]+}")
	@Operation(summary = "Fetch all Travel Agent booking", description = "Returns a JSON array of all stored Travel Agent tabooking.")
	@APIResponse(responseCode = "200", description = "TravelAgents would be return")
	@APIResponse(responseCode = "404", description = "Booking with id not found")
	public Response getById(
			@Parameter(description = "Id of Booking to be updated", required = true) @Schema(minimum = "0") @PathParam("id") Integer id) {
		TABooking tabooking = tabookingService.findById(id)
				.orElseThrow(() -> new RestServiceException("The id is not found", Response.Status.NOT_FOUND));

		Booking result[] = new Booking[3];

		// we don't need exception here since the database should be consistent
		result[0] = tabooking.getFlightbooking();
		result[1] = taxiService.getBookingById(tabooking.getTaxibookingId());
		result[2] = hotelService.getBookingById(tabooking.getHotelbookingId());

		return Response.ok(result).build();
	}

	@POST
	@Operation(description = "Add a new Travel Agent booking to the database")
	@APIResponse(responseCode = "201", description = "TravelAgent created successfully.")
	@APIResponse(responseCode = "400", description = "Invalid TravelAgent supplied in request body")
	@APIResponse(responseCode = "409", description = "TravelAgent supplied in request body conflicts with an existing TravelAgent")
	@APIResponse(responseCode = "500", description = "An unexpected error occurred whilst processing the request")
	public Response createTravelAgent(
			@Parameter(description = "JSON representation of TravelAgent object to be added to the database", required = true) TABooking tabooking) {

		// first, check if we have a represent of our travel agent in each application
		// since I'm using my own application as other resources, it's safe to pass this
		// customer object. But if we are going to switch to other's application, we
		// must have some common API definition for the structure of customer object.

		travelagent.setEmail("TravelAgent@email.com");
		travelagent.setName("TravelAgent");
		travelagent.setPhonenumber("01234567890");

		try {
			taxiService.getByEmail("TravelAgent@email.com");
		} catch (ClientWebApplicationException e) {
			// if we not found, create a new one
			// NOTE: to keep simplify, this can not and should not fail
			taxiService.createCustomer(travelagent);
		}

		try {
			hotelService.getByEmail("TravelAgent@email.com");
		} catch (ClientWebApplicationException e) {
			// As we are duplicating the same service, let's disable this temporarily as
			// creating with same email is forbidden

			// hotelService.createCustomer(new Customer("TravelAgent@email.com"));
		}

		// booking for ourself
		try {
			Booking booking = flightService.createBooking(tabooking.getFlightbooking());
			tabooking.setFlightbooking(booking);
		} catch (Exception e) {
			throw new RestServiceException("Bad Request", Response.Status.BAD_REQUEST, e);
		}

		try {
			// As we are duplicating the same service, reset time to avoid violate unique of
			// flight with date
			Booking temp = tabooking.getFlightbooking();
			temp.setDate(new Date());
			temp = taxiService.createBooking(temp);

			tabooking.setTaxibookingId(temp.getId());
		} catch (Exception e) {
			// now we have to manually delete flight record
			// technically this method it-self could fail, but for simplify we ignore that
			// possibility
			e.printStackTrace();
			flightService.deleteBooking(tabooking.getFlightbooking().getId());
			throw new RestServiceException("Bad Request", Response.Status.BAD_REQUEST, e);
		}

		try {
			// same as taxi
			Booking temp = tabooking.getFlightbooking();
			temp.setDate(new Date());
			temp = hotelService.createBooking(temp);

			tabooking.setHotelbookingId(temp.getId());
		} catch (Exception e) {

			// now we have to manually delete both flight and taxi record
			// technically these methods it-self could fail, but for simplify we ignore that
			// possibility
			flightService.deleteBooking(tabooking.getFlightbooking().getId());
			taxiService.deleteBooking(tabooking.getTaxibookingId());
			throw new RestServiceException("Bad Request", Response.Status.BAD_REQUEST, e);
		}

		// we assume this could not fail and it should not fail since we can
		// successfully create resources of it.
		tabookingService.create(tabooking);

		return Response.ok(tabooking).status(Response.Status.CREATED).build();
	}

	@DELETE
	@Path("/{id:[0-9]+}")
	@Operation(description = "Delete a Travel Agent booking from the database")
	@APIResponses(value = {
			@APIResponse(responseCode = "204", description = "The TravelAgent has been successfully deleted"),
			@APIResponse(responseCode = "400", description = "Invalid TravelAgent id supplied"),
			@APIResponse(responseCode = "404", description = "TravelAgent with id not found"),
			@APIResponse(responseCode = "500", description = "An unexpected error occurred whilst processing the request") })
	@Transactional
	public Response deleteTravelAgent(
			@Parameter(description = "Id of TravelAgent to be deleted", required = true) @Schema(minimum = "0") @PathParam("id") Integer id) {

		TABooking tabooking = tabookingService.findById(id)
				.orElseThrow(() -> new RestServiceException("Bad Request", Response.Status.NOT_FOUND));

		try {
			// deal with remote application firstly

			taxiService.deleteBooking(tabooking.getTaxibookingId());

			hotelService.deleteBooking(tabooking.getHotelbookingId());

			// locally delete flight record

			flightService.deleteBooking(tabooking.getFlightbooking().getId());
		} catch (Exception e) {
			// instead of manually recover the data for each application, we just throw an
			// exception to log this failure for simplify

			throw new RestServiceException("We failed to do delete opration, database is inconsistent now",
					Response.Status.INTERNAL_SERVER_ERROR, e);
		}

		return Response.ok(tabooking).build();
	}
}
