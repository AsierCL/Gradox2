package com.example.gradox2.security;

import org.springframework.stereotype.Component;

import com.example.gradox2.persistence.entities.Degree;
import com.example.gradox2.persistence.entities.User;
import com.example.gradox2.service.exceptions.DegreeRequiredException;
import com.example.gradox2.service.exceptions.DegreeScopeException;
import com.example.gradox2.service.exceptions.InternalServerErrorException;
import com.example.gradox2.utils.GetAuthUser;

/**
 * Único punto de enforcement del ámbito de grado (GRADOS.md §5.3).
 *
 * <p>Regla: <b>toda escritura declara su ámbito</b>. Cada servicio que muta resuelve el grado del recurso
 * desde el recurso ya cargado —nunca desde el payload del cliente— y llama acá antes de tocar nada.
 *
 * <p>La lectura no pasa por este componente: es pública para todo el mundo (D6). Si en algún momento se
 * quisiera restringir, el lugar es este y no los servicios.
 *
 * <p>El grado se deriva siempre del recurso: {@code file.subject.course.degree},
 * {@code tempFile.subject.course.degree}, {@code promotionProposal.candidate.degree}. No se acepta un
 * {@code degreeId} enviado por el cliente como autorización.
 */
@Component
public class DegreeAccessGuard {

    /**
     * Exige que el usuario autenticado pertenezca al grado del recurso y lo devuelve, para que el servicio
     * no tenga que volver a resolverlo.
     *
     * @throws com.example.gradox2.service.exceptions.UnauthenticatedAccessException si no hay sesión
     * @throws DegreeRequiredException si el usuario no tiene grado asignado
     * @throws DegreeScopeException si el usuario es de otro grado
     */
    public User requireSameDegree(Degree resourceDegree) {
        return requireSameDegree(GetAuthUser.getAuthUser(), resourceDegree);
    }

    public User requireSameDegree(User actor, Degree resourceDegree) {
        if (resourceDegree == null) {
            // Un recurso sin grado es un bug de datos, no un problema de permisos: no se debe degradar a
            // "permitido" ni a 403, porque escondería una inconsistencia real.
            throw new InternalServerErrorException("El recurso no tiene grado asociado.");
        }

        Degree actorDegree = actor.getDegree();
        if (actorDegree == null) {
            throw new DegreeRequiredException("Debes seleccionar un grado");
        }

        if (!actorDegree.getId().equals(resourceDegree.getId())) {
            throw new DegreeScopeException("Solo puedes participar en tu grado");
        }

        return actor;
    }
}
