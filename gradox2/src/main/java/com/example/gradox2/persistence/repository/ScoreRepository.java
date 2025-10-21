package com.example.gradox2.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.gradox2.persistence.entities.Score;
import com.example.gradox2.persistence.entities.File;
import com.example.gradox2.persistence.entities.User;

@Repository
public interface ScoreRepository extends JpaRepository<Score, Long> {
    Score findByFileAndUser(File file, User user);
}
