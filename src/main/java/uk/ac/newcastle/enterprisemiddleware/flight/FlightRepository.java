package uk.ac.newcastle.enterprisemiddleware.flight;

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
public class FlightRepository {

	@Inject
	Logger log;

	@Inject
	EntityManager em;

	List<FlightEntity> findAll() {
		TypedQuery<FlightEntity> query = em.createNamedQuery("Flight.findAll", FlightEntity.class);
		return query.getResultList();
	}

	Optional<FlightEntity> findById(Integer id) {
		return Optional.ofNullable(em.find(FlightEntity.class, Long.valueOf(id)));
	}

	Optional<FlightEntity> findByNumber(String number) {
		TypedQuery<FlightEntity> query = em.createNamedQuery("Flight.findByNumber", FlightEntity.class)
				.setParameter("number", number);
		return query.getResultStream().findFirst();
	}

	FlightEntity create(FlightEntity Flight) {
		log.info("FlightRepository.create() - Creating " + Flight.getNumber());

		em.persist(Flight);
		return Flight;
	}

	FlightEntity update(FlightEntity Flight) {
		log.info("FlightRepository.update() - Updating " + Flight.getNumber());

		em.merge(Flight);
		return Flight;
	}

	void delete(FlightEntity Flight) {
		log.info("FlightRepository.delete() - Deleting " + Flight.getNumber());

		em.remove(em.merge(Flight));
	}
}
