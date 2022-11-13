package uk.ac.newcastle.enterprisemiddleware.travelagentbooking;

import java.util.Objects;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import uk.ac.newcastle.enterprisemiddleware.booking.BookingEntity;
import uk.ac.newcastle.enterprisemiddleware.customer.CustomerEntity;

@Entity
@ApplicationScoped
@Table(name = "TABooking")
@NamedQuery(name = "TABooking.findAll", query = "SELECT t FROM TABookingEntity t ORDER BY t.id ASC")
public class TABookingEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "TABookingId_seq")
	@SequenceGenerator(name = "TABookingId_seq", initialValue = 1, allocationSize = 1)
	private Long id;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "customer_id", nullable = false)
	private CustomerEntity customer;

	@OneToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "booking_id", nullable = false)
	private BookingEntity flightbooking;

	// belong to upstream booking so we just store id here
	private Long taxibookingid;

	private Long hotelbookingid;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public CustomerEntity getCustomer() {
		return customer;
	}

	public void setCustomer(CustomerEntity customer) {
		this.customer = customer;
		if (customer != null)
			customer.addTravelAgentBooking(this);
	}

	public BookingEntity getFlightbooking() {
		return flightbooking;
	}

	public void setFlightbooking(BookingEntity flightbooking) {
		this.flightbooking = flightbooking;
	}

	public Long getTaxibookingId() {
		return taxibookingid;
	}

	public void setTaxibookingId(Long taxibookingid) {
		this.taxibookingid = taxibookingid;
	}

	public Long getHotelbookingId() {
		return hotelbookingid;
	}

	public void setHotelbookingId(Long hotelbookingid) {
		this.hotelbookingid = hotelbookingid;
	}

	// Hibernate requirement
	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof TABookingEntity))
			return false;
		TABookingEntity other = (TABookingEntity) o;
		return flightbooking.equals(other.flightbooking) && taxibookingid.equals(other.taxibookingid)
				&& hotelbookingid.equals(other.hotelbookingid);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(getClass());
	}
}
