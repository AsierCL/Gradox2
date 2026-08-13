-- V5: revocación global de sesiones JWT vía token_version.
-- Cada evento de seguridad (reset de contraseña, baneo) incrementa esta columna
-- y el JwtAuthFilter rechaza los tokens con un `ver` inferior al actual.

alter table users add column token_version int not null default 1;
