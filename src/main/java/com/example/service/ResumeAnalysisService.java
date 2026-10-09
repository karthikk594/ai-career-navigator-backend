
package com.example.service;

import com.example.model.Resume;
import com.example.repository.ResumeRepository;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Files;
import java.util.*;
import java.util.regex.Pattern;

@Service
public class ResumeAnalysisService {

    private final ResumeRepository resumeRepository;

    public ResumeAnalysisService(ResumeRepository resumeRepository) {
        this.resumeRepository = resumeRepository;
    }

    private static final Map<String, List<String>> ROLE_SKILLS =
            new LinkedHashMap<>();

    private static final Map<String, List<String>> ALIASES =
            new HashMap<>();

    static {
        ROLE_SKILLS.put("AI Engineer", List.of(
                "Python", "Machine Learning", "Deep Learning",
                "NLP", "TensorFlow", "PyTorch", "SQL",
                "Data Structures", "Git", "REST API"
        ));

        ROLE_SKILLS.put("ML Engineer", List.of(
                "Python", "Machine Learning", "Deep Learning",
                "Scikit-learn", "TensorFlow", "PyTorch",
                "SQL", "Pandas", "NumPy", "Git"
        ));

        ROLE_SKILLS.put("Data Scientist", List.of(
                "Python", "SQL", "Statistics", "Pandas",
                "NumPy", "Machine Learning", "Data Visualization",
                "Matplotlib", "Scikit-learn", "Excel"
        ));

        ROLE_SKILLS.put("Java Developer", List.of(
                "Java", "OOP", "Data Structures", "SQL",
                "Spring Boot", "REST API", "Git",
                "Maven", "JUnit", "Hibernate"
        ));

        ROLE_SKILLS.put("Web Developer", List.of(
                "HTML", "CSS", "JavaScript", "React",
                "REST API", "SQL", "Git", "Node.js",
                "Responsive Design", "TypeScript"
        ));

        ALIASES.put("Machine Learning",
                List.of("machine learning", "machine-learning", "ML"));

        ALIASES.put("Deep Learning",
                List.of("deep learning", "deep-learning", "DL"));

        ALIASES.put("NLP",
                List.of("NLP", "natural language processing"));

        ALIASES.put("Scikit-learn",
                List.of("scikit-learn", "sklearn"));

        ALIASES.put("OOP",
                List.of("OOP", "object oriented programming",
                        "object-oriented programming"));

        ALIASES.put("REST API",
                List.of("REST API", "RESTful API", "REST APIs"));

        ALIASES.put("Data Visualization",
                List.of("data visualization", "data visualisation"));

        ALIASES.put("Responsive Design",
                List.of("responsive design", "responsive web design"));
    }

    public Map<String, Object> analyzeResume(
            Long userId, String targetRole) {

        Resume resume = resumeRepository.findByUserId(userId);

        if (resume == null) {
            throw new IllegalArgumentException(
                    "No uploaded resume found for this user."
            );
        }

        String filePath = resume.getFilePath();

        if (filePath == null || filePath.isBlank()) {
            throw new IllegalArgumentException(
                    "Resume file path is missing."
            );
        }

        File pdfFile = new File(filePath);

        if (!pdfFile.isFile() || !Files.isReadable(pdfFile.toPath())) {
            throw new IllegalArgumentException(
                    "Resume PDF could not be found or read."
            );
        }

        String text = extractPdfText(pdfFile);

        if (text.isBlank()) {
            throw new IllegalArgumentException(
                    "No readable text found. The PDF may be scanned or image-only."
            );
        }

        String role = normalizeRole(targetRole);
        List<String> requiredSkills = ROLE_SKILLS.get(role);

        List<String> matchedSkills = new ArrayList<>();
        List<String> missingSkills = new ArrayList<>();
        Map<String, String> skillEvidence = new LinkedHashMap<>();

        for (String skill : requiredSkills) {
            String evidence = findSkillEvidence(text, skill);

            if (evidence != null) {
                matchedSkills.add(skill);
                skillEvidence.put(skill, evidence);
            } else {
                missingSkills.add(skill);
            }
        }

        // This is a role-specific skill match, not a resume-quality score.
        int score = (int) Math.round(
                matchedSkills.size() * 100.0 / requiredSkills.size()
        );

        Map<String, Boolean> sections = detectSections(text);
        List<String> suggestions = buildSuggestions(
                missingSkills, sections, text
        );

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("targetRole", role);
        result.put("matchScore", score);
        result.put("matchedSkills", matchedSkills);
        result.put("missingSkills", missingSkills);
        result.put("requiredSkills", requiredSkills);
        result.put("skillEvidence", skillEvidence);
        result.put("sectionsDetected", sections);
        result.put("wordCount", text.trim().split("\\s+").length);
        result.put("suggestions", suggestions);
        result.put("extractedTextPreview",
                text.substring(0, Math.min(1200, text.length())));

        return result;
    }

    // Alias if another part of your backend calls analyze().
    public Map<String, Object> analyze(Long userId, String targetRole) {
        return analyzeResume(userId, targetRole);
    }

