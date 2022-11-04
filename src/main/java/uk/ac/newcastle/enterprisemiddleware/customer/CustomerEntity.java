package uk.ac.newcastle.enterprisemiddleware.customer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

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
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import uk.ac.newcastle.enterprisemiddleware.booking.Booking;

@Entity
@ApplicationScoped
@Table(name = "customer")
@NamedQueries({ @NamedQuery(name = "Customer.findAll", query = "SELECT c FROM CustomerEntity c ORDER BY c.name ASC"),
		@NamedQuery(name = "Customer.findByEmail", query = "SELECT c FROM CustomerEntity c WHERE c.email = :email") })
@SuppressWarnings("serial")
public class CustomerEntity implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "customerId_seq")
	@SequenceGenerator(name = "customerId_seq", initialValue = 1, allocationSize = 1)
	private Long id;

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
	@Column(unique = true)
	private String email;

	@OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
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

	public void setPhonenumber(String phonenumber) {
		this.phonenumber = phonenumber;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public List<Booking> getBooking() {
		return bookings;
	}

	// Hibernate requirement
	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof CustomerEntity))
			return false;
		CustomerEntity other = (CustomerEntity) o;
		return id != null && id.equals(other.getId());
	}

	@Override
	public int hashCode() {
		return getClass().hashCode();
	}
}