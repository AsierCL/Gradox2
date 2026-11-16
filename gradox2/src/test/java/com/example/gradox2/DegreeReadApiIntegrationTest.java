package com.example.gradox2;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.example.gradox2.persistence.entities.Course;
import com.example.gradox2.persistence.entities.Degree;
import com.example.gradox2.persistence.entities.File;
import com.example.gradox2.persistence.entities.Subject;
import com.example.gradox2.persistence.entities.User;
import com.example.gradox2.persistence.entities.enums.FileType;
import com.example.gradox2.persistence.entities.enums.FileVisibility;
import com.example.gradox2.persistence.entities.enums.UserRole;
import com.example.gradox2.persistence.repository.CourseRepository;
import com.example.gradox2.persistence.repository.DegreeRepository;
import com.example.gradox2.persistence.repository.FileRepository;
import com.example.gradox2.persistence.repository.SubjectRepository;
import com.example.gradox2.persistence.repository.UserRepository;
import com.example.gradox2.security.InMemoryRateLimiter;
import com.example.gradox2.support.TestDatabaseCleaner;
import com.example.gradox2.support.TestDegrees;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Cara de lectura del multi-grado (GRADOS.md §7.1, §7.2): el catálogo es público, cada asignatura es
 * independiente aunque comparta nombre, y leer material de otro grado devuelve 200.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DegreeReadApiIntegrationTest {

    private static final String PASSWORD = "SecurePass1!";
    private static final AtomicInteger IP_COUNTER = new AtomicInteger();

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private DegreeRepository degreeRepository;
    @Autowired
    private CourseRepository courseRepository;
    @Autowired
    private SubjectRepository subjectRepository;
    @Autowired
    private FileRepository fileRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private InMemoryRateLimiter rateLimiter;
    @Autowired
    private TestDatabaseCleaner databaseCleaner;
    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    void setUp() {
        rateLimiter.clear();
        databaseCleaner.clean();
        for (String cache : new String[] {"degrees", "courses", "subjects"}) {
            var region = cacheManager.getCache(cache);
            if (region != null) {
                region.clear();
            }
        }
    }

    // ── catálogo (V5) ───────────────────────────────────────────────────────

    @Test
    void degreesArePublicAndOrderedByCode() throws Exception {
        Degree informatica = TestDegrees.informatica(degreeRepository);
        TestDegrees.quimica(degreeRepository);
        createCourse(informatica, "1");

        // Sin sesión: el selector de grado del registro se llena desde acá (§9.1).
        mockMvc.perform(get("/degrees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].code").value("G4012"))
                .andExpect(jsonPath("$[0].name").value("Ingeniería Informática"))
                .andExpect(jsonPath("$[0].coursesCount").value(1))
                .andExpect(jsonPath("$[1].code").value("G4022"));
    }

    @Test
    void degreeCoursesIncludeSubjectsAndDegreeFields() throws Exception {
        Degree informatica = TestDegrees.informatica(degreeRepository);
        Course course = createCourse(informatica, "1");
        createSubject(course, "G4012102", "Álgebra");

        mockMvc.perform(get("/degrees/{id}/courses", informatica.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].code").value("1"))
                .andExpect(jsonPath("$[0].degreeCode").value("G4012"))
                .andExpect(jsonPath("$[0].subjects[0].code").value("G4012102"))
                .andExpect(jsonPath("$[0].subjects[0].credits").value(6.0))
                .andExpect(jsonPath("$[0].subjects[0].semester").value("Primer semestre"));
    }

    @Test
    void unknownDegreeReturnsNotFound() throws Exception {
        mockMvc.perform(get("/degrees/{id}/courses", 999_999))
                .andExpect(status().isNotFound());
    }


    @Test
    void degreeSubjectsReturnTheirCourseAndPlanMetadata() throws Exception {
        Degree informatica = TestDegrees.informatica(degreeRepository);
        Course course = createCourse(informatica, "1");
        createSubject(course, "G4012103", "Matemática Discreta");

        mockMvc.perform(get("/degrees/{id}/subjects", informatica.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].code").value("G4012103"))
                .andExpect(jsonPath("$[0].courseName").value("Primer curso"))
                .andExpect(jsonPath("$[0].degreeCode").value("G4012"))
                .andExpect(jsonPath("$[0].credits").value(6.0))
                .andExpect(jsonPath("$[0].semester").value("Primer semestre"))
                .andExpect(jsonPath("$[0].subjectType").value("Formación básica"));
    }

    @Test
    void unknownDegreeSubjectsReturnNotFound() throws Exception {
        mockMvc.perform(get("/degrees/{id}/subjects", 999_999))
                .andExpect(status().isNotFound());
    }

    @Test
    void aDegreeWithoutCoursesReturnsAnEmptyListNotEmptyA404() throws Exception {
        Degree quimica = TestDegrees.quimica(degreeRepository);

        mockMvc.perform(get("/degrees/{id}/courses", quimica.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // ── independencia de asignaturas (V6) ───────────────────────────────────

    @Test
    void subjectsWithTheSameNameInDifferentDegreesAreIndependent() throws Exception {
        Degree informatica = TestDegrees.informatica(degreeRepository);
        Degree ia = TestDegrees.ia(degreeRepository);
        Subject algebraInformatica = createSubject(createCourse(informatica, "1"), "G4012102", "Álgebra");
        Subject algebraIa = createSubject(createCourse(ia, "1"), "G4121101", "Álgebra");

        assertThat(algebraInformatica.getId()).isNotEqualTo(algebraIa.getId());

        mockMvc.perform(get("/degrees/{degreeId}/subjects", ia.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(algebraIa.getId()))
                .andExpect(jsonPath("$[0].name").value("Álgebra"))
                .andExpect(jsonPath("$[0].degreeCode").value("G4121"));

        // Cada grado expone su propia asignatura: el nombre se repite, la fila no.
        mockMvc.perform(get("/degrees/{degreeId}/subjects", informatica.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(algebraInformatica.getId()));
    }

    // ── lectura cross-grado (V14) ───────────────────────────────────────────

    @Test
    void readingAFileFromAnotherDegreeIsAllowedAndCarriesItsDegree() throws Exception {
        Degree informatica = TestDegrees.informatica(degreeRepository);
        Degree quimica = TestDegrees.quimica(degreeRepository);
        User uploader = createUser("owner", informatica);
        createUser("visitor", quimica);
        Subject subject = createSubject(createCourse(informatica, "1"), "G4012101", "Fundamentos de Matemáticas");
        File file = createPublishedFile(uploader, subject);

        String visitorToken = loginAndGetToken("visitor");

        mockMvc.perform(get("/files/{id}", file.getId())
                        .header("Authorization", bearer(visitorToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subjectId").value(subject.getId()))
                .andExpect(jsonPath("$.subjectCode").value("G4012101"))
                .andExpect(jsonPath("$.degreeId").value(informatica.getId()))
                .andExpect(jsonPath("$.degreeCode").value("G4012"));

        // Y también sin sesión: la lectura es pública (D6).
        mockMvc.perform(get("/files/{id}", file.getId()))
                .andExpect(status().isOk());
    }

    // ── helpers ─────────────────────────────────────────────────────────────

    private Course createCourse(Degree degree, String code) {
        return courseRepository.save(Course.builder()
                .code(code)
                .name("Primer curso")
                .degree(degree)
                .build());
    }

    private Subject createSubject(Course course, String code, String name) {
        return subjectRepository.save(Subject.builder()
                .code(code)
                .name(name)
                .course(course)
                .credits(6.0)
                .semester("Primer semestre")
                .subjectType("Formación básica")
                .build());
    }

    private File createPublishedFile(User uploader, Subject subject) {
        return fileRepository.save(File.builder()
                .title("Apuntes")
                .description("Apuntes de prueba")
                .type(FileType.APUNTES)
                .objectKey("files/" + subject.getCode() + ".pdf")
                .originalFilename("apuntes.pdf")
                .sizeBytes(10L)
                .fileHash("hash-" + subject.getCode())
                .subject(subject)
                .uploader(uploader)
                .visibilityLevel(FileVisibility.PUBLIC)
                .build());
    }

    private User createUser(String username, Degree degree) {
        return userRepository.save(User.builder()
                .username(username)
                .email(username + "@rai.usc.es")
                .passwordHash(passwordEncoder.encode(PASSWORD))
                .role(UserRole.USER)
                .degree(degree)
                .enabled(true)
                .build());
    }

    private String loginAndGetToken(String username) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .with(request -> {
                            request.setRemoteAddr("203.0.113." + IP_COUNTER.incrementAndGet());
                            return request;
                        })
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "username", username,
                                "password", PASSWORD))))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode node = objectMapper.readTree(result.getResponse().getContentAsString());
        return node.get("token").asText();
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }
}
