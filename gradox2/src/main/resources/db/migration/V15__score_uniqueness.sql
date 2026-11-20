-- V15: un único score por (archivo, votante).
--
-- `FileServiceImpl.voteFile` hacía "buscar y después insertar": dos peticiones concurrentes del mismo
-- usuario insertaban dos filas para el mismo archivo y el promedio quedaba contado dos veces. `votes` ya
-- tenía su índice único (`uk_votes_voter_proposal`); `scores` no tenía ninguno.
--
-- Antes de crear el índice se deduplican las filas existentes: se conserva la fila de id mayor de cada
-- par, que es la última intención registrada del votante. Sin este paso, la creación del índice fallaría
-- en una base con datos reales.

delete from scores older
using scores newer
where older.file_id = newer.file_id
  and older.voter_id = newer.voter_id
  and older.id < newer.id;

create unique index if not exists uk_scores_file_voter on scores (file_id, voter_id);
