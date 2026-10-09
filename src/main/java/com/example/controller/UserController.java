package com.example.controller;


import org.springframework.web.bind.annotation.*;
import com.example.model.User;
import com.example.repository.UserRepository;


@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:5173")
public class UserController {


    private final UserRepository userRepository;


    public UserController(UserRepository userRepository){

        this.userRepository = userRepository;

    }



    @GetMapping("/{id}")
    public User getUser(@PathVariable Long id){

        return userRepository.findById(id)
                .orElse(null);

    }

}