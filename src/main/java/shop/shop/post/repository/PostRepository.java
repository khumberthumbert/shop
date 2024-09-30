package shop.shop.post.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import shop.shop.post.entity.Post;

public interface PostRepository extends JpaRepository<Post, Long> {
}
