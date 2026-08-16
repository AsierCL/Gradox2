-- V7: Agregar snapshots de pesos de voto a la tabla proposals
alter table if exists proposals
    add column if not exists master_vote_weight float(53) default 2.0;

alter table if exists proposals
    add column if not exists user_vote_weight float(53) default 1.0;

-- Actualizar valores históricos con los valores por defecto de la configuración
update proposals set
    master_vote_weight = 2.0,
    user_vote_weight = 1.0
where master_vote_weight is null or user_vote_weight is null;