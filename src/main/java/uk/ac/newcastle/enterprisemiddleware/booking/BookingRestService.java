package uk.ac.newcastle.enterprisemiddleware.booking;

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
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.hibernate.service.spi.ServiceException;

import uk.ac.newcastle.enterprisemiddleware.customer.Customer;
import uk.ac.newcastle.enterprisemiddleware.customer.CustomerService;
import uk.ac.newcastle.enterprisemiddleware.flight.Flight;
import uk.ac.newcastle.enterprisemiddleware.flight.FlightService;
import uk.ac.newcastle.enterprisemiddleware.util.RestServiceException;

@Path("/bookings")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class BookingRestService {

	@Inject
	Logger log;

	@Inject
	BookingService bookingService;

	@Inject
	CustomerService customerService;

	@Inject
	FlightService flightService;

	@GET
	@Operation(summary = "Fetch all Bookings", description = "Returns a JSON array of all stored Booking objects.")
	@APIResponse(responseCode = "200", description = "Bookings would be return")
	public Response getAll() {
		return Response.ok(bookingService.findAll()).build();
	}

	@POST
	@Operation(description = "Add a new Booking to the database")
	@APIResponse(responseCode = "201", description = "Booking created successfully.")
	@APIResponse(responseCode = "400", description = "Invalid Booking supplied in request body")
	@APIResponse(responseCode = "409", description = "Booking supplied in request body conflicts with an existing Booking")
	@APIResponse(responseCode = "500", description = "An unexpected error occurred whilst processing the request")
	public Response createBooking(
			@Parameter(description = "JSON representation of Booking object to be added to the database", required = true) Booking booking) {

		// the situation hare is similar to guest booking as we want to make sure the
		// customer/flight info is correct
		try {

			Customer customer = customerService.findByEmail(booking.getCustomer().getEmail())
					.orElseThrow(() -> new IllegalArgumentException("The customer doesn't exist"));
			if (!customer.equals(booking.getCustomer()))
				throw new IllegalArgumentException("The customer info is not match with database");

			Flight flight = flightService.findByNumber(booking.getFlight().getNumber())
					.orElseThrow(() -> new IllegalArgumentException("The flight doesn't exist"));
			if (!flight.equals(booking.getFlight()))
				throw new IllegalArgumentException("The customer info is not match with database");

		} catch (IllegalArgumentException e) {
			throw new RestServiceException("The customer/flight info is problem", Response.Status.BAD_REQUEST, e);

		}

		try {

			booking.setId(null);
			bookingService.validateBooking(booking);

			bookingService.create(booking);
		} catch (ConstraintViolationException ce) {

			// Handle bean validation issues
			Map<String, String> responseObj = new HashMap<>();

			for (ConstraintViolation<?> violation : ce.getConstraintViolations()) {
				responseObj.put(violation.getPropertyPath().toString(), violation.getMessage());
			}
			throw new RestServiceException("Bad Request", responseObj, Response.Status.BAD_REQUEST, ce);

		} catch (UniqueFlightWithDateException e) {
			// Handle the unique constraint violation
			Map<String, String> responseObj = new HashMap<>();
			responseObj.put("flight", "The flight" + booking.getFlight().getNumber() + "is already booked");
			throw new RestServiceException("Bad Request", responseObj, Response.Status.CONFLICT, e);

		} catch (Exception e) {
			// Handle generic exceptions
			e.printStackTrace();
			throw new RestServiceException(e);
		}

		return Response.ok(booking).status(Response.Status.CREATED).build();
	}

	@PUT
	@Path("/{id:[0-9]+}")
	@Operation(description = "Update a Booking in the database")
	@APIResponses(value = { @APIResponse(responseCode = "200", description = "Booking updated successfully"),
			@APIResponse(responseCode = "400", description = "Invalid Booking supplied in request body"),
			@APIResponse(responseCode = "404", description = "Booking with id not found"),
			@APIResponse(responseCode = "409", description = "Booking details supplied in request body conflict with another existing Booking"),
			@APIResponse(responseCode = "500", description = "An unexpected error occurred whilst processing the request") })
	@Transactional
	public Response updateBooking(
			@Parameter(description = "Id of Booking to be updated", required = true) @Schema(minimum = "0") @PathParam("id") Integer id,
			@Parameter(description = "JSON representation of Booking object to be updated in the database", required = true) Booking booking) {

		try {

			customerService.validateCustomer(booking.getCustomer());
			flightService.validateFlight(booking.getFlight());

			bookingService.validateBooking(booking);

		} catch (ConstraintViolationException ce) {
			// Handle bean validation issues
			Map<String, String> responseObj = new HashMap<>();

			for (ConstraintViolation<?> violation : ce.getConstraintViolations()) {
				responseObj.put(violation.getPropertyPath().toString(), violation.getMessage());
			}
			throw new RestServiceException("Bad Request", responseObj, Response.Status.BAD_REQUEST, ce);
		} catch (UniqueFlightWithDateException e) {
			// // we are updating an existence flight, so ignore this as expected
		}

		try {
			// we do NOT further check whether the id is associated with this customer by
			// comparing the email, it means with this API the user could change everything
			// including email. The behavior would be like creating a new object with same
			// id. For simplify I think treat this as correct operation.
			bookingService.update(id);
		} catch (ServiceException e) {
			Map<String, String> responseObj = new HashMap<>();
			responseObj.put("id", "please ensure the id is associated with this number");
			throw new RestServiceException("Bad Request", responseObj, Response.Status.NOT_FOUND, e);
		}
		bookingService.update(id);
		return Response.ok(booking).build();
	}

	@DELETE
	@Path("/{id:[0-9]+}")
	@Operation(description = "Delete a Booking from the database")
	@APIResponses(value = {
			@APIResponse(responseCode = "204", description = "The Booking has been successfully deleted"),
			@APIResponse(responseCode = "400", description = "Invalid Booking id supplied"),
			@APIResponse(responseCode = "404", description = "Booking with id not found"),
			@APIResponse(responseCode = "500", description = "An unexpected error occurred whilst processing the request") })
	@Transactional
	public Response deleteBooking(
			@Parameter(description = "Id of Booking to be deleted", required = true) @Schema(minimum = "0") @PathParam("id") Integer id) {

		Booking booking = bookingService.findById(id).orElseThrow(
				() -> new RestServiceException("The id " + id + " was not found", Response.Status.NOT_FOUND));
		bookingService.delete(id);

		return Response.ok(booking).build();
	}
}