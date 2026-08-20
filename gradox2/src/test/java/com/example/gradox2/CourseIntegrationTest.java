package com.example.gradox2;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;

import com.example.gradox2.persistence.entities.Course;
import com.example.gradox2.persistence.entities.Subject;
import com.example.gradox2.persistence.repository.CourseRepository;
import com.example.gradox2.persistence.repository.SubjectRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CourseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private CacheManager cacheManager;

    @MockitoSpyBean
    private CourseRepository courseRepository;

    @BeforeEach
    void cleanDatabase() {
        subjectRepository.deleteAll();
        courseRepository.deleteAll();
        cacheManager.getCache("courses").clear();
        cacheManager.getCache("subjects").clear();
    }

    @Test
    void getAllCoursesReturnsCoursesOrderedByCodeWithSubjects() throws Exception {
        Course primero = courseRepository.save(Course.builder().code("1").name("Primer curso").build());
        Course segundo = courseRepository.save(Course.builder().code("2").name("Segundo curso").build());

        subjectRepository.save(Subject.builder().code("FMAT1").name("Fundamentos de Matemáticas").course(primero).build());
        subjectRepository.save(Subject.builder().code("ALG1").name("Álgebra").course(primero).build());
        subjectRepository.save(Subject.builder().code("BD1").name("Bases de Datos I").course(segundo).build());

        mockMvc.perform(get("/courses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].code").value("1"))
                .andExpect(jsonPath("$[0].name").value("Primer curso"))
                .andExpect(jsonPath("$[0].subjects.length()").value(2))
                .andExpect(jsonPath("$[0].subjects[0].code").value("ALG1"))
                .andExpect(jsonPath("$[0].subjects[1].code").value("FMAT1"))
                .andExpect(jsonPath("$[1].code").value("2"))
                .andExpect(jsonPath("$[1].subjects[0].name").value("Bases de Datos I"));
    }

    @Test
    void getCourseReturnsDetailWithSubjects() throws Exception {
        Course curso = courseRepository.save(Course.builder().code("3").name("Tercer curso").build());
        subjectRepository.save(Subject.builder().code("IA3").name("Inteligencia Artificial").course(curso).build());

        mockMvc.perform(get("/courses/{id}", curso.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(curso.getId()))
                .andExpect(jsonPath("$.code").value("3"))
                .andExpect(jsonPath("$.name").value("Tercer curso"))
                .andExpect(jsonPath("$.subjects.length()").value(1))
                .andExpect(jsonPath("$.subjects[0].code").value("IA3"));
    }

    @Test
    void getCourseReturns404WhenMissing() throws Exception {
        mockMvc.perform(get("/courses/{id}", 999999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllCoursesIsServedFromCacheOnSecondRequest() throws Exception {
        courseRepository.save(Course.builder().code("1").name("Primer curso").build());

        mockMvc.perform(get("/courses")).andExpect(status().isOk());
        mockMvc.perform(get("/courses")).andExpect(status().isOk());
        mockMvc.perform(get("/courses")).andExpect(status().isOk());

        verify(courseRepository, times(1))
                .findAllWithSubjectsByOrderByCodeAsc();
    }

    @Test
    void getAllCoursesRehitsDatabaseAfterCacheEviction() throws Exception {
        courseRepository.save(Course.builder().code("1").name("Primer curso").build());

        mockMvc.perform(get("/courses")).andExpect(status().isOk());
        cacheManager.getCache("courses").clear();

        mockMvc.perform(get("/courses")).andExpect(status().isOk());

        verify(courseRepository, times(2))
                .findAllWithSubjectsByOrderByCodeAsc();
    }

    @Test
    void getCourseIsServedFromCacheOnSecondRequest() throws Exception {
        Course curso = courseRepository.save(Course.builder().code("1").name("Primer curso").build());

        mockMvc.perform(get("/courses/{id}", curso.getId())).andExpect(status().isOk());
        mockMvc.perform(get("/courses/{id}", curso.getId())).andExpect(status().isOk());

        verify(courseRepository, times(1)).findByIdWithSubjects(curso.getId());
    }
}