package uk.ac.newcastle.enterprisemiddleware.travelagentbooking;

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
public class TABookingService {

	@Inject
	Logger log;

	@Inject
	TABookingMapper tabookingMapper;

	@Inject
	TABookingRepository tabookingRepository;

	// As we are dealing with other REST resources, use their validator instead of
	// reinventing the wheel
	// public void validateTravleAgent(TravleAgent tabooking) {
	//
	// }

	public List<TABooking> findAll() {
		return tabookingMapper.toDomainList(tabookingRepository.findAll());
	}

	public Optional<TABooking> findById(Integer id) {
		return tabookingRepository.findById(id).map(tabookingMapper::toDomain);
	}

	@Transactional
	public void create(TABooking tabooking) {
		log.info("TravleAgentService.create() - Creating: {} " + tabooking);

		TABookingEntity entity = tabookingMapper.toEntity(tabooking);
		tabookingRepository.create(entity);
		tabookingMapper.updateDomainFromEntity(entity, tabooking);
	}

	@Transactional
	public void update(Integer id) {
		log.info("TravleAgentService.update() - Updating " + id);

		TABookingEntity entity = tabookingRepository.findById(id)
				.orElseThrow(() -> new ServiceException("No TravleAgent found for tabookingId[%s]" + id));
		tabookingRepository.update(entity);
	}

	@Transactional
	public void delete(Integer id) {
		log.info("TravleAgentService.delete() - Deleting " + id);

		TABookingEntity entity = tabookingRepository.findById(id)
				.orElseThrow(() -> new ServiceException("No TravleAgent found for tabookingId[%s]" + id));
		tabookingRepository.delete(entity);
	}
}
