package uk.ac.newcastle.enterprisemiddleware.booking;

import javax.validation.ValidationException;

/**
 * <p>ValidationException caused if a Contact's email address conflicts with that of another Contact.</p>
 *
 * <p>This violates the uniqueness constraint.</p>
 *
 * @author hugofirth
 * @see Contact
 */
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

