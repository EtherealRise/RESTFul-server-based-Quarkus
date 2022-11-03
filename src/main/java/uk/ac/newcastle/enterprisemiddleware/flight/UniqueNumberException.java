package uk.ac.newcastle.enterprisemiddleware.flight;

import javax.validation.ValidationException;

/**
 * <p>ValidationException caused if a Contact's email address conflicts with that of another Contact.</p>
 *
 * <p>This violates the uniqueness constraint.</p>
 *
 * @author hugofirth
 * @see Contact
 */
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

