package PetMoa.PetMoa.domain.user.repository;

import PetMoa.PetMoa.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
