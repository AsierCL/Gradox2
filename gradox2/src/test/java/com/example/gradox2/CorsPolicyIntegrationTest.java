package com.example.gradox2;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Política CORS (WAVE1 S5).
 *
 * <p>La API se consume desde GitHub Pages. Con {@code allowedOriginPatterns("*")} cualquier sitio podía
 * emitir peticiones y leer las respuestas autenticadas que el usuario ya tenía abiertas.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CorsPolicyIntegrationTest {

    private static final String FRONTEND_ORIGIN = "https://gradox2.github.io";

    @Autowired
    private MockMvc mockMvc;

    @Test
    void configuredFrontendOriginIsAllowed() throws Exception {
        mockMvc.perform(get("/health").header("Origin", FRONTEND_ORIGIN))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", FRONTEND_ORIGIN));
    }

    @Test
    void localDevOriginIsAllowedWhenListed() throws Exception {
        mockMvc.perform(get("/health").header("Origin", "http://localhost:5173"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"));
    }

    @Test
    void foreignOriginGetsNoCorsPermission() throws Exception {
        mockMvc.perform(get("/health").header("Origin", "https://evil.example"))
                .andExpect(header().doesNotExist("Access-Control-Allow-Origin"));
    }

    @Test
    void credentialsAreNotSharedWithAnyOrigin() throws Exception {
        mockMvc.perform(options("/api/auth/login")
                        .header("Origin", FRONTEND_ORIGIN)
                        .header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers", "content-type,authorization"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", FRONTEND_ORIGIN))
                .andExpect(header().doesNotExist("Access-Control-Allow-Credentials"));
    }

    @Test
    void preflightFromForeignOriginIsRejected() throws Exception {
        mockMvc.perform(options("/api/auth/login")
                        .header("Origin", "https://evil.example")
                        .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isForbidden());
    }
}
