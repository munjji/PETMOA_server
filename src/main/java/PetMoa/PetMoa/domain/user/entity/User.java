package PetMoa.PetMoa.domain.user.entity;

import PetMoa.PetMoa.domain.pet.entity.Pet;
import PetMoa.PetMoa.domain.reservation.entity.Reservation;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, unique = true, length = 20)
    private String phoneNumber;

    @Column(nullable = false, length = 200)
    private String address;

    @Column(length = 100)
    private String email;

    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL)
    private List<Pet> pets = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Reservation> reservations = new ArrayList<>();

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    // 전화번호 정규식: 010-1234-5678 또는 01012345678
    private static final Pattern PHONE_PATTERN = Pattern.compile("^01(?:0|1|[6-9])-?(?:\\d{3}|\\d{4})-?\\d{4}$");

    // 이메일 정규식
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    @Builder
    public User(String name, String phoneNumber, String address, String email) {
        validateFields(name, phoneNumber, address, email);

        this.name = name;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.email = email;
        this.createdAt = LocalDateTime.now();
    }

    private void validateFields(String name, String phoneNumber, String address, String email) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("이름은 필수입니다.");
        }

        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("전화번호는 필수입니다.");
        }

        if (!PHONE_PATTERN.matcher(phoneNumber).matches()) {
            throw new IllegalArgumentException("전화번호 형식이 올바르지 않습니다. (예: 010-1234-5678 또는 01012345678)");
        }

        if (address == null || address.trim().isEmpty()) {
            throw new IllegalArgumentException("주소는 필수입니다.");
        }

        if (email != null && !email.trim().isEmpty() && !EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("이메일 형식이 올바르지 않습니다.");
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // 연관관계 편의 메서드
    public void addPet(Pet pet) {
        this.pets.add(pet);
    }

    public void addReservation(Reservation reservation) {
        this.reservations.add(reservation);
    }
}
