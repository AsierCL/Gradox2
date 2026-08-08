package com.example.gradox2.persistence.entities;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "forum_threads")
public class ForumThread {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "file_id", nullable = false, unique = true)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private File file;

    @Builder.Default
    private boolean locked = false;

    @Builder.Default
    private Instant createdAt = Instant.now();

    private Instant updatedAt;

    @OneToMany(mappedBy = "thread", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ThreadComment> comments = new ArrayList<>();

    public void addComment(ThreadComment comment) {
        comments.add(comment);
        comment.setThread(this);
        this.updatedAt = Instant.now();
    }
}