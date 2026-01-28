package com.hdtpt.pentachat.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hdtpt.pentachat.model.Message;
import com.hdtpt.pentachat.repository.MessageRepository;

@RestController
@RequestMapping("/api/messages")
public class MessageController {
    @Autowired
    private MessageRepository repo;

    @PostMapping // Lưu tin: POST http://localhost:8080/api/messages
    public Message save(@RequestBody Message data) {
        return repo.save(data);
    }

    @GetMapping("/{id}") // Lấy tin: GET http://localhost:8080/api/messages/1
    public List<Message> get(@PathVariable int id) {
        return repo.findByFromOrTo(id, id);
    }
}