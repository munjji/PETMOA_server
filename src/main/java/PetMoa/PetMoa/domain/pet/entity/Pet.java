package PetMoa.PetMoa.domain.pet.entity;

import PetMoa.PetMoa.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pets")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Pet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PetType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PetSize size;

    private Integer age;

    private Double weight;

    @Column(length = 50)
    private String breed;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    // MedicalRecord는 Task #11에서 구현 예정
    @Transient
    private List<Object> medicalRecords = new ArrayList<>();

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public Pet(String name, PetType type, PetSize size, Integer age, Double weight, String breed, User owner) {
        validateFields(name, type, size, age, weight, owner);

        this.name = name;
        this.type = type;
        this.size = size;
        this.age = age;
        this.weight = weight;
        this.breed = breed;
        this.owner = owner;
        this.createdAt = LocalDateTime.now();
    }

    private void validateFields(String name, PetType type, PetSize size, Integer age, Double weight, User owner) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("이름은 필수입니다.");
        }

        if (type == null) {
            throw new IllegalArgumentException("종류는 필수입니다.");
        }

        if (size == null) {
            throw new IllegalArgumentException("크기는 필수입니다.");
        }

        if (owner == null) {
            throw new IllegalArgumentException("소유자는 필수입니다.");
        }

        if (age != null && age < 0) {
            throw new IllegalArgumentException("나이는 0 이상이어야 합니다.");
        }

        if (weight != null && weight <= 0) {
            throw new IllegalArgumentException("몸무게는 0보다 커야 합니다.");
        }
    }

    public void updateInfo(String name, PetSize size, Integer age, Double weight, String breed) {
        if (name != null && !name.isBlank()) {
            this.name = name;
        }
        if (size != null) {
            this.size = size;
        }
        if (age != null) {
            if (age < 0) {
                throw new IllegalArgumentException("나이는 0 이상이어야 합니다.");
            }
            this.age = age;
        }
        if (weight != null) {
            if (weight <= 0) {
                throw new IllegalArgumentException("몸무게는 0보다 커야 합니다.");
            }
            this.weight = weight;
        }
        if (breed != null) {
            this.breed = breed;
        }
    }
}
