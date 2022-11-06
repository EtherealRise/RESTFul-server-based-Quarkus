package uk.ac.newcastle.enterprisemiddleware.booking;

import javax.validation.ValidationException;

@SuppressWarnings("serial")
public class UniqueFlightWithDateException extends ValidationException {

	public UniqueFlightWithDateException(String message) {
		super(message);
	}

	public UniqueFlightWithDateException(String message, Throwable cause) {
		super(message, cause);
	}

	public UniqueFlightWithDateException(Throwable cause) {
		super(cause);
	}
}
