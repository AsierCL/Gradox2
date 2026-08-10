package com.example.gradox2;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.example.gradox2.persistence.entities.Badge;
import com.example.gradox2.persistence.repository.BadgeRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BadgeCatalogIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BadgeRepository badgeRepository;

    @BeforeEach
    void setUp() {
        badgeRepository.deleteAll();
    }

    @Test
    void catalogShouldExposeIconUrlOnlyWhenIconKeyIsSet() throws Exception {
        badgeRepository.save(badge("PRIMER_ARCHIVO", "Sube tu primer archivo", "icon-abc.png"));
        badgeRepository.save(badge("SIN_ICONO", "Insignia sin icono", null));

        mockMvc.perform(get("/badges"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("PRIMER_ARCHIVO"))
                .andExpect(jsonPath("$[0].iconUrl").value(
                        "http://s3.test.local/test-bucket/icon-abc.png?X-Amz-Signature=test-signature"
                                + "&response-content-disposition=inline; filename=\"PRIMER_ARCHIVO.png\""))
                .andExpect(jsonPath("$[1].name").value("SIN_ICONO"))
                .andExpect(jsonPath("$[1].iconUrl").value(org.hamcrest.Matchers.nullValue()));
    }

    @Test
    void catalogShouldBePublicWithoutAuthentication() throws Exception {
        badgeRepository.save(badge("PRIMER_ARCHIVO", "Sube tu primer archivo", "icon-abc.png"));

        mockMvc.perform(get("/badges"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void badgesShouldBeSortedByName() throws Exception {
        badgeRepository.save(badge("ZETA", "Ultima", null));
        badgeRepository.save(badge("ALFA", "Primera", null));

        mockMvc.perform(get("/badges"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("ALFA"))
                .andExpect(jsonPath("$[1].name").value("ZETA"));
    }

    private Badge badge(String name, String description, String iconKey) {
        Badge badge = new Badge();
        badge.setName(name);
        badge.setDescription(description);
        badge.setIconKey(iconKey);
        return badge;
    }
}