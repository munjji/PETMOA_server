package PetMoa.PetMoa.domain.taxi.repository;

import PetMoa.PetMoa.domain.taxi.entity.PetTaxi;
import PetMoa.PetMoa.domain.taxi.entity.TaxiStatus;
import PetMoa.PetMoa.domain.taxi.entity.VehicleSize;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface PetTaxiRepository extends JpaRepository<PetTaxi, Long> {

    List<PetTaxi> findByStatus(TaxiStatus status);

    List<PetTaxi> findByStatusAndVehicleSizeIn(TaxiStatus status, Collection<VehicleSize> vehicleSizes);
}
