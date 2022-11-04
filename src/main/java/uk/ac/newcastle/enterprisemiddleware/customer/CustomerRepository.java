package uk.ac.newcastle.enterprisemiddleware.customer;

import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

/**
 * There are no access modifiers on the methods making them 'package' scope.
 * They should only be accessed by a service object.
 */

@ApplicationScoped
public class CustomerRepository {

	@Inject
	Logger log;

	@Inject
	EntityManager em;

	List<CustomerEntity> findAll() {
		TypedQuery<CustomerEntity> query = em.createNamedQuery("Customer.findAll", CustomerEntity.class);
		return query.getResultList();
	}

	Optional<CustomerEntity> findById(Integer id) {
		return Optional.ofNullable(em.find(CustomerEntity.class, Long.valueOf(id)));
	}

	Optional<CustomerEntity> findByEmail(String email) {
		TypedQuery<CustomerEntity> query = em.createNamedQuery("Customer.findByEmail", CustomerEntity.class)
				.setParameter("email", email);
		return query.getResultStream().findFirst();
	}

	CustomerEntity create(CustomerEntity Customer) {
		log.info("CustomerRepository.create() - Creating " + Customer.getEmail());

		em.persist(Customer);
		return Customer;
	}

	CustomerEntity update(CustomerEntity Customer) {
		log.info("CustomerRepository.update() - Updating " + Customer.getEmail());

		em.merge(Customer);
		return Customer;
	}

	void delete(CustomerEntity Customer) {
		log.info("CustomerRepository.delete() - Deleting " + Customer.getEmail());

		em.remove(em.merge(Customer));
	}
}