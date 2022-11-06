package uk.ac.newcastle.enterprisemiddleware.customer;

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

@Path("/customers")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class CustomerRestService {

	@Inject
	Logger log;

	@Inject
	CustomerService customerService;

	@GET
	@Operation(summary = "Fetch all Customers", description = "Returns a JSON array of all stored Customer objects.")
	@APIResponse(responseCode = "200", description = "Customers would be return")
	public Response getAll() {
		return Response.ok(customerService.findAll()).build();
	}

	@GET
	@Cache
	@Path("/id/{id:[0-9]+}")
	@Operation(summary = "Fetch a Customer by id", description = "Returns a JSON representation of the Customer object with the provided id.")
	@APIResponse(responseCode = "200", description = "Customer found")
	@APIResponse(responseCode = "404", description = "Customer with id not found")
	public Response getById(
			@Parameter(description = "Id of Customer to be fetched") @Schema(minimum = "0", required = true) @PathParam("id") Integer id) {
		return customerService.findById(id).map(customer -> Response.ok(customer).build())
				.orElse(Response.status(Response.Status.NOT_FOUND).build());
	}

	@GET
	@Cache
	@Path("/email/{email:.+}")
	@Operation(summary = "Fetch a Customer by Email", description = "Returns a JSON representation of the Customer object with the provided email.")
	@APIResponse(responseCode = "200", description = "Customer found")
	@APIResponse(responseCode = "404", description = "Customer with email not found")
	public Response getByEmail(
			@Parameter(description = "Email of Customer to be fetched", required = true) @PathParam("email") String email) {
		return customerService.findByEmail(email).map(customer -> Response.ok(customer).build())
				.orElse(Response.status(Response.Status.NOT_FOUND).build());
	}

	@GET
	@Path("/booking/{id:.+}")
	@Operation(summary = "Fetch all bookings made by this customer", description = "Returns a JSON representation of the list of bookings.")
	@APIResponse(responseCode = "200", description = "Bookings would be return, empty if customer not found")
	public Response getBooking(
			@Parameter(description = "id of Customer to be fetched", required = true) @PathParam("id") Integer id) {
		return Response.ok(customerService.getBooking(id)).build();
	}

	@POST
	@Operation(description = "Add a new Customer to the database")
	@APIResponse(responseCode = "201", description = "Customer created successfully.")
	@APIResponse(responseCode = "400", description = "Invalid Customer supplied in request body")
	@APIResponse(responseCode = "409", description = "Customer supplied in request body conflicts with an existing Customer")
	@APIResponse(responseCode = "500", description = "An unexpected error occurred whilst processing the request")
	@Transactional
	public Response createCustomer(
			@Parameter(description = "JSON representation of Customer object to be added to the database", required = true) Customer customer) {
		
        if (customer == null) {
            throw new RestServiceException("Bad Request", Response.Status.BAD_REQUEST);
        }

		try {
			customer.setId(null);
			customerService.validateCustomer(customer);
			customerService.create(customer);
		} catch (ConstraintViolationException ce) {
			Map<String, String> responseObj = new HashMap<>();
			for (ConstraintViolation<?> violation : ce.getConstraintViolations()) {
				responseObj.put(violation.getPropertyPath().toString(), violation.getMessage());
			}
			throw new RestServiceException("Bad Request", responseObj, Response.Status.BAD_REQUEST, ce);
		} catch (UniqueEmailException e) {
			Map<String, String> responseObj = new HashMap<>();
			responseObj.put("email", "please use a unique email");
			throw new RestServiceException("email details supplied in request body conflict with another Customer",
					responseObj, Response.Status.CONFLICT, e);
		}

		return Response.ok(customer).status(Response.Status.CREATED).build();
	}

	@PUT
	@Path("/id/{id:[0-9]+}")
	@Operation(description = "Update a Customer in the database")
	@APIResponse(responseCode = "200", description = "Customer updated successfully")
	@APIResponse(responseCode = "400", description = "Invalid Customer supplied in request body")
	@APIResponse(responseCode = "404", description = "Customer with id not found")
	@APIResponse(responseCode = "409", description = "Customer details supplied in request body conflict with another existing Customer")
	@APIResponse(responseCode = "500", description = "An unexpected error occurred whilst processing the request")
	public Response updateCustomer(
			@Parameter(description = "Id of customer to be updated", required = true) @Schema(minimum = "0") @PathParam("id") Integer id,
			@Parameter(description = "JSON representation of Customer object to be added to the database", required = true) Customer customer) {

        if (customer == null) {
            throw new RestServiceException("Bad Request", Response.Status.BAD_REQUEST);
        }
		
		try {
			
			customerService.validateCustomer(customer);
			
		} catch (ConstraintViolationException ce) {
			// Handle bean validation issues
			Map<String, String> responseObj = new HashMap<>();

			for (ConstraintViolation<?> violation : ce.getConstraintViolations()) {
				responseObj.put(violation.getPropertyPath().toString(), violation.getMessage());
			}
			throw new RestServiceException("Bad Request", responseObj, Response.Status.BAD_REQUEST, ce);
			
		} catch (UniqueEmailException e) {
			// we are updating an existence flight, so ignore this as expected
		}

		try {
			// we do NOT further check whether the id is associated with this customer by
			// comparing the email, it means with this API the user could change everything
			// including email. The behavior would be like creating a new object with same
			// id. For simplify I think treat this as correct operation.
			
			customerService.update(id);
			
		} catch (ServiceException e) {
			
			Map<String, String> responseObj = new HashMap<>();
			responseObj.put("id", "please ensure the id is associated with this number");
			throw new RestServiceException("Bad Request", responseObj, Response.Status.NOT_FOUND, e);
			
		}

		return Response.ok(customer).build();
	}

	@DELETE
	@Path("/id/{id:[0-9]+}")
	@Operation(description = "Delete a Customer from the database")
	@APIResponse(responseCode = "200", description = "The Customer has been successfully deleted")
	@APIResponse(responseCode = "400", description = "Invalid Customer id supplied")
	@APIResponse(responseCode = "404", description = "Customer with id not found")
	@APIResponse(responseCode = "500", description = "An unexpected error occurred whilst processing the request")
	public Response deleteCustomer(
			@Parameter(description = "Id of customer to be deleted", required = true) @Schema(minimum = "0") @PathParam("id") Integer id) {

		Customer customer = customerService.findById(id).orElseThrow(
				() -> new RestServiceException("The id " + id + " was not found", Response.Status.NOT_FOUND));
		
		customerService.delete(id);

		return Response.ok(customer).build();
	}
}