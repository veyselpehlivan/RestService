package com.fourplay.controller;

import com.fourplay.model.User;
import com.fourplay.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Created by veyselpehlivan on 10/07/2017.
 */

@RestController
@RequestMapping(value = "/app/customers")
public class Controller {

    @Autowired
    UserRepository userRepository;

    @GetMapping(value = "/all")
    public List<User> getAll(){
        return userRepository.findAll();
    }

    @GetMapping(value = "/{id}")
    public User findCustomer(@PathVariable("id") int id){
        return userRepository.findOne(id);
    }

    @PostMapping(value = "/load")
    public int persist(@RequestBody final User customer){
        userRepository.save(customer);
        return userRepository.findOne(customer.getId()).getId();
    }
}
