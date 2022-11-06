package uk.ac.newcastle.enterprisemiddleware.booking;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import uk.ac.newcastle.enterprisemiddleware.flight.Flight;

/**
 * There are no access modifiers on the methods making them 'package' scope.
 * They should only be accessed by a service object.
 */

@ApplicationScoped
public class BookingRepository {

	@Inject
	Logger log;

	@Inject
	EntityManager em;

	List<BookingEntity> findAll() {
		TypedQuery<BookingEntity> query = em.createNamedQuery("Booking.findAll", BookingEntity.class);
		return query.getResultList();
	}

	List<BookingEntity> findByFlight(Flight flight) {
		TypedQuery<BookingEntity> query = em.createNamedQuery("Booking.findByFlight", BookingEntity.class)
				.setParameter("flight", flight);
		return query.getResultList();
	}

	List<BookingEntity> findByDate(Date d) {
		TypedQuery<BookingEntity> query = em.createNamedQuery("Booking.findByDate", BookingEntity.class)
				.setParameter("d", d);
		return query.getResultList();
	}

	Optional<BookingEntity> findById(Integer id) {
		return Optional.ofNullable(em.find(BookingEntity.class, Long.valueOf(id)));
	}

	BookingEntity create(BookingEntity booking) {
		log.info("BookingRepository.create() - Creating " + booking.getId());

		em.persist(booking);
		return booking;
	}

	BookingEntity update(BookingEntity booking) {
		log.info("BookingRepository.update() - Updating " + booking.getId());

		em.merge(booking);
		return booking;
	}

	BookingEntity delete(BookingEntity booking) {
		log.info("BookingRepository.delete() - Deleting " + booking.getId());

		em.remove(em.merge(booking));
		return booking;
	}
}
