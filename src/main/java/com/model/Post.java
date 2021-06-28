package com.model;

import org.json.simple.JSONObject;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Entity
public class Post {

    @Id
    @Column(name = "id")
    private Long id;

    @Column(name = "user_id")
    private int userId;

    @Column(name = "title")
    private String title;

    public Post() {
    }

    public Post(Long id, int userId, String title, String body, Status status) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.body = body;
        this.status = status;
    }

    @Column(name = "body")
    private String body;

    @Column(name = "status")
    private Status status = Status.ACTIVE;

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Post post = (Post) o;
        return userId == post.userId && id.equals(post.id) && title.equals(post.title) && body.equals(post.body) && status == post.status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public JSONObject createJsonFromPostObject() {
        Map<String, String> params = new HashMap<>();
        params.put("title", this.title);
        params.put("body", this.body);
        params.put("id", String.valueOf(this.id));
        return new JSONObject(params);
    }
}
