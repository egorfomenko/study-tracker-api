package com.example.taskManager.repository;

import com.example.taskManager.model.Tag;
import com.example.taskManager.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {

}
