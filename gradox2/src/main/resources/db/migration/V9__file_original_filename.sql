-- V9: Persistir o nome de arquivo orixinal (con extensión) para descargas
alter table if exists files
    add column if not exists original_filename varchar(255);

alter table if exists temp_files
    add column if not exists original_filename varchar(255);
