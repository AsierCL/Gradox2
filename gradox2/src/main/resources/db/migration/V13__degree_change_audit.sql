-- V13: acción de auditoría para el override de grado (D2, GRADOS.md §4.3).
--
-- Cambiar el grado de un usuario altera sus permisos de forma retroactiva, así que tiene que quedar
-- registrado con un tipo de acción propio y no escondido dentro de POLICY_CHANGE.
--
-- Nota: `proposals.action_type` no se toca: DEGREE_CHANGE no es una propuesta votable, es una acción
-- administrativa directa.

alter table if exists audit_records drop constraint if exists audit_records_action_type_check;

alter table if exists audit_records add constraint audit_records_action_type_check
    check (action_type in ('UPLOAD','DELETE','VETO','POLICY_CHANGE','PROMOTION','EXPULSION','BAN','UNBAN','DEGREE_CHANGE'));
