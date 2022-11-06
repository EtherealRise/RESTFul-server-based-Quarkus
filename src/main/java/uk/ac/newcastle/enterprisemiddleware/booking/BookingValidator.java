package uk.ac.newcastle.enterprisemiddleware.booking;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;

@ApplicationScoped
public class BookingValidator {
	@Inject
	Validator validator;

	@Inject
	BookingRepository bookingRepository;

	/**
	 * Validates the given Booking object and throws validation exceptions based on
	 * the type of error. If the error is standard bean validation errors then it
	 * will throw a ConstraintValidationException with the set of the constraints
	 * violated.
	 * 
	 * If the error is caused by an existing Booking with the same email is
	 * registered it throws a regular validation exception so that it can be
	 * interpreted separately.
	 * 
	 * NOTE: When a database unicity constraint is set on a JPA Entity, we usually
	 * got two options :
	 * 
	 * Catch the PersistenceException and the nested JPA provider exception (for
	 * Hibernate : ConstraintViolationException). It is very hard to create a
	 * generic handler for this exception (extract column name from the exception,
	 * recreate the form context, …)
	 * 
	 * Query the database before the persist/merge operation, in order to check if
	 * the unique value is already inserted in the database. this POC uses this
	 * option.
	 * 
	 * If we don't use UniqueEmailException to isolation the validate flow, then it
	 * will directly go to create a new booking entity even with same email address
	 * which will raise a sql exception almost unreadable.
	 *
	 */
	void validate(BookingEntity booking) {

		Set<ConstraintViolation<BookingEntity>> violations = validator.validate(booking);

		if (!violations.isEmpty()) {
			throw new ConstraintViolationException(new HashSet<ConstraintViolation<?>>(violations));
		}

		List<BookingEntity> bookings = bookingRepository.findByDate(booking.getDate());

		for (BookingEntity b : bookings)
			if (b.getFlight().getNumber().equals(booking.getFlight().getNumber()))
				throw new UniqueFlightWithDateException("this booking is already exist");

	}
}
