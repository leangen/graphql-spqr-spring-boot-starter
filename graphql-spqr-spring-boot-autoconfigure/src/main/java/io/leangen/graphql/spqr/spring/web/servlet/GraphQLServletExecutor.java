package io.leangen.graphql.spqr.spring.web.servlet;

import io.leangen.graphql.spqr.spring.web.GraphQLExecutor;
import org.springframework.web.context.request.NativeWebRequest;

@FunctionalInterface
public interface GraphQLServletExecutor extends GraphQLExecutor<NativeWebRequest> {
}
