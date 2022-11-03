package uk.ac.newcastle.enterprisemiddleware.booking;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.inject.Named;
import javax.validation.ConstraintViolationException;

import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

/**
 * <p>This Service assumes the Control responsibility in the ECB pattern.</p>
 *
 * <p>The validation is done here so that it may be used by other Boundary Resources. Other Business Logic would go here
 * as well.</p>
 *
 * <p>There are no access modifiers on the methods, making them 'package' scope.  They should only be accessed by a
 * Boundary / Web Service class with public methods.</p>
 *
 *
 * @author Joshua Wilson
 * @see BookingValidator
 * @see BookingRepository
 */
@Dependent
public class BookingService {

    @Inject
    @Named("logger")
    Logger log;

    @Inject
    BookingValidator validator;

    @Inject
    BookingRepository crud;

    /**
     * <p>Returns a List of all persisted {@link Booking} objects, sorted alphabetically by last name.<p/>
     *
     * @return List of Booking objects
     */
    public List<Booking> findAllOrderedByDate() {
        return crud.findAllOrderedByDate();
    }

    /**
     * <p>Returns a single Booking object, specified by a Long id.<p/>
     *
     * @param id The id field of the Booking to be returned
     * @return The Booking with the specified id
     */
    public Booking findById(Long id) {
        return crud.findById(id);
    }

    /**
     * <p>Returns a single Booking object, specified by a String firstName.<p/>
     *
     * @param firstName The firstName field of the Booking to be returned
     * @return The first Booking with the specified firstName
     */
    public List<Booking> findAllByDate(Date d) {
        return crud.findAllByDate(d);
    }

    /**
     * <p>Writes the provided Booking object to the application database.<p/>
     *
     * <p>Validates the data in the provided Booking object using a {@link BookingValidator} object.<p/>
     *
     * @param Booking The Booking object to be written to the database using a {@link BookingRepository} object
     * @return The Booking object that has been successfully written to the application database
     * @throws ConstraintViolationException, ValidationException, Exception
     */
    public Booking create(Booking booking) throws Exception {
        log.info("BookingService.create() - Creating " + booking.getId());

        // Check to make sure the data fits with the parameters in the Booking model and passes validation.
        validator.validateBooking(booking);

        // Write the Booking to the database.
        return crud.create(booking);
    }

    /**
     * <p>Updates an existing Booking object in the application database with the provided Booking object.<p/>
     *
     * <p>Validates the data in the provided Booking object using a BookingValidator object.<p/>
     *
     * @param Booking The Booking object to be passed as an update to the application database
     * @return The Booking object that has been successfully updated in the application database
     * @throws ConstraintViolationException, ValidationException, Exception
     */
    public Booking update(Booking booking) throws Exception {
        log.info("BookingService.update() - Updating " + booking.getId());

        // Check to make sure the data fits with the parameters in the Booking model and passes validation.
        validator.validateBooking(booking);

        // Either update the Booking or add it if it can't be found.
        return crud.update(booking);
    }
    
    /**
     * <p>Deletes the provided Booking object from the application database if found there.<p/>
     *
     * @param Booking The Booking object to be removed from the application database
     * @return The Booking object that has been successfully removed from the application database; or null
     * @throws Exception
     */
    public Booking delete(Booking booking) throws Exception {
        log.info("delete() - Deleting " + booking.getId());

        Booking deletedbooking = null;

        if (booking.getId() != null) {
            deletedbooking = crud.delete(booking);
        } else {
            log.info("delete() - No ID was found so can't Delete.");
        }

        return deletedbooking;
    }
}