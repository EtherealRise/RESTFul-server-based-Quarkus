package uk.ac.newcastle.enterprisemiddleware.flight;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.NoResultException;
import javax.validation.ConstraintViolationException;

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
 * @see FlightValidator
 * @see FlightRepository
 */
@Dependent
public class FlightService {

    @Inject
    @Named("logger")
    Logger log;

    @Inject
    FlightValidator validator;

    @Inject
    FlightRepository crud;

    /**
     * <p>Returns a List of all persisted {@link Flight} objects, sorted alphabetically by last name.<p/>
     *
     * @return List of Flight objects
     */
    public List<Flight> findAllOrderedByNumber() {
        return crud.findAllOrderedByNumber();
    }

    /**
     * <p>Returns a single Flight object, specified by a Long id.<p/>
     *
     * @param id The id field of the Flight to be returned
     * @return The Flight with the specified id
     */
    public Flight findById(Long id) {
        return crud.findById(id);
    }

    /**
     * <p>Returns a single Flight object, specified by a String email.</p>
     *
     * <p>If there is more than one Flight with the specified email, only the first encountered will be returned.<p/>
     *
     * @param email The email field of the Flight to be returned
     * @return The first Flight with the specified email
     */
    public Flight findByNumber(String number) throws NoResultException {
        return crud.findByNumber(number);
    }

    /**
     * <p>Returns a single Flight object, specified by a String firstName.<p/>
     *
     * @param firstName The firstName field of the Flight to be returned
     * @return The first Flight with the specified firstName
     */
    public List<Flight> findAllByDeparture(String Departure) {
        return crud.findAllByDeparture(Departure);
    }

    /**
     * <p>Writes the provided Flight object to the application database.<p/>
     *
     * <p>Validates the data in the provided Flight object using a {@link FlightValidator} object.<p/>
     *
     * @param Flight The Flight object to be written to the database using a {@link FlightRepository} object
     * @return The Flight object that has been successfully written to the application database
     * @throws ConstraintViolationException, ValidationException, Exception
     */
    public Flight create(Flight Flight) throws Exception {
        log.info("FlightService.create() - Creating " + Flight.getNumber());

        // Check to make sure the data fits with the parameters in the Flight model and passes validation.
        validator.validateFlight(Flight);

        // Write the Flight to the database.
        return crud.create(Flight);
    }

    /**
     * <p>Updates an existing Flight object in the application database with the provided Flight object.<p/>
     *
     * <p>Validates the data in the provided Flight object using a FlightValidator object.<p/>
     *
     * @param Flight The Flight object to be passed as an update to the application database
     * @return The Flight object that has been successfully updated in the application database
     * @throws ConstraintViolationException, ValidationException, Exception
     */
    public Flight update(Flight flight) throws Exception {
        log.info("FlightService.update() - Updating " + flight.getNumber());

        // Check to make sure the data fits with the parameters in the Flight model and passes validation.
        validator.validateFlight(flight);

        // Either update the Flight or add it if it can't be found.
        return crud.update(flight);
    }
    
    /**
     * <p>Deletes the provided Flight object from the application database if found there.<p/>
     *
     * @param Flight The Flight object to be removed from the application database
     * @return The Flight object that has been successfully removed from the application database; or null
     * @throws Exception
     */
    public Flight delete(Flight Flight) throws Exception {
        log.info("delete() - Deleting " + Flight.toString());

        Flight deletedFlight = null;

        if (Flight.getId() != null) {
            deletedFlight = crud.delete(Flight);
        } else {
            log.info("delete() - No ID was found so can't Delete.");
        }

        return deletedFlight;
    }
}