package uk.ac.newcastle.enterprisemiddleware.customer;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class Customer {

	@NotNull
	private Integer id;

	@NotBlank
	private String name;

	@NotBlank
	private String phonenumber;

	@NotBlank
	private String email;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
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
	
	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof Customer))
			return false;
		Customer other = (Customer) o;
		return email != null && email.equals(other.getEmail());
	}

	@Override
	public int hashCode() {
		return getClass().hashCode();
	}
}