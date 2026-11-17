package com.example.gradox2.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import com.example.gradox2.persistence.entities.Degree;
import com.example.gradox2.persistence.entities.User;
import com.example.gradox2.persistence.entities.enums.UserRole;
import com.example.gradox2.service.exceptions.DegreeRequiredException;
import com.example.gradox2.service.exceptions.DegreeScopeException;
import com.example.gradox2.service.exceptions.InternalServerErrorException;
import com.example.gradox2.service.exceptions.UnauthenticatedAccessException;

/**
 * El guard es el único punto de enforcement del ámbito (GRADOS.md §5.3), así que sus cuatro resultados
 * posibles se prueban acá y no en cada servicio.
 */
class DegreeAccessGuardTest {

    private final DegreeAccessGuard guard = new DegreeAccessGuard();

    private final Degree informatica = Degree.builder().id(1L).code("G4012").name("Ingeniería Informática").build();
    private final Degree quimica = Degree.builder().id(2L).code("G4022").name("Ingeniería Química").build();

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void allowsAUserInTheSameDegreeAndReturnsIt() {
        User actor = authenticate(informatica);

        assertThat(guard.requireSameDegree(informatica)).isSameAs(actor);
        assertThatCode(() -> guard.requireSameDegree(informatica)).doesNotThrowAnyException();
    }

    @Test
    void rejectsAUserFromAnotherDegree() {
        authenticate(informatica);

        assertThatThrownBy(() -> guard.requireSameDegree(quimica))
                .isInstanceOf(DegreeScopeException.class)
                .hasMessage("Solo puedes participar en tu grado");
    }

    @Test
    void rejectsAnAnonymousRequest() {
        SecurityContextHolder.clearContext();

        assertThatThrownBy(() -> guard.requireSameDegree(informatica))
                .isInstanceOf(UnauthenticatedAccessException.class);
    }

    @Test
    void rejectsAUserWithoutDegree() {
        authenticate(null);

        assertThatThrownBy(() -> guard.requireSameDegree(informatica))
                .isInstanceOf(DegreeRequiredException.class);
    }

    @Test
    void refusesToTreatAResourceWithoutDegreeAsAllowed() {
        authenticate(informatica);

        // Un recurso sin grado es un bug de datos: nunca se degrada a permitido ni a 403.
        assertThatThrownBy(() -> guard.requireSameDegree((Degree) null))
                .isInstanceOf(InternalServerErrorException.class);
    }

    private User authenticate(Degree degree) {
        User user = User.builder()
                .id(10L)
                .username("actor")
                .email("actor@rai.usc.es")
                .passwordHash("hash")
                .role(UserRole.USER)
                .degree(degree)
                .enabled(true)
                .build();

        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken(user, null, user.getAuthorities()));
        return user;
    }
}
