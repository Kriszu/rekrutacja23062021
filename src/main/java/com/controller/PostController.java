package com.controller;

import com.model.Post;
import com.service.PostService;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
public class PostController {

    private final PostService postService;

    private static final Logger logger = LoggerFactory.getLogger(PostController.class);

    @Autowired
    public PostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping("/callRestGet")
    @Scheduled(cron = "* */10 * * * *")
    public ResponseEntity<List<Post>> callRestGetPosts() {
        logger.info("Get mapping: callRestGet invoked");
        List<Post> postsFromApi = postService.getPostsFromRestCall();
        postService.saveListToDatabse(postsFromApi);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/getPosts")
    public ResponseEntity<List<JSONObject>> getPostsByTitle(@RequestParam(required = false) String title) {
        logger.info("Get mapping: getPosts invoked");

        List<Post> resultsFromDb;
        List<JSONObject> results = new ArrayList<>();

        if (title != null) {
            resultsFromDb = postService.getPostsByTitle(title);
        } else {
            resultsFromDb = postService.getPosts();
        }
        resultsFromDb.forEach(post -> results.add(post.createJsonFromPostObject()));
        return new ResponseEntity<>(results, HttpStatus.OK);
    }

    @DeleteMapping("/deletePost")
    public void deletePost(@RequestParam Long postId) {
        logger.info("delete mapping: deletePost invoked");
        postService.deletePostById(postId);
    }

    @PutMapping("/update")
    public ResponseEntity<JSONObject> updatePost(@RequestBody Post newPost, @RequestParam Long id) {
        logger.info("update mapping: update");
        return new ResponseEntity<>(postService.updatePost(newPost).createJsonFromPostObject(), HttpStatus.OK);
    }
}
