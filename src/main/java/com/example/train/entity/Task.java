package com.example.train.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

@Entity
@Data
@NoArgsConstructor
@Table(name = "tasks")
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(columnDefinition = "longtext")
    private String question;
    @Column(columnDefinition = "longtext")
    private String answer;
    @Column(columnDefinition = "longtext")
    private String theory;
    @Enumerated(EnumType.STRING)
    private CategoryNames category;

}
