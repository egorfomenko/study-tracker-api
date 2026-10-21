package com.example.taskManager.model;

import com.example.taskManager.enums.TaskPriority;
import com.example.taskManager.enums.TaskStatus;
import jakarta.persistence.*;


import java.time.LocalDate;


@Entity
@Table(name = "tasks")

public class Task{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "task_id")
    private Long id;

    @Column(name = "creator_id")
    private Long creatorId;

    @Column(name = "assigned_user_id")
    private Long assignedUserId;

    @Enumerated(EnumType.STRING)
    private TaskStatus status;

    @Column(name = "create_date_time")
    private LocalDate createDate;

    @Column(name = "deadline_date")
    private LocalDate deadlineDate;

    @Enumerated(EnumType.STRING)
    private TaskPriority priority;

    public Task(Long id ,Long creatorId, Long assignedUserId, TaskStatus status,
                LocalDate createDate, LocalDate deadlineDate, TaskPriority priority) {
        this.id = id;
        this.creatorId = creatorId;
        this.assignedUserId = assignedUserId;
        this.status = status;
        this.createDate = createDate;
        this.deadlineDate = deadlineDate;
        this.priority = priority;
    }
    public Task(){}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(Long creatorId) {
        this.creatorId = creatorId;
    }

    public Long getAssignedUserId() {
        return assignedUserId;
    }

    public void setAssignedUserId(Long assignedUserId) {
        this.assignedUserId = assignedUserId;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public LocalDate getCreateDate() {
        return createDate;
    }

    public void setCreateDate(LocalDate createDate) {
        this.createDate = createDate;
    }

    public LocalDate getDeadlineDate() {
        return deadlineDate;
    }

    public void setDeadlineDate(LocalDate deadlineDate) {
        this.deadlineDate = deadlineDate;
    }

    public TaskPriority getPriority() {
        return priority;
    }

    public void setPriority(TaskPriority priority) {
        this.priority = priority;
    }
}