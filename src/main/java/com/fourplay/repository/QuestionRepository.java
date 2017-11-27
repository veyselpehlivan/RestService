package com.fourplay.repository;

import com.fourplay.model.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Repository;

/**
 * Created by veyselpehlivan on 7/28/2017.
 */

@Repository
public interface QuestionRepository extends JpaRepository<Question, Integer> {


    @Async
    @Query(value = "SELECT * FROM question order by rand() limit 1",
    nativeQuery = true)
    Question selectRandomQuestion();
}
