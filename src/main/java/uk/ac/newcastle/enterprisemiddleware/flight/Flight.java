package uk.ac.newcastle.enterprisemiddleware.flight;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import uk.ac.newcastle.enterprisemiddleware.customer.Customer;

public class Flight {

	@NotNull
	private Integer id;

	@NotBlank
	private String number;

	@NotBlank
	private String departure;

	@NotBlank
	private String destination;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
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
	
	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof Flight))
			return false;
		Flight other = (Flight) o;
		return number != null && number.equals(other.getNumber());
	}

	@Override
	public int hashCode() {
		return getClass().hashCode();
	}
}
