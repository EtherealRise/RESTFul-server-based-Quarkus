package uk.ac.newcastle.enterprisemiddleware.flight;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.NoResultException;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;
import javax.validation.Validator;

import java.util.HashSet;
import java.util.Set;

/**
 * <p>This class provides methods to check Flight objects against arbitrary requirements.</p>
 *
 * @author Joshua Wilson
 * @see Flight
 * @see FlightRepository
 * @see javax.validation.Validator
 */
@ApplicationScoped
public class FlightValidator {
    @Inject
    Validator validator;

    @Inject
    FlightRepository crud;

    /**
     * <p>Validates the given Flight object and throws validation exceptions based on the type of error. If the error is standard
     * bean validation errors then it will throw a ConstraintValidationException with the set of the constraints violated.<p/>
     *
     *
     * <p>If the error is caused because an existing Flight with the same email is registered it throws a regular validation
     * exception so that it can be interpreted separately.</p>
     *
     *
     * @param Flight The Flight object to be validated
     * @throws ConstraintViolationException If Bean Validation errors exist
     * @throws ValidationException If Flight with the same email already exists
     */
    void validateFlight(Flight Flight) throws ConstraintViolationException, ValidationException {
        // Create a bean validator and check for issues.
        Set<ConstraintViolation<Flight>> violations = validator.validate(Flight);

        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(new HashSet<ConstraintViolation<?>>(violations));
        }

        // Check the uniqueness of the flight number
        if (numberAlreadyExists(Flight.getNumber(), Flight.getId())) {
            throw new UniqueNumberException("Unique Number Violation");
        }
    }

    /**
     * <p>Checks if a Flight with the same email address is already registered. This is the only way to easily capture the
     * "@UniqueConstraint(columnNames = "email")" constraint from the Flight class.</p>
     *
     * <p>Since Update will being using an email that is already in the database we need to make sure that it is the email
     * from the record being updated.</p>
     *
     * @param email The email to check is unique
     * @param id The user id to check the email against if it was found
     * @return boolean which represents whether the email was found, and if so if it belongs to the user with id
     */
    boolean numberAlreadyExists(String number, Long id) {
        Flight flight = null;
        Flight flightWithID = null;
        try {
            flight = crud.findByNumber(number);
        } catch (NoResultException e) {
            // ignore
        }

        if (flight != null && id != null) {
            try {
                flightWithID = crud.findById(id);
                if (flightWithID != null && flightWithID.getNumber().equals(number)) {
                    flight = null;
                }
            } catch (NoResultException e) {
                // ignore
            }
        }
        return flight != null;
    }
}

