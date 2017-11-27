package com.fourplay.model;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;


/**
 * Created by veyselpehlivan on 7/20/2017.
 */

@Entity
@Table(name = "question")
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @Column(name = "question_url")
    private String questionUrl;

    @Column(name = "true_answer")
    private TrueAnswer trueAnswer;


    public enum TrueAnswer{
        A("A"), B("B"), C("C"), D("D");

        private String value;

        TrueAnswer(String value){
            this.value=value;
        }

        @Enumerated(EnumType.STRING)
        public String getValue(){
            return value;
        }
    }

    public Question() {
    }

    public Question(String questionUrl, TrueAnswer trueAnswer) {
        this.questionUrl = questionUrl;
        this.trueAnswer=trueAnswer;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getQuestionUrl() {
        return questionUrl;
    }

    public void setQuestionUrl(String questionUrl) {
        this.questionUrl = questionUrl;
    }

    public TrueAnswer getTrueAnswer() {
        return trueAnswer;
    }

    public void setTrueAnswer(TrueAnswer trueAnswer) {
        this.trueAnswer = trueAnswer;
    }

    @Override
    public String toString() {
        return "Question{" +
                "id=" + id +
                ", questionUrl='" + questionUrl + '\'' +
                ", trueAnswer=" + trueAnswer +
                '}';
    }
}
