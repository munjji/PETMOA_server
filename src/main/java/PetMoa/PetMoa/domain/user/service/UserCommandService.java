package PetMoa.PetMoa.domain.user.service;

import PetMoa.PetMoa.domain.user.dto.UserCreateRequest;
import PetMoa.PetMoa.domain.user.dto.UserUpdateRequest;
import PetMoa.PetMoa.domain.user.entity.User;
import PetMoa.PetMoa.domain.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserCommandService {

    private final UserRepository userRepository;

    public User createUser(UserCreateRequest request) {
        if (userRepository.existsByPhoneNumber(request.phoneNumber())) {
            throw new IllegalArgumentException("이미 등록된 전화번호입니다.");
        }

        User user = User.builder()
                .name(request.name())
                .phoneNumber(request.phoneNumber())
                .address(request.address())
                .email(request.email())
                .build();

        return userRepository.save(user);
    }

    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다. id=" + id));
        userRepository.delete(user);
    }

    public User updateUser(Long id, UserUpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다. id=" + id));
        user.updateInfo(request.phoneNumber(), request.address());
        return user;
    }
}
