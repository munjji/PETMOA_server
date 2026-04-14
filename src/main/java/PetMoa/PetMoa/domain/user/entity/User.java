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

    @Column(unique = true, length = 20)
    private String phoneNumber;

    @Column(length = 200)
    private String address;

    @Column(length = 100)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private AuthProvider provider;

    @Column(unique = true)
    private String providerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role = Role.USER;

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
    public User(String name, String phoneNumber, String address, String email,
                AuthProvider provider, String providerId, Role role) {
        validateFields(name, phoneNumber, address, email, provider);

        this.name = name;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.email = email;
        this.provider = provider;
        this.providerId = providerId;
        this.role = (role != null) ? role : Role.USER;
        this.createdAt = LocalDateTime.now();
    }

    private void validateFields(String name, String phoneNumber, String address, String email, AuthProvider provider) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("이름은 필수입니다.");
        }

        // 소셜 로그인이 아닌 경우에만 전화번호, 주소 필수
        if (provider == null) {
            if (phoneNumber == null || phoneNumber.isBlank()) {
                throw new IllegalArgumentException("전화번호는 필수입니다.");
            }

            if (!PHONE_PATTERN.matcher(phoneNumber).matches()) {
                throw new IllegalArgumentException("전화번호 형식이 올바르지 않습니다. (예: 010-1234-5678 또는 01012345678)");
            }

            if (address == null || address.isBlank()) {
                throw new IllegalArgumentException("주소는 필수입니다.");
            }
        } else {
            // 소셜 로그인인 경우에도 전화번호가 있으면 형식 검증
            if (phoneNumber != null && !phoneNumber.isBlank() && !PHONE_PATTERN.matcher(phoneNumber).matches()) {
                throw new IllegalArgumentException("전화번호 형식이 올바르지 않습니다. (예: 010-1234-5678 또는 01012345678)");
            }
        }

        if (email != null && !email.isBlank() && !EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("이메일 형식이 올바르지 않습니다.");
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // 정보 수정
    public void updateInfo(String phoneNumber, String address) {
        if (phoneNumber != null && !phoneNumber.isBlank()) {
            if (!PHONE_PATTERN.matcher(phoneNumber).matches()) {
                throw new IllegalArgumentException("전화번호 형식이 올바르지 않습니다. (예: 010-1234-5678 또는 01012345678)");
            }
            this.phoneNumber = phoneNumber;
        }
        if (address != null && !address.isBlank()) {
            this.address = address;
        }
    }

    // 소셜 로그인 사용자 프로필 업데이트
    public void updateSocialProfile(String name, String email) {
        if (name != null && !name.isBlank()) {
            this.name = name;
        }
        if (email != null && !email.isBlank()) {
            if (!EMAIL_PATTERN.matcher(email).matches()) {
                throw new IllegalArgumentException("이메일 형식이 올바르지 않습니다.");
            }
            this.email = email;
        }
    }

    // 연관관계 편의 메서드
    public void addPet(Pet pet) {
        this.pets.add(pet);
    }

    public void addReservation(Reservation reservation) {
        this.reservations.add(reservation);
    }
}
