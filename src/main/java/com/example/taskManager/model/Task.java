package com.example.taskManager.model;

import com.example.taskManager.enums.TaskPriority;
import com.example.taskManager.enums.TaskStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;


import java.time.LocalDate;


@Entity
@Table(name = "tasks")

public class Task{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "task_id")
    private Long id;

    @Column(name = "title", nullable = false)
    @NotNull
    private String title;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;


    @Enumerated(EnumType.STRING)
    private TaskStatus status;

    @Column(name = "create_date_time")
    private LocalDate createDate;

    @Column(name = "deadline_date")
    private LocalDate deadlineDate;

    @Enumerated(EnumType.STRING)
    private TaskPriority priority;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tag_id", nullable = false)
    private Tag tag;

    public Task(Long id ,User user,String title ,TaskStatus status,
                LocalDate createDate, LocalDate deadlineDate, TaskPriority priority, Tag tag) {
        this.id = id;
        this.user = user;
        this.title = title;
        this.status = status;
        this.createDate = createDate;
        this.deadlineDate = deadlineDate;
        this.priority = priority;
        this.tag = tag;
    }
    public Task(){}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Tag getTag() {
        return tag;
    }

    public void setTag(Tag tag) {
        this.tag = tag;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}