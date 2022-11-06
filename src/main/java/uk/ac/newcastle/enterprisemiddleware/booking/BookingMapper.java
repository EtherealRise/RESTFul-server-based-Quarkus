package uk.ac.newcastle.enterprisemiddleware.booking;

import java.util.List;

import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "cdi")
public interface BookingMapper {

	List<Booking> toDomainList(List<BookingEntity> entities);

	Booking toDomain(BookingEntity entity);

	@InheritInverseConfiguration(name = "toDomain")
	BookingEntity toEntity(Booking domain);

	void updateEntityFromDomain(Booking domain, @MappingTarget BookingEntity entity);

	void updateDomainFromEntity(BookingEntity entity, @MappingTarget Booking domain);

}
