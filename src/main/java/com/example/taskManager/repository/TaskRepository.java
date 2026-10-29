package com.example.taskManager.repository;

import com.example.taskManager.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {

    @Query("Select t from Task t left join fetch t.tag")
    List<Task> findAll();

    @Modifying
    @Query("UPDATE Task t SET t.tag = null WHERE t.tag.id = :tagId")
    void clearTagReferences(@Param("tagId") Long tagId);
}
