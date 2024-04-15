package com.testcontainers.demo.dao;

import com.testcontainers.demo.entity.Comment;

import java.util.List;

public interface ICommentsDAO {
    List<Comment> findAllByTicketId(Integer ticketId);

    void add(Integer ticketId, String commentText, Integer userId);
}
