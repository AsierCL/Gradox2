-- V12: rol de plataforma ADMIN (D3, D12).
--
-- Hasta ahora MASTER significaba dos cosas a la vez: gobernar la comunidad y administrar la plataforma.
-- Con masters por grado eso dejaría a un master de Química con acceso al panel de toda la plataforma, así
-- que el acceso a /admin/** pasa a depender de ADMIN y MASTER queda como gobernanza de su grado.
--
-- El rol es un valor único por usuario: ADMIN no vota y no es candidato a promoción (D12).

alter table users drop constraint if exists users_role_check;

alter table users add constraint users_role_check
    check (role in ('GUEST', 'USER', 'MASTER', 'ADMIN'));
