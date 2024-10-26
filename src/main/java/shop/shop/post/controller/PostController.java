package shop.shop.post.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import shop.shop.file.entity.FileMetadata;
import shop.shop.file.service.FileService;
import shop.shop.post.entity.Post;
import shop.shop.post.service.PostService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final FileService fileService;

    @GetMapping("/writePage")
    public String createPostPage() {
        return "post/postWritePage";
    }

    // Create a new post with multiple file attachments
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @ResponseBody
    public ResponseEntity<Post> createPost(
            Authentication authentication,
            @RequestParam("title") String title,
            @RequestParam("content") String content,
            @RequestParam(value = "files", required = false) List<MultipartFile> files) {

        String username = authentication.getName(); // 로그인된 사용자의 이름을 가져옴
        List<FileMetadata> fileMetadataList = new ArrayList<>();

        try {
            // 파일들이 있을 경우 업로드 처리
            if (files != null && !files.isEmpty()) {
                for (MultipartFile file : files) {
                    if (!file.isEmpty()) {
                        String fileName = fileService.uploadFile(file);
                        FileMetadata fileMetadata = fileService.getFileMetadataByFileName(fileName);
                        fileMetadataList.add(fileMetadata);
                    }
                }
            }

            // 게시글 생성
            Post newPost = new Post();
            newPost.setTitle(title);
            newPost.setContent(content);
            newPost.setAuthor(username);
            newPost.setFileMetadataList(fileMetadataList); // 파일 목록 설정

            // 각 파일 메타데이터에 게시글 설정
            for (FileMetadata fileMetadata : fileMetadataList) {
                fileMetadata.setPost(newPost);
            }

            // 서비스 레이어를 통해 게시글 저장
            Post savedPost = postService.createPost(newPost);
            return new ResponseEntity<>(savedPost, HttpStatus.CREATED);

        } catch (IOException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Get all posts (모든 사용자가 가능)
    @GetMapping
    @ResponseBody
    public ResponseEntity<List<Post>> getAllPosts() {
        List<Post> posts = postService.getAllPosts();
        return new ResponseEntity<>(posts, HttpStatus.OK);
    }

    // Get a post by id (모든 사용자가 가능)
    @GetMapping("/{id}")
    @ResponseBody
    public ResponseEntity<Post> getPostById(@PathVariable Long id) {
        Optional<Post> post = postService.getPostById(id);
        return post.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    // Update a post by id (로그인된 사용자만 가능)
    @PostMapping("/update/{id}")
    @ResponseBody
    public ResponseEntity<Post> updatePost(
            Authentication authentication,
            @PathVariable Long id,
            @RequestParam("title") String title,
            @RequestParam("content") String content,
            @RequestPart(value = "file", required = false) MultipartFile file) {

        String username = authentication.getName(); // 로그인된 사용자의 이름을 가져옴
        Optional<Post> existingPost = postService.getPostById(id);

        if (existingPost.isPresent() && existingPost.get().getAuthor().equals(username)) {
            try {
                Post updatedPost = existingPost.get();

                // 기존 파일 이름 가져오기
                String fileName = null;
                if (!updatedPost.getFileMetadataList().isEmpty()) {
                    fileName = updatedPost.getFileMetadataList().get(0).getFileName(); // 첫 번째 파일의 이름
                }

                // 파일이 새로 업로드된 경우 처리
                if (file != null && !file.isEmpty()) {
                    fileName = fileService.uploadFile(file);

                    // 새 파일 메타데이터 추가
                    FileMetadata newFileMetadata = fileService.getFileMetadataByFileName(fileName);
                    newFileMetadata.setPost(updatedPost);
                    updatedPost.getFileMetadataList().clear(); // 기존 파일 메타데이터를 제거 (필요에 따라 유지할 수도 있음)
                    updatedPost.getFileMetadataList().add(newFileMetadata);
                }

                // 게시글 업데이트
                updatedPost.setTitle(title);
                updatedPost.setContent(content);
                Post savedPost = postService.updatePost(id, updatedPost);

                return new ResponseEntity<>(savedPost, HttpStatus.OK);
            } catch (IOException e) {
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } else {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN); // 작성자만 수정할 수 있음
        }
    }

    // Delete a post by id (로그인된 사용자만 가능)
    @DeleteMapping("/{id}")
    @ResponseBody
    public ResponseEntity<String> deletePost(Authentication authentication, @PathVariable Long id) {
        String username = authentication.getName(); // 로그인된 사용자의 이름을 가져옴
        Optional<Post> existingPost = postService.getPostById(id);

        if (existingPost.isPresent() && existingPost.get().getAuthor().equals(username)) {
            postService.deletePost(id);
            return new ResponseEntity<>("Post deleted", HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN); // 작성자만 삭제할 수 있음
        }
    }

    @GetMapping("/page")
    public String postPage() {
        return "post/postPage";
    }
}
