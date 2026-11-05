package com.example.gradox2;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import com.example.gradox2.persistence.entities.Course;
import com.example.gradox2.persistence.entities.Subject;
import com.example.gradox2.persistence.entities.User;
import com.example.gradox2.persistence.entities.enums.FileType;
import com.example.gradox2.persistence.entities.enums.UserRole;
import com.example.gradox2.persistence.repository.CourseRepository;
import com.example.gradox2.persistence.repository.SubjectRepository;
import com.example.gradox2.persistence.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.example.gradox2.support.TestDatabaseCleaner;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * El nombre original del archivo lo elige el cliente y se persiste en una
 * columna {@code varchar(255)}, y además viaja en el {@code Content-Disposition}
 * de la descarga. Estas pruebas fijan qué se guarda en los casos que el cliente
 * controla: nombres largos y nombres con componentes de ruta.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OriginalFilenameIntegrationTest {

    private static final AtomicInteger IP_COUNTER = new AtomicInteger(400);
    private static final int COLUMN_LENGTH = 255;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TestDatabaseCleaner databaseCleaner;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private CourseRepository courseRepository;

    @BeforeEach
    void setUp() {
        // La BD H2 del perfil `test` es compartida entre clases de test: limpiar
        // en orden de dependencias antes de crear los datos de este caso.
        databaseCleaner.clean();
    }

    @Test
    void longFilenameIsAcceptedAndTruncatedKeepingTheExtension() throws Exception {
        String token = loginAsUploader();
        String longName = "a".repeat(300) + ".pdf";

        long proposalId = upload(token, longName);
        String stored = downloadedFilename(proposalId, token);

        assertThat(stored).hasSizeLessThanOrEqualTo(COLUMN_LENGTH);
        assertThat(stored).endsWith(".pdf");
    }

    @Test
    void pathComponentsAreStrippedFromTheFilename() throws Exception {
        String token = loginAsUploader();

        long proposalId = upload(token, "../../etc/notas.pdf");
        String stored = downloadedFilename(proposalId, token);

        assertThat(stored).isEqualTo("notas.pdf");
    }

    @Test
    void normalFilenameIsPreserved() throws Exception {
        String token = loginAsUploader();

        long proposalId = upload(token, "Apuntes Tema 1.pdf");
        String stored = downloadedFilename(proposalId, token);

        assertThat(stored).isEqualTo("Apuntes Tema 1.pdf");
    }

    private String loginAsUploader() throws Exception {
        createEnabledUser("uploader", "uploader@rai.usc.es");
        return loginAndGetToken("uploader", "SecurePass1!");
    }

    private void createEnabledUser(String username, String email) {
        userRepository.save(User.builder()
                .username(username)
                .email(email)
                .passwordHash(passwordEncoder.encode("SecurePass1!"))
                .enabled(true)
                .role(UserRole.USER)
                .build());
    }

    private String loginAndGetToken(String username, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .with(request -> {
                            request.setRemoteAddr(nextTestIp());
                            return request;
                        })
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("username", username, "password", password))))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode node = objectMapper.readTree(result.getResponse().getContentAsString());
        return node.get("token").asText();
    }

    private long upload(String token, String originalFilename) throws Exception {
        Long subjectId = createSubject();
        MockMultipartFile file = new MockMultipartFile(
                "file",
                originalFilename,
                MediaType.APPLICATION_PDF_VALUE,
                "contenido-de-prueba".getBytes());

        MockHttpServletRequestBuilder request = multipart("/uploadProposal/upload")
                .file(file)
                .param("title", "Apuntes")
                .param("description", "Prueba de nombre original")
                .param("type", FileType.APUNTES.name())
                .param("subjectId", String.valueOf(subjectId))
                .header("Authorization", bearer(token));

        MvcResult result = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn();

        JsonNode node = objectMapper.readTree(result.getResponse().getContentAsString());
        return node.get("id").asLong();
    }

    private String downloadedFilename(long proposalId, String token) throws Exception {
        MvcResult result = mockMvc.perform(get("/uploadProposal/{id}/download", proposalId)
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andReturn();

        String url = objectMapper.readTree(result.getResponse().getContentAsString()).get("url").asText();
        Matcher matcher = Pattern.compile("filename=\"([^\"]*)\"").matcher(url);
        assertThat(matcher.find()).as("la URL de descarga debe declarar un filename").isTrue();
        return matcher.group(1);
    }

    private Long createSubject() {
        Course course = courseRepository.save(Course.builder()
                .code("C" + System.nanoTime())
                .name("Curso Test")
                .build());

        Subject subject = subjectRepository.save(Subject.builder()
                .code("S" + System.nanoTime())
                .name("Asignatura Test")
                .course(course)
                .build());

        return subject.getId();
    }

    private String nextTestIp() {
        return "198.51.100." + IP_COUNTER.incrementAndGet();
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }

    private String json(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }
}
