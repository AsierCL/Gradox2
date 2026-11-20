package com.example.gradox2.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Resuelve la IP del cliente con un número explícito de proxies de confianza (WAVE1 S3).
 *
 * <p>Confiar en el primer valor de {@code X-Forwarded-For} es equivalente a dejar que el cliente elija su
 * propia IP: el navegador puede mandar la cabecera que quiera y el proxy de confianza la conserva. Cada
 * proxy agrega la dirección que vio al final, así que el último salto es el único dato que el atacante no
 * controla.
 *
 * <p>{@code app.rate-limit.trusted-proxy-hops} declara cuántos proxies de confianza hay delante:
 * {@code 0} ignora la cabecera por completo (desarrollo local, acceso directo al contenedor),
 * {@code 1} es Render (un único proxy de borde).
 */
@Component
public class ClientIpResolver {

    private static final String FORWARDED_FOR = "X-Forwarded-For";

    private final int trustedProxyHops;

    public ClientIpResolver(@Value("${app.rate-limit.trusted-proxy-hops:0}") int trustedProxyHops) {
        this.trustedProxyHops = Math.max(trustedProxyHops, 0);
    }

    /**
     * @return la IP del cliente, o la dirección del socket cuando la cabecera no es confiable.
     */
    public String resolve(HttpServletRequest request) {
        String socketAddress = request.getRemoteAddr();
        if (trustedProxyHops == 0) {
            return socketAddress;
        }

        String forwardedFor = request.getHeader(FORWARDED_FOR);
        if (forwardedFor == null || forwardedFor.isBlank()) {
            return socketAddress;
        }

        String[] hops = forwardedFor.split(",");
        int clientIndex = hops.length - trustedProxyHops;
        if (clientIndex < 0) {
            // La cadena es más corta que los saltos declarados: no se puede saber quién es el cliente.
            return socketAddress;
        }

        String candidate = hops[clientIndex].trim();
        return isIpLiteral(candidate) ? candidate : socketAddress;
    }

    /**
     * Solo direcciones IP literales pueden convertirse en clave de bucket; cualquier otro texto (o una
     * cabecera manipulada con basura) cae al socket.
     */
    private static boolean isIpLiteral(String value) {
        if (value == null || value.isEmpty() || value.length() > 45) {
            return false;
        }

        if (value.indexOf(':') >= 0) {
            return value.chars().allMatch(ch -> Character.digit(ch, 16) >= 0
                    || ch == ':' || ch == '.');
        }

        String[] octets = value.split("\\.", -1);
        if (octets.length != 4) {
            return false;
        }

        for (String octet : octets) {
            if (octet.isEmpty() || octet.length() > 3) {
                return false;
            }
            for (int index = 0; index < octet.length(); index++) {
                if (!Character.isDigit(octet.charAt(index))) {
                    return false;
                }
            }
            if (Integer.parseInt(octet) > 255) {
                return false;
            }
        }

        return true;
    }
}
