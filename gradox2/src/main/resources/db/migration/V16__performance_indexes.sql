-- V16: índices para las consultas que la aplicación ya ejecuta.
--
-- El esquema solo tenía los índices de claves únicas y los dos de `degree_id` de la V10. Las consultas que
-- sostienen la votación, el catálogo y el scheduler recorrían la tabla entera:
--
--  * `findByProposalId` (listar votos de una propuesta) → votes(proposal_id)
--  * el scheduler de expiración (`findByStatusAndEndsAtBefore`) → proposals(status, ends_at)
--  * la cuota de subidas pendientes (count por proponente/estado/tipo) → proposals(proposer_id, status, action_type)
--  * catálogo por asignatura y "mis archivos" → files(subject_id), files(uploader_id)
--  * cuota de temporales y limpieza → temp_files(subject_id), temp_files(uploader_id)
--  * refresh y logout por usuario → refresh_tokens(user_id)
--  * auditoría ordenada por fecha → audit_records(timestamp desc)
--
-- Todos son `if not exists`: la migración es idempotente si alguien ya los creó a mano.

create index if not exists idx_votes_proposal on votes (proposal_id);

create index if not exists idx_proposals_status_ends_at on proposals (status, ends_at);
create index if not exists idx_proposals_proposer_status on proposals (proposer_id, status, action_type);

create index if not exists idx_files_subject on files (subject_id);
create index if not exists idx_files_uploader on files (uploader_id);

create index if not exists idx_temp_files_subject on temp_files (subject_id);
create index if not exists idx_temp_files_uploader on temp_files (uploader_id);

create index if not exists idx_refresh_tokens_user on refresh_tokens (user_id);

create index if not exists idx_audit_records_timestamp on audit_records (timestamp desc);

create index if not exists idx_thread_comments_thread on thread_comments (thread_id);
create index if not exists idx_thread_comments_author on thread_comments (author_id);
