package com.example.gradox2.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

/**
 * Resolución de la IP del cliente detrás de proxies (WAVE1 S3).
 *
 * <p>En Render hay exactamente un proxy de confianza delante de la app: el que agrega el último salto de
 * {@code X-Forwarded-For}. Confiar en el primer valor (lo que hacía el filtro) deja que el cliente invente
 * su propia IP y esquive el rate limiting de login.
 */
class ClientIpResolverTest {

    @Test
    void withoutTrustedProxiesTheHeaderIsIgnored() {
        ClientIpResolver resolver = new ClientIpResolver(0);
        MockHttpServletRequest request = request("203.0.113.9");
        request.addHeader("X-Forwarded-For", "1.2.3.4");

        assertThat(resolver.resolve(request)).isEqualTo("203.0.113.9");
    }

    @Test
    void oneTrustedProxyUsesTheHopItAppendedNotTheSpoofedPrefix() {
        ClientIpResolver resolver = new ClientIpResolver(1);
        MockHttpServletRequest request = request("10.0.0.1");
        // El cliente manda una IP falsa; Render agrega la real al final.
        request.addHeader("X-Forwarded-For", "1.2.3.4, 198.51.100.7");

        assertThat(resolver.resolve(request)).isEqualTo("198.51.100.7");
    }

    @Test
    void twoTrustedProxiesSkipBothHops() {
        ClientIpResolver resolver = new ClientIpResolver(2);
        MockHttpServletRequest request = request("10.0.0.1");
        request.addHeader("X-Forwarded-For", "1.2.3.4, 198.51.100.7, 10.0.0.2");

        assertThat(resolver.resolve(request)).isEqualTo("198.51.100.7");
    }

    @Test
    void chainShorterThanConfiguredHopsFallsBackToTheSocketAddress() {
        ClientIpResolver resolver = new ClientIpResolver(2);
        MockHttpServletRequest request = request("203.0.113.9");
        request.addHeader("X-Forwarded-For", "198.51.100.7");

        assertThat(resolver.resolve(request)).isEqualTo("203.0.113.9");
    }

    @Test
    void nonIpGarbageNeverBecomesABucketKey() {
        ClientIpResolver resolver = new ClientIpResolver(1);
        MockHttpServletRequest request = request("203.0.113.9");
        request.addHeader("X-Forwarded-For", "<script>alert(1)</script>");

        assertThat(resolver.resolve(request)).isEqualTo("203.0.113.9");
    }

    @Test
    void ipv6LiteralIsAccepted() {
        ClientIpResolver resolver = new ClientIpResolver(1);
        MockHttpServletRequest request = request("10.0.0.1");
        request.addHeader("X-Forwarded-For", "2001:db8::1");

        assertThat(resolver.resolve(request)).isEqualTo("2001:db8::1");
    }

    private MockHttpServletRequest request(String remoteAddr) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr(remoteAddr);
        return request;
    }
}
