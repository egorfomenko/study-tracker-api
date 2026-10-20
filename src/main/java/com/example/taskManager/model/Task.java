package com.example.taskManager.model;

import com.example.taskManager.enums.TaskPriority;
import com.example.taskManager.enums.TaskStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "tasks")
@NoArgsConstructor
public class Task{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "task")
    private Long id;

    @Column(name = "creator_id")
    private Long creatorId;

    @Column(name = "assigned_user_id")
    private Long assignedUserId;

    @Enumerated(EnumType.STRING)
    private TaskStatus status;

    @Column(name = "create_date_time")
    private LocalDate createDateTime;

    @Column(name = "deadline_date")
    private LocalDate deadlineDate;

    @Enumerated(EnumType.STRING)
    private TaskPriority priority;

    public Task(Long id ,Long creatorId, Long assignedUserId, TaskStatus status,
                LocalDate createDateTime, LocalDate deadlineDate, TaskPriority priority) {
        this.id = id;
        this.creatorId = creatorId;
        this.assignedUserId = assignedUserId;
        this.status = status;
        this.createDateTime = createDateTime;
        this.deadlineDate = deadlineDate;
        this.priority = priority;
    }


}