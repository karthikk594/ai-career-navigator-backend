package com.example.service;


import com.example.model.User;
import com.example.repository.UserRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;



@Service
public class UserService {


    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;



    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder){

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;

    }

    public User userdetails(long id){

    return userRepository.findById(id).orElse(null);

}


    public User register(User user){

        user.setPassword(
                passwordEncoder.encode(user.getPassword())
        );

        return userRepository.save(user);

    }


public User login(String email, String password){


    User user = userRepository.findByEmail(email);


    if(user != null){

        if(passwordEncoder.matches(password, user.getPassword())){

            return user;

        }

    }


    return null;
}

}