package uk.ac.newcastle.enterprisemiddleware.customer;

import java.util.HashSet;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.NoResultException;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;
import javax.validation.Validator;

/**
 * <p>This class provides methods to check Customer objects against arbitrary requirements.</p>
 *
 * @author Joshua Wilson
 * @see Customer
 * @see CustomerRepository
 * @see javax.validation.Validator
 */
@ApplicationScoped
public class CustomerValidator {
    @Inject
    Validator validator;

    @Inject
    CustomerRepository customerRepository;

    /**
     * <p>Validates the given Customer object and throws validation exceptions based on the type of error. If the error is standard
     * bean validation errors then it will throw a ConstraintValidationException with the set of the constraints violated.<p/>
     *
     *
     * <p>If the error is caused because an existing Customer with the same email is registered it throws a regular validation
     * exception so that it can be interpreted separately.</p>
     *
     *
     * @param Customer The Customer object to be validated
     * @throws ConstraintViolationException If Bean Validation errors exist
     * @throws ValidationException If Customer with the same email already exists
     */
    void validateCustomer(Customer Customer) throws ConstraintViolationException, ValidationException {
        // Create a bean validator and check for issues.
        Set<ConstraintViolation<Customer>> violations = validator.validate(Customer);

        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(new HashSet<ConstraintViolation<?>>(violations));
        }

        // Check the uniqueness of the email address
        if (emailAlreadyExists(Customer.getEmail(), Customer.getId())) {
            throw new UniqueEmailException("Unique Email Violation");
        }
    }

    /**
     * <p>Checks if a Customer with the same email address is already registered. This is the only way to easily capture the
     * "@UniqueConstraint(columnNames = "email")" constraint from the Customer class.</p>
     *
     * <p>Since Update will being using an email that is already in the database we need to make sure that it is the email
     * from the record being updated.</p>
     *
     * @param email The email to check is unique
     * @param id The user id to check the email against if it was found
     * @return boolean which represents whether the email was found, and if so if it belongs to the user with id
     */
    boolean emailAlreadyExists(String email, Long id) {
        Customer Customer = null;
        Customer CustomerWithID = null;
        try {
            Customer = customerRepository.findByEmail(email);
        } catch (NoResultException e) {
            // ignore
        }

        if (Customer != null && id != null) {
            try {
                CustomerWithID = customerRepository.findById(id);
                if (CustomerWithID != null && CustomerWithID.getEmail().equals(email)) {
                    Customer = null;
                }
            } catch (NoResultException e) {
                // ignore
            }
        }
        return Customer != null;
    }
}

