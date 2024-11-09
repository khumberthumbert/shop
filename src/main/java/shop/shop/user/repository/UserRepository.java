package shop.shop.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import shop.shop.user.entity.UserEntity;

public interface UserRepository extends JpaRepository<UserEntity, Integer> {

    Boolean existsByUsername(String username);

    //username을 받아 DB 테이블에서 회원을 조회하는 메서드
    UserEntity findByUsername(String username);

    // username으로 사용자 찾기
    @Query("SELECT u.id FROM UserEntity u WHERE u.username = :username")
    Integer findUserIdByUsername(@Param("username") String username);
}

