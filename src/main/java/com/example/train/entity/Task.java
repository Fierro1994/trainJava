package com.example.train.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@Setter
@RequiredArgsConstructor
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
    private List<String> options = new ArrayList<>();

    private boolean isMultipleChoice;

    private Integer correctOptionIndex;
    @ManyToMany(mappedBy = "tasksForReview", fetch = FetchType.LAZY)
    private Set<User> users = new HashSet<>();


}