package com.example.gradox2.support;

import com.example.gradox2.persistence.entities.Degree;
import com.example.gradox2.persistence.repository.DegreeRepository;

/**
 * Grados de apoyo para los tests.
 *
 * <p>En el perfil {@code test} Flyway está desactivado y el schema lo genera Hibernate, así que el catálogo
 * de grados que siembra la V10 no existe. Este helper lo crea bajo demanda y es idempotente: los tests
 * pueden pedirlo tantas veces como quieran sin chocar con el `unique (code)`.
 */
public final class TestDegrees {

    /** Ingeniería Informática: el grado por defecto de los fixtures. */
    public static final String INFORMATICA = "G4012";

    /** Ingeniería Química: el «otro grado» de los tests de ámbito. */
    public static final String QUIMICA = "G4022";

    /** Inteligencia Artificial: segundo «otro grado», necesario para los tests de gobernanza. */
    public static final String IA = "G4121";

    private TestDegrees() {
    }

    public static Degree informatica(DegreeRepository repository) {
        return byCode(repository, INFORMATICA, "Ingeniería Informática");
    }

    public static Degree quimica(DegreeRepository repository) {
        return byCode(repository, QUIMICA, "Ingeniería Química");
    }

    public static Degree ia(DegreeRepository repository) {
        return byCode(repository, IA, "Inteligencia Artificial");
    }

    public static Degree byCode(DegreeRepository repository, String code, String name) {
        return repository.findByCode(code)
                .orElseGet(() -> repository.save(Degree.builder().code(code).name(name).build()));
    }
}
