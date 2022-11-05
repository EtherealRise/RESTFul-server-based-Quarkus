package uk.ac.newcastle.enterprisemiddleware.flight;

import javax.validation.ValidationException;

@SuppressWarnings("serial")
public class UniqueNumberException extends ValidationException {

	public UniqueNumberException(String message) {
		super(message);
	}

	public UniqueNumberException(String message, Throwable cause) {
		super(message, cause);
	}

	public UniqueNumberException(Throwable cause) {
		super(cause);
	}
}
