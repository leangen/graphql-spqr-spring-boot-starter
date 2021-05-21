package io.leangen.graphql.spqr.spring.web.reactive;

import com.fasterxml.jackson.databind.ObjectMapper;
import graphql.GraphQL;
import io.leangen.graphql.spqr.spring.web.GraphQLController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;

@RestController
@CrossOrigin
public class DefaultGraphQLController extends GraphQLController<ServerWebExchange> {

    @Autowired
    public DefaultGraphQLController(GraphQL graphQL, GraphQLReactiveExecutor executor, ObjectMapper objectMapper) {
        super(graphQL, executor, objectMapper);
    }
}
