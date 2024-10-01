package shop.shop.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import shop.shop.user.entity.User;
import shop.shop.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
@Log4j2
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User createUser(User user) {
        log.info("Encoding password for user: {}", user.getUsername());

        // 입력받은 비밀번호를 BCrypt로 인코딩
        String encodedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);

        // 사용자 정보를 DB에 저장
        User savedUser = userRepository.save(user);
        log.info("User {} saved successfully", savedUser.getUsername());

        return savedUser;
    }
}


