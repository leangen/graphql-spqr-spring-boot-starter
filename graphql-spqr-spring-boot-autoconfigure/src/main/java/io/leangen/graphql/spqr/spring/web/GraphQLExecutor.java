package io.leangen.graphql.spqr.spring.web;

import graphql.GraphQL;
import io.leangen.graphql.spqr.spring.web.dto.GraphQLRequest;

public interface GraphQLExecutor<REQ, RESP> {

    RESP execute(GraphQL graphQL, GraphQLRequest graphQLRequest, REQ request);
}
