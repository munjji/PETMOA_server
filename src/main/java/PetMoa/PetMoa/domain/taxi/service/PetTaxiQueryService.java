package PetMoa.PetMoa.domain.taxi.service;

import PetMoa.PetMoa.domain.pet.entity.PetSize;
import PetMoa.PetMoa.domain.reservation.repository.TaxiReservationRepository;
import PetMoa.PetMoa.domain.taxi.dto.TaxiAvailabilityResponse;
import PetMoa.PetMoa.domain.taxi.entity.PetTaxi;
import PetMoa.PetMoa.domain.taxi.entity.TaxiStatus;
import PetMoa.PetMoa.domain.taxi.entity.VehicleSize;
import PetMoa.PetMoa.domain.taxi.repository.PetTaxiRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PetTaxiQueryService {

    private static final int RESERVATION_BUFFER_HOURS = 1;

    private final PetTaxiRepository petTaxiRepository;
    private final TaxiReservationRepository taxiReservationRepository;

    public PetTaxi getPetTaxiById(Long id) {
        return petTaxiRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("펫택시를 찾을 수 없습니다. id=" + id));
    }

    public List<PetTaxi> getAvailableTaxis() {
        return petTaxiRepository.findByStatus(TaxiStatus.AVAILABLE);
    }

    public List<PetTaxi> getAvailableTaxisForPetSize(PetSize petSize) {
        Set<VehicleSize> allowedVehicleSizes = getAllowedVehicleSizes(petSize);
        return petTaxiRepository.findByStatusAndVehicleSizeIn(TaxiStatus.AVAILABLE, allowedVehicleSizes);
    }

    /**
     * 펫택시 이용 가능 여부 확인 (호출 방식)
     * @param petSize 반려동물 크기
     * @param pickupTime 픽업 희망 시간
     * @param pickupAddress 출발지 주소
     * @return 이용 가능 여부, 예상 요금, 예상 도착 시간, 가용 차량 수
     */
    public TaxiAvailabilityResponse checkAvailability(PetSize petSize, LocalDateTime pickupTime, String pickupAddress) {
        List<PetTaxi> availableTaxis = getAvailableTaxisForPetSize(petSize);

        if (availableTaxis.isEmpty()) {
            return TaxiAvailabilityResponse.notAvailable();
        }

        // 해당 시간대에 이미 예약된 택시 제외
        Set<Long> reservedTaxiIds = getReservedTaxiIds(pickupTime);
        List<PetTaxi> actuallyAvailableTaxis = availableTaxis.stream()
                .filter(taxi -> !reservedTaxiIds.contains(taxi.getId()))
                .toList();

        if (actuallyAvailableTaxis.isEmpty()) {
            return TaxiAvailabilityResponse.notAvailable();
        }

        int estimatedFee = calculateEstimatedFee(pickupAddress);
        int estimatedMinutes = calculateEstimatedArrivalMinutes(pickupAddress);

        return TaxiAvailabilityResponse.of(actuallyAvailableTaxis.size(), estimatedFee, estimatedMinutes);
    }

    /**
     * 특정 시간대에 이미 예약된 택시 ID 조회
     * pickupTime 전후 RESERVATION_BUFFER_HOURS 시간 내 예약된 택시
     */
    private Set<Long> getReservedTaxiIds(LocalDateTime pickupTime) {
        LocalDateTime startTime = pickupTime.minusHours(RESERVATION_BUFFER_HOURS);
        LocalDateTime endTime = pickupTime.plusHours(RESERVATION_BUFFER_HOURS);
        return taxiReservationRepository.findReservedTaxiIdsBetween(startTime, endTime);
    }

    /**
     * 예상 요금 계산 (추후 거리 기반으로 구현)
     */
    private int calculateEstimatedFee(String pickupAddress) {
        // TODO: 실제 거리 기반 요금 계산 로직 구현
        // 현재는 기본 요금 반환
        return 15000;
    }

    /**
     * 예상 도착 시간 계산 (추후 거리 기반으로 구현)
     */
    private int calculateEstimatedArrivalMinutes(String pickupAddress) {
        // TODO: 실제 거리 기반 도착 시간 계산 로직 구현
        // 현재는 기본 시간 반환
        return 10;
    }

    private Set<VehicleSize> getAllowedVehicleSizes(PetSize petSize) {
        return switch (petSize) {
            case SMALL -> Set.of(VehicleSize.SMALL, VehicleSize.MEDIUM, VehicleSize.LARGE);
            case MEDIUM -> Set.of(VehicleSize.MEDIUM, VehicleSize.LARGE);
            case LARGE -> Set.of(VehicleSize.LARGE);
        };
    }
}
