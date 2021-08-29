package io.leangen.graphql.spqr.spring.modules.security;

import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import graphql.execution.DataFetcherResult;
import io.leangen.graphql.execution.InvocationContext;
import io.leangen.graphql.execution.ResolverInterceptor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationTrustResolver;
import org.springframework.security.authentication.AuthenticationTrustResolverImpl;
import org.springframework.security.core.context.SecurityContextHolder;


public class AccessDeniedInterceptor implements ResolverInterceptor {
    private final AuthenticationTrustResolver resolver;

    public AccessDeniedInterceptor(AuthenticationTrustResolver resolver) {
        this.resolver = resolver != null ? resolver : new AuthenticationTrustResolverImpl();
    }

    @Override
    public Object aroundInvoke(InvocationContext context, Continuation continuation) throws Exception {
        try {
            return continuation.proceed(context);
        } catch (AccessDeniedException e) {
            GraphQLError error;
            if (resolver.isAnonymous(SecurityContextHolder.getContext().getAuthentication())) {
                error = GraphqlErrorBuilder.newError()
                        .errorType(SecurityErrorType.UNAUTHORIZED)
                        .message("Unauthorized")
                        .build();
            } else {
                error = GraphqlErrorBuilder.newError()
                        .errorType(SecurityErrorType.FORBIDDEN)
                        .message(String.format("Forbidden: %s", e.getMessage()))
                        .build();
            }
            return DataFetcherResult.newResult()
                    .error(error)
                    .build();
        }
    }
}
