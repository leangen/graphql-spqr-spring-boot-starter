package io.leangen.graphql.spqr.spring.web.reactive;

import io.leangen.graphql.spqr.spring.web.GraphQLExecutor;
import org.springframework.web.server.ServerWebExchange;

@FunctionalInterface
public interface GraphQLReactiveExecutor extends GraphQLExecutor<ServerWebExchange> {
}
