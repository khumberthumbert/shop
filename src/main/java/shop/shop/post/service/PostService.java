package shop.shop.post.service;

import shop.shop.post.entity.Post;

import java.util.List;
import java.util.Optional;

public interface PostService {
    Post createPost(Post post);

    List<Post> getAllPosts();

    Optional<Post> getPostById(Long id);

    Post updatePost(Long id, Post post);

    void deletePost(Long id);

}