    private String extractPdfText(File file) {
        try (PDDocument document = Loader.loadPDF(file)) {
            if (document.isEncrypted()) {
                throw new IllegalArgumentException(
                        "Password-protected PDFs are not supported."
                );
            }

            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);

            return stripper.getText(document)
                    .replace('\u00A0', ' ')
                    .replaceAll("[\\t ]+", " ")
                    .replaceAll("\\n{3,}", "\n\n")
                    .trim();

        } catch (IllegalArgumentException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalArgumentException(
                    "Unable to extract text from this PDF. Please upload a readable PDF."
            );
        }
    }

    private String normalizeRole(String requestedRole) {
        if (requestedRole == null || requestedRole.isBlank()) {
            return "AI Engineer";
        }

        String input = requestedRole.trim().toLowerCase(Locale.ROOT);

        for (String role : ROLE_SKILLS.keySet()) {
            if (role.toLowerCase(Locale.ROOT).equals(input)) {
                return role;
            }
        }

        if (input.contains("data scientist")) {
            return "Data Scientist";
        }
        if (input.contains("machine learning") || input.equals("ml engineer")) {
            return "ML Engineer";
        }
        if (input.contains("java")) {
            return "Java Developer";
        }
        if (input.contains("web") || input.contains("frontend")
                || input.contains("front-end")) {
            return "Web Developer";
        }

        return "AI Engineer";
    }

    private String findSkillEvidence(String text, String skill) {
        List<String> terms = ALIASES.getOrDefault(
                skill, List.of(skill)
        );

        for (String term : terms) {
            String pattern = "(?i)(?<![a-z0-9])"
                    + Pattern.quote(term).replace("\\ ", "\\E\\s+\\Q")
                    + "(?![a-z0-9])";

            if (Pattern.compile(pattern).matcher(text).find()) {
                return findEvidenceLine(text, term);
            }
        }

        return null;
    }

    private String findEvidenceLine(String text, String term) {
        String[] lines = text.split("\\R");

        for (String line : lines) {
            if (findSkillEvidenceOnLine(line, term)) {
                String clean = line.trim().replaceAll("\\s+", " ");

                if (clean.length() > 180) {
                    clean = clean.substring(0, 177) + "...";
                }

                return clean;
            }
        }

        return "Found in extracted resume text.";
    }

    private boolean findSkillEvidenceOnLine(String line, String term) {
        String pattern = "(?i)(?<![a-z0-9])"
                + Pattern.quote(term).replace("\\ ", "\\E\\s+\\Q")
                + "(?![a-z0-9])";

        return Pattern.compile(pattern).matcher(line).find();
    }

    private Map<String, Boolean> detectSections(String text) {
        String lower = text.toLowerCase(Locale.ROOT);
        Map<String, Boolean> sections = new LinkedHashMap<>();

        sections.put("contact", containsAny(lower,
                "email", "phone", "linkedin", "github"));

        sections.put("education", containsAny(lower,
                "education", "university", "college", "b.tech", "bachelor"));

        sections.put("skills", containsAny(lower,
                "skills", "technical skills", "technologies"));

        sections.put("projects", containsAny(lower,
                "projects", "project experience", "academic project"));

        sections.put("experience", containsAny(lower,
                "experience", "internship", "work history", "employment"));

        sections.put("certifications", containsAny(lower,
                "certifications", "certificates", "certification"));

        return sections;
    }

    private boolean containsAny(String text, String... terms) {
        for (String term : terms) {
            if (text.contains(term)) {
                return true;
            }
        }
        return false;
    }

    private List<String> buildSuggestions(
            List<String> missingSkills,
            Map<String, Boolean> sections,
            String text) {

        List<String> suggestions = new ArrayList<>();

        if (!missingSkills.isEmpty()) {
            suggestions.add(
                    "For your target role, consider learning and demonstrating: "
                            + String.join(", ", missingSkills) + "."
            );
        }

        if (!sections.get("projects")) {
            suggestions.add(
                    "Add a Projects section with the problem, technologies used, "
                            + "your contribution and measurable results."
            );
        }

        if (!sections.get("experience")) {
            suggestions.add(
                    "If you have completed internships, volunteering or relevant "
                            + "team work, include that experience. Do not invent experience."
            );
        }

        if (!sections.get("education")) {
            suggestions.add(
                    "Add an Education section with your degree, institution and expected graduation year."
            );
        }

        if (!sections.get("skills")) {
            suggestions.add(
                    "Add a clearly labelled Technical Skills section."
            );
        }

        if (text.trim().split("\\s+").length < 100) {
            suggestions.add(
                    "The extracted resume text is short. Check that all pages were uploaded "
                            + "and that the PDF contains selectable text."
            );
        }

        if (suggestions.isEmpty()) {
            suggestions.add(
                    "Your resume contains the main detected sections. Review each project "
                            + "and describe your impact with specific, truthful results."
            );
        }

        return suggestions;
    }
}