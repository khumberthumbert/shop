package shop.shop.users;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import shop.shop.user.entity.User;
import shop.shop.user.repository.UserRepository;
import shop.shop.user.service.UserService;

import java.util.Optional;

@SpringBootTest
@Transactional
public class UserTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("유저 생성")
    @Transactional
    @Rollback(value = false)
    public void 유저_생성() throws Exception {
        //given
        User user = new User();
        user.setUsername("admin");
        user.setPassword("1234");
        user.setRole("admin");

        //when
        User createUser = userService.createUser(user);
        
        //then
        Optional<User> result = userRepository.findById(createUser.getId());
        Assertions.assertThat(result.get().getUsername()).isEqualTo(user.getUsername());
    }
}
