package PetMoa.PetMoa.domain.reservation.entity;

import PetMoa.PetMoa.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

// 임시 클래스 - Task #7에서 구체적으로 구현 예정
@Entity
@Table(name = "reservations")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
}
