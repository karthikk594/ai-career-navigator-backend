package com.example.controller;


import com.example.model.User;
import com.example.service.UserService;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.GetMapping;






@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:5173")
public class AuthController {
    

    private final UserService userService;


    public AuthController(UserService userService){

        this.userService = userService;

    }
    @PostMapping("/login")
public User login(@RequestBody User user){

    return userService.login(
            user.getEmail(),
            user.getPassword()
    );

}



    @PostMapping("/register")
    public User register(@RequestBody User user){

        return userService.register(user);

    }
   
    @GetMapping("/hi/{id}")
    public User hello(@PathVariable long id) {
        return userService.userdetails(id);
    }
    

    
    



}
