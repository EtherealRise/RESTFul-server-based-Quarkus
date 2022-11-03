package uk.ac.newcastle.enterprisemiddleware.customer;

import java.util.List;
import java.util.logging.Logger;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.inject.Named;
import javax.validation.ConstraintViolationException;

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
 * @see CustomerValidator
 * @see CustomerRepository
 */
@Dependent
public class CustomerService {
	
    @Inject
    @Named("logger")
    Logger log;
    
    @Inject
    CustomerValidator validator;

    @Inject
    CustomerRepository customerRepository;

    /**
     * <p>Returns a List of all persisted {@link Customer} objects, sorted alphabetically by last name.<p/>
     *
     * @return List of Customer objects
     */
    public List<Customer> findAllOrderedByName() {
    	return customerRepository.findAllOrderedByName();
    }

    /**
     * <p>Returns a single Customer object, specified by a Long id.<p/>
     *
     * @param id The id field of the Customer to be returned
     * @return The Customer with the specified id
     */
    public Customer findById(Long id) {
        return customerRepository.findById(id);
    }

    /**
     * <p>Returns a single Customer object, specified by a String email.</p>
     *
     * <p>If there is more than one Customer with the specified email, only the first encountered will be returned.<p/>
     *
     * @param email The email field of the Customer to be returned
     * @return The first Customer with the specified email
     */
    public Customer findByEmail(String email) {
    	return customerRepository.findByEmail(email);
    }

    /**
     * <p>Writes the provided Customer object to the application database.<p/>
     *
     * <p>Validates the data in the provided Customer object using a {@link CustomerValidator} object.<p/>
     *
     * @param Customer The Customer object to be written to the database using a {@link CustomerRepository} object
     * @return The Customer object that has been successfully written to the application database
     * @throws ConstraintViolationException, ValidationException, Exception
     */
    public Customer create(Customer customer) throws Exception {
        log.info("CustomerService.create() - Creating: {} " + customer);
        
        // Check to make sure the data fits with the parameters in the Flight model and passes validation.
        validator.validateCustomer(customer);

        // Write the Flight to the database.
        return customerRepository.create(customer);
    }

    /**
     * <p>Updates an existing Customer object in the application database with the provided Customer object.<p/>
     *
     * <p>Validates the data in the provided Customer object using a CustomerValidator object.<p/>
     *
     * @param Customer The Customer object to be passed as an update to the application database
     * @return The Customer object that has been successfully updated in the application database
     * @throws ConstraintViolationException, ValidationException, Exception
     */
    public Customer update(Customer customer) throws Exception {
        log.info("FlightService.update() - Updating " + customer.getId());

        // Check to make sure the data fits with the parameters in the Flight model and passes validation.
        validator.validateCustomer(customer);

        // Either update the Flight or add it if it can't be found.
        return customerRepository.update(customer);
    }
    /**
     * <p>Deletes the provided Customer object from the application database if found there.<p/>
     *
     * @param Customer The Customer object to be removed from the application database
     * @return The Customer object that has been successfully removed from the application database; or null
     * @throws Exception
     */
    public Customer delete(Customer customer) throws Exception {
        log.info("delete() - Deleting " + customer.toString());

        Customer deletedCustomer = null;

        if (customer.getId() != null) {
        	deletedCustomer = customerRepository.delete(customer);
        } else {
            log.info("delete() - No ID was found so can't Delete.");
        }

        return deletedCustomer;
    }
}
