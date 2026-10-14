
package com.example.controller;

import java.io.IOException;
import java.nio.file.*;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.example.model.Resume;
import com.example.model.User;
import com.example.repository.ResumeRepository;
import com.example.repository.UserRepository;
import com.example.service.ResumeAnalysisService;

@RestController
@RequestMapping("/api/resume")
@CrossOrigin(
    origins = {
        "http://localhost:5173",
        "https://ai-career-navigator-frontend-24eg107e63.onrender.com"
    }
)
public class ResumeController {

    private final ResumeRepository resumeRepository;
    private final UserRepository userRepository;
    private final ResumeAnalysisService resumeAnalysisService;

    public ResumeController(
            ResumeRepository resumeRepository,
            UserRepository userRepository,
            ResumeAnalysisService resumeAnalysisService) {

        this.resumeRepository = resumeRepository;
        this.userRepository = userRepository;
        this.resumeAnalysisService = resumeAnalysisService;
    }

    @GetMapping("/{userId}")
    public Resume getResume(@PathVariable Long userId) {
        return resumeRepository.findByUserId(userId);
    }

    @PostMapping("/upload/{userId}")
    public Resume uploadResume(
            @PathVariable Long userId,
            @RequestParam("file") MultipartFile file) throws IOException {

        User user = userRepository.findById(userId).orElse(null);

        if (user == null) {
            throw new ResponseStatusException(
                HttpStatus.NOT_FOUND, "User not found");
        }

        if (file.isEmpty()) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST, "Please select a file");
        }

        String originalName = file.getOriginalFilename();

        if (originalName == null ||
                !originalName.toLowerCase().endsWith(".pdf")) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST, "Please upload a PDF file");
        }

        Path uploadPath = Paths.get("uploads").toAbsolutePath().normalize();
        Files.createDirectories(uploadPath);

        // Generate a unique filename to avoid collisions.
        String fileName = userId + "_" +
                System.currentTimeMillis() + ".pdf";

        Path filePath = uploadPath.resolve(fileName).normalize();

        if (!filePath.startsWith(uploadPath)) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST, "Invalid filename");
        }

        Files.copy(file.getInputStream(), filePath);

        Resume resume = resumeRepository.findByUserId(userId);

        if (resume == null) {
            resume = new Resume();
        }

        resume.setFileName(originalName);
        resume.setFilePath(filePath.toString());
        resume.setUser(user);

        return resumeRepository.save(resume);
    }


@PostMapping("/analyze/{userId}")
public ResponseEntity<Map<String, Object>> analyzeResume(
        @PathVariable Long userId,
        @RequestParam(defaultValue = "AI Engineer") String targetRole) {

    Resume resume = resumeRepository.findByUserId(userId);

    if (resume == null) {
        throw new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Please upload a resume before analysis");
    }

    Map<String, Object> result =
            resumeAnalysisService.analyzeResume(userId, targetRole);

    return ResponseEntity.ok(result);
}



}