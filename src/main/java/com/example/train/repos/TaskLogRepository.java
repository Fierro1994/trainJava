package com.example.train.repos;

import com.example.train.entity.TaskLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskLogRepository extends JpaRepository<TaskLog, Long> {
}