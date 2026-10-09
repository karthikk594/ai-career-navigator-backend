package com.example.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import jakarta.persistence.OneToOne;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    private String name;


    @Column(unique = true)
    private String email;


    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;


    private String role;


    public Long getId() {
        return id;
    }


    public void setId(Long id) {
        this.id = id;
    }


    public String getName() {
        return name;
    }


    public void setName(String name) {
        this.name = name;
    }


    public String getEmail() {
        return email;
    }


    public void setEmail(String email) {
        this.email = email;
    }


    public String getPassword() {
        return password;
    }


    public void setPassword(String password) {
        this.password = password;
    }


    public String getRole() {
        return role;
    }


    public void setRole(String role) {
        this.role = role;
    }

    @OneToOne(mappedBy = "user")
    @JsonIgnore
    private Profile profile;

    public Profile getProfile() {
    return profile;
}


public void setProfile(Profile profile) {
    this.profile = profile;
}

@OneToOne(mappedBy = "user")
@JsonIgnore
private Resume resume;


public Resume getResume() {
    return resume;
}


public void setResume(Resume resume) {
    this.resume = resume;
}
}
