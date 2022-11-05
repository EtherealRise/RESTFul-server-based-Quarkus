package uk.ac.newcastle.enterprisemiddleware.flight;

import java.util.List;

import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "cdi")
public interface FlightMapper {

	List<Flight> toDomainList(List<FlightEntity> entities);

	Flight toDomain(FlightEntity entity);

	@InheritInverseConfiguration(name = "toDomain")
	FlightEntity toEntity(Flight domain);

	void updateEntityFromDomain(Flight domain, @MappingTarget FlightEntity entity);

	void updateDomainFromEntity(FlightEntity entity, @MappingTarget Flight domain);

}