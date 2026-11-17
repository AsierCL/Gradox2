-- V14: auditoría con actor de sistema.
--
-- La V1 creó `audit_records.actor_id` como NOT NULL, pero el servicio registra auditorías sin actor cuando
-- no hay sesión: el scheduler que cierra propuestas expiradas corre en un hilo propio, sin SecurityContext,
-- y `AuditServiceImpl.record` documenta explícitamente que el actor puede ser null.
--
-- Efecto real en producción: al cerrar una propuesta vencida, el INSERT del registro de auditoría violaba
-- NOT NULL y la excepción cortaba el cierre. La funcionalidad de expiración quedaba rota, y no se detectaba
-- en tests porque en el perfil `test` el schema lo genera Hibernate desde la entidad (actor nullable).
--
-- Se alinea la base con el comportamiento documentado y se preserva el historial al borrar un usuario:
-- la FK pasa a ON DELETE SET NULL en lugar de bloquear el borrado (o de borrar la auditoría).

alter table audit_records alter column actor_id drop not null;

alter table audit_records drop constraint if exists FKiecytmv1w9223y1vcqfftvwm2;
alter table audit_records add constraint FKiecytmv1w9223y1vcqfftvwm2
    foreign key (actor_id) references users on delete set null;
