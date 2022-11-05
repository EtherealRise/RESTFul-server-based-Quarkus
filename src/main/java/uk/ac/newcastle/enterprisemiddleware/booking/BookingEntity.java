package uk.ac.newcastle.enterprisemiddleware.booking;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import uk.ac.newcastle.enterprisemiddleware.customer.Customer;
import uk.ac.newcastle.enterprisemiddleware.flight.Flight;

import java.io.Serializable;
import java.util.Date;
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
@ApplicationScoped
@NamedQueries({
		@NamedQuery(name = Booking.FIND_ALL, query = "SELECT b FROM Booking b ORDER BY b.d ASC"),
		@NamedQuery(name = Booking.FIND_BY_DATE, query = "SELECT b FROM Booking b WHERE b.d = :d") })

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
@Table(name = "booking")
public class Booking implements Serializable {
	/** Default value included to remove warning. Remove or modify at will. **/
	private static final long serialVersionUID = 1L;

	// for simplicities sake there is only one customer per flight/date, thus we only need to find by date.
	public static final String FIND_ALL = "booking.findAll";
	public static final String FIND_BY_DATE = "booking.findByDate";

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "bookingId_seq")
	@SequenceGenerator(name = "bookingId_seq", initialValue = 1, allocationSize = 1)
	private Long id;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "flight_id", nullable = false)
    private Flight flight;

	@NotNull
	@Column(name = "date")
	private Date d;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
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
		return d;
	}
	
	public void setDate(Date d) {
		this.d = d;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof Booking))
			return false;
		Booking booking = (Booking) o;
		return flight.equals(booking.flight)
			&& d.equals(booking.d);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(customer.toString() + flight.toString());
	}
}
