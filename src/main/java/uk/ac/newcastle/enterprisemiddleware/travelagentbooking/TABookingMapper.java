package uk.ac.newcastle.enterprisemiddleware.travelagentbooking;

import java.util.List;

import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "cdi")
public interface TABookingMapper {

	List<TABooking> toDomainList(List<TABookingEntity> entities);

	TABooking toDomain(TABookingEntity entity);

	@InheritInverseConfiguration(name = "toDomain")
	@Mapping(source = "customer", target = "customer")
	TABookingEntity toEntity(TABooking domain);

	void updateEntityFromDomain(TABooking domain, @MappingTarget TABookingEntity entity);

	void updateDomainFromEntity(TABookingEntity entity, @MappingTarget TABooking domain);

}
