-- V3: las imágenes (foto de perfil e insignias) se almacenan en S3.
-- Guardamos la clave del objeto (object key) en lugar de los bytes (columna oid).

alter table users drop column profile_picture;
alter table users add column profile_picture_key varchar(512);

alter table badges drop column icon;
alter table badges add column icon_key varchar(512);