package com.example.taskManager.repository;

import com.example.taskManager.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Long> {

    @Query("Select t from Task t left join fetch t.tag")
    List<Task> findAll();


    boolean existsByTagId(Long tagId);

    List<Task> findAllByUser_Id(Long userId);

    Optional<Task> findByIdAndUser_Id(Long taskId, Long userId);



}
