package shop.shop.user.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Arrays;
import java.util.stream.Collectors;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import org.springframework.stereotype.Service;
import shop.shop.user.entity.User;
import shop.shop.user.repository.UserRepository;

/**
 * UserDetailsService : Spring Security에서 사용자 인증을 처리할 때 사용되는 인터페이스
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /*
    loadUserByUsername 메서드를 통해 사용자 정보를 조회하고 인증을 처리.
    map()메서드 : Optional 메서드로, 사용자가 존재하는 경우 해당 사용자의 정보 "UserDetails"로 변환한다.
    User.withUsername() : Spring Security의 빌더 패턴을 이용해서 UserDetails 객체 생성.
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                Arrays.stream(user.getRoles().split(","))
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList())
        );
    }
}

