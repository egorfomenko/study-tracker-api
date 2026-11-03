package com.example.taskManager.repository;

import com.example.taskManager.model.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TagRepository extends JpaRepository<Tag, Long> {
    @Query("SELECT t FROM Tag t LEFT JOIN FETCH t.user")
    List<Tag> findAll();

    List<Tag> findAllByUserId(Long userId);
}
