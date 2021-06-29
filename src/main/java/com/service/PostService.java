package com.service;

import com.model.Post;
import com.model.Status;
import com.repository.PostRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PostService {

    final String uri = "https://jsonplaceholder.typicode.com/";

    private static final Logger logger = LoggerFactory.getLogger(PostService.class);

    private WebClient webClient = WebClient.create(uri);

    private PostRepository postRepository;

    @Autowired
    public PostService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    public PostService(WebClient webClient) {
        this.webClient = webClient;
    }

    public List<Post> getPostsFromRestCall() {
        try {
            Flux<Post> posts = webClient.get()
                    .uri("/posts")
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .retrieve()
                    .bodyToFlux(Post.class);
            List<Post> result = posts.toStream().collect(Collectors.toList());
            logger.info("Successful get following data from API: " + result.toString());
            return result;
        } catch (IllegalArgumentException e) {
            logger.info("There was problem with API CALL");
            return new ArrayList<>();
        }
    }

    public void saveListToDatabse(List<Post> postListToSave) {
        List<Post> postListFromDb = new ArrayList<>(postRepository.findAll());
        List<Post> filteredPostList = new ArrayList<>();
        postListToSave.stream().filter(post -> postDatabaseValidator(post, postListFromDb)).forEach(filteredPostList::add);
        filteredPostList.forEach(this::saveWithLogger);
    }

    public List<Post> getPosts() {
        List<Post> filteredPosts = new ArrayList<>();
        List<Post> postListFromDB = new ArrayList<>(postRepository.findAll());
        postListFromDB.stream().filter(this::checkIfPostIsNotDeleted).forEach(filteredPosts::add);
        logger.info("Successful got posts from DB: " + filteredPosts.toString());
        return filteredPosts;
    }

    public List<Post> getPostsByTitle(String title) {
        List<Post> results = postRepository.findByTitleContainingIgnoreCase(title).
                stream().filter(this::checkIfPostIsNotDeleted).
                collect(Collectors.toList());
        logger.info("Successful got posts by title " + title + " from DB: " + results.toString());
        return results;
    }

    public void deletePostById(Long id) {
        Optional<Post> postToDeleteOptional = postRepository.findById(id);
        if (postToDeleteOptional.isPresent()) {
            Post postToDelete = postToDeleteOptional.get();
            postToDelete.setStatus(Status.DELETED);
            postRepository.save(postToDelete);
            logger.info("Successful deleted post: " + postToDelete.toString());
        } else {
            logger.info("Post doesn't exist");
        }
    }

    public Post updatePost(Post newPost) {
        Optional<Post> oldPostOptional = postRepository.findById(newPost.getId());
        if (oldPostOptional.isPresent()) {
            Post oldPost = oldPostOptional.get();
            oldPost.setTitle(newPost.getTitle());
            oldPost.setBody(newPost.getBody());
            oldPost.setStatus(Status.UPDATED);
            postRepository.save(oldPost);
            logger.info("Successful updated post to: " + oldPost.toString());
            return oldPost;
        } else {
            logger.info("Post doesn't exist");
            return null;
        }
    }

    private boolean postDatabaseValidator(Post postToSave, List<Post> postsFromDb) {
        if (!checkIfListHasObjectWithSameId(postsFromDb, postToSave))
            return true;

        return postsFromDb.stream().filter(post -> post.getId().equals(postToSave.getId()))
                .collect(Collectors.toList())
                .get(0)
                .getStatus()
                .equals(Status.ACTIVE);
    }

    private boolean checkIfListHasObjectWithSameId(List<Post> postList, Post post) {
        for (Post value : postList) {
            if (value.getId().equals(post.getId())) {
                return true;
            }
        }
        return false;
    }

    private void saveWithLogger(Post post) {
        postRepository.save(post);
        logger.info("Succesful saved following post: " + post.toString());
    }

    private boolean checkIfPostIsNotDeleted(Post post) {
        return !post.getStatus().equals(Status.DELETED);
    }
}
