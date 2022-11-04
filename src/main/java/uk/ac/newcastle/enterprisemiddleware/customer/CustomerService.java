package uk.ac.newcastle.enterprisemiddleware.customer;

import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;

import org.hibernate.service.spi.ServiceException;

import uk.ac.newcastle.enterprisemiddleware.booking.Booking;

/**
 * This Service assumes the Control responsibility in the ECB pattern.
 *
 * The validation is done here so that it may be used by other Boundary
 * Resources. Other Business Logic would go here as well.
 *
 * There are no access modifiers on the methods, making them 'package' scope.
 * They should only be accessed by a Boundary / Web Service class with public
 * methods.
 */
@ApplicationScoped
public class CustomerService {

	@Inject
	Logger log;

	@Inject
	CustomerValidator customerValidator;

	@Inject
	CustomerMapper customerMapper;

	@Inject
	CustomerRepository customerRepository;

	// wrapper to demonstrate how validator works, it's really as same as @valid.
	// Keep in mind we do not want to expose internal service error state to REST
	// API, especially not the validated value contained in the violation object.
	// But for simplify, let's re-throw it right now.
	public void validateCustomer(Customer customer) {
		customerValidator.validate(customerMapper.toEntity(customer));
	}

	public List<Customer> findAll() {
		return customerMapper.toDomainList(customerRepository.findAll());
	}

	public Optional<Customer> findById(Integer id) {
		return customerRepository.findById(id).map(customerMapper::toDomain);
	}

	public Optional<Customer> findByEmail(String email) {
		return customerRepository.findByEmail(email).map(customerMapper::toDomain);
	}

	public List<Booking> getBooking(Integer id) {
		return customerRepository.findById(id).map(CustomerEntity::getBooking).orElse(List.of());
	}

	@Transactional
	public void create(Customer customer) {
		log.info("CustomerService.create() - Creating: {} " + customer);

		CustomerEntity entity = customerMapper.toEntity(customer);
		customerRepository.create(entity);
		customerMapper.updateDomainFromEntity(entity, customer);
	}

	@Transactional
	public void update(Customer customer) {
		log.info("CustomerService.update() - Updating " + customer);

		CustomerEntity entity = customerRepository.findById(customer.getId())
				.orElseThrow(() -> new ServiceException("No Customer found for customerId[%s]" + customer.getId()));
		customerMapper.updateEntityFromDomain(customer, entity);
		customerRepository.update(entity);
		customerMapper.updateDomainFromEntity(entity, customer);
	}

	@Transactional
	public void delete(Integer id) {
		log.info("CustomerService.delete() - Deleting " + id);

		CustomerEntity entity = customerRepository.findById(id)
				.orElseThrow(() -> new ServiceException("No Customer found for customerId[%s]" + id));
		customerRepository.delete(entity);
	}
}