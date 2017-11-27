package com.fourplay.repository;

import com.fourplay.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by veyselpehlivan on 10/07/2017.
 */

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    boolean existsByFacebookId(String facebookId);

    User findUserByFacebookId(String facebookId);
}
