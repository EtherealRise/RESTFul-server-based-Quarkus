package uk.ac.newcastle.enterprisemiddleware.booking;

import java.util.Date;
import java.util.Objects;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import uk.ac.newcastle.enterprisemiddleware.customer.CustomerEntity;
import uk.ac.newcastle.enterprisemiddleware.flight.FlightEntity;

@Entity
@ApplicationScoped
@Table(name = "booking")
@NamedQueries({ @NamedQuery(name = "Booking.findAll", query = "SELECT b FROM BookingEntity b ORDER BY b.d ASC"),
		@NamedQuery(name = "Booking.findByFlight", query = "SELECT b FROM BookingEntity b WHERE b.flight = :flight"),
		@NamedQuery(name = "Booking.findByDate", query = "SELECT b FROM BookingEntity b WHERE b.d = :d") })
public class BookingEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "bookingId_seq")
	@SequenceGenerator(name = "bookingId_seq", initialValue = 1, allocationSize = 1)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "customer_id", nullable = false)
	private CustomerEntity customer;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "flight_id", nullable = false)
	private FlightEntity flight;

	@NotNull
	@Column(name = "date")
	private Date d;

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
			customer.addBooking(this);
	}

	public FlightEntity getFlight() {
		return flight;
	}

	public void setFlight(FlightEntity flight) {
		this.flight = flight;
	}

	public Date getDate() {
		return new Date(d.getTime());
	}

	public void setDate(Date d) {
		this.d = d;
	}

	// Hibernate requirement
	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof BookingEntity))
			return false;
		BookingEntity booking = (BookingEntity) o;
		return flight.equals(booking.flight) && d.equals(booking.d);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(customer.toString() + flight.toString());
	}
}
