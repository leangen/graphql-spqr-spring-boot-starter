package io.leangen.graphql.spqr.spring.autoconfigure;

import org.springframework.web.server.ServerWebExchange;

public interface ReactiveContextFactory extends ContextFactory<ServerWebExchange> {
}
