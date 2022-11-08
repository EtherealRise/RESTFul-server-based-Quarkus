package uk.ac.newcastle.enterprisemiddleware.flight;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

import uk.ac.newcastle.enterprisemiddleware.booking.BookingEntity;

@Entity
@ApplicationScoped
@NamedQueries({ @NamedQuery(name = "Flight.findAll", query = "SELECT f FROM FlightEntity f ORDER BY f.number ASC"),
		@NamedQuery(name = "Flight.findByNumber", query = "SELECT f FROM FlightEntity f WHERE f.number = :number") })
@Table(name = "flight", uniqueConstraints = @UniqueConstraint(columnNames = "number"))
public class FlightEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "flightId_seq")
	@SequenceGenerator(name = "flightId_seq", initialValue = 1, allocationSize = 1)
	private Long id;

	@NotBlank
	@Pattern(regexp = "^[A-Z0-9]{5}$", message = "Please type correct 5 characters alpha-numerical string")
	@Column(name = "number")
	private String number;

	// the departure can't be equal to destination
	// to make this easier, deal it at API level directly
	@NotBlank
	@Pattern(regexp = "^[A-Z]{3}$")
	@Column(name = "departure")
	private String departure;

	@NotBlank
	@Pattern(regexp = "^[A-Z]{3}$")
	@Column(name = "destination")
	private String destination;

	@OneToMany(mappedBy = "flight", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<BookingEntity> bookings = new ArrayList<>();

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	public String getDeparture() {
		return departure;
	}

	public void setDeparture(String departure) {
		this.departure = departure;
	}

	public String getDestination() {
		return destination;
	}

	public void setDestination(String destination) {
		this.destination = destination;
	}

	public List<BookingEntity> getBooking() {
		return bookings;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof FlightEntity))
			return false;
		FlightEntity flight = (FlightEntity) o;
		return number.equals(flight.number);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(number);
	}
}