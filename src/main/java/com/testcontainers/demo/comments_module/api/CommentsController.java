package com.testcontainers.demo.comments_module.api;

import com.testcontainers.demo.comments_module.domain.Comment;
import com.testcontainers.demo.comments_module.repository.CommentsRepository;

import java.util.List;

import com.testcontainers.demo.service.TicketService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

/**
 * Controller class for managing ratings.
 */
@RestController
@RequestMapping("api/comments")
public class CommentsController {

    private final KafkaTemplate<String, Comment> kafkaTemplate;

    private final CommentsRepository commentsRepository;

    private final TicketService     ticketService;

    public CommentsController(
        KafkaTemplate<String, Comment> kafkaTemplate,
        CommentsRepository commentsRepository,
        TicketService ticketService
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.commentsRepository = commentsRepository;
        this.ticketService = ticketService;
    }

    /**
     * Records a comment and sending it to the Kafka topic "comments".
     *
     * @param comment the Comment object to be recorded
     * @return a ResponseEntity indicating that the comment has been accepted
     * @throws Exception if an error occurs while sending the comment to the topic
     */
    @PostMapping("/add")
    public ResponseEntity<Object> addComment(@RequestBody Comment comment) throws Exception {
        if (!ticketService.canTicketBeCommented(comment.getTicketId())) {
            return ResponseEntity.notFound().build();
        }
        //TODO mock kafka template
        commentsRepository.add(comment.getTicketId(), comment.getCommentText(), comment.getUserId());
        kafkaTemplate.send("comments", comment).get();
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping
    public List<Comment> getComments(@RequestParam Integer ticketId) {
        return commentsRepository.findAllByTicketId(ticketId);
    }
}
