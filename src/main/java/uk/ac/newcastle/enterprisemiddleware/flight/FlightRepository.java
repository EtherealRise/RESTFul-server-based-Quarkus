package uk.ac.newcastle.enterprisemiddleware.flight;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.validation.ConstraintViolationException;

import java.util.List;
import java.util.logging.Logger;

/**
 * <p>This is a Repository class and connects the Service/Control layer (see {@link FlightService} with the
 * Domain/Entity Object (see {@link Flight}).<p/>
 *
 * <p>There are no access modifiers on the methods making them 'package' scope.  They should only be accessed by a
 * Service/Control object.<p/>
 *
 * @author Joshua Wilson
 * @see Flight
 * @see javax.persistence.EntityManager
 */
@RequestScoped
public class FlightRepository {

    @Inject
    @Named("logger")
    Logger log;

    @Inject
    EntityManager em;

    /**
     * <p>Returns a List of all persisted {@link Flight} objects, sorted alphabetically by last name.</p>
     *
     * @return List of Flight objects
     */
    List<Flight> findAllOrderedByNumber() {
        TypedQuery<Flight> query = em.createNamedQuery(Flight.FIND_ALL, Flight.class);
        return query.getResultList();
    }

    /**
     * <p>Returns a single Flight object, specified by a Long id.<p/>
     *
     * @param id The id field of the Flight to be returned
     * @return The Flight with the specified id
     */
    Flight findById(Long id) {
        return em.find(Flight.class, id);
    }

    /**
     * <p>Returns a single Flight object, specified by a String email.</p>
     *
     * <p>If there is more than one Flight with the specified email, only the first encountered will be returned.<p/>
     *
     * @param email The email field of the Flight to be returned
     * @return The first Flight with the specified email
     */
    Flight findByNumber(String number) throws NoResultException {
        TypedQuery<Flight> query = em.createNamedQuery(Flight.FIND_BY_NUMBER, Flight.class).setParameter("number", number);
        return query.getSingleResult();
    }

    /**
     * <p>Returns a list of Flight objects, specified by a String firstName.<p/>
     *
     * @param firstName The firstName field of the Flights to be returned
     * @return The Flights with the specified firstName
     */
    List<Flight> findAllByDeparture(String departure) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Flight> criteria = cb.createQuery(Flight.class);
        Root<Flight> Flight = criteria.from(Flight.class);
        // Swap criteria statements if you would like to try out type-safe criteria queries, a new feature in JPA 2.0.
        // criteria.select(Flight).where(cb.equal(Flight.get(Flight_.firstName), firstName));
        criteria.select(Flight).where(cb.equal(Flight.get("departure"), departure));
        return em.createQuery(criteria).getResultList();
    }

    /**
     * <p>Persists the provided Flight object to the application database using the EntityManager.</p>
     *
     * <p>{@link javax.persistence.EntityManager#persist(Object) persist(Object)} takes an entity instance, adds it to the
     * context and makes that instance managed (ie future updates to the entity will be tracked)</p>
     *
     * <p>persist(Object) will set the @GeneratedValue @Id for an object.</p>
     *
     * @param Flight The Flight object to be persisted
     * @return The Flight object that has been persisted
     * @throws ConstraintViolationException, ValidationException, Exception
     */
    Flight create(Flight Flight) throws Exception {
        log.info("FlightRepository.create() - Creating " + Flight.getNumber());

        // Write the Flight to the database.
        em.persist(Flight);

        return Flight;
    }

    /**
     * <p>Updates an existing Flight object in the application database with the provided Flight object.</p>
     *
     * <p>{@link javax.persistence.EntityManager#merge(Object) merge(Object)} creates a new instance of your entity,
     * copies the state from the supplied entity, and makes the new copy managed. The instance you pass in will not be
     * managed (any changes you make will not be part of the transaction - unless you call merge again).</p>
     *
     * <p>merge(Object) however must have an object with the @Id already generated.</p>
     *
     * @param Flight The Flight object to be merged with an existing Flight
     * @return The Flight that has been merged
     * @throws ConstraintViolationException, ValidationException, Exception
     */
    Flight update(Flight Flight) throws Exception {
        log.info("FlightRepository.update() - Updating " + Flight.getNumber());

        // Either update the Flight or add it if it can't be found.
        em.merge(Flight);

        return Flight;
    }
    
    /**
     * <p>Deletes the provided Flight object from the application database if found there</p>
     *
     * @param Flight The Flight object to be removed from the application database
     * @return The Flight object that has been successfully removed from the application database; or null
     * @throws Exception
     */
    Flight delete(Flight Flight) throws Exception {
        log.info("FlightRepository.delete() - Deleting " + Flight.getNumber());

        if (Flight.getId() != null) {
            /*
             * The Hibernate session (aka EntityManager's persistent context) is closed and invalidated after the commit(),
             * because it is bound to a transaction. The object goes into a detached status. If you open a new persistent
             * context, the object isn't known as in a persistent state in this new context, so you have to merge it.
             *
             * Merge sees that the object has a primary key (id), so it knows it is not new and must hit the database
             * to reattach it.
             *
             * Note, there is NO remove method which would just take a primary key (id) and a entity class as argument.
             * You first need an object in a persistent state to be able to delete it.
             *
             * Therefore we merge first and then we can remove it.
             */
            em.remove(em.merge(Flight));

        } else {
            log.info("FlightRepository.delete() - No ID was found so can't Delete.");
        }

        return Flight;
    }
}
