package com.example.model;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;


@Entity
@Table(name="profiles")
public class Profile {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    private String education;


    private String college;


    private String branch;


    private String skills;


    private String github;


    private String linkedin;


    private String careerGoal;



    @OneToOne
@JoinColumn(name="user_id")
@JsonIgnore
private User user;



    public Long getId() {
        return id;
    }


    public void setId(Long id) {
        this.id = id;
    }


    public String getEducation() {
        return education;
    }


    public void setEducation(String education) {
        this.education = education;
    }


    public String getCollege() {
        return college;
    }


    public void setCollege(String college) {
        this.college = college;
    }


    public String getBranch() {
        return branch;
    }


    public void setBranch(String branch) {
        this.branch = branch;
    }


    public String getSkills() {
        return skills;
    }


    public void setSkills(String skills) {
        this.skills = skills;
    }


    public String getGithub() {
        return github;
    }


    public void setGithub(String github) {
        this.github = github;
    }


    public String getLinkedin() {
        return linkedin;
    }


    public void setLinkedin(String linkedin) {
        this.linkedin = linkedin;
    }


    public String getCareerGoal() {
        return careerGoal;
    }


    public void setCareerGoal(String careerGoal) {
        this.careerGoal = careerGoal;
    }


    public User getUser() {
        return user;
    }


    public void setUser(User user) {
        this.user = user;
    }

}