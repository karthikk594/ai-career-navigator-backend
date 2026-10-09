package com.example.controller;


import org.springframework.web.bind.annotation.*;

import com.example.model.Profile;
import com.example.model.User;
import com.example.repository.ProfileRepository;
import com.example.repository.UserRepository;



@RestController
@RequestMapping("/api/profile")
@CrossOrigin(origins = "http://localhost:5173")
public class ProfileController {


    private final ProfileRepository profileRepository;

    private final UserRepository userRepository;



    public ProfileController(
            ProfileRepository profileRepository,
            UserRepository userRepository
    ){

        this.profileRepository = profileRepository;
        this.userRepository = userRepository;

    }



    // CREATE PROFILE

    @PostMapping("/{userId}")
    public Profile createProfile(
            @PathVariable Long userId,
            @RequestBody Profile profile
    ){


        User user = userRepository.findById(userId)
                .orElse(null);


        if(user == null){

            return null;

        }


        profile.setUser(user);


        return profileRepository.save(profile);

    }





    // CHECK PROFILE EXISTS

    @GetMapping("/{userId}")
    public Profile getProfile(
            @PathVariable Long userId
    ){

        User user = userRepository.findById(userId)
                .orElse(null);



        if(user == null){

            return null;

        }



        return user.getProfile();

    }
    @PutMapping("/{userId}")
public Profile updateProfile(
        @PathVariable Long userId,
        @RequestBody Profile profile
){


    User user = userRepository.findById(userId)
            .orElse(null);



    Profile oldProfile = user.getProfile();



    oldProfile.setEducation(profile.getEducation());

    oldProfile.setCollege(profile.getCollege());

    oldProfile.setBranch(profile.getBranch());

    oldProfile.setSkills(profile.getSkills());

    oldProfile.setGithub(profile.getGithub());

    oldProfile.setLinkedin(profile.getLinkedin());

    oldProfile.setCareerGoal(profile.getCareerGoal());



    return profileRepository.save(oldProfile);

}

}