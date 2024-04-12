package com.testcontainers.demo.comments_module.repository;

import java.util.List;

import com.testcontainers.demo.comments_module.domain.Comment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class CommentsRepository implements ICommentsRepository {

    public CommentsRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<Comment> findAllByTicketId(Integer ticketId) {
        return jdbcTemplate.query(
            "SELECT * FROM ratings WHERE ticketId = ?",
            (row, i) -> new Comment(row.getInt("ticketId"), row.getString("commentText"), row.getInt("userId")),
            ticketId
        );
    }

    @Override
    public void add(Integer ticketId, String commentText, Integer userId) {
        jdbcTemplate.update("INSERT INTO ratings (ticketId, commentText, userId) VALUES (?, ?, ?)", ticketId, commentText, userId);
    }

    public Boolean exists(Integer ticketId) {
        List<Boolean> results = jdbcTemplate.query(
            "SELECT 1 FROM ratings WHERE ticketId = ?",
            (row, i) -> true,
            ticketId
        );
        return !results.isEmpty();
    }
}
