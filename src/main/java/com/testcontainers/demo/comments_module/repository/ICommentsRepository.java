package com.testcontainers.demo.comments_module.repository;

import com.testcontainers.demo.comments_module.domain.Comment;

import java.util.List;

public interface ICommentsRepository {
    List<Comment> findAllByTicketId(Integer ticketId);

    void add(Integer ticketId, String commentText, Integer userId);
}
