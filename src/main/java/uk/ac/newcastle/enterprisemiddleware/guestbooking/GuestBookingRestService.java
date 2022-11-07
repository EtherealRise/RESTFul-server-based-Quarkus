package uk.ac.newcastle.enterprisemiddleware.guestbooking;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;

import uk.ac.newcastle.enterprisemiddleware.booking.BookingService;
import uk.ac.newcastle.enterprisemiddleware.booking.UniqueFlightWithDateException;
import uk.ac.newcastle.enterprisemiddleware.customer.CustomerService;
import uk.ac.newcastle.enterprisemiddleware.customer.UniqueEmailException;
import uk.ac.newcastle.enterprisemiddleware.flight.FlightService;
import uk.ac.newcastle.enterprisemiddleware.util.RestServiceException;

@Path("/guestbooking")
public class GuestBookingRestService {

	@Inject
	CustomerService customerService;

	@Inject
	FlightService flightService;

	@Inject
	BookingService bookingService;

	@Inject
	UserTransaction ts;

	@POST
	@Operation(description = "make a guest booking and create related new customer object")
	public Response makingGuestBook(
			@Parameter(description = "JSON representation of Custoemr and Booking object to be added to the database", required = true) GuestBooking gb)
			throws IllegalStateException, SecurityException, SystemException {

		// For simplify, we assume the flight is valid in booking
		try {
			ts.begin();
			customerService.validateCustomer(gb.getCustomer());
			customerService.create(gb.getCustomer());

			gb.getBooking().setCustomer(gb.getCustomer());
			bookingService.validateBooking(gb.getBooking());
			bookingService.create(gb.getBooking());
			ts.commit();
		} catch (ConstraintViolationException ce) {
			Map<String, String> responseObj = new HashMap<>();

			for (ConstraintViolation<?> violation : ce.getConstraintViolations()) {
				responseObj.put(violation.getPropertyPath().toString(), violation.getMessage());
			}
			ce.printStackTrace();
			ts.rollback();
			throw new RestServiceException("Bad Request", responseObj, Response.Status.BAD_REQUEST, ce);

		} catch (UniqueEmailException e) {
			// The guest's email has already been used
			Map<String, String> responseObj = new HashMap<>();
			responseObj.put("email", "The email is already used, please use a unique email");
			ts.rollback();
			throw new RestServiceException("Bad Request", responseObj, Response.Status.CONFLICT, e);

		} catch (UniqueFlightWithDateException e) {
			// The booking has already been made for one customer
			Map<String, String> responseObj = new HashMap<>();
			responseObj.put("date", "The date is already used");
			ts.rollback();
			throw new RestServiceException("Bad Request", responseObj, Response.Status.CONFLICT, e);
			
		} catch (Exception e) {
			e.printStackTrace();
			ts.rollback();
		}
		return Response.ok(gb).status(201).build();
	}
}
