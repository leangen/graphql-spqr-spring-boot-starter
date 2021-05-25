package io.leangen.graphql.spqr.spring.web.mvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import graphql.GraphQL;
import io.leangen.graphql.spqr.spring.web.GraphQLController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.NativeWebRequest;

@RestController
@CrossOrigin
public class DefaultGraphQLController extends GraphQLController<NativeWebRequest> {

    @Autowired
    public DefaultGraphQLController(GraphQL graphQL, GraphQLMvcExecutor executor,  ObjectMapper objectMapper) {
        super(graphQL, executor, objectMapper);
    }
}
