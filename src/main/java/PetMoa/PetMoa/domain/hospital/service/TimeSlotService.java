package PetMoa.PetMoa.domain.hospital.service;

import PetMoa.PetMoa.domain.hospital.entity.TimeSlot;
import PetMoa.PetMoa.domain.hospital.repository.TimeSlotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TimeSlotService {

    private final TimeSlotRepository timeSlotRepository;

    public TimeSlot getTimeSlotById(Long id) {
        return timeSlotRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("타임슬롯을 찾을 수 없습니다. id=" + id));
    }

    public List<TimeSlot> getTimeSlotsByVeterinarianAndDate(Long veterinarianId, LocalDate date) {
        return timeSlotRepository.findByVeterinarianAndDate(veterinarianId, date);
    }

    public List<TimeSlot> getAvailableTimeSlots(Long veterinarianId, LocalDate date) {
        return timeSlotRepository.findAvailableSlots(veterinarianId, date);
    }
}
