package com.fourplay.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.sql.Timestamp;


/**
 * Created by veyselpehlivan on 7/25/2017.
 */

@Entity
@Table(name = "competition_log",
        indexes = {@Index(name = "my_index", columnList = "competition_status, user_id, id")})
public class CompetitionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    public enum CompetitionStatus{
        FINISHED, INCOMPITION
    }


    @Column(name = "competition_status")
    CompetitionStatus competitionStatus;

    @Column(name = "start_time")
    private Timestamp startTime;

    @Column(name = "end_time")
    private Timestamp endTime;

    @ManyToOne
    private User user;

    public CompetitionLog() {
    }

    public CompetitionLog(Timestamp startTime,User userId, CompetitionStatus competitionStatus) {
        this.startTime=startTime;
        this.user = userId;
        this.competitionStatus=competitionStatus;
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Timestamp getEndTime() {
        return endTime;
    }

    public void setEndTime(Timestamp endTime) {
        this.endTime = endTime;
    }


    public User getUserId() {
        return user;
    }

    public void setUserId(User user) {
        this.user = user;
    }

    public Timestamp getStartTime() {
        return startTime;
    }

    public void setStartTime(Timestamp startTime) {
        this.startTime = startTime;
    }

    public CompetitionStatus getCompetitionStatus(){
        return competitionStatus;
    }

    public void setCompetitionStatus(CompetitionStatus competitionStatus){
        this.competitionStatus=competitionStatus;
    }

    @Override
    public String toString() {
        return "CompetitionLog{" +
                "id=" + id +
                ", competitionStatus=" + competitionStatus +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", user=" + user +
                '}';
    }
}
