package com.example.gradox2;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.example.gradox2.persistence.entities.Course;
import com.example.gradox2.persistence.entities.File;
import com.example.gradox2.persistence.entities.Subject;
import com.example.gradox2.persistence.entities.User;
import com.example.gradox2.persistence.entities.enums.FileType;
import com.example.gradox2.persistence.entities.enums.FileVisibility;
import com.example.gradox2.persistence.entities.enums.UserRole;
import com.example.gradox2.persistence.repository.CourseRepository;
import com.example.gradox2.persistence.repository.FileRepository;
import com.example.gradox2.persistence.repository.SubjectRepository;
import com.example.gradox2.persistence.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class StatsIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void statsEndpointIsPublicAndReturnsExpectedShape() throws Exception {
        mockMvc.perform(get("/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalFiles").isNumber())
                .andExpect(jsonPath("$.totalUsers").isNumber())
                .andExpect(jsonPath("$.totalStorageBytes").isNumber())
                .andExpect(jsonPath("$.totalDownloads").isNumber())
                .andExpect(jsonPath("$.byType").isArray())
                .andExpect(jsonPath("$.bySubject").isArray());
    }

    @Test
    void downloadsAndUploadsAreReflectedInStats() throws Exception {
        User uploader = createEnabledUser("statowner", "statowner@rai.usc.es", "SecurePass1!", UserRole.USER);

        JsonNode before = stats();

        Subject subject = createSubject();
        fileRepository.save(File.builder()
                .title("apuntes-stats.pdf")
                .description("Descripcion")
                .type(FileType.APUNTES)
                .objectKey("key-stats" + System.nanoTime())
                .fileHash("hash-stats")
                .sizeBytes(1024L)
                .uploader(uploader)
                .subject(subject)
                .visibilityLevel(FileVisibility.PUBLIC)
                .build());

        Long fileId = fileRepository.findAll().stream()
                .filter(f -> f.getObjectKey().startsWith("key-stats"))
                .findFirst().orElseThrow().getId();

        mockMvc.perform(get("/files/{id}/download", fileId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url").exists());

        JsonNode after = stats();

        org.junit.jupiter.api.Assertions.assertEquals(
                before.get("totalDownloads").asLong() + 1,
                after.get("totalDownloads").asLong());
        org.junit.jupiter.api.Assertions.assertEquals(
                before.get("totalFiles").asLong() + 1,
                after.get("totalFiles").asLong());
        org.junit.jupiter.api.Assertions.assertEquals(
                before.get("totalUsers").asLong(),
                after.get("totalUsers").asLong());
        org.junit.jupiter.api.Assertions.assertEquals(
                before.get("totalStorageBytes").asLong() + 1024L,
                after.get("totalStorageBytes").asLong());
    }

    private JsonNode stats() throws Exception {
        MvcResult result = mockMvc.perform(get("/stats"))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    private User createEnabledUser(String username, String email, String password, UserRole role) {
        User user = User.builder()
                .username(username)
                .email(email)
                .passwordHash(passwordEncoder.encode(password))
                .enabled(true)
                .role(role)
                .build();
        return userRepository.save(user);
    }

    private Subject createSubject() {
        Course course = courseRepository.save(Course.builder()
                .code("STC" + System.nanoTime())
                .name("Curso Stats")
                .build());

        return subjectRepository.save(Subject.builder()
                .code("STS" + System.nanoTime())
                .name("Asignatura Stats")
                .course(course)
                .build());
    }
}