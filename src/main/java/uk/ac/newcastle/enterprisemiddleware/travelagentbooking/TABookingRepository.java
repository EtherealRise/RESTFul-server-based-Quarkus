package uk.ac.newcastle.enterprisemiddleware.travelagentbooking;

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
public class TABookingRepository {

	@Inject
	Logger log;

	@Inject
	EntityManager em;

	List<TABookingEntity> findAll() {
		TypedQuery<TABookingEntity> query = em.createNamedQuery("TABooking.findAll", TABookingEntity.class);
		return query.getResultList();
	}

	Optional<TABookingEntity> findById(Integer id) {
		return Optional.ofNullable(em.find(TABookingEntity.class, Long.valueOf(id)));
	}

	TABookingEntity create(TABookingEntity booking) {
		log.info("TravleAgentRepository.create() - Creating " + booking.getId());

		em.persist(booking);
		return booking;
	}

	TABookingEntity update(TABookingEntity booking) {
		log.info("TravleAgentRepository.update() - Updating " + booking.getId());

		em.merge(booking);
		return booking;
	}

	TABookingEntity delete(TABookingEntity booking) {
		log.info("TravleAgentRepository.delete() - Deleting " + booking.getId());

		em.remove(em.merge(booking));
		return booking;
	}
}
