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
class SubjectIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private CacheManager cacheManager;

    @MockitoSpyBean
    private SubjectRepository subjectRepository;

    @BeforeEach
    void cleanDatabase() {
        subjectRepository.deleteAll();
        courseRepository.deleteAll();
        cacheManager.getCache("subjects").clear();
    }

    @Test
    void getAllSubjectsReturnsSeededSubjectsOrderedByCourseAndCode() throws Exception {
        Course primerCurso = courseRepository.save(Course.builder().code("1").name("Primer curso").build());
        Course segundo = courseRepository.save(Course.builder().code("2").name("Segundo curso").build());

        subjectRepository.save(Subject.builder().code("MAT1").name("Matemáticas I").course(primerCurso).build());
        subjectRepository.save(Subject.builder().code("LEN1").name("Lengua Castellana I").course(primerCurso).build());
        subjectRepository.save(Subject.builder().code("ECO2").name("Economía II").course(segundo).build());

        mockMvc.perform(get("/subjects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].code").value("LEN1"))
                .andExpect(jsonPath("$[1].code").value("MAT1"))
                .andExpect(jsonPath("$[2].code").value("ECO2"))
                .andExpect(jsonPath("$[2].courseName").value("Segundo curso"))
                .andExpect(jsonPath("$[2].courseCode").value("2"));
    }

    @Test
    void getSubjectReturnsDetail() throws Exception {
        Course curso = courseRepository.save(Course.builder().code("1").name("Primer curso").build());
        Subject subject = subjectRepository.save(
                Subject.builder().code("MAT1").name("Matemáticas I").course(curso).build());

        mockMvc.perform(get("/subjects/{id}", subject.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(subject.getId()))
                .andExpect(jsonPath("$.code").value("MAT1"))
                .andExpect(jsonPath("$.name").value("Matemáticas I"))
                .andExpect(jsonPath("$.courseName").value("Primer curso"));
    }

    @Test
    void getSubjectReturns404WhenMissing() throws Exception {
        mockMvc.perform(get("/subjects/{id}", 999999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllSubjectsIsServedFromCacheOnSecondRequest() throws Exception {
        Course curso = courseRepository.save(Course.builder().code("1").name("Primer curso").build());
        subjectRepository.save(Subject.builder().code("MAT1").name("Matemáticas I").course(curso).build());

        mockMvc.perform(get("/subjects")).andExpect(status().isOk());
        mockMvc.perform(get("/subjects")).andExpect(status().isOk());
        mockMvc.perform(get("/subjects")).andExpect(status().isOk());

        verify(subjectRepository, times(1))
                .findAllByOrderByCourseIdAscCodeAsc();
    }

    @Test
    void getAllSubjectsRehitsDatabaseAfterCacheEviction() throws Exception {
        Course curso = courseRepository.save(Course.builder().code("1").name("Primer curso").build());
        subjectRepository.save(Subject.builder().code("MAT1").name("Matemáticas I").course(curso).build());

        mockMvc.perform(get("/subjects")).andExpect(status().isOk());
        cacheManager.getCache("subjects").clear();

        mockMvc.perform(get("/subjects")).andExpect(status().isOk());

        verify(subjectRepository, times(2))
                .findAllByOrderByCourseIdAscCodeAsc();
    }

    @Test
    void getSubjectIsServedFromCacheOnSecondRequest() throws Exception {
        Course curso = courseRepository.save(Course.builder().code("1").name("Primer curso").build());
        Subject subject = subjectRepository.save(
                Subject.builder().code("MAT1").name("Matemáticas I").course(curso).build());

        mockMvc.perform(get("/subjects/{id}", subject.getId())).andExpect(status().isOk());
        mockMvc.perform(get("/subjects/{id}", subject.getId())).andExpect(status().isOk());

        verify(subjectRepository, times(1)).findById(subject.getId());
    }
}