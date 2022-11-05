package uk.ac.newcastle.enterprisemiddleware.flight;

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
public class FlightService {

	@Inject
	Logger log;

	@Inject
	FlightValidator flightValidator;

	@Inject
	FlightMapper flightMapper;

	@Inject
	FlightRepository flightRepository;

	// wrapper to demonstrate how validator works, it's really as same as @valid.
	// Keep in mind we do not want to expose internal service error state to REST
	// API, especially not the validated value contained in the violation object.
	// But for simplify, let's re-throw it right now.
	public void validateFlight(Flight flight) {
		flightValidator.validate(flightMapper.toEntity(flight));
	}

	public List<Flight> findAll() {
		return flightMapper.toDomainList(flightRepository.findAll());
	}

	public Optional<Flight> findById(Integer id) {
		return flightRepository.findById(id).map(flightMapper::toDomain);
	}

	public Optional<Flight> findByNumber(String number) {
		return flightRepository.findByNumber(number).map(flightMapper::toDomain);
	}

	public List<Booking> getBooking(Integer id) {
		return flightRepository.findById(id).map(FlightEntity::getBooking).orElse(List.of());
	}

	@Transactional
	public void create(Flight flight) {
		log.info("FlightService.create() - Creating: {} " + flight);

		FlightEntity entity = flightMapper.toEntity(flight);
		flightRepository.create(entity);
		flightMapper.updateDomainFromEntity(entity, flight);
	}

	@Transactional
	public void update(Integer id) {
		log.info("FlightService.update() - Updating " + id);

		FlightEntity entity = flightRepository.findById(id)
				.orElseThrow(() -> new ServiceException("No Flight found for flightId[%s]" + id));
		flightRepository.update(entity);
	}

	@Transactional
	public void delete(Integer id) {
		log.info("FlightService.delete() - Deleting " + id);

		FlightEntity entity = flightRepository.findById(id)
				.orElseThrow(() -> new ServiceException("No Flight found for flightId[%s]" + id));
		flightRepository.delete(entity);
	}
}
