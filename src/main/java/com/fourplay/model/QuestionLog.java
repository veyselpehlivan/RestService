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
 * Created by veyselpehlivan on 7/26/2017.
 */

@Entity
@Table(name = "question_log",
indexes = {@Index(name = "my_index", columnList = "user_id, question_id,, competition_log_id, user_answer, question_status")})
public class QuestionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @ManyToOne
    private User user;

    @ManyToOne
    private Question question;

    @ManyToOne
    private CompetitionLog competitionLog;

    @Column(name = "user_answer")
    private String userAnswer;

    @Column(name = "question_status")
    private QuestionStatus questionStatus;

    public enum QuestionStatus{
        ASKED, NOTASKED, NOTANSWERED
    }

    @Column(name = "answer_time")
    private Timestamp answerTime;

    @Column(name = "question_number")
    private int questionNumber;

    public QuestionLog() {
    }

    public QuestionLog(User user, Question question, CompetitionLog competitionLog, QuestionStatus questionStatus, int questionNumber) {
        this.user = user;
        this.question = question;
        this.competitionLog = competitionLog;
        this.questionStatus = questionStatus;
        this.questionNumber = questionNumber;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


    public User getUserId() {
        return user;
    }

    public void setUserId(User userId) {
        this.user = userId;
    }


    public Question getQuestion() {
        return question;
    }

    public void setQuestion(Question question) {
        this.question = question;
    }


    public CompetitionLog getCompetitionLogs() {
        return competitionLog;
    }

    public void setCompetitionLogs(CompetitionLog competitionLog) {
        this.competitionLog = competitionLog;
    }

    public String getUserAnswer() {
        return userAnswer;
    }

    public void setUserAnswer(String userAnswer) {
        this.userAnswer = userAnswer;
    }

    public QuestionStatus getQuestionStatus() {
        return questionStatus;
    }

    public void setQuestionStatus(QuestionStatus questionStatus) {
        this.questionStatus = questionStatus;
    }

    public Timestamp getAnswerTime() {
        return answerTime;
    }

    public void setAnswerTime(Timestamp answerTime) {
        this.answerTime = answerTime;
    }

    public int getQuestionNumber() {
        return questionNumber;
    }

    public void setQuestionNumber(int questionNumber) {
        this.questionNumber = questionNumber;
    }


    @Override
    public String toString() {
        return "QuestionLog{" +
                "id=" + id +
                ", user=" + user +
                ", question=" + question +
                ", competitionLog=" + competitionLog +
                ", userAnswer='" + userAnswer + '\'' +
                ", questionStatus=" + questionStatus +
                ", answerTime=" + answerTime +
                ", questionNumber=" + questionNumber +
                '}';
    }
}
