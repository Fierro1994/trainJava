package com.example.train.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.List;
import java.util.Set;

@Entity
@NoArgsConstructor
@Getter
@Setter
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
    @ElementCollection(fetch = FetchType.EAGER)
    private List<String> options;

    private boolean isMultipleChoice;

    private Integer correctOptionIndex;
    @ManyToMany(mappedBy = "tasksForReview", fetch = FetchType.LAZY)
    private Set<User> users;


}