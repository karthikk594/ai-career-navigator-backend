package com.example.repository;


import org.springframework.data.jpa.repository.JpaRepository;

import com.example.model.Resume;


public interface ResumeRepository 
extends JpaRepository<Resume, Long>{


    Resume findByUserId(Long userId);


}