package shop.shop.post.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import shop.shop.post.entity.Post;
import shop.shop.post.service.PostService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    // Create a new post (로그인된 사용자만 가능)
    @PostMapping
    public ResponseEntity<Post> createPost(Authentication authentication, @RequestBody Post post) {
        String username = authentication.getName();  // 로그인된 사용자의 이름을 가져옴
        post.setAuthor(username);  // 작성자를 설정 (Post 엔티티에 author 필드가 있다고 가정)
        Post newPost = postService.createPost(post);
        return new ResponseEntity<>(newPost, HttpStatus.CREATED);
    }

    // Get all posts (모든 사용자가 가능)
    @GetMapping
    public ResponseEntity<List<Post>> getAllPosts() {
        List<Post> posts = postService.getAllPosts();
        return new ResponseEntity<>(posts, HttpStatus.OK);
    }

    // Get a post by id (모든 사용자가 가능)
    @GetMapping("/{id}")
    public ResponseEntity<Post> getPostById(@PathVariable Long id) {
        Optional<Post> post = postService.getPostById(id);
        return post.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    // Update a post by id (로그인된 사용자만 가능)
    @PostMapping("/update/{id}")
    public ResponseEntity<Post> updatePost(Authentication authentication, @PathVariable Long id, @RequestBody Post postDetails) {
        String username = authentication.getName();  // 로그인된 사용자의 이름을 가져옴
        Optional<Post> existingPost = postService.getPostById(id);

        if (existingPost.isPresent() && existingPost.get().getAuthor().equals(username)) {
            Post updatedPost = postService.updatePost(id, postDetails);
            return new ResponseEntity<>(updatedPost, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);  // 작성자만 수정할 수 있음
        }
    }

    // Delete a post by id (로그인된 사용자만 가능)
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deletePost(Authentication authentication, @PathVariable Long id) {
        String username = authentication.getName();  // 로그인된 사용자의 이름을 가져옴
        Optional<Post> existingPost = postService.getPostById(id);

        if (existingPost.isPresent() && existingPost.get().getAuthor().equals(username)) {
            postService.deletePost(id);
            return new ResponseEntity<>("Post deleted", HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);  // 작성자만 삭제할 수 있음
        }
    }
}
