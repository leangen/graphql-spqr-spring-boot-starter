package io.leangen.graphql.spqr.spring.web.reactive;

import graphql.GraphQL;
import io.leangen.graphql.spqr.spring.web.GraphQLController;
import io.leangen.graphql.spqr.spring.web.GraphQLExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@CrossOrigin
public class GraphQLReactiveController extends GraphQLController<ServerWebExchange, CompletableFuture<Map<String, Object>>> {

    @Autowired
    public GraphQLReactiveController(GraphQL graphQL, GraphQLExecutor<ServerWebExchange, CompletableFuture<Map<String, Object>>> executor) {
        super(graphQL, executor);
    }
}
