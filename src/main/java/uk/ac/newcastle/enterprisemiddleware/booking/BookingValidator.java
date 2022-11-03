package uk.ac.newcastle.enterprisemiddleware.booking;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;
import javax.validation.Validator;

import uk.ac.newcastle.enterprisemiddleware.flight.Flight;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * <p>This class provides methods to check Book objects against arbitrary requirements.</p>
 *
 * @author Joshua Wilson
 * @see Booking
 * @see BookingRepository
 * @see javax.validation.Validator
 */
@ApplicationScoped
public class BookingValidator {
    @Inject
    Validator validator;

    @Inject
    BookingRepository crud;

    /**
     * <p>Validates the given Book object and throws validation exceptions based on the type of error. If the error is standard
     * bean validation errors then it will throw a ConstraintValidationException with the set of the constraints violated.<p/>
     *
     *
     * <p>If the error is caused because an existing Book with the same email is registered it throws a regular validation
     * exception so that it can be interpreted separately.</p>
     *
     *
     * @param Book The Book object to be validated
     * @throws ConstraintViolationException If Bean Validation errors exist
     * @throws ValidationException If Book with the same email already exists
     */
    void validateBooking(Booking booking) throws ConstraintViolationException, ValidationException {
        // Create a bean validator and check for issues.
        Set<ConstraintViolation<Booking>> violations = validator.validate(booking);

        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(new HashSet<ConstraintViolation<?>>(violations));
        }

        // Check the uniqueness of the Book number
        if (bookingAlreadyExists(booking.getFlight(), booking.getDate())) {
            throw new UniqueFlightWithDateException("Unique Flight&Date Violation");
        }
    }

    /**
     * <p>Checks if a Book with the same email address is already registered. This is the only way to easily capture the
     * "@UniqueConstraint(columnNames = "email")" constraint from the Book class.</p>
     *
     * <p>Since Update will being using an email that is already in the database we need to make sure that it is the email
     * from the record being updated.</p>
     *
     * @param email The email to check is unique
     * @param id The user id to check the email against if it was found
     * @return boolean which represents whether the email was found, and if so if it belongs to the user with id
     */
    boolean bookingAlreadyExists(Flight fight, Date d) {       
        List<Booking> bookings = crud.findAllByDate(d);
        System.out.print(bookings.size());
        
        for(Booking b : bookings)
        	if( b.getFlight().equals(fight))
        		return true;
        
        return false;
    }
}