package com.fourplay.repository;

import com.fourplay.model.CompetitionLog;
import com.fourplay.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by veyselpehlivan on 7/28/2017.
 */

@Repository
public interface CompetitionLogRepository extends JpaRepository<CompetitionLog, Integer> {

    CompetitionLog findCompetitionLogByCompetitionStatusAndUser(CompetitionLog.CompetitionStatus competitionStatus, User user);




}
