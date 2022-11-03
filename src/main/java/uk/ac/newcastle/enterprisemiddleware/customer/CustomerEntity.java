package uk.ac.newcastle.enterprisemiddleware.customer;

import java.io.Serializable;
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
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

import uk.ac.newcastle.enterprisemiddleware.booking.Booking;

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
		@NamedQuery(name = Customer.FIND_ALL, query = "SELECT c FROM Customer c ORDER BY c.name ASC"),
		@NamedQuery(name = Customer.FIND_BY_EMAIL, query = "SELECT c FROM Customer c WHERE c.email = :email") })

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
@Table(name = "customer", uniqueConstraints = @UniqueConstraint(columnNames = "email"))
public class Customer implements Serializable {
	/** Default value included to remove warning. Remove or modify at will. **/
	private static final long serialVersionUID = 1L;

	public static final String FIND_ALL = "Customer.findAll";
	public static final String FIND_BY_EMAIL = "Customer.findByEmail";

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "customerId_seq")
	@SequenceGenerator(name = "customerId_seq", initialValue = 1, allocationSize = 1)
	private Long id;

	// The overall design doesn't prevent name and phone number to be unique
	@NotBlank
	@Size(min = 1, max = 50)
	@Pattern(regexp = "[A-Za-z-']+", message = "Please use a name without numbers or specials")
	@Column(name = "name")
	private String name;

	@NotBlank
	@Pattern(regexp = "^0[0-9]{10}$")
	@Column(name = "phonenumber")
	private String phonenumber;
	
	@NotBlank
	@Email(message = "The email address must be in the format of name@domain.com")
	private String email;
	
	@OneToMany(
	        mappedBy = "customer",
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

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPhonenumber() {
		return phonenumber;
	}

	public void setPhonenumber(String phonenumber)  {
		this.phonenumber = phonenumber;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}


	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof Customer))
			return false;
		Customer customer = (Customer) o;
		return email.equals(customer.email);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(email);
	}
	
	
}
