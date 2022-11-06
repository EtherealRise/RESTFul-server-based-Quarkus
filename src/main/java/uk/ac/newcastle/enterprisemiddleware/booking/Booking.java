package uk.ac.newcastle.enterprisemiddleware.booking;

import java.util.Date;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import uk.ac.newcastle.enterprisemiddleware.customer.Customer;
import uk.ac.newcastle.enterprisemiddleware.flight.Flight;

public class Booking {

	@NotNull
	private Integer id;

	@NotBlank
	private Customer customer;

	@NotBlank
	private Flight flight;

	@NotBlank
	private Date d;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Customer getCustomer() {
		return customer;
	}

	public void setCustomer(Customer customer) {
		this.customer = customer;
	}

	public Flight getFlight() {
		return flight;
	}

	public void setFlight(Flight flight) {
		this.flight = flight;
	}

	public Date getDate() {
		return new Date(d.getTime());
	}

	public void setDate(Date d) {
		this.d = d;
	}
}
