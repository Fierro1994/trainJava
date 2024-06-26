package com.example.train.repos;

import com.example.train.entity.CategoryNames;
import com.example.train.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TasksRepos extends JpaRepository<Task, Long> {
    Optional<Task> findById(Long id);
    @Query(value = "SELECT * FROM tasks ORDER BY RAND() LIMIT 1", nativeQuery = true)
    Task findRandomTask();
    List<Task> findByCategory(CategoryNames category);
}
