package shop.shop.user.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import shop.shop.user.entity.User;
import shop.shop.user.repository.UserRepository;

import java.util.Collections;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    CommandLineRunner initData() {
        return args -> {
            if (userRepository.findByUsername("admin").isEmpty()) {
                // 새로운 사용자 생성
                User adminUser = new User();
                adminUser.setUsername("admin");
                adminUser.setPassword(passwordEncoder.encode("1234"));
                adminUser.setRoles("ROLE_ADMIN");

                // 사용자 저장
                userRepository.save(adminUser);
                System.out.println("Admin 계정이 생성되었습니다: admin/1234");
            } else {
                System.out.println("Admin 계정이 이미 존재합니다.");
            }
        };
    }
}
