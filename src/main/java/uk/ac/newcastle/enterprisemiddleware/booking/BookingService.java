package uk.ac.newcastle.enterprisemiddleware.booking;

import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;

import org.hibernate.service.spi.ServiceException;

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
public class BookingService {

	@Inject
	Logger log;

	@Inject
	BookingValidator bookingValidator;

	@Inject
	BookingMapper bookingMapper;

	@Inject
	BookingRepository bookingRepository;

	// wrapper to demonstrate how validator works, it's really as same as @valid.
	// Keep in mind we do not want to expose internal service error state to REST
	// API, especially not the validated value contained in the violation object.
	// But for simplify, let's re-throw it right now.
	public void validateBooking(Booking booking) {
		bookingValidator.validate(bookingMapper.toEntity(booking));
	}

	public List<Booking> findAll() {
		return bookingMapper.toDomainList(bookingRepository.findAll());
	}

	public Optional<Booking> findById(Integer id) {
		return bookingRepository.findById(id).map(bookingMapper::toDomain);
	}

	@Transactional
	public void create(Booking booking) {
		log.info("BookingService.create() - Creating: {} " + booking);

		BookingEntity entity = bookingMapper.toEntity(booking);
		bookingRepository.create(entity);
		bookingMapper.updateDomainFromEntity(entity, booking);
	}

	@Transactional
	public void update(Integer id, Booking booking) {
		log.info("BookingService.update() - Updating " + id);

		bookingRepository.findById(id)
				.orElseThrow(() -> new ServiceException("No Booking found for bookingId[%s]" + id));
		bookingRepository.update(bookingMapper.toEntity(booking));
	}

	@Transactional
	public void delete(Integer id) {
		log.info("BookingService.delete() - Deleting " + id);

		BookingEntity entity = bookingRepository.findById(id)
				.orElseThrow(() -> new ServiceException("No Booking found for bookingId[%s]" + id));
		bookingRepository.delete(entity);
	}
}
