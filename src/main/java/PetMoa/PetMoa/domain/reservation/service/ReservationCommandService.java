package PetMoa.PetMoa.domain.reservation.service;

import PetMoa.PetMoa.domain.hospital.entity.TimeSlot;
import PetMoa.PetMoa.domain.hospital.service.TimeSlotQueryService;
import PetMoa.PetMoa.domain.notification.dto.NotificationEvent;
import PetMoa.PetMoa.domain.notification.dto.NotificationEventType;
import PetMoa.PetMoa.domain.notification.publisher.NotificationEventPublisher;
import PetMoa.PetMoa.domain.pet.entity.Pet;
import PetMoa.PetMoa.domain.pet.service.PetQueryService;
import PetMoa.PetMoa.domain.reservation.dto.CancellationResult;
import PetMoa.PetMoa.domain.reservation.dto.ReservationCreateRequest;
import PetMoa.PetMoa.domain.reservation.dto.TaxiRequest;
import PetMoa.PetMoa.domain.reservation.entity.*;
import PetMoa.PetMoa.domain.reservation.repository.HospitalReservationRepository;
import PetMoa.PetMoa.domain.reservation.repository.ReservationRepository;
import PetMoa.PetMoa.domain.reservation.repository.TaxiReservationRepository;
import PetMoa.PetMoa.domain.taxi.entity.PetTaxi;
import PetMoa.PetMoa.domain.taxi.service.PetTaxiQueryService;
import PetMoa.PetMoa.domain.user.entity.User;
import PetMoa.PetMoa.domain.user.service.UserQueryService;
import PetMoa.PetMoa.global.exception.ForbiddenException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class ReservationCommandService {

    private final ReservationRepository reservationRepository;
    private final HospitalReservationRepository hospitalReservationRepository;
    private final TaxiReservationRepository taxiReservationRepository;
    private final EntityManager entityManager;

    private final UserQueryService userQueryService;
    private final PetQueryService petQueryService;
    private final TimeSlotQueryService timeSlotQueryService;
    private final PetTaxiQueryService petTaxiQueryService;
    private final NotificationEventPublisher notificationEventPublisher;

    private static final int DEPOSIT_AMOUNT = 10000;

    public Reservation createReservation(Long userId, ReservationCreateRequest request) {
        // 1. 사용자와 펫 조회
        User user = userQueryService.getUserById(userId);
        Pet pet = petQueryService.getPetById(request.petId());
        validatePetOwnership(userId, pet);

        // 2. TimeSlot 조회 및 정원 체크
        TimeSlot timeSlot = timeSlotQueryService.getTimeSlotById(request.hospitalReservation().timeSlotId());
        if (!timeSlot.hasAvailableSpace()) {
            throw new IllegalStateException("해당 시간대의 예약이 마감되었습니다.");
        }

        // 3. Reservation 생성
        Reservation reservation = Reservation.builder()
                .user(user)
                .pet(pet)
                .build();
        reservationRepository.save(reservation);

        // 4. HospitalReservation 생성
        HospitalReservation hospitalReservation = HospitalReservation.builder()
                .reservation(reservation)
                .veterinarian(timeSlot.getVeterinarian())
                .timeSlot(timeSlot)
                .visitType(VisitType.FIRST_VISIT)
                .symptoms(request.hospitalReservation().symptomDescription())
                .depositAmount(DEPOSIT_AMOUNT)
                .build();
        hospitalReservationRepository.save(hospitalReservation);

        // 5. TimeSlot 예약 카운트 증가
        timeSlot.incrementReservations();

        // 6. 택시 요청이 있으면 자동 배차
        if (request.taxiRequests() != null && !request.taxiRequests().isEmpty()) {
            for (TaxiRequest taxiRequest : request.taxiRequests()) {
                PetTaxi taxi = assignTaxi(pet, taxiRequest.scheduledTime());

                TaxiReservation taxiReservation = TaxiReservation.builder()
                        .reservation(reservation)
                        .taxi(taxi)
                        .pickupAddress(taxiRequest.pickupAddress())
                        .dropoffAddress(taxiRequest.dropoffAddress())
                        .pickupTime(taxiRequest.scheduledTime())
                        .type(taxiRequest.type())
                        .build();
                taxiReservationRepository.save(taxiReservation);

                // 택시 배차 이벤트 발행
                publishTaxiAssignedEvent(taxiReservation, user);
            }
        }

        // 영속성 컨텍스트를 flush하고 clear하여 DB에서 새로 조회
        entityManager.flush();
        entityManager.clear();

        Reservation savedReservation = reservationRepository.findByIdWithDetails(reservation.getId());

        // 예약 생성 이벤트 발행
        publishReservationCreatedEvent(savedReservation);

        return savedReservation;
    }

    public CancellationResult cancelReservation(Long userId, Long reservationId) {
        Reservation reservation = reservationRepository.findByIdWithDetails(reservationId);
        if (reservation == null) {
            throw new EntityNotFoundException("예약을 찾을 수 없습니다. id=" + reservationId);
        }

        validateReservationOwnership(userId, reservation);

        if (!reservation.canCancel()) {
            throw new IllegalStateException("취소할 수 없는 상태입니다.");
        }

        // 환불 비율 계산 (취소 처리 전에 동일 시점으로 계산)
        LocalDateTime now = LocalDateTime.now();
        int refundRate = calculateRefundRate(reservation, now);

        // 예약 취소
        reservation.cancel();

        // TimeSlot 예약 카운트 감소
        HospitalReservation hospitalReservation = reservation.getHospitalReservation();
        if (hospitalReservation != null) {
            hospitalReservation.getTimeSlot().decrementReservations();
        }

        // 예약 취소 이벤트 발행
        publishReservationCancelledEvent(reservation);

        return CancellationResult.of(reservation, refundRate);
    }

    private int calculateRefundRate(Reservation reservation, LocalDateTime referenceTime) {
        HospitalReservation hr = reservation.getHospitalReservation();
        if (hr == null) {
            return 0;
        }

        LocalDateTime reservationTime = hr.getTimeSlot().getDate()
                .atTime(hr.getTimeSlot().getStartTime());

        long hoursUntilReservation = ChronoUnit.HOURS.between(referenceTime, reservationTime);

        if (hoursUntilReservation >= 24) {
            return 100;
        } else if (hoursUntilReservation >= 12) {
            return 50;
        } else {
            return 0;
        }
    }

    private PetTaxi assignTaxi(Pet pet, LocalDateTime pickupTime) {
        List<PetTaxi> availableTaxis = petTaxiQueryService.getAvailableTaxisForPetSizeAndTime(pet.getSize(), pickupTime);

        if (availableTaxis.isEmpty()) {
            throw new IllegalStateException("해당 시간대에 배차 가능한 택시가 없습니다.");
        }

        return availableTaxis.get(0);
    }

    private void validatePetOwnership(Long userId, Pet pet) {
        if (!pet.getOwner().getId().equals(userId)) {
            throw new ForbiddenException("해당 반려동물의 소유자가 아닙니다.");
        }
    }

    private void validateReservationOwnership(Long userId, Reservation reservation) {
        if (!reservation.getUser().getId().equals(userId)) {
            throw new ForbiddenException("해당 예약의 소유자가 아닙니다.");
        }
    }

    private void publishReservationCreatedEvent(Reservation reservation) {
        Map<String, Object> payload = Map.of(
                "reservationId", reservation.getId(),
                "petName", reservation.getPet().getName(),
                "hospitalName", reservation.getHospitalReservation().getVeterinarian().getHospital().getName(),
                "reservationDate", reservation.getHospitalReservation().getTimeSlot().getDate().toString(),
                "reservationTime", reservation.getHospitalReservation().getTimeSlot().getStartTime().toString()
        );

        NotificationEvent event = NotificationEvent.of(
                NotificationEventType.RESERVATION_CREATED,
                reservation.getUser(),
                payload
        );

        notificationEventPublisher.publish(event);
    }

    private void publishReservationCancelledEvent(Reservation reservation) {
        Map<String, Object> payload = Map.of(
                "reservationId", reservation.getId(),
                "petName", reservation.getPet().getName()
        );

        NotificationEvent event = NotificationEvent.of(
                NotificationEventType.RESERVATION_CANCELLED,
                reservation.getUser(),
                payload
        );

        notificationEventPublisher.publish(event);
    }

    private void publishTaxiAssignedEvent(TaxiReservation taxiReservation, User user) {
        Map<String, Object> payload = Map.of(
                "taxiReservationId", taxiReservation.getId(),
                "driverName", taxiReservation.getTaxi().getDriverName(),
                "licensePlate", taxiReservation.getTaxi().getLicensePlate(),
                "pickupTime", taxiReservation.getPickupTime().toString(),
                "pickupAddress", taxiReservation.getPickupAddress(),
                "dropoffAddress", taxiReservation.getDropoffAddress()
        );

        NotificationEvent event = NotificationEvent.of(
                NotificationEventType.TAXI_ASSIGNED,
                user,
                payload
        );

        notificationEventPublisher.publish(event);
    }
}
