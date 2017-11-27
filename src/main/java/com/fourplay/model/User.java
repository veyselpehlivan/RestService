package com.fourplay.model;

import javax.persistence.*;


/**
 * Created by veyselpehlivan on 10/07/2017.
 */

@Entity
@Table(name = "user",
        indexes = {@Index(name = "my_index", columnList = "id, facebook_id")})
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @Column(name = "facebook_id")
    private String facebookId;

    public User(){

    }

    public User(String facebookId) {
        this.facebookId = facebookId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFacebookId() {
        return facebookId;
    }

    public void setFacebookId(String facebookId) {
        this.facebookId = facebookId;
    }


}
