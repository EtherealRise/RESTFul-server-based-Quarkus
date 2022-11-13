package uk.ac.newcastle.enterprisemiddleware.travelagentbooking;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import uk.ac.newcastle.enterprisemiddleware.booking.Booking;
import uk.ac.newcastle.enterprisemiddleware.customer.Customer;

//duplicate with local flight service
@RegisterRestClient(configKey = "taxiservice-api")
public interface TaxiService {

	@GET
	@Path("/customers/email/{email:.+}")
	@Produces({ MediaType.APPLICATION_JSON })
	Response getByEmail(@PathParam("email") String email);

	@POST
	@Path("/customers")
	@Produces({ MediaType.APPLICATION_JSON })
	Response createCustomer(Customer customer);

	@GET
	@Path("/flights")
	@Produces({ MediaType.APPLICATION_JSON })
	Response getAll();

	@GET
	@Path("/bookings/{id:[0-9]+}")
	@Produces({ MediaType.APPLICATION_JSON })
	Booking getBookingById(@PathParam("id") Integer id);

	@POST
	@Path("/bookings")
	@Produces({ MediaType.APPLICATION_JSON })
	Booking createBooking(Booking booking);

	@PUT
	@Path("/bookings/{id:[0-9]+}")
	@Produces({ MediaType.APPLICATION_JSON })
	Booking updateBooking(@PathParam("id") Integer id, Booking booking);

	@DELETE
	@Path("/bookings/{id:[0-9]+}")
	@Produces({ MediaType.APPLICATION_JSON })
	Booking deleteBooking(@PathParam("id") Integer id);

}
