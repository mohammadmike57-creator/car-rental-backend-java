package com.carrental.backend.controller;

import com.carrental.backend.model.Message;
import com.carrental.backend.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageRepository messageRepository;

    @GetMapping
    @PreAuthorize("hasAuthority('VIEW_INTERNAL_MESSAGES')")
    public ResponseEntity<List<Message>> getAllMessages() {
        return ResponseEntity.ok(messageRepository.findAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('VIEW_INTERNAL_MESSAGES')")
    public ResponseEntity<Message> getMessage(@PathVariable String id) {
        return messageRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('VIEW_INTERNAL_MESSAGES')")
    public ResponseEntity<Message> createMessage(@RequestBody Message message) {
        Message saved = messageRepository.save(message);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('VIEW_INTERNAL_MESSAGES')")
    public ResponseEntity<Message> updateMessage(@PathVariable String id, @RequestBody Message message) {
        if (!messageRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        message.setId(id);
        Message saved = messageRepository.save(message);
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('VIEW_INTERNAL_MESSAGES')")
    public ResponseEntity<Void> deleteMessage(@PathVariable String id) {
        if (!messageRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        messageRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
