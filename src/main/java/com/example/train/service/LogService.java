package com.example.train.service;

import com.example.train.entity.TaskLog;
import com.example.train.repos.TaskLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LogService {

    @Autowired
    private TaskLogRepository taskLogRepository;

    public List<TaskLog> getAllLogs() {
        return taskLogRepository.findAll();
    }
}