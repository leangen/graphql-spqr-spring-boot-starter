package io.leangen.graphql.spqr.spring.web;

import graphql.cachecontrol.CacheControl;
import org.springframework.http.HttpHeaders;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class CacheControlHeader {

    /**
     * For any response whose overall cache policy has a non-zero maxAge, This method will automatically set the
     * Cache-Control HTTP response header to an appropriate value describing the maxAge and scope,
     * such as Cache-Control: max-age=60, private.
     * https://www.apollographql.com/docs/apollo-server/features/caching/#serving-http-cache-headers
     *
     * @param response graphql response.
     * @param headers response headers.
     */
    public static void addCacheControlHeader(Object response, HttpHeaders headers) {
        CachePolicy cachePolicy;
        if (response instanceof CompletableFuture) {
            try {
                cachePolicy = computeOverallCacheMaxAge(((CompletableFuture<Map<String, Object>>) response).get());
            } catch (ExecutionException | InterruptedException e) {
                throw new RuntimeException("", e);
            }
        } else {
            cachePolicy = computeOverallCacheMaxAge((Map<String, Object>) response);
        }

        if (cachePolicy != null) {
            headers.add(HttpHeaders.CACHE_CONTROL, "max-age=" + cachePolicy.getMaxAge() + ", " + cachePolicy.getScope().name().toLowerCase());
        }
    }

    static private <T> T get(
            String keyName,
            Map<String, Object> executionResult
    ) {
        if (executionResult == null || executionResult.get(keyName) == null) {
            return null;
        }
        return (T) executionResult.get(keyName);
    }

    // reference https://github.com/apollographql/apollo-server/blob/d5015f4ea00cadb2a74b09956344e6f65c084629/packages/apollo-cache-control/src/index.ts#L180
    static private CachePolicy computeOverallCacheMaxAge(
            Map<String, Object> executionResult
    ) {
        Map<String, Object> extensions = get("extensions", executionResult);
        Map<String, Object> cacheControl = get("cacheControl", extensions);
        List<Map<Object, Object>> hints = get("hints", cacheControl);
        if (hints == null) {
            return null;
        }

        // find lowest maxAge by hints.
        Integer lowestMaxAge = null;
        CacheControl.Scope scope = CacheControl.Scope.PUBLIC;
        for (Map<Object, Object> hint : hints) {
            Integer maxAge = (Integer) hint.get("maxAge");
            lowestMaxAge = lowestMaxAge == null ? maxAge : Math.min(maxAge, lowestMaxAge);
            if (CacheControl.Scope.PRIVATE.name().equals(hint.get("scope"))) {
                scope = CacheControl.Scope.PRIVATE;
            }
        }

        // check all data fields has hints.
        Map<String, Object> data = get("data", executionResult);
        if (data == null) {
            return null;
        }
        boolean isExistHint = data.entrySet().stream()
                .allMatch((entry) -> hints.stream()
                        .anyMatch((it) -> String.join(".", ((List<String>) it.get("path"))).equals(entry.getKey())));

        // if hints don't exists, then return null(not cacheable).
        return isExistHint ? new CachePolicy(lowestMaxAge, scope) : null;
    }

    static class CachePolicy {
        private Integer maxAge;
        private CacheControl.Scope scope;

        CachePolicy(Integer maxAge, CacheControl.Scope scope) {
            this.maxAge = maxAge;
            this.scope = scope;
        }

        public Integer getMaxAge() {
            return maxAge;
        }

        public CacheControl.Scope getScope() {
            return scope;
        }
    }
}
