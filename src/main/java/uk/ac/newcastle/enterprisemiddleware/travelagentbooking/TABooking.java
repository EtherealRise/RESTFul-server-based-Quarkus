package uk.ac.newcastle.enterprisemiddleware.travelagentbooking;

import java.util.Objects;

import javax.validation.constraints.NotBlank;

import uk.ac.newcastle.enterprisemiddleware.booking.Booking;
import uk.ac.newcastle.enterprisemiddleware.customer.Customer;

public class TABooking {

	private Integer id;

	@NotBlank
	private Customer customer;

	@NotBlank
	private Booking flightbooking;

	// belong to upstream booking so we just store id here
	private Integer taxibookingid;

	private Integer hotelbookingid;

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

	public Booking getFlightbooking() {
		return flightbooking;
	}

	public void setFlightbooking(Booking flightbooking) {
		this.flightbooking = flightbooking;
	}

	public Integer getTaxibookingId() {
		return taxibookingid;
	}

	public void setTaxibookingId(Integer taxibookingid) {
		this.taxibookingid = taxibookingid;
	}

	public Integer getHotelbookingId() {
		return hotelbookingid;
	}

	public void setHotelbookingId(Integer hotelbookingid) {
		this.hotelbookingid = hotelbookingid;
	}

	// Hibernate requirement
	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof TABooking))
			return false;
		TABooking other = (TABooking) o;
		return flightbooking.equals(other.flightbooking) && taxibookingid.equals(other.taxibookingid)
				&& hotelbookingid.equals(other.hotelbookingid);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(getClass());
	}
}
