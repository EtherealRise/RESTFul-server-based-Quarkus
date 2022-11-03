package uk.ac.newcastle.enterprisemiddleware.flight;

import javax.persistence.*;
import javax.validation.constraints.*;
import javax.xml.bind.annotation.XmlRootElement;

import uk.ac.newcastle.enterprisemiddleware.booking.Booking;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * <p>
 * This is a the Domain object. The Contact class represents how contact
 * resources are represented in the application database.
 * </p>
 *
 * <p>
 * The class also specifies how a contacts are retrieved from the database
 * (with @NamedQueries), and acceptable values for Contact fields
 * (with @NotNull, @Pattern etc...)
 * <p/>
 *
 * @author Joshua Wilson
 */
/*
 * The @NamedQueries included here are for searching against the table that
 * reflects this object. This is the most efficient form of query in JPA though
 * is it more error prone due to the syntax being in a String. This makes it
 * harder to debug.
 */
@Entity
@NamedQueries({
		@NamedQuery(name = Flight.FIND_ALL, query = "SELECT f FROM Flight f ORDER BY f.number ASC"),
		@NamedQuery(name = Flight.FIND_BY_NUMBER, query = "SELECT f FROM Flight f WHERE f.number = :number") })

/**
 * When a database unicity constraint is set on a JPA Entity, we usually got two
 * options :
 *
 * Catch the PersistenceException and the nested JPA provider exception (for
 * Hibernate : ConstraintViolationException). It is very hard to create a
 * generic handler for this exception (extract column name from the exception,
 * recreate the form context, …) 
 * 
 * Query the database before the persist/merge
 * operation, in order to check if the unique value is already inserted in the
 * database. this POC uses this option.
 */

@XmlRootElement
@Table(name = "flight", uniqueConstraints = @UniqueConstraint(columnNames = "number"))
public class Flight implements Serializable {
	/** Default value included to remove warning. Remove or modify at will. **/
	private static final long serialVersionUID = 1L;

	public static final String FIND_ALL = "flight.findAll";
	public static final String FIND_BY_NUMBER = "flight.findByNumber";

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "flightId_seq")
	@SequenceGenerator(name = "flightId_seq", initialValue = 1, allocationSize = 1)
	private Long id;

	@NotNull
	@Pattern(regexp = "^[A-Z0-9]{5}$", message = "Please type correct 5 characters alpha-numerical string")
	@Column(name = "number")
	private String number;

	// the departure can't be equal to destination
	@NotNull
	@Pattern(regexp = "^[A-Z]{3}$")
	@Column(name = "departure")
	private String departure;
	
	@NotNull
	@Pattern(regexp = "^[A-Z]{3}$")
	@Column(name = "destination")
	private String destination;
	
	@OneToMany(
	        mappedBy = "flight",
	        cascade = CascadeType.ALL,
	        orphanRemoval = true
	    )
	private List<Booking> bookings = new ArrayList<>();

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

	public void setDeparture(String departure) throws Exception {
		if(departure.equals(this.destination))
			throw new IllegalArgumentException("departure can't be equal to desination");
		else
			this.departure = departure;
	}

	public String getDestination() {
		return destination;
	}

	public void setDestination(String destination) {
		if(destination.equals(this.departure))
			throw new IllegalArgumentException("departure can't be equal to desination");
		else
			this.destination = destination;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof Flight))
			return false;
		Flight flight = (Flight) o;
		return number.equals(flight.number);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(number);
	}
}
