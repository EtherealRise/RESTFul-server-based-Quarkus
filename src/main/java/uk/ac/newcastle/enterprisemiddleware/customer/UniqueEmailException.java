package uk.ac.newcastle.enterprisemiddleware.customer;

import javax.validation.ValidationException;

@SuppressWarnings("serial")
public class UniqueEmailException extends ValidationException {

	public UniqueEmailException(String message) {
		super(message);
	}

	public UniqueEmailException(String message, Throwable cause) {
		super(message, cause);
	}

	public UniqueEmailException(Throwable cause) {
		super(cause);
	}
}