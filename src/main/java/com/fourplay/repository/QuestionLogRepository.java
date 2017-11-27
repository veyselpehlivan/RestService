package com.fourplay.repository;

import com.fourplay.model.CompetitionLog;
import com.fourplay.model.QuestionLog;
import com.fourplay.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by veyselpehlivan on 7/28/2017.
 */

@Repository
public interface QuestionLogRepository extends JpaRepository <QuestionLog, Integer>{

    int countQuestionLogByCompetitionLogAndUser(CompetitionLog competitionLog, User user);



    QuestionLog findQuestionLogByUserAndQuestionStatusAndCompetitionLogOrderByQuestionNumberAsc(User user, QuestionLog.QuestionStatus questionStatus, CompetitionLog competitionLog);


}
