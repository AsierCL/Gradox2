-- V4: métricas comunitarias de archivos.
-- Guardamos el tamaño (bytes) y el contador de descargas para las estadísticas /stats.

alter table files add column size_bytes bigint;
alter table files add column download_count bigint not null default 0;

alter table temp_files add column size_bytes bigint;