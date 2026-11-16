package com.example.gradox2.persistence.entities.enums;

public enum UserRole {
    /** Sin sesión o sin verificar: solo lectura. */
    GUEST,
    /** Estudiante: escribe únicamente dentro de su grado. */
    USER,
    /** Master de un grado: gobierna ese grado (promociones, expulsiones, configuraciones). */
    MASTER,
    /**
     * Administrador de plataforma: opera el sistema ({@code /admin/**}, grados, override de grado).
     * No vota y no es candidato a promoción (D12).
     */
    ADMIN
}
