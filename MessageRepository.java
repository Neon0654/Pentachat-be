package com.hdtpt.pentachat.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hdtpt.pentachat.model.Message;

public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByFromOrTo(int from, int to);
}