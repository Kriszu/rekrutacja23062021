package com.service;


import com.model.Post;
import com.model.Status;
import com.repository.PostRepository;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Transactional
@SpringBootTest
class PostServiceTest {

    MockWebServer mockWebServer;

    PostService postService;

    @Autowired
    PostRepository postRepository;

    @BeforeEach
    void tearUp() throws IOException {
        this.mockWebServer = new MockWebServer();
        this.mockWebServer.start();
        this.postService = new PostService(WebClient.builder().baseUrl(mockWebServer.url("/").toString()).build());
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    public final String resposneBody = "[{\n" +
            "    \"userId\": 1,\n" +
            "    \"id\": 1,\n" +
            "    \"title\": \"title1\",\n" +
            "    \"body\": \"body1\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"userId\": 1,\n" +
            "    \"id\": 2,\n" +
            "    \"title\": \"title2\",\n" +
            "    \"body\": \"body2\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"userId\": 1,\n" +
            "    \"id\": 3,\n" +
            "    \"title\": \"title3\",\n" +
            "    \"body\": \"body3\"\n" +
            "  }]";

    @Test
    void getPostsFromRestCall_should_return_list() {
        Post postActive = new Post(1L, 1, "title1",
                "body1", Status.ACTIVE);
        Post postUpdated = new Post(2L, 1, "title2",
                "body2", Status.ACTIVE);
        Post postDeleted = new Post(3L, 1, "title3",
                "body3", Status.ACTIVE);

        mockWebServer.enqueue(new MockResponse().setResponseCode(200)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(resposneBody)
        );

        List<Post> resultsFromService = postService.getPostsFromRestCall();
        Assertions.assertEquals(postActive, resultsFromService.get(0));
        Assertions.assertEquals(postUpdated, resultsFromService.get(1));
        Assertions.assertEquals(postDeleted, resultsFromService.get(2));
    }

    @Test
    void saveListToDatabse_shouldSavePost() {
        Assertions.assertFalse(postRepository.findAll().iterator().hasNext());

        PostService postService = new PostService(postRepository);

        Post post1 = new Post(1L, 1, "title1",
                "body1", Status.ACTIVE);
        Post post2 = new Post(2L, 1, "title2",
                "body2", Status.ACTIVE);
        Post post3 = new Post(3L, 1, "title3",
                "body3", Status.ACTIVE);

        List<Post> postList = new ArrayList<>();
        postList.add(post1);
        postList.add(post2);
        postList.add(post3);
        postService.saveListToDatabse(postList);
        Assertions.assertEquals(postRepository.findAll(), postList);
    }

    @Test
    void saveListToDatabase_shouldNotUpdatePostsWithStatusDeletedOrUpdated() {
        PostService postService = new PostService(postRepository);
        Post post1 = new Post(1L, 1, "title1",
                "body1", Status.UPDATED);
        Post post2 = new Post(2L, 1, "title2",
                "body2", Status.DELETED);

        Post postToSave1 = new Post(1L, 1, "title1",
                "body1", Status.ACTIVE);
        Post postToSave2 = new Post(2L, 1, "title2",
                "body2", Status.ACTIVE);

        List<Post> postList = new ArrayList<>();
        List<Post> postsToSave = new ArrayList<>();

        postList.add(post1);
        postList.add(post2);
        postsToSave.add(postToSave1);
        postsToSave.add(postToSave2);

        postRepository.saveAll(postList);
        postService.saveListToDatabse(postsToSave);

        Assertions.assertEquals(postRepository.findAll(), postList);
    }

    @Test
    void getPosts_ShouldNotReturnDeletedPosts() {
        PostService postService = new PostService(postRepository);
        Post post1 = new Post(1L, 1, "title1",
                "body1", Status.UPDATED);
        Post post2 = new Post(2L, 1, "title2",
                "body2", Status.DELETED);
        Post post3 = new Post(3L, 1, "title3",
                "body3", Status.ACTIVE);

        List<Post> expectedPosts = new ArrayList<>();
        expectedPosts.add(post1);
        expectedPosts.add(post3);

        List<Post> postsToSave = new ArrayList<>();
        postsToSave.add(post1);
        postsToSave.add(post2);
        postsToSave.add(post3);

        postService.saveListToDatabse(postsToSave);

        Assertions.assertEquals(postService.getPosts(), expectedPosts);
    }

    @Test
    void getPostsByTitle_shouldReturnPostWithTitle() {
        PostService postService = new PostService(postRepository);
        Post post1 = new Post(1L, 1, "title",
                "body1", Status.UPDATED);
        Post post2 = new Post(2L, 1, "Topic",
                "body2", Status.DELETED);
        Post post3 = new Post(3L, 1, "title3",
                "body3", Status.ACTIVE);
        Post post4 = new Post(2L, 1, "title",
                "body2", Status.DELETED);


        List<Post> postsToSave = new ArrayList<>();
        postsToSave.add(post1);
        postsToSave.add(post2);
        postsToSave.add(post3);
        postsToSave.add(post4);

        postService.saveListToDatabse(postsToSave);

        List<Post> expectedResult = new ArrayList<>();
        expectedResult.add(post1);
        expectedResult.add(post3);
        Assertions.assertEquals(postService.getPostsByTitle("title"), expectedResult);
    }

    @Test
    void deletePostById_shouldChangeStatus() {
        PostService postService = new PostService(postRepository);
        Post post1 = new Post(1L, 1, "title",
                "body1", Status.UPDATED);
        Post post2 = new Post(3L, 1, "title3",
                "body3", Status.ACTIVE);

        List<Post> postsToSave = new ArrayList<>();
        postsToSave.add(post1);
        postsToSave.add(post2);
        postService.saveListToDatabse(postsToSave);
        postService.deletePostById(post1.getId());
        postService.deletePostById(post2.getId());

        Assertions.assertEquals(postRepository.getOne(post1.getId()).getStatus(), Status.DELETED);
        Assertions.assertEquals(postRepository.getOne(post2.getId()).getStatus(), Status.DELETED);
    }

    @Test
    void updatePost_shoudlUpdatePost() {
        PostService postService = new PostService(postRepository);

        Post post1 = new Post(1L, 1, "title",
                "body1", Status.UPDATED);
        Post post2 = new Post(2L, 1, "title3",
                "body3", Status.ACTIVE);

        List<Post> postsToSave = new ArrayList<>();
        postsToSave.add(post1);
        postsToSave.add(post2);
        postService.saveListToDatabse(postsToSave);

        Post newPost1 = new Post(1L, 1, "UPDATED",
                "UPDATED", Status.UPDATED);
        Post newPost2 = new Post(2L, 1, "UPDATED",
                "UPDATED", Status.UPDATED);

        List<Post> expectedPosts = new ArrayList<>();
        expectedPosts.add(newPost1);
        expectedPosts.add(newPost2);
        postService.updatePost(newPost1);
        postService.updatePost(newPost2);

        Assertions.assertEquals(postRepository.findAll(), expectedPosts);
    }
}