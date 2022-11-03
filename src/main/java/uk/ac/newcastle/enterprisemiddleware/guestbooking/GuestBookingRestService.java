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

import uk.ac.newcastle.enterprisemiddleware.booking.Booking;
import uk.ac.newcastle.enterprisemiddleware.booking.BookingService;
import uk.ac.newcastle.enterprisemiddleware.booking.UniqueFlightWithDateException;
import uk.ac.newcastle.enterprisemiddleware.customer.Customer;
import uk.ac.newcastle.enterprisemiddleware.customer.CustomerService;
import uk.ac.newcastle.enterprisemiddleware.customer.UniqueEmailException;
import uk.ac.newcastle.enterprisemiddleware.util.RestServiceException;

@Path("/guestbooking")
public class GuestBookingRestService {

	@Inject
	CustomerService customerService;

	@Inject
	BookingService bookingService;

	@Inject
	UserTransaction ts;
	
	Customer c = null;
	Booking b = null;

	@POST
	@Operation(description = "make a guest booking and create related new customer object")
	public Response makingGuestBook(
			@Parameter(description = "JSON representation of Custoemr and Booking object to be added to the database", required = true) GuestBooking gb) throws IllegalStateException, SecurityException, SystemException {

		Response.ResponseBuilder builder = null;
		
		try {
			ts.begin();
			c = customerService.create(gb.getCustomer());
			System.out.print(c.getEmail());
			b = gb.getBooking();
			if(gb.getBooking() == null)
				System.out.print("NONONONONO");
			System.out.print(b);
			System.out.print(b.getDate());
			b.setCustomer(c);
			b = bookingService.create(b);
			builder = Response.status(Response.Status.CREATED).entity(b);
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
            // Handle the unique constraint violation
            Map<String, String> responseObj = new HashMap<>();
            responseObj.put("email", "The email" + c.getEmail() + "is already used, please use a unique email");
            ts.rollback();
            throw new RestServiceException("Bad Request", responseObj, Response.Status.CONFLICT, e);
        } catch (UniqueFlightWithDateException e) {
			// Handle the unique constraint violation
			Map<String, String> responseObj = new HashMap<>();
			responseObj.put("flight", "The flight" + b.getFlight().getNumber() + "is already booked");
			ts.rollback();
			throw new RestServiceException("Bad Request", responseObj, Response.Status.CONFLICT, e);
		} catch (Exception e) {
        	e.printStackTrace();
        	ts.rollback(); 	
        }
		return builder.build();
	}
}