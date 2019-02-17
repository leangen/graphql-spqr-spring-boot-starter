package io.leangen.graphql.spqr.spring.web;

import graphql.GraphQL;
import io.leangen.graphql.spqr.spring.web.GraphQLController;
import io.leangen.graphql.spqr.spring.web.GraphQLExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.NativeWebRequest;

import java.util.Map;

@RestController
@CrossOrigin
public class GraphQLMvcController extends GraphQLController<NativeWebRequest, Map<String, Object>> {

    @Autowired
    public GraphQLMvcController(GraphQL graphQL, GraphQLExecutor<NativeWebRequest, Map<String, Object>> executor) {
        super(graphQL, executor);
    }
}
