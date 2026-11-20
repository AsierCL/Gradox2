package com.example.gradox2.security;

import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Deque;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.github.benmanes.caffeine.cache.Cache;

/**
 * Rate limiter de ventana deslizante en memoria (WAVE1 S4).
 *
 * <p>Render corre una sola instancia, así que el estado en memoria es suficiente; lo que no era aceptable
 * era el crecimiento sin techo: el mapa de buckets nunca borraba claves y cada IP nueva dejaba una entrada
 * viva para siempre en un contenedor con memoria acotada. La caché tiene tamaño máximo y expira por acceso.
 *
 * <p>Si el despliegue pasa a varias instancias, el conteo deja de ser global: en ese momento el bucket
 * tiene que moverse a un almacén compartido.
 */
@Component
public class InMemoryRateLimiter {

    private static final int DEFAULT_MAX_BUCKETS = 10_000;
    private static final long DEFAULT_BUCKET_TTL_MS = 30 * 60 * 1000L;

    private final Cache<String, Deque<Long>> requestBuckets;

    @Autowired
    public InMemoryRateLimiter(
            @Value("${app.rate-limit.max-buckets:10000}") int maxBuckets,
            @Value("${app.rate-limit.bucket-ttl-ms:1800000}") long bucketTtlMs) {
        this(CaffeineHolder.build(maxBuckets, bucketTtlMs));
    }

    InMemoryRateLimiter(Cache<String, Deque<Long>> requestBuckets) {
        this.requestBuckets = requestBuckets;
    }

    public void clear() {
        requestBuckets.invalidateAll();
    }

    /**
     * @return cuántas claves distintas se están rastreando (acotado por {@code app.rate-limit.max-buckets}).
     */
    public long trackedBuckets() {
        requestBuckets.cleanUp();
        return requestBuckets.estimatedSize();
    }

    public boolean isAllowed(String key, int maxRequests, long windowMs) {
        long now = System.currentTimeMillis();
        Deque<Long> bucket = requestBuckets.get(key, ignored -> new ArrayDeque<>());

        synchronized (bucket) {
            while (!bucket.isEmpty() && now - bucket.peekFirst() > windowMs) {
                bucket.pollFirst();
            }

            if (bucket.size() >= maxRequests) {
                return false;
            }

            bucket.addLast(now);
            return true;
        }
    }

    /**
     * Construye la caché fuera del constructor principal para que las anotaciones de Spring queden en un
     * solo constructor y el de tests no compita por la inyección.
     */
    private static final class CaffeineHolder {

        private CaffeineHolder() {
        }

        static Cache<String, Deque<Long>> build(int maxBuckets, long bucketTtlMs) {
            return com.github.benmanes.caffeine.cache.Caffeine.newBuilder()
                    .maximumSize(Math.max(maxBuckets, 1))
                    .expireAfterAccess(Duration.ofMillis(Math.max(bucketTtlMs, 1_000L)))
                    .build();
        }
    }
}
